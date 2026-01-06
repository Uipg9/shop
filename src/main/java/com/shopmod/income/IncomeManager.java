package com.shopmod.income;

import com.shopmod.ShopMod;
import com.shopmod.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

/**
 * Manages income from player activities
 */
public class IncomeManager {
    
    public static void initialize() {
        // Block break event handling moved to BlockEarningsHandler for proper batching
        // Do NOT register duplicate handler here - causes conflicts with timber/vein miner batching
    }
    
    private static void onBlockBreak(Level world, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        // Only process on server side and for real players
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        Block block = state.getBlock();
        int reward = 0;
        String activityType = "";
        
        // Check if it's a fully grown crop
        if (isFullyGrownCrop(state)) {
            // Get the crop item that would drop
            Item cropItem = getCropItem(block);
            if (cropItem != null && IncomeConfig.hasCropReward(cropItem)) {
                reward = IncomeConfig.getCropReward(cropItem);
                activityType = "farming";
            }
        }
        // Check if it's a rewarded block (mining/logging)
        else if (IncomeConfig.hasBlockReward(block)) {
            reward = IncomeConfig.getBlockReward(block);
            // Determine activity type
            if (isLog(block)) {
                activityType = "logging";
            } else {
                activityType = "mining";
            }
        }
        
        // Award money if there's a reward
        if (reward > 0 && ShopMod.dataManager != null) {
            // Apply income multiplier upgrade
            double multiplier = UpgradeManager.getIncomeMultiplier(serverPlayer.getUUID());
            int finalReward = (int) Math.round(reward * multiplier);
            
            ShopMod.dataManager.addMoney(serverPlayer.getUUID(), finalReward);
            
            // Show money notification in action bar only
            String message = "§6+$" + String.format("%,d", finalReward);
            if (multiplier > 1.0) {
                message += " §7(§e" + String.format("%.0f", multiplier * 100) + "%§7)";
            }
            serverPlayer.displayClientMessage(Component.literal(message), true);
        }
    }
    
    private static boolean isFullyGrownCrop(BlockState state) {
        Block block = state.getBlock();
        
        // Standard crops (wheat, carrots, potatoes, beetroot)
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        
        // Cocoa beans
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) == 2; // Max age is 2
        }
        
        // Nether wart
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) == 3; // Max age is 3
        }
        
        // Sweet berries
        if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 2; // Age 2-3 can be harvested
        }
        
        // Glow berries (cave vines)
        if (block instanceof CaveVinesBlock) {
            return state.getValue(CaveVinesBlock.BERRIES); // Has berries
        }
        
        return false;
    }
    
    private static Item getCropItem(Block block) {
        // Map blocks to their crop items - simplified approach
        if (block == Blocks.WHEAT) return Items.WHEAT;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT;
        if (block == Blocks.NETHER_WART) return Items.NETHER_WART;
        if (block == Blocks.SWEET_BERRY_BUSH) return Items.SWEET_BERRIES;
        if (block == Blocks.COCOA) return Items.COCOA_BEANS;
        if (block == Blocks.MELON) return Items.MELON_SLICE;
        if (block == Blocks.PUMPKIN) return Items.PUMPKIN;
        return null;
    }
    
    private static boolean isLog(Block block) {
        String name = block.toString().toLowerCase();
        return name.contains("log") || name.contains("stem");
    }
    
    /**
     * Process passive income for a player (should be called periodically)
     */
    public static void processPassiveIncome(ServerPlayer player) {
        if (ShopMod.dataManager == null) return;
        
        long currentTime = System.currentTimeMillis();
        long lastTime = ShopMod.dataManager.getLastPassiveIncomeTime(player.getUUID());
        
        // First time or calculate time difference
        if (lastTime > 0) {
            long timeDiff = currentTime - lastTime;
            double secondsElapsed = timeDiff / 1000.0;
            
            // Award passive income
            int income = (int) (secondsElapsed * IncomeConfig.PASSIVE_INCOME_PER_SECOND);
            if (income > 0) {
                ShopMod.dataManager.addMoney(player.getUUID(), income);
                player.sendSystemMessage(
                    Component.literal("§6Passive Income: +$" + income)
                );
            }
        }
        
        // Update last passive income time
        ShopMod.dataManager.setLastPassiveIncomeTime(player.getUUID(), currentTime);
    }
}
