package com.shopmod.farm;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.village.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Digital Farm System - Buy farms, pay salaries, collect resources
 * Separate from village system - direct player ownership
 */
public class FarmManager {
    private static final Map<UUID, PlayerFarms> playerFarms = new ConcurrentHashMap<>();
    
    /**
     * Player farm data
     */
    public static class PlayerFarms {
        private final Map<FarmType, FarmData> farms = new HashMap<>();
        private final Map<ResourceType, Long> harvestedResources = new HashMap<>();
        private int farmLevel = 1; // Player's farm technology level
        private long lastProcessedDay = -1;
        private boolean autoSellEnabled = false;
        private boolean hasFertilizer = false;
        private long totalHarvested = 0;
        private long totalEarned = 0;
        
        public PlayerFarms() {
            // Initialize harvested storage
            for (ResourceType type : ResourceType.values()) {
                harvestedResources.put(type, 0L);
            }
        }
        
        public Map<FarmType, FarmData> getFarms() { return farms; }
        public Map<ResourceType, Long> getHarvestedResources() { return harvestedResources; }
        public int getFarmLevel() { return farmLevel; }
        public void setFarmLevel(int level) { this.farmLevel = level; }
        public long getLastProcessedDay() { return lastProcessedDay; }
        public void setLastProcessedDay(long day) { this.lastProcessedDay = day; }
        public boolean isAutoSellEnabled() { return autoSellEnabled; }
        public void setAutoSellEnabled(boolean enabled) { this.autoSellEnabled = enabled; }
        public boolean hasFertilizer() { return hasFertilizer; }
        public void setHasFertilizer(boolean fertilizer) { this.hasFertilizer = fertilizer; }
        public long getTotalHarvested() { return totalHarvested; }
        public void addTotalHarvested(long amount) { this.totalHarvested += amount; }
        public long getTotalEarned() { return totalEarned; }
        public void addTotalEarned(long amount) { this.totalEarned += amount; }
        
        public int getActiveFarmCount() {
            return (int) farms.values().stream().filter(FarmData::isActive).count();
        }
    }
    
    /**
     * Individual farm data
     */
    public static class FarmData {
        private boolean isActive = true;
        private long totalProduced = 0;
        private int farmLevel = 1;
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { this.isActive = active; }
        public long getTotalProduced() { return totalProduced; }
        public void addProduction(long amount) { this.totalProduced += amount; }
        public int getFarmLevel() { return farmLevel; }
        public void setFarmLevel(int level) { this.farmLevel = level; }
    }
    
    /**
     * Get or create player farms
     */
    public static PlayerFarms getPlayerFarms(UUID playerUUID) {
        return playerFarms.computeIfAbsent(playerUUID, k -> new PlayerFarms());
    }
    
    /**
     * Purchase a farm
     */
    public static boolean purchaseFarm(ServerPlayer player, FarmType farmType) {
        PlayerFarms farms = getPlayerFarms(player.getUUID());
        
        // Check if already owned
        if (farms.getFarms().containsKey(farmType)) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[FARM] You already own a %s!", farmType.getDisplayName())));
            return false;
        }
        
