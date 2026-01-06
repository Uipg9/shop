package com.shopmod.events;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Universal earnings system for all block breaking activities
 * Players earn money and XP for breaking blocks
 * Batches rewards for compatibility with timber/vein miner mods
 */
public class BlockEarningsHandler {
    
    // Batch rewards for timber/vein miner compatibility
    private static final Map<UUID, PendingRewards> pendingRewards = new HashMap<>();
    private static final int BATCH_DELAY_TICKS = 2; // Wait 2 ticks before displaying
    
    private static class PendingRewards {
        long totalMoney = 0;
        int totalXP = 0;
        int ticksRemaining = BATCH_DELAY_TICKS;
    }
    
    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(BlockEarningsHandler::onBlockBreak);
        
        // Process batched rewards
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            pendingRewards.entrySet().removeIf(entry -> {
                entry.getValue().ticksRemaining--;
                
                if (entry.getValue().ticksRemaining <= 0) {
                    ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    if (player != null) {
                        PendingRewards rewards = entry.getValue();
                        
                        // Award money and XP
                        if (rewards.totalMoney > 0) {
                            CurrencyManager.addMoney(player, rewards.totalMoney);
                        }
                        if (rewards.totalXP > 0) {
                            player.giveExperiencePoints(rewards.totalXP);
                        }
                        
                        // Show combined earnings in action bar only
                        if (rewards.totalMoney > 0 || rewards.totalXP > 0) {
                            String message = "§6+$" + String.format("%,d", rewards.totalMoney);
                            if (rewards.totalXP > 0) {
                                message += " §a+" + rewards.totalXP + " XP";
                            }
                            player.displayClientMessage(Component.literal(message), true);
                        }
                    }
                    return true; // Remove from map
                }
                return false; // Keep in map
            });
        });
    }
    
    private static void onBlockBreak(Level world, Player player, BlockPos pos, 
                                    BlockState state, @Nullable BlockEntity blockEntity) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        Block block = state.getBlock();
        long baseMoneyReward = getBlockValue(block);
        int baseXpReward = getBlockXP(block);
        
        if (baseMoneyReward <= 0 && baseXpReward <= 0) {
            return; // No rewards for this block
        }
        
        // Apply upgrade multipliers
        double incomeMultiplier = UpgradeManager.getIncomeMultiplier(serverPlayer.getUUID());
        double xpMultiplier = UpgradeManager.getXPMultiplier(serverPlayer.getUUID());
        
        long finalMoney = (long)(baseMoneyReward * incomeMultiplier);
        int finalXP = (int)(baseXpReward * xpMultiplier);
        
        // Add to pending rewards (batch for timber/vein miner)
        UUID playerUUID = serverPlayer.getUUID();
        PendingRewards rewards = pendingRewards.computeIfAbsent(playerUUID, k -> new PendingRewards());
        rewards.totalMoney += finalMoney;
        rewards.totalXP += finalXP;
        rewards.ticksRemaining = BATCH_DELAY_TICKS; // Reset timer
    }
    
    /**
     * Get the base money value for breaking a block
     */
    private static long getBlockValue(Block block) {
        // Ores (high value)
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 100;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 120;
        if (block == Blocks.ANCIENT_DEBRIS) return 200;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return 50;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 20;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 15;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 25;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 15;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 5;
        if (block == Blocks.NETHER_QUARTZ_ORE) return 10;
        
        // Wood (medium value)
        if (block == Blocks.OAK_LOG || block == Blocks.STRIPPED_OAK_LOG) return 2;
        if (block == Blocks.SPRUCE_LOG || block == Blocks.STRIPPED_SPRUCE_LOG) return 2;
        if (block == Blocks.BIRCH_LOG || block == Blocks.STRIPPED_BIRCH_LOG) return 2;
        if (block == Blocks.JUNGLE_LOG || block == Blocks.STRIPPED_JUNGLE_LOG) return 3;
        if (block == Blocks.ACACIA_LOG || block == Blocks.STRIPPED_ACACIA_LOG) return 2;
        if (block == Blocks.DARK_OAK_LOG || block == Blocks.STRIPPED_DARK_OAK_LOG) return 3;
        if (block == Blocks.MANGROVE_LOG || block == Blocks.STRIPPED_MANGROVE_LOG) return 3;
        if (block == Blocks.CHERRY_LOG || block == Blocks.STRIPPED_CHERRY_LOG) return 4;
        if (block == Blocks.CRIMSON_STEM || block == Blocks.STRIPPED_CRIMSON_STEM) return 5;
        if (block == Blocks.WARPED_STEM || block == Blocks.STRIPPED_WARPED_STEM) return 5;
        
        // Stone (low value but common)
        if (block == Blocks.STONE) return 1;
        if (block == Blocks.DEEPSLATE) return 1;
        if (block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE) return 1;
        if (block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE) return 1;
        
        // Nether (medium value)
        if (block == Blocks.NETHERRACK) return 1;
        if (block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL) return 2;
        if (block == Blocks.BLACKSTONE) return 2;
        if (block == Blocks.BASALT || block == Blocks.SMOOTH_BASALT) return 2;
        if (block == Blocks.GLOWSTONE) return 10;
        
        // End (high value)
        if (block == Blocks.END_STONE) return 5;
        if (block == Blocks.PURPUR_BLOCK || block == Blocks.PURPUR_PILLAR) return 8;
        
        // Crops (low value)
        if (block == Blocks.WHEAT) return 1;
        if (block == Blocks.CARROTS || block == Blocks.POTATOES) return 1;
        if (block == Blocks.BEETROOTS) return 1;
        if (block == Blocks.MELON) return 2;
        if (block == Blocks.PUMPKIN) return 2;
        if (block == Blocks.SUGAR_CANE) return 1;
        if (block == Blocks.CACTUS) return 1;
        
        return 0; // No reward
    }
    
    /**
     * Get the base XP value for breaking a block
     */
    private static int getBlockXP(Block block) {
        // Ores give good XP
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 5;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 6;
        if (block == Blocks.ANCIENT_DEBRIS) return 10;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return 3;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 2;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 3;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 2;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 1;
        
        // Wood gives minimal XP
        if (block.defaultBlockState().is(net.minecraft.tags.BlockTags.LOGS)) return 1;
        
        // Most blocks don't give XP
        return 0;
    }
}
