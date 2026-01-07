package com.shopmod.events;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * v1.0.18: Detects timber/vein miner harvesting by tracking inventory changes
 */
public class BlockEarningsHandler {
    
    private static final Map<UUID, PendingRewards> pendingRewards = new HashMap<>();
    private static final int ACCUMULATION_TICKS = 40; // Wait 40 ticks (2 seconds) for items to be collected
    private static final boolean DEBUG_LOGGING = false; // Set to true for verbose logs
    
    private static class PendingRewards {
        long totalMoney = 0;
        int totalXP = 0;
        int blocksBroken = 0;
        int ticksSinceLastBlock = 0;
        ServerPlayer player = null;
        Block brokenBlockType = null;
        Map<Item, Integer> inventoryBefore = new HashMap<>();
        boolean inventoryCaptured = false;
    }
    
    public static void register() {
        // Capture inventory BEFORE block break
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                Block block = state.getBlock();
                if (getBlockValue(block) > 0 || getBlockXP(block) > 0) {
                    captureInventoryBefore(serverPlayer, block);
                }
            }
            return true;
        });
        
        // Process AFTER block break
        PlayerBlockBreakEvents.AFTER.register(BlockEarningsHandler::onBlockBreak);
        
        // Clean up on player disconnect to prevent crashes
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            pendingRewards.remove(handler.getPlayer().getUUID());
        });
        
        // Check inventory changes each tick and process payouts
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            pendingRewards.entrySet().removeIf(entry -> {
                UUID playerId = entry.getKey();
                PendingRewards rewards = entry.getValue();
                rewards.ticksSinceLastBlock++;
                
                // Check inventory after a short delay to let harvester finish
                if (rewards.ticksSinceLastBlock == ACCUMULATION_TICKS && rewards.inventoryCaptured) {
                    checkInventoryChanges(rewards);
                }
                
                // Payout after accumulation window
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
                    return true;
                }
                return false;
            });
        });
    }
    
    /**
     * Captures player inventory BEFORE breaking a block
     * This lets us compare what items were added by the harvester
     */
    private static void captureInventoryBefore(ServerPlayer player, Block blockType) {
        UUID playerId = player.getUUID();
        PendingRewards rewards = pendingRewards.computeIfAbsent(playerId, k -> {
            PendingRewards r = new PendingRewards();
            r.player = player;
            return r;
        });
        
        // Capture current inventory counts for relevant items only
        rewards.inventoryBefore.clear();
        rewards.brokenBlockType = blockType;
        rewards.inventoryCaptured = true;
        
        Container inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                rewards.inventoryBefore.merge(item, stack.getCount(), Integer::sum);
            }
        }
        
        if (DEBUG_LOGGING) {
            System.out.println("[SHOP] Captured inventory before break: " + rewards.inventoryBefore.size() + " item types");
        }
    }
    
    /**
     * Checks what items were added to inventory and calculates rewards
     */
    private static void checkInventoryChanges(PendingRewards rewards) {
        if (rewards.player == null || !rewards.inventoryCaptured) {
            return;
        }
        
        Map<Item, Integer> inventoryAfter = new HashMap<>();
        Container inventory = rewards.player.getInventory();
        
        // Count current inventory
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                inventoryAfter.merge(item, stack.getCount(), Integer::sum);
            }
        }
        
        // Calculate what was ADDED (after - before)
        Map<Item, Integer> itemsAdded = new HashMap<>();
        for (Map.Entry<Item, Integer> entry : inventoryAfter.entrySet()) {
            Item item = entry.getKey();
            int afterCount = entry.getValue();
            int beforeCount = rewards.inventoryBefore.getOrDefault(item, 0);
            int added = afterCount - beforeCount;
            
            if (added > 0) {
                itemsAdded.put(item, added);
            }
        }
        
        if (DEBUG_LOGGING) {
            System.out.println("[SHOP] Inventory check - items added: " + itemsAdded.size());
            for (Map.Entry<Item, Integer> entry : itemsAdded.entrySet()) {
                System.out.println("  - " + entry.getValue() + "x " + entry.getKey().toString());
            }
        }
        
        // Calculate rewards based on items actually added
        double incomeMultiplier = UpgradeManager.getIncomeMultiplier(rewards.player.getUUID());
        double xpMultiplier = UpgradeManager.getXPMultiplier(rewards.player.getUUID());
        
        for (Map.Entry<Item, Integer> entry : itemsAdded.entrySet()) {
            Item item = entry.getKey();
            int count = entry.getValue();
            
            Block block = Block.byItem(item);
            
            if (DEBUG_LOGGING) {
                System.out.println("[SHOP] Item " + item.toString() + " -> Block " + block.getName().getString());
            }
            
            if (block != Blocks.AIR) {
                long baseValue = getBlockValue(block);
                int baseXP = getBlockXP(block);
                
                if (DEBUG_LOGGING) {
                    System.out.println("[SHOP]   Value: $" + baseValue + ", XP: " + baseXP);
                }
                
                if (baseValue > 0 || baseXP > 0) {
                    rewards.totalMoney += (long)(baseValue * count * incomeMultiplier);
                    rewards.totalXP += (int)(baseXP * count * xpMultiplier);
                    rewards.blocksBroken += count;
                    
                    if (DEBUG_LOGGING) {
                        System.out.println("[SHOP]   Rewarding: " + count + "x " + item.getName(ItemStack.EMPTY).getString() + " = $" + (baseValue * count));
                    }
                }
            }
        }
        
        rewards.inventoryCaptured = false; // Done processing
    }

    
    private static void onBlockBreak(Level world, Player player, BlockPos pos, 
                                    BlockState state, @Nullable BlockEntity blockEntity) {
        if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        UUID playerUUID = serverPlayer.getUUID();
        PendingRewards rewards = pendingRewards.get(playerUUID);
        
        if (rewards != null) {
            // Reset timer so we keep waiting for more items
            rewards.ticksSinceLastBlock = 0;
            
            if (DEBUG_LOGGING) {
                System.out.println("[SHOP] Block broken, waiting for inventory changes...");
            }
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
}
