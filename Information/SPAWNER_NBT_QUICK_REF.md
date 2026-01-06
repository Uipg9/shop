# QUICK REFERENCE: Spawner NBT in MC 1.21.11

## ✅ CORRECT CODE (Copy & Paste)

```java
// IMPORTS
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.component.TypedEntityData; // ← NOT from SpawnerBlockEntity!
import net.minecraft.nbt.CompoundTag;

// CREATE SPAWNER FROM BLOCK ENTITY
CompoundTag spawnerData = spawner.saveWithoutMetadata(world.registryAccess());
spawnerData.remove("x");
spawnerData.remove("y");
spawnerData.remove("z");
spawnerData.remove("id");

spawnerItem.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, spawnerData)
);

// CREATE SPAWNER FROM SCRATCH
CompoundTag data = new CompoundTag();
CompoundTag spawnData = new CompoundTag();
CompoundTag entity = new CompoundTag();
entity.putString("id", "minecraft:zombie"); // ← Set mob here
spawnData.put("entity", entity);
data.put("SpawnData", spawnData);

spawnerItem.set(
    DataComponents.BLOCK_ENTITY_DATA,
    TypedEntityData.of(BlockEntityType.MOB_SPAWNER, data)
);

// READ NBT SAFELY (MC 1.21.11 returns Optional)
if (tag.contains("SpawnData")) {
    CompoundTag spawnData = tag.getCompound("SpawnData").orElse(new CompoundTag());
    if (spawnData.contains("entity")) {
        CompoundTag entity = spawnData.getCompound("entity").orElse(new CompoundTag());
        String mobId = entity.getString("id").orElse("unknown");
    }
}
```

## ❌ WRONG (Don't Use)

```java
// OLD 1.20 API - DOESN'T WORK
spawner.setTag(nbt);
nbt.put("BlockEntityTag", data);

// WRONG IMPORT
import net.minecraft.world.level.block.entity.SpawnerBlockEntity.TypedEntityData; // ❌

// WRONG NBT API (1.20 style)
if (tag.contains("key", 10)) { // ❌ Type parameter removed
    String value = tag.getString("key"); // ❌ Now returns Optional
}
```

## 📋 NBT Structure

```
Spawner Item NBT:
{
    SpawnData: {
        entity: {
            id: "minecraft:zombie"
        }
    }
}
```

## 🔧 Common Mob IDs

```
minecraft:zombie
minecraft:skeleton
minecraft:spider
minecraft:cave_spider
minecraft:creeper
minecraft:blaze
minecraft:magma_cube
minecraft:silverfish
```

## 🐛 Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| Spawner spawns pigs | Missing/wrong SpawnData | Check `SpawnData -> entity -> id` structure |
| Spawner empty (no flames) | entity compound missing | Ensure entity has `id` field |
| Import error | Wrong location | Use `net.minecraft.world.item.component.TypedEntityData` |
| Optional errors | New MC 1.21 API | Use `.orElse()` on get methods |

---
**Quick Ref v1.0** | MC 1.21.11 Fabric + Mojang Mappings
