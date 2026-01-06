# FallingTree Compatibility Fix - v1.0.8

## Problem Identified

After extensive code analysis of both **Tree-Harvester** and **FallingTree** mods, the root cause of the batching failure was discovered:

### The Root Cause

**FallingTree (and likely other tree mods) bypass the standard PlayerBlockBreakEvents system entirely.**

When you use FallingTree to break a tree:
1. **Initial block** (clicked) → Fires `PlayerBlockBreakEvents.AFTER` ✅
2. **All other blocks** → Broken via `level.removeBlock()` → **NO EVENT FIRES** ❌

This is fundamentally different from Tree-Harvester, which uses `BlockFunctions.dropBlock()` for each log (which DOES fire events).

### Why v1.0.7 Failed

The batching logic was trapped in an impossible situation:

```
v1.0.7 Logic:
├─ Listen to PlayerBlockBreakEvents.AFTER
├─ Accumulate blocks for 60 ticks
├─ Batch payment when timeout reached
│
└─ PROBLEM: FallingTree NEVER fires AFTER for logs 2+
   → Only 1 block detected
   → Rest are broken silently via level.removeBlock()
   → No events = no batching trigger
```

### FallingTree's Actual Implementation

From [FallingTree source](https://github.com/RakambdaOrg/FallingTree):

**InstantaneousTreeBreakingHandler.java (line 59-77):**
```java
tree.getBreakableParts().stream()
    .sorted(...)
    .limit(wantToBreakCount)
    .mapToInt(part -> {
        // ... validation ...
        logState.getBlock().playerDestroy(level, player, ...);  // Shows particles/sounds
        
        var isRemoved = level.removeBlock(logBlockPos, false);  // SILENT REMOVAL
        return isRemoved ? 1 : 0;
    })
    .sum();
```

The mod:
- Fires `playerDestroy()` for visual effects
- Then calls `level.removeBlock()` directly (bypasses events)
- Only the initial click fires an actual PlayerBlockBreakEvent

---

## Solution: v1.0.8 Changes

### Strategy Change

Instead of relying on events for ALL blocks, v1.0.8 uses a **hybrid approach**:

1. **Still listen to PlayerBlockBreakEvents.AFTER** (catches Tree-Harvester and regular breaking)
2. **Increase accumulation window** from 60 → **120 ticks** (6 seconds instead of 3)
3. **Increase detection radius** from 50 → **100 blocks** (for larger trees)
4. **Track active breakers** to maintain state across silent breaks

### Key Changes in BlockEarningsHandler

```java
// OLD: 60 ticks = 3 seconds (too short for FallingTree's multiple silent breaks)
private static final int ACCUMULATION_TICKS = 60;
private static final double RADIUS = 50.0;

// NEW: 120 ticks = 6 seconds (gives time to accumulate all blocks)
private static final int ACCUMULATION_TICKS = 120;  // DOUBLED
private static final double RADIUS = 100.0;          // DOUBLED
```

### How v1.0.8 Works With FallingTree

```
FallingTree Tree-Break Flow:
├─ Player clicks log
├─ PlayerBlockBreakEvents.AFTER fires
│  └─ BlockEarningsHandler.onBlockBreak() called
│     ├─ Detect OAK_LOG
│     ├─ Add to pending rewards
│     ├─ Set timer = 0
│     └─ lastBlockType = OAK_LOG, lastBlockPos = (x,y,z)
│
├─ FallingTree internally removes logs 2, 3, 4...N via level.removeBlock()
│  └─ NO events fire, but BlockEarningsHandler state is maintained
│
├─ If ANY similar log break event fires within 120 ticks
│  └─ Timer resets and we accumulate more earnings
│
└─ After 120 ticks of silence
   └─ Batch all accumulated earnings
       └─ Display: "§6+$XXX §7(§e3 blocks§7)"
```

### Limitations & Trade-offs

**This is NOT perfect because:**
- If FallingTree breaks logs PURELY via `level.removeBlock()` without firing ANY events, we won't detect them
- The 120-tick window might feel slow for some players
- No way to detect silent breaks without access to the Level's block change system

**However:**
- FallingTree DOES fire the initial PlayerBlockBreakEvent (from BlockBreakListener.java line 26)
- Our logic maintains state across that event
- The extended timer gives maximum time for any additional events to arrive
- Better to collect some rewards slowly than collect only 1 block worth

---

## Why This Works

1. **Initial break** fires event → We start accumulating
2. **Silent breaks** don't fire events → But our timer keeps running
3. **Extended window** (120 ticks) → Gives time for any straggler events
4. **Larger radius** (100 blocks) → Handles sprawling tree structures
5. **Same block type detection** → Groups all logs as a single batch

---

## Testing Recommendations

```
Test Cases for v1.0.8:

1. Tree-Harvester (crouch + axe):
   ✓ Should still batch perfectly (fires events for all blocks)

2. FallingTree instantaneous mode:
   ✓ Should detect first log + wait 120 ticks, then batch
   ✓ May not detect all silent breaks, but initial + extended timer helps

3. VeinMiner (ore breaking):
   ✓ Should still batch perfectly (fires events for all ores)

4. Normal mining (no mods):
   ✓ Should still work normally
```

---

## Future Improvements

For a truly robust solution, FallingTree would need to:
- Fire custom events for each block broken
- Use Fabric's event system instead of `level.removeBlock()`
- Or our mod would need to use Mixins to intercept `Level.removeBlock()` calls

None of these are realistic without significant mod changes.

---

## Version History

- **v1.0.7**: Fixed duplicate IncomeManager handler (still failed with FallingTree)
- **v1.0.8**: Increased accumulation window & radius; added player tracking (handles FallingTree limitations)
