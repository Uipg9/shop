package com.shopmod.village;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trade Center - stores village resources and allows auto-selling
 * Like a bank but for resources produced by village
 */
public class TradeCenterManager {
    private static final Map<UUID, TradeCenter> tradeCenters = new ConcurrentHashMap<>();
    
    /**
     * Trade Center data - separate storage from village warehouse
     */
    public static class TradeCenter {
        private final Map<ResourceType, Long> storage = new HashMap<>();
        private final Map<ResourceType, Long> storageCapacity = new HashMap<>();
        private boolean autoSellEnabled = false;
        private final Set<ResourceType> autoSellTypes = new HashSet<>();
        
        public TradeCenter() {
            // Initialize with base capacity (smaller than village warehouse)
            for (ResourceType type : ResourceType.values()) {
                storage.put(type, 0L);
                storageCapacity.put(type, 200L); // Base 200 per type
            }
        }
        
        public Map<ResourceType, Long> getStorage() { return storage; }
        public Map<ResourceType, Long> getStorageCapacity() { return storageCapacity; }
        public boolean isAutoSellEnabled() { return autoSellEnabled; }
        public void setAutoSellEnabled(boolean enabled) { this.autoSellEnabled = enabled; }
        public Set<ResourceType> getAutoSellTypes() { return autoSellTypes; }
        
        /**
         * Add resource to trade center
         */
        public boolean addResource(ResourceType type, long amount) {
            long current = storage.getOrDefault(type, 0L);
            long capacity = storageCapacity.getOrDefault(type, 200L);
            
            if (current + amount > capacity) {
                return false; // Would exceed capacity
            }
            
            storage.put(type, current + amount);
            return true;
        }
        
        /**
         * Remove resource from trade center
         */
        public boolean removeResource(ResourceType type, long amount) {
            long current = storage.getOrDefault(type, 0L);
            if (current < amount) return false;
            
            storage.put(type, current - amount);
            return true;
        }
        
        /**
         * Get total value of all stored resources
         */
        public long getTotalValue() {
            long total = 0;
            for (Map.Entry<ResourceType, Long> entry : storage.entrySet()) {
                total += entry.getValue() * entry.getKey().getValuePerUnit();
            }
            return total;
        }
    }
    
    /**
     * Get or create trade center for player
     */
    public static TradeCenter getTradeCenter(UUID playerUUID) {
        return tradeCenters.computeIfAbsent(playerUUID, k -> new TradeCenter());
    }
    
    /**
     * Transfer resources from village to trade center
     */
    public static boolean transferFromVillage(ServerPlayer player, ResourceType type, long amount) {
        VillageManager.Village village = VillageManager.getVillage(player.getUUID());
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        
        // Check if village has resource
        if (!village.hasResource(type, amount)) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] Village doesn't have %d %s!",
                    amount, type.getDisplayName())));
            return false;
        }
        
        // Check trade center capacity
        if (!tradeCenter.addResource(type, amount)) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] Trade center full! Current: %d/%d",
                    tradeCenter.getStorage().getOrDefault(type, 0L),
                    tradeCenter.getStorageCapacity().getOrDefault(type, 0L))));
            return false;
        }
        
        // Transfer
        village.removeResource(type, amount);
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[TRADE] Transferred %d %s to trade center!",
                amount, type.getDisplayName())));
        
        return true;
    }
    
    /**
     * Transfer resources from trade center to village
     */
    public static boolean transferToVillage(ServerPlayer player, ResourceType type, long amount) {
        VillageManager.Village village = VillageManager.getVillage(player.getUUID());
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        
        // Check if trade center has resource
        long available = tradeCenter.getStorage().getOrDefault(type, 0L);
        if (available < amount) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] Not enough in trade center! Have %d, need %d",
                    available, amount)));
            return false;
        }
        
        // Transfer (village will cap at capacity automatically)
        tradeCenter.removeResource(type, amount);
        village.addResource(type, amount);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[TRADE] Transferred %d %s to village warehouse!",
                amount, type.getDisplayName())));
        
        return true;
    }
    
    /**
     * Sell resources from trade center for money
     */
    public static boolean sellResource(ServerPlayer player, ResourceType type, long amount) {
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        
        // Check availability
        long available = tradeCenter.getStorage().getOrDefault(type, 0L);
        if (available < amount) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] Not enough %s! Have %d, need %d",
                    type.getDisplayName(), available, amount)));
            return false;
        }
        
        // Calculate value
        long value = type.getValuePerUnit() * amount;
        
        // Sell
        tradeCenter.removeResource(type, amount);
        CurrencyManager.addMoney(player, value);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[TRADE] Sold %d %s for %s!",
                amount, type.getDisplayName(), CurrencyManager.format(value))));
        
        return true;
    }
    
    /**
     * Sell ALL of a resource type
     */
    public static boolean sellAllResource(ServerPlayer player, ResourceType type) {
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        long amount = tradeCenter.getStorage().getOrDefault(type, 0L);
        
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] No %s to sell!", type.getDisplayName())));
            return false;
        }
        
        return sellResource(player, type, amount);
    }
    
    /**
     * Toggle auto-sell for a resource type
     */
    public static void toggleAutoSell(ServerPlayer player, ResourceType type) {
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        
        if (tradeCenter.getAutoSellTypes().contains(type)) {
            tradeCenter.getAutoSellTypes().remove(type);
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] Auto-sell DISABLED for %s", type.getDisplayName())));
        } else {
            tradeCenter.getAutoSellTypes().add(type);
            player.sendSystemMessage(Component.literal(
                String.format("§a§l[TRADE] Auto-sell ENABLED for %s", type.getDisplayName())));
        }
    }
    
    /**
     * Process auto-selling (called daily or on-demand)
     */
    public static long processAutoSell(ServerPlayer player) {
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        
        if (!tradeCenter.isAutoSellEnabled()) {
            return 0;
        }
        
        long totalEarned = 0;
        
        for (ResourceType type : tradeCenter.getAutoSellTypes()) {
            long amount = tradeCenter.getStorage().getOrDefault(type, 0L);
            if (amount > 0) {
                long value = type.getValuePerUnit() * amount;
                tradeCenter.removeResource(type, amount);
                CurrencyManager.addMoney(player, value);
                totalEarned += value;
            }
        }
        
        if (totalEarned > 0) {
            player.sendSystemMessage(Component.literal(
                String.format("§6§l[TRADE] Auto-sold resources for %s!",
                    CurrencyManager.format(totalEarned))));
        }
        
        return totalEarned;
    }
    
    /**
     * Expand storage capacity (purchasable upgrade)
     */
    public static boolean upgradeStorage(ServerPlayer player, ResourceType type) {
        TradeCenter tradeCenter = getTradeCenter(player.getUUID());
        long currentCapacity = tradeCenter.getStorageCapacity().getOrDefault(type, 200L);
        
        // Cost = $1000 per 100 capacity increase
        long cost = (currentCapacity / 100) * 1000;
        long balance = CurrencyManager.getBalance(player);
        
        if (balance < cost) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[TRADE] Insufficient funds! Need %s",
                    CurrencyManager.format(cost))));
            return false;
        }
        
        // Upgrade
        CurrencyManager.removeMoney(player, cost);
        tradeCenter.getStorageCapacity().put(type, currentCapacity + 100);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[TRADE] Upgraded %s storage to %d!",
                type.getDisplayName(), currentCapacity + 100)));
        
        return true;
    }
}
