package com.shopmod.events;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * v1.0.10: Detects timber/vein miner harvesting by scanning for disappearing blocks
 */
public class BlockEarningsHandler {
    
    private static final Map<UUID, PendingRewards> pendingRewards = new HashMap<>();
    private static final int ACCUMULATION_TICKS = 20;
    private static final double RADIUS = 8.0; // Enough for tall trees but not entire forests
    private static final int MAX_BLOCKS_PER_SCAN = 64; // Maximum blocks to count in one scan
    private static final int MAX_BLOCKS_PER_TICK = 128; // Cap scan operations per tick
    private static final boolean DEBUG_LOGGING = false; // Set to true for verbose logs
    private static final Set<UUID> recentBreakers = new HashSet<>();
    private static final Map<UUID, Map<BlockPos, Block>> previousBlockStates = new HashMap<>();
    private static final Set<UUID> processedDropEntities = new HashSet<>();
    private static final int DROP_AGE_WINDOW = 40; // Only count drops spawned in last 2 seconds
    
    private static class PendingRewards {
        long totalMoney = 0;
        int totalXP = 0;
        int blocksBroken = 0;
        int ticksSinceLastBlock = 0;
        BlockPos lastBlockCenterPos = null;
        ServerPlayer player = null;
    }
    
    public static void register() {
        // Catch normal player block breaks
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> {
            onBlockBreak(level, player, pos, state, entity);
            return true;
        });
        
        PlayerBlockBreakEvents.AFTER.register(BlockEarningsHandler::onBlockBreak);
        