        // Check level requirement
        if (farmType.getRequiredLevel() > farms.getFarmLevel()) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[FARM] Requires Farm Level %d! (Currently %d)",
                    farmType.getRequiredLevel(), farms.getFarmLevel())));
            return false;
        }
        
        // Check cost
        long cost = farmType.getPurchaseCost();
        long balance = CurrencyManager.getBalance(player);
        if (balance < cost) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[FARM] Insufficient funds! Need %s, have %s",
                    CurrencyManager.format(cost), CurrencyManager.format(balance))));
            return false;
        }
        
        // Purchase
        CurrencyManager.removeMoney(player, cost);
        farms.getFarms().put(farmType, new FarmData());
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[FARM] Purchased %s for %s!",
                farmType.getDisplayName(), CurrencyManager.format(cost))));
        
        return true;
    }
    
    /**
     * Toggle farm active/inactive
     */
    public static boolean toggleFarm(ServerPlayer player, FarmType farmType) {
        PlayerFarms farms = getPlayerFarms(player.getUUID());
        FarmData data = farms.getFarms().get(farmType);
        
        if (data == null) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[FARM] You don't own a %s!", farmType.getDisplayName())));
            return false;
        }
        
        data.setActive(!data.isActive());
        
        player.sendSystemMessage(Component.literal(
            String.format("§%s§l[FARM] %s %s!",
                data.isActive() ? "a" : "c",
                farmType.getDisplayName(),
                data.isActive() ? "ACTIVATED" : "DEACTIVATED")));
        
        return true;
    }
    
    /**
     * Collect harvested resources
     */
    public static boolean collectResources(ServerPlayer player, ResourceType resourceType, long amount) {
        PlayerFarms farms = getPlayerFarms(player.getUUID());
        
        long available = farms.getHarvestedResources().getOrDefault(resourceType, 0L);
        if (available < amount) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[FARM] Not enough %s! Have %d, requested %d",
                    resourceType.getDisplayName(), available, amount)));
            return false;
        }
        
        // Give items to player inventory or sell directly
        long value = resourceType.getValuePerUnit() * amount;
        CurrencyManager.addMoney(player, value);
        
        farms.getHarvestedResources().put(resourceType, available - amount);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[FARM] Collected %d %s and sold for %s!",
                amount, resourceType.getDisplayName(), CurrencyManager.format(value))));
        
        return true;
    }
    
    /**
     * Process daily farm production (called by server tick)
     */
    public static void processDailyProduction(long currentDay) {
        for (UUID playerUUID : playerFarms.keySet()) {
            PlayerFarms farms = getPlayerFarms(playerUUID);
            
            if (farms.getLastProcessedDay() >= currentDay) {
                continue; // Already processed today
            }
            
            long totalSalary = 0;
            double productionMultiplier = farms.hasFertilizer() ? 1.5 : 1.0;
            
            for (Map.Entry<FarmType, FarmData> entry : farms.getFarms().entrySet()) {
                FarmType farmType = entry.getKey();
                FarmData data = entry.getValue();
                
                if (!data.isActive()) continue;
                
                // Pay salary (deduct from player balance)
                totalSalary += farmType.getDailySalary();
                
                // Produce resources with fertilizer boost
                ResourceType output = farmType.getOutputResource();
                int baseProduction = farmType.getDailyOutput() * data.getFarmLevel();
                int production = (int)(baseProduction * productionMultiplier);
                
                long current = farms.getHarvestedResources().getOrDefault(output, 0L);
                farms.getHarvestedResources().put(output, current + production);
                farms.addTotalHarvested(production);
                
                data.addProduction(production);
                
                // Handle special cases (Animal Farm produces both leather and wool)
                if (farmType == FarmType.ANIMAL_FARM) {
                    long woolCurrent = farms.getHarvestedResources().getOrDefault(ResourceType.WOOL, 0L);
                    farms.getHarvestedResources().put(ResourceType.WOOL, woolCurrent + production);
                    farms.addTotalHarvested(production);
                }
            }
            
            // Clear fertilizer after use
            if (farms.hasFertilizer()) {
                farms.setHasFertilizer(false);
            }
            
            // Auto-sell if enabled
            if (farms.isAutoSellEnabled()) {
                long totalValue = 0;
                for (Map.Entry<ResourceType, Long> entry : farms.getHarvestedResources().entrySet()) {
                    long amount = entry.getValue();
                    if (amount > 0) {
                        totalValue += amount * entry.getKey().getValuePerUnit();
                        entry.setValue(0L); // Clear harvested
                    }
                }
                
                if (totalValue > 0) {
                    farms.addTotalEarned(totalValue);
                    // Note: Money will be added when player is online via server tick
                }
            }
            
            farms.setLastProcessedDay(currentDay);
        }
    }
    
    /**
     * Upgrade farm level
     */
    public static boolean upgradeFarmLevel(ServerPlayer player) {
        PlayerFarms farms = getPlayerFarms(player.getUUID());
        
        if (farms.getFarmLevel() >= 5) {
            player.sendSystemMessage(Component.literal("§c§l[FARM] Maximum farm level reached!"));
            return false;
        }
        
        long cost = farms.getFarmLevel() * 10000L; // $10k per level
        long balance = CurrencyManager.getBalance(player);
        
        if (balance < cost) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[FARM] Insufficient funds! Need %s for upgrade",
                    CurrencyManager.format(cost))));
            return false;
        }
        
        CurrencyManager.removeMoney(player, cost);
        farms.setFarmLevel(farms.getFarmLevel() + 1);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[FARM] Upgraded to Farm Level %d!", farms.getFarmLevel())));
        
        return true;
    }
    
    /**
     * Toggle auto-sell for player
     */
    public static void toggleAutoSell(ServerPlayer player) {
        PlayerFarms farms = getPlayerFarms(player.getUUID());
        farms.setAutoSellEnabled(!farms.isAutoSellEnabled());
        
        player.sendSystemMessage(Component.literal(
            "§" + (farms.isAutoSellEnabled() ? "a" : "c") + "§l[FARM] Auto-Sell " + 
            (farms.isAutoSellEnabled() ? "ENABLED" : "DISABLED") + "!"));
    }
    
    /**
     * Get auto-sell status
     */
    public static boolean getAutoSellEnabled(UUID playerUUID) {
        return getPlayerFarms(playerUUID).isAutoSellEnabled();
    }
    
    /**
     * Purchase fertilizer for next harvest
     */
    public static boolean purchaseFertilizer(ServerPlayer player, long cost) {
        PlayerFarms farms = getPlayerFarms(player.getUUID());
        
        if (farms.hasFertilizer()) {
            player.sendSystemMessage(Component.literal("§c§l[FARM] Fertilizer already active!"));
            return false;
        }
        
        if (!CurrencyManager.canAfford(player, cost)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[FARM] Insufficient funds! Need " + CurrencyManager.format(cost)));
            return false;
        }
        
        CurrencyManager.removeMoney(player, cost);
        farms.setHasFertilizer(true);
        
        player.sendSystemMessage(Component.literal("§a§l[FARM] Fertilizer applied! Next harvest +50%"));
        
        return true;
    }
}