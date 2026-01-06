package com.shopmod.income;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for income sources
 */
public class IncomeConfig {
    
    // Passive income: money per second while online
    public static final double PASSIVE_INCOME_PER_SECOND = 0.5; // $0.50/sec = $30/min = $1,800/hour
    
    // Block breaking rewards
    private static final Map<Block, Integer> BLOCK_REWARDS = new HashMap<>();
    
    // Crop harvesting rewards (fully grown crops)
    private static final Map<Item, Integer> CROP_REWARDS = new HashMap<>();
    
    static {
        // Mining rewards
        BLOCK_REWARDS.put(Blocks.STONE, 1);
        BLOCK_REWARDS.put(Blocks.COBBLESTONE, 1);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE, 1);
        BLOCK_REWARDS.put(Blocks.COAL_ORE, 5);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_COAL_ORE, 5);
        BLOCK_REWARDS.put(Blocks.IRON_ORE, 15);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_IRON_ORE, 15);
        BLOCK_REWARDS.put(Blocks.COPPER_ORE, 10);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_COPPER_ORE, 10);
        BLOCK_REWARDS.put(Blocks.GOLD_ORE, 25);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_GOLD_ORE, 25);
        BLOCK_REWARDS.put(Blocks.LAPIS_ORE, 20);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_LAPIS_ORE, 20);
        BLOCK_REWARDS.put(Blocks.REDSTONE_ORE, 15);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_REDSTONE_ORE, 15);
        BLOCK_REWARDS.put(Blocks.DIAMOND_ORE, 100);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_DIAMOND_ORE, 100);
        BLOCK_REWARDS.put(Blocks.EMERALD_ORE, 75);
        BLOCK_REWARDS.put(Blocks.DEEPSLATE_EMERALD_ORE, 75);
        BLOCK_REWARDS.put(Blocks.NETHER_QUARTZ_ORE, 10);
        BLOCK_REWARDS.put(Blocks.NETHER_GOLD_ORE, 20);
        BLOCK_REWARDS.put(Blocks.ANCIENT_DEBRIS, 500);
        
        // Logging rewards
        BLOCK_REWARDS.put(Blocks.OAK_LOG, 2);
        BLOCK_REWARDS.put(Blocks.SPRUCE_LOG, 2);
        BLOCK_REWARDS.put(Blocks.BIRCH_LOG, 2);
        BLOCK_REWARDS.put(Blocks.JUNGLE_LOG, 2);
        BLOCK_REWARDS.put(Blocks.ACACIA_LOG, 2);
        BLOCK_REWARDS.put(Blocks.DARK_OAK_LOG, 2);
        BLOCK_REWARDS.put(Blocks.MANGROVE_LOG, 2);
        BLOCK_REWARDS.put(Blocks.CHERRY_LOG, 2);
        BLOCK_REWARDS.put(Blocks.CRIMSON_STEM, 2);
        BLOCK_REWARDS.put(Blocks.WARPED_STEM, 2);
        
        // Crop harvesting rewards (when fully grown)
        CROP_REWARDS.put(Items.WHEAT, 3);
        CROP_REWARDS.put(Items.CARROT, 3);
        CROP_REWARDS.put(Items.POTATO, 3);
        CROP_REWARDS.put(Items.BEETROOT, 3);
        CROP_REWARDS.put(Items.MELON_SLICE, 2);
        CROP_REWARDS.put(Items.PUMPKIN, 5);
        CROP_REWARDS.put(Items.SUGAR_CANE, 2);
        CROP_REWARDS.put(Items.COCOA_BEANS, 4);
        CROP_REWARDS.put(Items.NETHER_WART, 5);
        CROP_REWARDS.put(Items.SWEET_BERRIES, 2);
        CROP_REWARDS.put(Items.GLOW_BERRIES, 2);
    }
    
    public static int getBlockReward(Block block) {
        return BLOCK_REWARDS.getOrDefault(block, 0);
    }
    
    public static int getCropReward(Item item) {
        return CROP_REWARDS.getOrDefault(item, 0);
    }
    
    public static boolean hasBlockReward(Block block) {
        return BLOCK_REWARDS.containsKey(block);
    }
    
    public static boolean hasCropReward(Item item) {
        return CROP_REWARDS.containsKey(item);
    }
}
