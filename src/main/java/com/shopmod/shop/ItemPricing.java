package com.shopmod.shop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.*;

/**
 * Comprehensive pricing for ALL Minecraft items, organized by shop tier.
 * Sell price is 80% of buy price (20% shop fee).
 */
public class ItemPricing {
    
    private static final Map<Item, PriceData> PRICES = new HashMap<>();
    
    static {
        // ===== TIER 0: STARTER (FREE) - Basic survival essentials =====
        // Basic blocks
        addItem(Items.DIRT, 1, ShopTier.STARTER);
        addItem(Items.COBBLESTONE, 1, ShopTier.STARTER);
        addItem(Items.STONE, 2, ShopTier.STARTER);
        addItem(Items.SAND, 2, ShopTier.STARTER);
        addItem(Items.GRAVEL, 2, ShopTier.STARTER);
        addItem(Items.ANDESITE, 1, ShopTier.STARTER);
        addItem(Items.DIORITE, 1, ShopTier.STARTER);
        addItem(Items.GRANITE, 1, ShopTier.STARTER);
        
        // Basic wood
        addItem(Items.OAK_LOG, 5, ShopTier.STARTER);
        addItem(Items.OAK_PLANKS, 2, ShopTier.STARTER);
        addItem(Items.STICK, 1, ShopTier.STARTER);
        addItem(Items.CRAFTING_TABLE, 10, ShopTier.STARTER);
        addItem(Items.FURNACE, 20, ShopTier.STARTER);
        addItem(Items.TORCH, 2, ShopTier.STARTER);
        addItem(Items.LADDER, 5, ShopTier.STARTER);
        
        // Basic food & farming
        addItem(Items.BREAD, 10, ShopTier.STARTER);
        addItem(Items.APPLE, 8, ShopTier.STARTER);
        addItem(Items.COOKED_BEEF, 15, ShopTier.STARTER);
        addItem(Items.COOKED_PORKCHOP, 15, ShopTier.STARTER);
        addItem(Items.COOKED_CHICKEN, 12, ShopTier.STARTER);
        addItem(Items.COOKED_MUTTON, 12, ShopTier.STARTER);
        addItem(Items.BAKED_POTATO, 10, ShopTier.STARTER);
        
        // Seeds
        addItem(Items.WHEAT_SEEDS, 2, ShopTier.STARTER);
        addItem(Items.WHEAT, 5, ShopTier.STARTER);
        addItem(Items.CARROT, 3, ShopTier.STARTER);
        addItem(Items.POTATO, 3, ShopTier.STARTER);
        addItem(Items.BEETROOT_SEEDS, 2, ShopTier.STARTER);
        addItem(Items.BEETROOT, 5, ShopTier.STARTER);
        
        // Basic tools
        addItem(Items.WOODEN_PICKAXE, 5, ShopTier.STARTER);
        addItem(Items.WOODEN_AXE, 5, ShopTier.STARTER);
        addItem(Items.WOODEN_SHOVEL, 3, ShopTier.STARTER);
        addItem(Items.WOODEN_HOE, 5, ShopTier.STARTER);
        addItem(Items.WOODEN_SWORD, 5, ShopTier.STARTER);
        addItem(Items.STONE_PICKAXE, 10, ShopTier.STARTER);
        addItem(Items.STONE_AXE, 10, ShopTier.STARTER);
        addItem(Items.STONE_SHOVEL, 8, ShopTier.STARTER);
        addItem(Items.STONE_HOE, 10, ShopTier.STARTER);
        addItem(Items.STONE_SWORD, 10, ShopTier.STARTER);
        addItem(Items.BOW, 50, ShopTier.STARTER);
        addItem(Items.ARROW, 2, ShopTier.STARTER);
        addItem(Items.SHIELD, 40, ShopTier.STARTER);
        addItem(Items.FISHING_ROD, 30, ShopTier.STARTER);
        
        // Basic materials
        addItem(Items.COAL, 10, ShopTier.STARTER);
        addItem(Items.CHARCOAL, 8, ShopTier.STARTER);
        addItem(Items.IRON_NUGGET, 5, ShopTier.STARTER);
        addItem(Items.FLINT, 5, ShopTier.STARTER);
        addItem(Items.SNOWBALL, 1, ShopTier.STARTER);
        addItem(Items.STRING, 5, ShopTier.STARTER);
        addItem(Items.FEATHER, 5, ShopTier.STARTER);
        
        
        // ===== TIER 1: FARMER ($2,000) - All farming and animals =====
        // All wood types
        addItem(Items.SPRUCE_LOG, 5, ShopTier.FARMER);
        addItem(Items.BIRCH_LOG, 5, ShopTier.FARMER);
        addItem(Items.JUNGLE_LOG, 6, ShopTier.FARMER);
        addItem(Items.ACACIA_LOG, 5, ShopTier.FARMER);
        addItem(Items.DARK_OAK_LOG, 6, ShopTier.FARMER);
        addItem(Items.MANGROVE_LOG, 6, ShopTier.FARMER);
        addItem(Items.CHERRY_LOG, 7, ShopTier.FARMER);
        addItem(Items.BAMBOO, 3, ShopTier.FARMER);
        addItem(Items.BAMBOO_BLOCK, 10, ShopTier.FARMER);
        
        // All saplings
        addItem(Items.OAK_SAPLING, 10, ShopTier.FARMER);
        addItem(Items.SPRUCE_SAPLING, 10, ShopTier.FARMER);
        addItem(Items.BIRCH_SAPLING, 10, ShopTier.FARMER);
        addItem(Items.JUNGLE_SAPLING, 15, ShopTier.FARMER);
        addItem(Items.ACACIA_SAPLING, 10, ShopTier.FARMER);
        addItem(Items.DARK_OAK_SAPLING, 15, ShopTier.FARMER);
        addItem(Items.MANGROVE_PROPAGULE, 15, ShopTier.FARMER);
        addItem(Items.CHERRY_SAPLING, 20, ShopTier.FARMER);
        addItem(Items.AZALEA, 15, ShopTier.FARMER);
        addItem(Items.FLOWERING_AZALEA, 20, ShopTier.FARMER);
        
        // Advanced crops
        addItem(Items.PUMPKIN_SEEDS, 15, ShopTier.FARMER);
        addItem(Items.PUMPKIN, 25, ShopTier.FARMER);
        addItem(Items.MELON_SEEDS, 20, ShopTier.FARMER);
        addItem(Items.MELON_SLICE, 3, ShopTier.FARMER);
        addItem(Items.SUGAR_CANE, 5, ShopTier.FARMER);
        addItem(Items.CACTUS, 8, ShopTier.FARMER);
        addItem(Items.KELP, 5, ShopTier.FARMER);
        addItem(Items.SWEET_BERRIES, 6, ShopTier.FARMER);
        addItem(Items.GLOW_BERRIES, 8, ShopTier.FARMER);
        addItem(Items.COCOA_BEANS, 10, ShopTier.FARMER);
        
        // Flowers & dyes
        addItem(Items.DANDELION, 5, ShopTier.FARMER);
        addItem(Items.POPPY, 5, ShopTier.FARMER);
        addItem(Items.BLUE_ORCHID, 7, ShopTier.FARMER);
        addItem(Items.ALLIUM, 7, ShopTier.FARMER);
        addItem(Items.AZURE_BLUET, 5, ShopTier.FARMER);
        addItem(Items.RED_TULIP, 7, ShopTier.FARMER);
        addItem(Items.ORANGE_TULIP, 7, ShopTier.FARMER);
        addItem(Items.WHITE_TULIP, 7, ShopTier.FARMER);
        addItem(Items.PINK_TULIP, 7, ShopTier.FARMER);
        addItem(Items.OXEYE_DAISY, 5, ShopTier.FARMER);
        addItem(Items.CORNFLOWER, 7, ShopTier.FARMER);
        addItem(Items.LILY_OF_THE_VALLEY, 7, ShopTier.FARMER);
        addItem(Items.WITHER_ROSE, 50, ShopTier.FARMER);
        addItem(Items.SUNFLOWER, 10, ShopTier.FARMER);
        addItem(Items.LILAC, 10, ShopTier.FARMER);
        addItem(Items.ROSE_BUSH, 10, ShopTier.FARMER);
        addItem(Items.PEONY, 10, ShopTier.FARMER);
        addItem(Items.PITCHER_PLANT, 15, ShopTier.FARMER);
        addItem(Items.TORCHFLOWER, 15, ShopTier.FARMER);
        
        // Dyes
        addItem(Items.WHITE_DYE, 5, ShopTier.FARMER);
        addItem(Items.ORANGE_DYE, 5, ShopTier.FARMER);
        addItem(Items.MAGENTA_DYE, 5, ShopTier.FARMER);
        addItem(Items.LIGHT_BLUE_DYE, 5, ShopTier.FARMER);
        addItem(Items.YELLOW_DYE, 5, ShopTier.FARMER);
        addItem(Items.LIME_DYE, 5, ShopTier.FARMER);
        addItem(Items.PINK_DYE, 5, ShopTier.FARMER);
        addItem(Items.GRAY_DYE, 5, ShopTier.FARMER);
        addItem(Items.LIGHT_GRAY_DYE, 5, ShopTier.FARMER);
        addItem(Items.CYAN_DYE, 5, ShopTier.FARMER);
        addItem(Items.PURPLE_DYE, 5, ShopTier.FARMER);
        addItem(Items.BLUE_DYE, 5, ShopTier.FARMER);
        addItem(Items.BROWN_DYE, 5, ShopTier.FARMER);
        addItem(Items.GREEN_DYE, 5, ShopTier.FARMER);
        addItem(Items.RED_DYE, 5, ShopTier.FARMER);
        addItem(Items.BLACK_DYE, 5, ShopTier.FARMER);
        
        // Building blocks
        addItem(Items.CLAY, 10, ShopTier.FARMER);
        addItem(Items.CLAY_BALL, 3, ShopTier.FARMER);
        addItem(Items.TERRACOTTA, 15, ShopTier.FARMER);
        addItem(Items.GLASS, 5, ShopTier.FARMER);
        addItem(Items.GLASS_PANE, 3, ShopTier.FARMER);
        addItem(Items.MOSS_BLOCK, 10, ShopTier.FARMER);
        addItem(Items.MOSS_CARPET, 5, ShopTier.FARMER);
        
        // Wool
        addItem(Items.WHITE_WOOL, 10, ShopTier.FARMER);
        addItem(Items.ORANGE_WOOL, 10, ShopTier.FARMER);
        addItem(Items.MAGENTA_WOOL, 10, ShopTier.FARMER);
        addItem(Items.LIGHT_BLUE_WOOL, 10, ShopTier.FARMER);
        addItem(Items.YELLOW_WOOL, 10, ShopTier.FARMER);
        addItem(Items.LIME_WOOL, 10, ShopTier.FARMER);
        addItem(Items.PINK_WOOL, 10, ShopTier.FARMER);
        addItem(Items.GRAY_WOOL, 10, ShopTier.FARMER);
        addItem(Items.LIGHT_GRAY_WOOL, 10, ShopTier.FARMER);
        addItem(Items.CYAN_WOOL, 10, ShopTier.FARMER);
        addItem(Items.PURPLE_WOOL, 10, ShopTier.FARMER);
        addItem(Items.BLUE_WOOL, 10, ShopTier.FARMER);
        addItem(Items.BROWN_WOOL, 10, ShopTier.FARMER);
        addItem(Items.GREEN_WOOL, 10, ShopTier.FARMER);
        addItem(Items.RED_WOOL, 10, ShopTier.FARMER);
        addItem(Items.BLACK_WOOL, 10, ShopTier.FARMER);
        
        // Animal products & spawn eggs
        addItem(Items.LEATHER, 20, ShopTier.FARMER);
        addItem(Items.RABBIT_HIDE, 15, ShopTier.FARMER);
        addItem(Items.EGG, 5, ShopTier.FARMER);
        addItem(Items.MILK_BUCKET, 25, ShopTier.FARMER);
        addItem(Items.BONE, 8, ShopTier.FARMER);
        addItem(Items.BONE_MEAL, 3, ShopTier.FARMER);
        addItem(Items.INK_SAC, 10, ShopTier.FARMER);
        addItem(Items.GLOW_INK_SAC, 20, ShopTier.FARMER);
        addItem(Items.TURTLE_SCUTE, 30, ShopTier.FARMER);
        addItem(Items.HONEYCOMB, 25, ShopTier.FARMER);
        addItem(Items.HONEY_BOTTLE, 30, ShopTier.FARMER);
        
        addItem(Items.COW_SPAWN_EGG, 100, ShopTier.FARMER);
        addItem(Items.PIG_SPAWN_EGG, 100, ShopTier.FARMER);
        addItem(Items.SHEEP_SPAWN_EGG, 100, ShopTier.FARMER);
        addItem(Items.CHICKEN_SPAWN_EGG, 100, ShopTier.FARMER);
        addItem(Items.RABBIT_SPAWN_EGG, 150, ShopTier.FARMER);
        addItem(Items.HORSE_SPAWN_EGG, 200, ShopTier.FARMER);
        addItem(Items.DONKEY_SPAWN_EGG, 200, ShopTier.FARMER);
        addItem(Items.MULE_SPAWN_EGG, 250, ShopTier.FARMER);
        addItem(Items.BEE_SPAWN_EGG, 150, ShopTier.FARMER);
        addItem(Items.FOX_SPAWN_EGG, 150, ShopTier.FARMER);
        addItem(Items.WOLF_SPAWN_EGG, 150, ShopTier.FARMER);
        addItem(Items.CAT_SPAWN_EGG, 150, ShopTier.FARMER);
        addItem(Items.PARROT_SPAWN_EGG, 150, ShopTier.FARMER);
        addItem(Items.LLAMA_SPAWN_EGG, 200, ShopTier.FARMER);
        addItem(Items.TURTLE_SPAWN_EGG, 200, ShopTier.FARMER);
        addItem(Items.PANDA_SPAWN_EGG, 250, ShopTier.FARMER);
        addItem(Items.OCELOT_SPAWN_EGG, 150, ShopTier.FARMER);
        
        // Composter & farming utilities
        addItem(Items.COMPOSTER, 50, ShopTier.FARMER);
        addItem(Items.BEEHIVE, 80, ShopTier.FARMER);
        addItem(Items.BEE_NEST, 100, ShopTier.FARMER);
        
        
        // ===== TIER 2: ENGINEER ($5,000) - Redstone and mechanisms =====
        // Redstone components
        addItem(Items.REDSTONE, 15, ShopTier.ENGINEER);
        addItem(Items.REDSTONE_TORCH, 20, ShopTier.ENGINEER);
        addItem(Items.REDSTONE_BLOCK, 150, ShopTier.ENGINEER);
        addItem(Items.REPEATER, 30, ShopTier.ENGINEER);
        addItem(Items.COMPARATOR, 40, ShopTier.ENGINEER);
        addItem(Items.REDSTONE_LAMP, 50, ShopTier.ENGINEER);
        addItem(Items.DAYLIGHT_DETECTOR, 60, ShopTier.ENGINEER);
        addItem(Items.TARGET, 35, ShopTier.ENGINEER);
        addItem(Items.CALIBRATED_SCULK_SENSOR, 200, ShopTier.ENGINEER);
        addItem(Items.SCULK_SENSOR, 150, ShopTier.ENGINEER);
        
        // Pistons & movers
        addItem(Items.PISTON, 80, ShopTier.ENGINEER);
        addItem(Items.STICKY_PISTON, 100, ShopTier.ENGINEER);
        addItem(Items.OBSERVER, 90, ShopTier.ENGINEER);
        addItem(Items.DROPPER, 70, ShopTier.ENGINEER);
        addItem(Items.DISPENSER, 70, ShopTier.ENGINEER);
        addItem(Items.HOPPER, 150, ShopTier.ENGINEER);
        addItem(Items.SLIME_BLOCK, 100, ShopTier.ENGINEER);
        addItem(Items.HONEY_BLOCK, 120, ShopTier.ENGINEER);
        
        // Storage
        addItem(Items.CHEST, 30, ShopTier.ENGINEER);
        addItem(Items.TRAPPED_CHEST, 50, ShopTier.ENGINEER);
        addItem(Items.BARREL, 40, ShopTier.ENGINEER);
        addItem(Items.ENDER_CHEST, 500, ShopTier.ENGINEER);
        
        // Rails & minecarts
        addItem(Items.RAIL, 20, ShopTier.ENGINEER);
        addItem(Items.POWERED_RAIL, 40, ShopTier.ENGINEER);
        addItem(Items.DETECTOR_RAIL, 35, ShopTier.ENGINEER);
        addItem(Items.ACTIVATOR_RAIL, 35, ShopTier.ENGINEER);
        addItem(Items.MINECART, 100, ShopTier.ENGINEER);
        addItem(Items.CHEST_MINECART, 150, ShopTier.ENGINEER);
        addItem(Items.FURNACE_MINECART, 150, ShopTier.ENGINEER);
        addItem(Items.HOPPER_MINECART, 200, ShopTier.ENGINEER);
        addItem(Items.TNT_MINECART, 150, ShopTier.ENGINEER);
        
        // Buttons, levers, plates
        addItem(Items.LEVER, 15, ShopTier.ENGINEER);
        addItem(Items.STONE_BUTTON, 10, ShopTier.ENGINEER);
        addItem(Items.OAK_BUTTON, 10, ShopTier.ENGINEER);
        addItem(Items.STONE_PRESSURE_PLATE, 15, ShopTier.ENGINEER);
        addItem(Items.OAK_PRESSURE_PLATE, 15, ShopTier.ENGINEER);
        addItem(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 40, ShopTier.ENGINEER);
        addItem(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 40, ShopTier.ENGINEER);
        addItem(Items.TRIPWIRE_HOOK, 25, ShopTier.ENGINEER);
        addItem(Items.LECTERN, 50, ShopTier.ENGINEER);
        
        // Doors & trapdoors
        addItem(Items.IRON_DOOR, 60, ShopTier.ENGINEER);
        addItem(Items.IRON_TRAPDOOR, 80, ShopTier.ENGINEER);
        addItem(Items.OAK_DOOR, 15, ShopTier.ENGINEER);
        addItem(Items.OAK_TRAPDOOR, 20, ShopTier.ENGINEER);
        addItem(Items.OAK_FENCE_GATE, 20, ShopTier.ENGINEER);
        
        // TNT & explosives
        addItem(Items.TNT, 80, ShopTier.ENGINEER);
        addItem(Items.GUNPOWDER, 30, ShopTier.ENGINEER);
        
        // Note blocks & music
        addItem(Items.NOTE_BLOCK, 40, ShopTier.ENGINEER);
        addItem(Items.JUKEBOX, 100, ShopTier.ENGINEER);
        
        
        // ===== TIER 3: MERCHANT ($10,000) - Precious materials and trading =====
        // Ores & minerals
        addItem(Items.COAL_ORE, 15, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_COAL_ORE, 18, ShopTier.MERCHANT);
        addItem(Items.COPPER_ORE, 25, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_COPPER_ORE, 30, ShopTier.MERCHANT);
        addItem(Items.COPPER_INGOT, 30, ShopTier.MERCHANT);
        addItem(Items.IRON_ORE, 40, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_IRON_ORE, 50, ShopTier.MERCHANT);
        addItem(Items.RAW_IRON, 35, ShopTier.MERCHANT);
        addItem(Items.IRON_INGOT, 50, ShopTier.MERCHANT);
        addItem(Items.IRON_BLOCK, 450, ShopTier.MERCHANT);
        addItem(Items.GOLD_ORE, 80, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_GOLD_ORE, 100, ShopTier.MERCHANT);
        addItem(Items.RAW_GOLD, 80, ShopTier.MERCHANT);
        addItem(Items.GOLD_INGOT, 100, ShopTier.MERCHANT);
        addItem(Items.GOLD_BLOCK, 900, ShopTier.MERCHANT);
        addItem(Items.GOLD_NUGGET, 10, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_ORE, 400, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_DIAMOND_ORE, 500, ShopTier.MERCHANT);
        addItem(Items.DIAMOND, 500, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_BLOCK, 4500, ShopTier.MERCHANT);
        addItem(Items.EMERALD_ORE, 350, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_EMERALD_ORE, 400, ShopTier.MERCHANT);
        addItem(Items.EMERALD, 400, ShopTier.MERCHANT);
        addItem(Items.EMERALD_BLOCK, 3600, ShopTier.MERCHANT);
        addItem(Items.LAPIS_ORE, 60, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_LAPIS_ORE, 75, ShopTier.MERCHANT);
        addItem(Items.LAPIS_LAZULI, 20, ShopTier.MERCHANT);
        addItem(Items.LAPIS_BLOCK, 180, ShopTier.MERCHANT);
        addItem(Items.REDSTONE_ORE, 50, ShopTier.MERCHANT);
        addItem(Items.DEEPSLATE_REDSTONE_ORE, 60, ShopTier.MERCHANT);
        addItem(Items.QUARTZ, 25, ShopTier.MERCHANT);
        addItem(Items.QUARTZ_BLOCK, 100, ShopTier.MERCHANT);
        addItem(Items.AMETHYST_SHARD, 50, ShopTier.MERCHANT);
        addItem(Items.AMETHYST_BLOCK, 200, ShopTier.MERCHANT);
        
        // Tools & armor
        // Golden tools (fast but weak)
        addItem(Items.GOLDEN_PICKAXE, 300, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_AXE, 300, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_SHOVEL, 250, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_HOE, 300, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_SWORD, 300, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_HELMET, 400, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_CHESTPLATE, 600, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_LEGGINGS, 500, ShopTier.MERCHANT);
        addItem(Items.GOLDEN_BOOTS, 350, ShopTier.MERCHANT);
        
        // Iron tools
        addItem(Items.IRON_PICKAXE, 150, ShopTier.MERCHANT);
        addItem(Items.IRON_AXE, 150, ShopTier.MERCHANT);
        addItem(Items.IRON_SHOVEL, 100, ShopTier.MERCHANT);
        addItem(Items.IRON_HOE, 150, ShopTier.MERCHANT);
        addItem(Items.IRON_SWORD, 150, ShopTier.MERCHANT);
        addItem(Items.IRON_HELMET, 200, ShopTier.MERCHANT);
        addItem(Items.IRON_CHESTPLATE, 300, ShopTier.MERCHANT);
        addItem(Items.IRON_LEGGINGS, 250, ShopTier.MERCHANT);
        addItem(Items.IRON_BOOTS, 150, ShopTier.MERCHANT);
        
        // Diamond tools
        addItem(Items.DIAMOND_PICKAXE, 1800, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_AXE, 1800, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_SHOVEL, 1500, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_HOE, 1800, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_SWORD, 1800, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_HELMET, 2500, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_CHESTPLATE, 4000, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_LEGGINGS, 3500, ShopTier.MERCHANT);
        addItem(Items.DIAMOND_BOOTS, 2000, ShopTier.MERCHANT);
        
        // Other tools
        addItem(Items.CROSSBOW, 500, ShopTier.MERCHANT);
        addItem(Items.SPECTRAL_ARROW, 10, ShopTier.MERCHANT);
        addItem(Items.TIPPED_ARROW, 15, ShopTier.MERCHANT);
        addItem(Items.SHEARS, 100, ShopTier.MERCHANT);
        addItem(Items.FLINT_AND_STEEL, 50, ShopTier.MERCHANT);
        addItem(Items.COMPASS, 80, ShopTier.MERCHANT);
        addItem(Items.CLOCK, 80, ShopTier.MERCHANT);
        addItem(Items.SPYGLASS, 150, ShopTier.MERCHANT);
        addItem(Items.BUCKET, 120, ShopTier.MERCHANT);
        addItem(Items.WATER_BUCKET, 150, ShopTier.MERCHANT);
        addItem(Items.LAVA_BUCKET, 200, ShopTier.MERCHANT);
        
        // Enchanting
        addItem(Items.ENCHANTING_TABLE, 500, ShopTier.MERCHANT);
        addItem(Items.BOOKSHELF, 50, ShopTier.MERCHANT);
        addItem(Items.BOOK, 15, ShopTier.MERCHANT);
        addItem(Items.ANVIL, 800, ShopTier.MERCHANT);
        addItem(Items.GRINDSTONE, 150, ShopTier.MERCHANT);
        addItem(Items.SMITHING_TABLE, 200, ShopTier.MERCHANT);
        addItem(Items.EXPERIENCE_BOTTLE, 100, ShopTier.MERCHANT);
        
        // End items
        addItem(Items.ENDER_PEARL, 150, ShopTier.MERCHANT);
        addItem(Items.ENDER_EYE, 300, ShopTier.MERCHANT);
        addItem(Items.END_STONE, 50, ShopTier.MERCHANT);
        addItem(Items.OBSIDIAN, 100, ShopTier.MERCHANT);
        
        // Trading
        addItem(Items.VILLAGER_SPAWN_EGG, 500, ShopTier.MERCHANT);
        addItem(Items.WANDERING_TRADER_SPAWN_EGG, 400, ShopTier.MERCHANT);
        addItem(Items.EMERALD_ORE, 400, ShopTier.MERCHANT);
        
        
        // ===== TIER 4: NETHER MASTER ($25,000) - Nether items and potions =====
        // Nether blocks
        addItem(Items.NETHERRACK, 5, ShopTier.NETHER_MASTER);
        addItem(Items.NETHER_BRICKS, 15, ShopTier.NETHER_MASTER);
        addItem(Items.RED_NETHER_BRICKS, 20, ShopTier.NETHER_MASTER);
        addItem(Items.SOUL_SAND, 20, ShopTier.NETHER_MASTER);
        addItem(Items.SOUL_SOIL, 15, ShopTier.NETHER_MASTER);
        addItem(Items.BASALT, 10, ShopTier.NETHER_MASTER);
        addItem(Items.BLACKSTONE, 15, ShopTier.NETHER_MASTER);
        addItem(Items.GILDED_BLACKSTONE, 50, ShopTier.NETHER_MASTER);
        addItem(Items.GLOWSTONE, 40, ShopTier.NETHER_MASTER);
        addItem(Items.GLOWSTONE_DUST, 10, ShopTier.NETHER_MASTER);
        addItem(Items.SHROOMLIGHT, 50, ShopTier.NETHER_MASTER);
        addItem(Items.CRYING_OBSIDIAN, 200, ShopTier.NETHER_MASTER);
        addItem(Items.MAGMA_BLOCK, 30, ShopTier.NETHER_MASTER);
        
        // Nether ores
        addItem(Items.NETHER_QUARTZ_ORE, 40, ShopTier.NETHER_MASTER);
        addItem(Items.NETHER_GOLD_ORE, 60, ShopTier.NETHER_MASTER);
        
        // Nether vegetation
        addItem(Items.NETHER_WART, 25, ShopTier.NETHER_MASTER);
        addItem(Items.CRIMSON_FUNGUS, 30, ShopTier.NETHER_MASTER);
        addItem(Items.WARPED_FUNGUS, 30, ShopTier.NETHER_MASTER);
        addItem(Items.CRIMSON_ROOTS, 10, ShopTier.NETHER_MASTER);
        addItem(Items.WARPED_ROOTS, 10, ShopTier.NETHER_MASTER);
        addItem(Items.CRIMSON_NYLIUM, 25, ShopTier.NETHER_MASTER);
        addItem(Items.WARPED_NYLIUM, 25, ShopTier.NETHER_MASTER);
        addItem(Items.WEEPING_VINES, 15, ShopTier.NETHER_MASTER);
        addItem(Items.TWISTING_VINES, 15, ShopTier.NETHER_MASTER);
        
        // Nether wood
        addItem(Items.CRIMSON_STEM, 15, ShopTier.NETHER_MASTER);
        addItem(Items.WARPED_STEM, 15, ShopTier.NETHER_MASTER);
        
        // Mob drops
        addItem(Items.BLAZE_ROD, 150, ShopTier.NETHER_MASTER);
        addItem(Items.BLAZE_POWDER, 80, ShopTier.NETHER_MASTER);
        addItem(Items.MAGMA_CREAM, 70, ShopTier.NETHER_MASTER);
        addItem(Items.GHAST_TEAR, 200, ShopTier.NETHER_MASTER);
        addItem(Items.WITHER_SKELETON_SKULL, 500, ShopTier.NETHER_MASTER);
        
        // Brewing
        addItem(Items.BREWING_STAND, 150, ShopTier.NETHER_MASTER);
        addItem(Items.CAULDRON, 100, ShopTier.NETHER_MASTER);
        addItem(Items.GLASS_BOTTLE, 10, ShopTier.NETHER_MASTER);
        addItem(Items.FERMENTED_SPIDER_EYE, 30, ShopTier.NETHER_MASTER);
        addItem(Items.SPIDER_EYE, 20, ShopTier.NETHER_MASTER);
        addItem(Items.SUGAR, 5, ShopTier.NETHER_MASTER);
        addItem(Items.GLISTERING_MELON_SLICE, 80, ShopTier.NETHER_MASTER);
        addItem(Items.GOLDEN_CARROT, 120, ShopTier.NETHER_MASTER);
        addItem(Items.RABBIT_FOOT, 100, ShopTier.NETHER_MASTER);
        addItem(Items.PUFFERFISH, 50, ShopTier.NETHER_MASTER);
        addItem(Items.PHANTOM_MEMBRANE, 150, ShopTier.NETHER_MASTER);
        addItem(Items.TURTLE_HELMET, 300, ShopTier.NETHER_MASTER);
        
        // Utility
        addItem(Items.LODESTONE, 400, ShopTier.NETHER_MASTER);
        addItem(Items.RESPAWN_ANCHOR, 600, ShopTier.NETHER_MASTER);
        
        // Spawn eggs
        addItem(Items.BLAZE_SPAWN_EGG, 300, ShopTier.NETHER_MASTER);
        addItem(Items.MAGMA_CUBE_SPAWN_EGG, 200, ShopTier.NETHER_MASTER);
        addItem(Items.GHAST_SPAWN_EGG, 400, ShopTier.NETHER_MASTER);
        addItem(Items.HOGLIN_SPAWN_EGG, 250, ShopTier.NETHER_MASTER);
        addItem(Items.PIGLIN_SPAWN_EGG, 200, ShopTier.NETHER_MASTER);
        addItem(Items.STRIDER_SPAWN_EGG, 200, ShopTier.NETHER_MASTER);
        
        
        // ===== TIER 5: ELITE ($50,000) - End-game and rare items =====
        // Netherite
        addItem(Items.ANCIENT_DEBRIS, 1500, ShopTier.ELITE);
        addItem(Items.NETHERITE_SCRAP, 1200, ShopTier.ELITE);
        addItem(Items.NETHERITE_INGOT, 5000, ShopTier.ELITE);
        addItem(Items.NETHERITE_BLOCK, 45000, ShopTier.ELITE);
        
        addItem(Items.NETHERITE_PICKAXE, 18000, ShopTier.ELITE);
        addItem(Items.NETHERITE_AXE, 18000, ShopTier.ELITE);
        addItem(Items.NETHERITE_SHOVEL, 15000, ShopTier.ELITE);
        addItem(Items.NETHERITE_HOE, 18000, ShopTier.ELITE);
        addItem(Items.NETHERITE_SWORD, 18000, ShopTier.ELITE);
        addItem(Items.NETHERITE_HELMET, 25000, ShopTier.ELITE);
        addItem(Items.NETHERITE_CHESTPLATE, 40000, ShopTier.ELITE);
        addItem(Items.NETHERITE_LEGGINGS, 35000, ShopTier.ELITE);
        addItem(Items.NETHERITE_BOOTS, 20000, ShopTier.ELITE);
        
        // End items
        addItem(Items.ELYTRA, 15000, ShopTier.ELITE);
        addItem(Items.SHULKER_SHELL, 5000, ShopTier.ELITE);
        addItem(Items.SHULKER_BOX, 10000, ShopTier.ELITE);
        addItem(Items.DRAGON_EGG, 50000, ShopTier.ELITE);
        addItem(Items.DRAGON_HEAD, 20000, ShopTier.ELITE);
        addItem(Items.DRAGON_BREATH, 500, ShopTier.ELITE);
        addItem(Items.END_CRYSTAL, 1000, ShopTier.ELITE);
        addItem(Items.CHORUS_FRUIT, 50, ShopTier.ELITE);
        addItem(Items.CHORUS_FLOWER, 100, ShopTier.ELITE);
        addItem(Items.PURPUR_BLOCK, 80, ShopTier.ELITE);
        
        // Ultimate items
        addItem(Items.BEACON, 10000, ShopTier.ELITE);
        addItem(Items.NETHER_STAR, 8000, ShopTier.ELITE);
        addItem(Items.CONDUIT, 3000, ShopTier.ELITE);
        addItem(Items.HEART_OF_THE_SEA, 2000, ShopTier.ELITE);
        addItem(Items.NAUTILUS_SHELL, 500, ShopTier.ELITE);
        addItem(Items.TRIDENT, 4000, ShopTier.ELITE);
        addItem(Items.TOTEM_OF_UNDYING, 5000, ShopTier.ELITE);
        
        // Spawn eggs
        addItem(Items.ENDER_DRAGON_SPAWN_EGG, 100000, ShopTier.ELITE);
        addItem(Items.WITHER_SPAWN_EGG, 50000, ShopTier.ELITE);
        addItem(Items.ENDERMAN_SPAWN_EGG, 500, ShopTier.ELITE);
        addItem(Items.SHULKER_SPAWN_EGG, 2000, ShopTier.ELITE);
        
        // Music discs (collectibles)
        addItem(Items.MUSIC_DISC_13, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_CAT, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_BLOCKS, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_CHIRP, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_FAR, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_MALL, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_MELLOHI, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_STAL, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_STRAD, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_WARD, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_11, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_WAIT, 500, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_OTHERSIDE, 1000, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_5, 1000, ShopTier.ELITE);
        addItem(Items.MUSIC_DISC_PIGSTEP, 2000, ShopTier.ELITE);
    }
    
    private static void addItem(Item item, long buyPrice, ShopTier tier) {
        long sellPrice = (long)(buyPrice * 0.8); // 20% shop fee
        PRICES.put(item, new PriceData(buyPrice, sellPrice, tier));
    }
    
    public static long getBuyPrice(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.buyPrice() : 0;
    }
    
    public static long getSellPrice(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.sellPrice() : 0;
    }
    
    public static ShopTier getTier(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.tier() : null;
    }
    
    public static boolean canSell(Item item) {
        return PRICES.containsKey(item);
    }
    
    public static boolean hasPrice(Item item) {
        return PRICES.containsKey(item);
    }
    
    public static List<Item> getItemsForTier(ShopTier tier) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<Item, PriceData> entry : PRICES.entrySet()) {
            if (entry.getValue().tier() == tier) {
                items.add(entry.getKey());
            }
        }
        return items;
    }
    
    public static Map<Item, PriceData> getAllPrices() {
        return new HashMap<>(PRICES);
    }
    
    /**
     * Price data record
     */
    public record PriceData(long buyPrice, long sellPrice, ShopTier tier) {}
}
