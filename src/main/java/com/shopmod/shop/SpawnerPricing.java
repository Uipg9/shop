package com.shopmod.shop;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.*;

/**
 * Pricing for mob spawners - organized by tier and difficulty
 */
public class SpawnerPricing {
    
    // Base spawner price (empty spawner, no mob)
    public static final long BASE_SPAWNER_PRICE = 1000;
    public static final String BASE_SPAWNER_NAME = "Empty Spawner";
    public static final String BASE_SPAWNER_DESC = "§7Place spawn eggs inside to configure";
    
    public static class SpawnEggData {
        public final EntityType<?> entityType;
        public final String displayName;
        public final long price;
        public final ShopTier tier;
        public final String description;
        
        public SpawnEggData(EntityType<?> entityType, String displayName, long price, ShopTier tier, String description) {
            this.entityType = entityType;
            this.displayName = displayName;
            this.price = price;
            this.tier = tier;
            this.description = description;
        }
    }
    
    private static final Map<EntityType<?>, SpawnEggData> SPAWN_EGGS = new HashMap<>();
    
    static {
        // ===== TIER 1: FARMER ($2,000) - Passive/Friendly Spawn Eggs =====
        addSpawnEgg(EntityType.COW, "Cow Spawn Egg", 2500, ShopTier.FARMER, 
            "§7Spawns cows for leather & beef");
        addSpawnEgg(EntityType.PIG, "Pig Spawn Egg", 2500, ShopTier.FARMER, 
            "§7Spawns pigs for porkchops");
        addSpawnEgg(EntityType.SHEEP, "Sheep Spawn Egg", 2500, ShopTier.FARMER, 
            "§7Spawns sheep for wool");
        addSpawnEgg(EntityType.CHICKEN, "Chicken Spawn Egg", 2000, ShopTier.FARMER, 
            "§7Spawns chickens for eggs & meat");
        addSpawnEgg(EntityType.RABBIT, "Rabbit Spawn Egg", 3000, ShopTier.FARMER, 
            "§7Spawns rabbits for hide & meat");
        addSpawnEgg(EntityType.HORSE, "Horse Spawn Egg", 4000, ShopTier.FARMER, 
            "§7Spawns horses for transportation");
        addSpawnEgg(EntityType.DONKEY, "Donkey Spawn Egg", 4000, ShopTier.FARMER, 
            "§7Spawns donkeys with storage");
        addSpawnEgg(EntityType.CAT, "Cat Spawn Egg", 3000, ShopTier.FARMER, 
            "§7Spawns cats for companionship");
        addSpawnEgg(EntityType.WOLF, "Wolf Spawn Egg", 3500, ShopTier.FARMER, 
            "§7Spawns wolves for protection");
        addSpawnEgg(EntityType.BEE, "Bee Spawn Egg", 3000, ShopTier.FARMER, 
            "§7Spawns bees for honey farming");
        addSpawnEgg(EntityType.TURTLE, "Turtle Spawn Egg", 4000, ShopTier.FARMER, 
            "§7Spawns turtles for scutes");
        addSpawnEgg(EntityType.FOX, "Fox Spawn Egg", 3500, ShopTier.FARMER, 
            "§7Spawns foxes");
        addSpawnEgg(EntityType.PANDA, "Panda Spawn Egg", 5000, ShopTier.FARMER, 
            "§7Spawns pandas");
        
        
        // ===== TIER 2: ENGINEER ($5,000) - Utility Spawn Eggs =====
        addSpawnEgg(EntityType.VILLAGER, "Villager Spawn Egg", 8000, ShopTier.ENGINEER, 
            "§7Spawns villagers for trading");
        addSpawnEgg(EntityType.IRON_GOLEM, "Iron Golem Spawn Egg", 10000, ShopTier.ENGINEER, 
            "§7Spawns iron golems for protection & iron");
        addSpawnEgg(EntityType.SNOW_GOLEM, "Snow Golem Spawn Egg", 3000, ShopTier.ENGINEER, 
            "§7Spawns snow golems for defense");
        addSpawnEgg(EntityType.SQUID, "Squid Spawn Egg", 2000, ShopTier.ENGINEER, 
            "§7Spawns squids for ink sacs");
        addSpawnEgg(EntityType.GLOW_SQUID, "Glow Squid Spawn Egg", 3500, ShopTier.ENGINEER, 
            "§7Spawns glow squids for glow ink");
        addSpawnEgg(EntityType.BAT, "Bat Spawn Egg", 1000, ShopTier.ENGINEER, 
            "§7Spawns bats (ambient)");
        
        
        // ===== TIER 3: MERCHANT ($10,000) - Basic Hostile Spawn Eggs (XP & Common Drops) =====
        addSpawnEgg(EntityType.ZOMBIE, "Zombie Spawn Egg", 5000, ShopTier.MERCHANT, 
            "§7Spawns zombies for XP & rotten flesh");
        addSpawnEgg(EntityType.SKELETON, "Skeleton Spawn Egg", 7000, ShopTier.MERCHANT, 
            "§7Spawns skeletons for XP, bones & arrows");
        addSpawnEgg(EntityType.SPIDER, "Spider Spawn Egg", 6000, ShopTier.MERCHANT, 
            "§7Spawns spiders for XP, string & eyes");
        addSpawnEgg(EntityType.CAVE_SPIDER, "Cave Spider Spawn Egg", 6500, ShopTier.MERCHANT, 
            "§7Spawns cave spiders for XP & string");
        addSpawnEgg(EntityType.CREEPER, "Creeper Spawn Egg", 8000, ShopTier.MERCHANT, 
            "§7Spawns creepers for gunpowder");
        addSpawnEgg(EntityType.SLIME, "Slime Spawn Egg", 7000, ShopTier.MERCHANT, 
            "§7Spawns slimes for slimeballs");
        addSpawnEgg(EntityType.SILVERFISH, "Silverfish Spawn Egg", 3000, ShopTier.MERCHANT, 
            "§7Spawns silverfish for XP");
        addSpawnEgg(EntityType.WITCH, "Witch Spawn Egg", 10000, ShopTier.MERCHANT, 
            "§7Spawns witches for potion ingredients");
        addSpawnEgg(EntityType.DROWNED, "Drowned Spawn Egg", 9000, ShopTier.MERCHANT, 
            "§7Spawns drowned for copper & tridents");
        addSpawnEgg(EntityType.HUSK, "Husk Spawn Egg", 6000, ShopTier.MERCHANT, 
            "§7Spawns husks (desert zombies)");
        addSpawnEgg(EntityType.STRAY, "Stray Spawn Egg", 7500, ShopTier.MERCHANT, 
            "§7Spawns strays (ice skeletons)");
        addSpawnEgg(EntityType.PHANTOM, "Phantom Spawn Egg", 12000, ShopTier.MERCHANT, 
            "§7Spawns phantoms for membranes");
        addSpawnEgg(EntityType.GUARDIAN, "Guardian Spawn Egg", 15000, ShopTier.MERCHANT, 
            "§7Spawns guardians for prismarine");
        
        
        // ===== TIER 4: NETHER MASTER ($25,000) - Nether Spawn Eggs (Valuable Drops) =====
        addSpawnEgg(EntityType.ZOMBIFIED_PIGLIN, "Zombified Piglin Spawn Egg", 8000, ShopTier.NETHER_MASTER, 
            "§7Spawns zombified piglins for gold");
        addSpawnEgg(EntityType.PIGLIN, "Piglin Spawn Egg", 12000, ShopTier.NETHER_MASTER, 
            "§7Spawns piglins for bartering & gold");
        addSpawnEgg(EntityType.PIGLIN_BRUTE, "Piglin Brute Spawn Egg", 15000, ShopTier.NETHER_MASTER, 
            "§7Spawns piglin brutes (strong)");
        addSpawnEgg(EntityType.BLAZE, "Blaze Spawn Egg", 20000, ShopTier.NETHER_MASTER, 
            "§7Spawns blazes for blaze rods!");
        addSpawnEgg(EntityType.WITHER_SKELETON, "Wither Skeleton Spawn Egg", 25000, ShopTier.NETHER_MASTER, 
            "§7Spawns wither skeletons for skulls!");
        addSpawnEgg(EntityType.MAGMA_CUBE, "Magma Cube Spawn Egg", 10000, ShopTier.NETHER_MASTER, 
            "§7Spawns magma cubes for magma cream");
        addSpawnEgg(EntityType.GHAST, "Ghast Spawn Egg", 30000, ShopTier.NETHER_MASTER, 
            "§7Spawns ghasts for tears & gunpowder");
        addSpawnEgg(EntityType.HOGLIN, "Hoglin Spawn Egg", 12000, ShopTier.NETHER_MASTER, 
            "§7Spawns hoglins for porkchops");
        addSpawnEgg(EntityType.STRIDER, "Strider Spawn Egg", 8000, ShopTier.NETHER_MASTER, 
            "§7Spawns striders for lava travel");
        
        
        // ===== TIER 5: ELITE ($50,000) - End-Game Spawn Eggs (Rare & Powerful) =====
        addSpawnEgg(EntityType.ENDERMAN, "Enderman Spawn Egg", 15000, ShopTier.ELITE, 
            "§7Spawns endermen for ender pearls");
        addSpawnEgg(EntityType.SHULKER, "Shulker Spawn Egg", 40000, ShopTier.ELITE, 
            "§7Spawns shulkers for shells!");
        addSpawnEgg(EntityType.ELDER_GUARDIAN, "Elder Guardian Spawn Egg", 50000, ShopTier.ELITE, 
            "§7Spawns elder guardians (rare drops)");
        addSpawnEgg(EntityType.EVOKER, "Evoker Spawn Egg", 35000, ShopTier.ELITE, 
            "§7Spawns evokers for totems!");
        addSpawnEgg(EntityType.VINDICATOR, "Vindicator Spawn Egg", 20000, ShopTier.ELITE, 
            "§7Spawns vindicators for emeralds");
        addSpawnEgg(EntityType.PILLAGER, "Pillager Spawn Egg", 18000, ShopTier.ELITE, 
            "§7Spawns pillagers for crossbows");
        addSpawnEgg(EntityType.RAVAGER, "Ravager Spawn Egg", 30000, ShopTier.ELITE, 
            "§7Spawns ravagers (strong raid mob)");
        addSpawnEgg(EntityType.VEX, "Vex Spawn Egg", 25000, ShopTier.ELITE, 
            "§7Spawns vexes (small flying mobs)");
        addSpawnEgg(EntityType.WARDEN, "Warden Spawn Egg", 100000, ShopTier.ELITE, 
            "§c§lEXTREMELY DANGEROUS!");
        addSpawnEgg(EntityType.WITHER, "Wither Spawn Egg", 150000, ShopTier.ELITE, 
            "§c§lSPAWNS A WITHER BOSS!");
        addSpawnEgg(EntityType.ENDER_DRAGON, "Ender Dragon Spawn Egg", 500000, ShopTier.ELITE, 
            "§5§lSPAWNS THE ENDER DRAGON!");
    }
    
