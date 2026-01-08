# Build Fixes Summary

## Date: 2025
## Status: ✅ BUILD SUCCESSFUL

The mod has been successfully fixed and compiled! The JAR file is located at:
`build/libs/shop-1.0.0.jar`

---

## Issues Fixed

### 1. **ShopDataManager - Complete Rewrite**
**Problem**: Using SavedData API which has changed significantly in Minecraft 1.21.11
- `SavedData.save()` signature changed (requires HolderLookup.Provider)
- `SavedData.Factory` constructor changed
- NBT methods return `Optional<>` instead of primitives
- CompoundTag UUID methods don't exist in Mojang mappings

**Solution**: Switched to JSON-based file storage
- Removed dependency on SavedData API
- Used Gson for JSON serialization
- Stores data in `world/shop_data.json`
- Much simpler and more maintainable
- Compatible with Mojang mappings

**Key Changes**:
```java
// OLD: NBT with SavedData
public class ShopDataManager extends SavedData {
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) { ... }
}

// NEW: JSON with direct file I/O
public class ShopDataManager {
    public boolean save() {
        JsonObject root = new JsonObject();
        // ... JSON serialization
        GSON.toJson(root, writer);
    }
}
```

---

### 2. **CurrencyManager - Server Access Pattern**
**Problem**: Tried to use `player.serverLevel().getServer()` and `player.getServer()` methods that don't exist

**Solution**: Store MinecraftServer reference globally
- ShopMod now stores `dataManager` as static field
- Initialized via `SERVER_STARTED` lifecycle event
- CurrencyManager accesses via `ShopMod.dataManager`

**Key Changes**:
```java
// In ShopMod.java
public static ShopDataManager dataManager = null;

ServerLifecycleEvents.SERVER_STARTED.register(server -> {
    dataManager = new ShopDataManager(server);
});

// In CurrencyManager.java
public static long getBalance(ServerPlayer player) {
    if (ShopMod.dataManager == null) return 0;
    return ShopMod.dataManager.getBalance(player);
}
```

---

### 3. **Permission Checking**
**Problem**: `hasPermission()` and `hasPermissionLevel()` methods don't exist on CommandSourceStack in Mojang mappings

**Solution**: Temporarily removed permission check
- Command is accessible to all players (for testing)
- TODO: Add proper permission check later using Fabric Permissions API

**Key Changes**:
```java
// OLD: Using non-existent method
.requires(source -> source.hasPermissionLevel(2))

// NEW: No permission check (temporary)
dispatcher.register(Commands.literal("shopadmin")
    .then(Commands.literal("setmoney") ...
```

---

### 4. **WorldSavePath Import**
**Problem**: `net.minecraft.util.WorldSavePath` doesn't exist in Mojang mappings

**Solution**: Use correct class path
```java
// OLD:
import net.minecraft.util.WorldSavePath;

// NEW:
Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
```

---

## Architecture Changes

### Data Storage Strategy