        // Scan for destroyed blocks each tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerList().getPlayers().forEach(BlockEarningsHandler::scanForDestroyedBlocks);
        });
        
        // Process payouts
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            pendingRewards.entrySet().removeIf(entry -> {
                UUID playerId = entry.getKey();
                PendingRewards rewards = entry.getValue();
                rewards.ticksSinceLastBlock++;
                
                if (rewards.ticksSinceLastBlock >= ACCUMULATION_TICKS && rewards.blocksBroken > 0) {
                    if (rewards.player != null) {
                        if (rewards.totalMoney > 0) {
                            CurrencyManager.addMoney(rewards.player, rewards.totalMoney);
                        }
                        if (rewards.totalXP > 0) {
                            rewards.player.giveExperiencePoints(rewards.totalXP);
                        }
                        
                        String message = "§6+$" + String.format("%,d", rewards.totalMoney);
                        message += " §7(§e" + rewards.blocksBroken + " blocks§7)";
                        if (rewards.totalXP > 0) {
                            message += " §a+" + rewards.totalXP + " XP";
                        }
                        rewards.player.displayClientMessage(Component.literal(message), true);
                    }
                    recentBreakers.remove(playerId);
                    previousBlockStates.remove(playerId);
                    return true;
                }
                return false;
            });
        });
    }
    
    private static void scanForDestroyedBlocks(ServerPlayer player) {
        Level level = player.level();
        UUID playerId = player.getUUID();
        BlockPos playerPos = player.blockPosition();
        
        if (!recentBreakers.contains(playerId)) {
            previousBlockStates.remove(playerId);
            return;
        }
        
        PendingRewards rewards = pendingRewards.get(playerId);
        if (rewards == null) {
            previousBlockStates.remove(playerId);
            return;
        }
        
        Map<BlockPos, Block> previous = previousBlockStates.computeIfAbsent(playerId, k -> new HashMap<>());
        Map<BlockPos, Block> current = new HashMap<>();
        
        BlockPos scanCenter = rewards.lastBlockCenterPos != null ? rewards.lastBlockCenterPos : playerPos;
        int range = (int) RADIUS;
        
        // Scan area for valuable blocks (with cap to prevent lag)
        int scannedBlocks = 0;
        outerLoop:
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (++scannedBlocks > MAX_BLOCKS_PER_TICK) {
                        break outerLoop; // Cap per tick to avoid lag
                    }
                    BlockPos checkPos = scanCenter.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    Block block = state.getBlock();
                    
                    // Skip leaves, flowers, and other non-valuable blocks
                    if (isExcludedBlock(block)) {
                        continue;
                    }
                    
                    long value = getBlockValue(block);
                    if (value > 0 || getBlockXP(block) > 0) {
                        current.put(checkPos, block);
                    }
                }
            }
        }
        
        // Find blocks that disappeared (with maximum limit per scan)
        int processedDestroyedBlocks = 0;
        int totalBlocksThisScan = 0;
        for (BlockPos pos : previous.keySet()) {
            if (!current.containsKey(pos)) {
                if (++processedDestroyedBlocks > MAX_BLOCKS_PER_TICK) {
                    break; // Cap processing to avoid lag
                }
                if (++totalBlocksThisScan > MAX_BLOCKS_PER_SCAN) {
                    break; // Cap total blocks counted per scan to prevent exploits
                }
                
                Block block = previous.get(pos);
                long money = getBlockValue(block);
                int xp = getBlockXP(block);
                
                double multiplier = UpgradeManager.getIncomeMultiplier(playerId);
                double xpMult = UpgradeManager.getXPMultiplier(playerId);
                
                long finalMoney = (long)(money * multiplier);
                int finalXP = (int)(xp * xpMult);
                
                if (DEBUG_LOGGING) {
                    System.out.println("[SHOP] Destroyed: " + block.getName().getString() + " = $" + finalMoney);
                }
                
                rewards.totalMoney += finalMoney;
                rewards.totalXP += finalXP;
                rewards.blocksBroken++;
                rewards.lastBlockCenterPos = pos;
                rewards.ticksSinceLastBlock = 0;
            }
        }

        // Drop counting disabled - block state comparison is more accurate
        // (Previously this caused double-counting with vein/tree miners)
        
        previousBlockStates.put(playerId, current);
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
            return;
        }
        
        double incomeMultiplier = UpgradeManager.getIncomeMultiplier(serverPlayer.getUUID());
        double xpMultiplier = UpgradeManager.getXPMultiplier(serverPlayer.getUUID());
        
        long finalMoney = (long)(baseMoneyReward * incomeMultiplier);
        int finalXP = (int)(baseXpReward * xpMultiplier);
        
        UUID playerUUID = serverPlayer.getUUID();
        recentBreakers.add(playerUUID);
        PendingRewards rewards = pendingRewards.computeIfAbsent(playerUUID, k -> {
            PendingRewards r = new PendingRewards();
            r.player = serverPlayer;
            return r;
        });
        
        rewards.totalMoney += finalMoney;
        rewards.totalXP += finalXP;
        rewards.blocksBroken++;
        rewards.lastBlockCenterPos = pos;
        rewards.ticksSinceLastBlock = 0;
        
        if (DEBUG_LOGGING) {
            System.out.println("[SHOP] Batch: " + rewards.blocksBroken + " blocks, $" + rewards.totalMoney);
        }
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
        
        return 0;
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
        
        return 0;
    }
    
    /**
     * Check if a block should be excluded from scanning/rewards
     * Excludes leaves, flowers, grass, and other decorative blocks
     */
    private static boolean isExcludedBlock(Block block) {
        // Exclude all leaves
        if (block.defaultBlockState().is(net.minecraft.tags.BlockTags.LEAVES)) return true;
        
        // Exclude flowers and plants
        if (block.defaultBlockState().is(net.minecraft.tags.BlockTags.FLOWERS)) return true;
        if (block.defaultBlockState().is(net.minecraft.tags.BlockTags.SAPLINGS)) return true;
        
        // Exclude grass, ferns, etc.
        if (block == Blocks.SHORT_GRASS || block == Blocks.TALL_GRASS) return true;
        if (block == Blocks.FERN || block == Blocks.LARGE_FERN) return true;
        if (block == Blocks.DEAD_BUSH) return true;
        if (block == Blocks.VINE || block == Blocks.GLOW_LICHEN) return true;
        
        // Exclude air and water
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) return true;
        if (block == Blocks.WATER || block == Blocks.LAVA) return true;
        
        return false;
    }
}
