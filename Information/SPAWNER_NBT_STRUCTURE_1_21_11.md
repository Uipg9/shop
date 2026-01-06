# Spawner NBT Structure for Minecraft 1.21.11

## Critical Changes in MC 1.21+

### Old System (1.20 and earlier)
```java
// OLD - DOES NOT WORK IN 1.21+
ItemStack spawner = new ItemStack(Items.SPAWNER);
CompoundTag nbt = new CompoundTag();
nbt.put("BlockEntityTag", spawnerData);
spawner.setTag(nbt);
```

### New System (1.21+)
```java
// NEW - WORKS IN 1.21.11
ItemStack spawner = new ItemStack(Items.SPAWNER);
spawner.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, spawnerData)
);
```

## Correct NBT Structure

### Full Spawner NBT (when saved from world)
```nbt
{
    "x": 100,           // Block position - REMOVE for items
    "y": 64,            // Block position - REMOVE for items  
    "z": 200,           // Block position - REMOVE for items
    "id": "minecraft:mob_spawner",  // Block entity type - REMOVE for items
    
    "SpawnData": {      // THIS IS WHAT MATTERS
        "entity": {     // The mob to spawn
            "id": "minecraft:zombie"  // Mob type
        }
    },
    
    "SpawnPotentials": [...],  // Optional: multiple mob types
    "Delay": 20,
    "MinSpawnDelay": 200,
    "MaxSpawnDelay": 800,
    "SpawnCount": 4,
    "MaxNearbyEntities": 6,
    "RequiredPlayerRange": 16,
    "SpawnRange": 4
}
```

### Minimal NBT for Item (what you need)
```nbt
{
    "SpawnData": {
        "entity": {
            "id": "minecraft:zombie"
        }
    }
}
```

## Working Java Implementation

### Method 1: Copy All Spawner Data (Recommended)
```java
// Get spawner data from block entity
CompoundTag spawnerData = spawner.saveWithoutMetadata(world.registryAccess());

// Remove position/ID data that shouldn't be on items
spawnerData.remove("x");
spawnerData.remove("y");
spawnerData.remove("z");
spawnerData.remove("id");

// Set on item using new component system
spawnerItem.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, spawnerData)
);
```

### Method 2: Create Spawner from Scratch
```java
// Create new spawner with specific mob
CompoundTag blockEntityData = new CompoundTag();

// Create the SpawnData structure
CompoundTag spawnData = new CompoundTag();
CompoundTag entity = new CompoundTag();
entity.putString("id", "minecraft:zombie");  // Set mob type
spawnData.put("entity", entity);
blockEntityData.put("SpawnData", spawnData);

// Optional: Set spawn parameters
blockEntityData.putShort("Delay", (short)20);
blockEntityData.putShort("MinSpawnDelay", (short)200);
blockEntityData.putShort("MaxSpawnDelay", (short)800);
blockEntityData.putShort("SpawnCount", (short)4);
blockEntityData.putShort("MaxNearbyEntities", (short)6);
blockEntityData.putShort("RequiredPlayerRange", (short)16);
blockEntityData.putShort("SpawnRange", (short)4);

// Set on item
spawnerItem.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, blockEntityData)
);
```

## Common Issues and Solutions

### Issue 1: Spawner places but spawns pigs
**Problem:** Wrong NBT structure or missing SpawnData  
**Solution:** Ensure `SpawnData -> entity -> id` structure is correct

### Issue 2: Spawner places but is empty (no flames)
**Problem:** SpawnData exists but `entity` compound is missing or malformed  
**Solution:** Check that entity has `id` field: `entity.putString("id", "minecraft:zombie")`

### Issue 3: Spawner doesn't retain mob type
**Problem:** Using old `BlockEntityTag` system  
**Solution:** Use `DataComponents.BLOCK_ENTITY_DATA` instead

### Issue 4: Compilation errors about TypedEntityData
**Problem:** Missing imports  
**Solution:** Add: `import net.minecraft.world.level.block.entity.SpawnerBlockEntity.TypedEntityData;`

## Required Imports

```java
// Core components
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity.TypedEntityData;

// NBT
import net.minecraft.nbt.CompoundTag;

// Items
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
```

## Valid Mob IDs (Examples)

```
minecraft:zombie
minecraft:skeleton
minecraft:spider
minecraft:cave_spider
minecraft:creeper
minecraft:blaze
minecraft:magma_cube
minecraft:silverfish
minecraft:zombie_villager
minecraft:drowned
minecraft:piglin
minecraft:piglin_brute
minecraft:wither_skeleton
```

## Testing Your Implementation

### 1. Create Test Spawner
```java
ItemStack testSpawner = new ItemStack(Items.SPAWNER);
CompoundTag data = new CompoundTag();
CompoundTag spawnData = new CompoundTag();
CompoundTag entity = new CompoundTag();
entity.putString("id", "minecraft:zombie");
spawnData.put("entity", entity);
data.put("SpawnData", spawnData);

testSpawner.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, data)
);

player.getInventory().add(testSpawner);
```

### 2. Verify in Game
1. Place the spawner
2. Look for spinning zombie model inside
3. Check if flames appear
4. Wait for spawn (needs player within 16 blocks, dark area)

### 3. Debug NBT
```java
// Print NBT to console
System.out.println("Spawner NBT: " + spawnerData.toString());

// Check if SpawnData exists
if (!spawnerData.contains("SpawnData")) {
    System.err.println("ERROR: Missing SpawnData!");
}

// Check entity structure
CompoundTag spawnData = spawnerData.getCompound("SpawnData");
if (!spawnData.contains("entity")) {
    System.err.println("ERROR: Missing entity in SpawnData!");
}
```

## References

- **Minecraft Wiki:** https://minecraft.wiki/w/Monster_Spawner#Block_data
- **Fabric API:** Uses PlayerBlockBreakEvents for spawner pickup
- **Data Components:** New system in 1.20.5+ replacing old NBT tags

## Migration Guide (1.20 → 1.21)

### Before (1.20.4)
```java
ItemStack spawner = new ItemStack(Items.SPAWNER);
CompoundTag nbt = new CompoundTag();
CompoundTag blockEntityTag = new CompoundTag();
// ... set spawner data
nbt.put("BlockEntityTag", blockEntityTag);
spawner.setTag(nbt);
```

### After (1.21.11)
```java
ItemStack spawner = new ItemStack(Items.SPAWNER);
CompoundTag blockEntityData = new CompoundTag();
// ... set spawner data
spawner.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, blockEntityData)
);
```

## Conclusion

The key difference in MC 1.21+ is:
1. **Use component system** (DataComponents.BLOCK_ENTITY_DATA)
2. **Use TypedEntityData** wrapper
3. **Structure:** `SpawnData -> entity -> id`
4. **Remove** position data (x, y, z) and block entity ID from item NBT

This structure is confirmed working in MC 1.21.11 with Fabric and Mojang mappings.