    private static void addSpawnEgg(EntityType<?> type, String name, long price, ShopTier tier, String desc) {
        SPAWN_EGGS.put(type, new SpawnEggData(type, name, price, tier, desc));
    }
    
    public static SpawnEggData getSpawnEggData(EntityType<?> type) {
        return SPAWN_EGGS.get(type);
    }
    
    public static List<SpawnEggData> getAllSpawnEggs() {
        return new ArrayList<>(SPAWN_EGGS.values());
    }
    
    public static List<SpawnEggData> getSpawnEggsForTier(ShopTier tier) {
        List<SpawnEggData> result = new ArrayList<>();
        for (SpawnEggData data : SPAWN_EGGS.values()) {
            if (data.tier == tier) {
                result.add(data);
            }
        }
        return result;
    }
    
    public static long getPrice(EntityType<?> type) {
        SpawnEggData data = SPAWN_EGGS.get(type);
        return data != null ? data.price : 0;
    }
    
    public static long getSellPrice(EntityType<?> type) {
        SpawnEggData data = SPAWN_EGGS.get(type);
        return data != null ? (long)(data.price * 0.8) : 0; // 80% sell price
    }
    
    /**
     * Creates a base/empty spawner item (no mob configured)
     */
    public static ItemStack createBaseSpawner() {
        ItemStack spawner = new ItemStack(Items.SPAWNER);
        spawner.set(DataComponents.CUSTOM_NAME, Component.literal(BASE_SPAWNER_NAME));
        
        // Add lore to explain usage
        net.minecraft.world.item.component.ItemLore lore = new net.minecraft.world.item.component.ItemLore(
            java.util.List.of(
                Component.literal(BASE_SPAWNER_DESC),
                Component.literal("§8Right-click with spawn egg to configure")
            )
        );
        spawner.set(DataComponents.LORE, lore);
        
        return spawner;
    }
    
    /**
     * Creates a spawn egg item for the specified mob type
     */
    public static ItemStack createSpawnEggItem(EntityType<?> entityType) {
        // Get the spawn egg data
        SpawnEggData data = SPAWN_EGGS.get(entityType);
        
        // Get the corresponding spawn egg item from entity type
        net.minecraft.world.item.Item spawnEggItem = net.minecraft.world.item.SpawnEggItem.byId(entityType);
        
        if (spawnEggItem == null) {
            // Fallback to pig spawn egg if not found
            spawnEggItem = Items.PIG_SPAWN_EGG;
        }
        
        ItemStack egg = new ItemStack(spawnEggItem);
        
        // Set custom name for display
        if (data != null) {
            egg.set(DataComponents.CUSTOM_NAME, 
                Component.literal("§6" + data.displayName));
        }
        
        return egg;
    }
    
}
