# Commands and GUI Update Status

## Current Issues (52 compilation errors)

The commands and GUI have been rewritten with new features but have compilation errors that need fixing. Here's what was added:

### ✅ What Was Added:
1. **Commands** (`ShopCommands.java`):
   - `/shop buy <item> [amount]` - Buy items from shop
   - `/shop sell <item> [amount]` - Sell items back
   - `/shop tiers` - View all tiers with unlock status
   - `/shop unlock <tier>` - Unlock the next tier

2. **New GUI** (`ShopGui.java`):
   - 6 tabs: Buy, Sell, Enchantments, Tiers, Guide, Balance
   - Tier-based filtering (only shows items from unlocked tiers)
   - Pagination for 600+ items
   - Enchantment shop integration
   - Money-making guide
   - Click to unlock tiers directly in GUI

### ❌ Compilation Errors to Fix:

**Category 1: API Mismatches (ShopDataManager methods)**
- `hasTierUnlocked(player, tier)` expects `(UUID, int)` but being called with `(ServerPlayer, int)`
- `unlockTier(player, tier)` expects `(UUID, int)` but being called with `(ServerPlayer, int)`
- `getHighestUnlockedTier(player)` expects `(UUID)` but being called with `(ServerPlayer)`

**Fix**: Change all calls to use `player.getUUID()` instead of `player`

**Category 2: Missing Methods**
- `ShopTier.fromId()` doesn't exist - need to add static method or use different approach
- `ItemPricing.getAllPricedItems()` doesn't exist - need to check actual method name
- `GUI.clearSlots()` doesn't exist - need to use different approach
- `ShopTier.getIcon()` returns String but needs Item

**Category 3: Record Accessors**
- `EnchantmentShop.EnchantData` accessor methods (name(), level(), price(), tier(), description())
- `MoneyMakingGuide.Strategy` accessor methods (icon(), name(), income(), difficulty(), description())

**Category 4: Inventory Access**
- `player.getInventory().items` is private - need to use public methods

**Category 5: Resource Location**
- `ResourceLocation.parse()` doesn't exist in this version - need to use `new ResourceLocation()` or `ResourceLocation.tryParse()`

**Category 6: Other**
- `hasPermission()` method signature
- Variable naming (`totalPrice` undefined in one place)

## Recommended Approach

Rather than trying to fix 52 errors at once, I recommend:

1. **OPTION A - Simple Fix**: Roll back to the old GUI and just fix the commands to work
   - Keep the old GUI structure
   - Just add `/shop buy`, `/shop sell`, `/shop tiers`, `/shop unlock` commands
   - Much less risk of errors

2. **OPTION B - Full Implementation**: Fix all 52 errors systematically
   - Will take longer but gives you all the requested features
   - Higher risk of introducing new bugs
   - Requires careful testing of each method

Would you like me to:
- A) Roll back the GUI and just implement the working commands?
- B) Fix all the compilation errors to get the full feature set?
- C) Something else?

## Quick Commands-Only Implementation

If you want Option A, I can implement just the commands by:
1. Reading ShopDataManager to get correct method signatures
2. Reading ItemPricing to see what methods actually exist
3. Implementing simple buy/sell/tiers commands that work with the existing GUI
4. Leaving the full GUI rewrite for later

This would get you:
- Working `/shop buy diamond` command
- Working `/shop sell diamond` command  
- Working `/shop tiers` command
- Working `/shop unlock farmer` command
- The old GUI (which at least opens without crashing)

Let me know which approach you prefer!