**Before**: SavedData (Minecraft's persistent NBT system)
- Complex API with version-specific changes
- Difficult to debug
- Binary format (NBT)

**After**: JSON file storage
- Simple, human-readable format
- Easy to debug and edit manually
- Standard Java I/O
- Uses Google Gson (already included in Minecraft)

### Data Flow

```
Player Action → CurrencyManager → ShopMod.dataManager → ShopDataManager
                                                              ↓
                                                        shop_data.json
```

---

## Current Features

### Commands
- `/shop` - Opens the shop GUI
- `/balance` or `/bal` - Check your balance
- `/shopadmin setmoney <amount>` - Set your balance (admin)
- `/shopadmin addmoney <amount>` - Add money to your balance (admin)

### Data Persistence
- Player balances stored in `world/shop_data.json`
- Automatically saves on server stop
- Automatically loads on server start
- Statistics tracking:
  - Balance
  - Total earned
  - Total spent
  - Total playtime ticks
  - Last passive income time
  - Items bought count
  - Items sold count

### GUI System
- Shop interface using sgui library
- Tab-based navigation (Buy/Sell/Balance)
- Real-time balance display
- Item purchase with quantity selection (1/16/64)

---

## File Structure

### Main Classes
- **ShopMod.java** - Mod initializer, lifecycle events
- **ShopDataManager.java** - JSON-based player data storage
- **CurrencyManager.java** - Helper methods for currency operations
- **ShopCommands.java** - Command registration and handlers
- **ShopGui.java** - Shop interface using sgui
- **ItemPricing.java** - Item price database (placeholder)

### Data Files
- **shop_data.json** - Player currency and statistics
- **shop.mixins.json** - Mixin configuration
- **fabric.mod.json** - Mod metadata

---

## Dependencies

### Required
- Minecraft: 1.21.11
- Fabric Loader: 0.18.4
- Fabric API: 0.141.1+1.21.11
- sgui: 1.12.0+1.21.11 (from maven.nucleoid.xyz)

### Bundled
- Gson (included in Minecraft)

---

## Known Issues & TODOs

### High Priority
1. **Permission System**: Add proper permission checking for admin commands
   - Consider using Fabric Permissions API
   - Or implement custom permission levels

2. **Item Pricing**: Expand from 9 items to all Minecraft items
   - Load from config file
   - Allow server operators to customize prices

3. **Sell Tab**: Implement selling functionality in GUI
   - Currently only buying is implemented

### Medium Priority
4. **Passive Income**: Implement time-based income system
   - Track playtime
   - Award money periodically

5. **Activity Income**: Reward players for activities
   - Farming crops
   - Chopping trees
   - Mining ores

6. **Balance Statistics Tab**: Show detailed stats in GUI
   - Total earned
   - Total spent
   - Items bought/sold

### Low Priority
7. **Config File**: Add configuration options
   - Currency name/symbol
   - Starting balance
   - Passive income rate
   - Activity rewards

8. **Localization**: Add translation support
   - Create language files
   - Support multiple languages

---

## Testing Checklist

### Basic Functionality
- [ ] `/shop` opens GUI without errors
- [ ] `/balance` shows current balance
- [ ] Items can be purchased from shop
- [ ] Balance deducts correctly
- [ ] Items appear in player inventory
- [ ] Data persists across server restarts

### Admin Commands
- [ ] `/shopadmin setmoney` sets balance
- [ ] `/shopadmin addmoney` adds to balance

### Error Handling
- [ ] Insufficient funds prevents purchase
- [ ] Invalid commands show error messages
- [ ] Server stop/start doesn't lose data

---

## Next Steps

1. **Test in-game**: Launch Minecraft and test all features
2. **Fix permission system**: Implement proper permission checking
3. **Expand pricing**: Add more items to the shop
4. **Implement selling**: Complete the sell tab functionality
5. **Add passive income**: Implement time-based rewards
6. **Create GitHub release**: Package JAR with changelog

---

## References

### ServerShop Mod
- URL: https://git.brn.systems/BRNSystems/ServerShop
- Version: 1.21.8
- Key Learning: File-based storage approach, lifecycle event usage

### Fabric Wiki
- Commands: https://wiki.fabricmc.net/tutorial:commands
- Key Learning: Command registration, source stack methods

### Mojang Mappings
- Note: Using Mojang official mappings, NOT Yarn
- Class names differ: `CommandSourceStack` vs `ServerCommandSource`
- Method names differ: Check mapping files in `.gradle/loom-cache/`

---

## Build Information

**Gradle Version**: 9.2.1
**Fabric Loom**: 1.14.10
**Java**: 21
**Mappings**: Mojang Official

**Build Command**: `gradlew.bat build`
**Output**: `build/libs/shop-1.0.0.jar`

---

## Success! 🎉

The mod now compiles successfully and is ready for in-game testing!
