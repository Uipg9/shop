package com.shopmod.mining;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mining Operations - Purchase and manage automated mines
 */
public class MiningManager {
    private static final Map<UUID, MiningData> playerMines = new ConcurrentHashMap<>();
    
    public enum MineType {
        COAL_MINE("Coal Mine", 12500, 300, 1.0),      // 50% cheaper
        IRON_MINE("Iron Mine", 37500, 600, 1.5),      // 50% cheaper
        GOLD_MINE("Gold Mine", 75000, 1200, 2.0),     // 50% cheaper
        DIAMOND_MINE("Diamond Mine", 250000, 3000, 3.0),  // 50% cheaper
        NETHERITE_MINE("Netherite Mine", 1000000, 10000, 5.0);  // 50% cheaper
        
        private final String displayName;
        private final long baseCost;
        private final long baseIncome;
        private final double multiplier;
        
        MineType(String displayName, long baseCost, long baseIncome, double multiplier) {
            this.displayName = displayName;
            this.baseCost = baseCost;
            this.baseIncome = baseIncome;
            this.multiplier = multiplier;
        }
        
        public String getDisplayName() { return displayName; }
        public long getBaseCost() { return baseCost; }
        public long getBaseIncome() { return baseIncome; }
        public double getMultiplier() { return multiplier; }
        
        public long getCost(int level) {
            return (long)(baseCost * Math.pow(multiplier, level));
        }
        
        public long getIncome(int level) {
            return (long)(baseIncome * Math.pow(1.2, level));
        }
    }
    
    public static class MiningData {
        private final Map<MineType, Integer> mineLevels = new HashMap<>();
        private long totalEarned = 0;
        
        public int getMineLevel(MineType type) {
            return mineLevels.getOrDefault(type, 0);
        }
        
        public void setMineLevel(MineType type, int level) {
            mineLevels.put(type, level);
        }
        
        public boolean hasMine(MineType type) {
            return getMineLevel(type) > 0;
        }
        
        public long getTotalEarned() { return totalEarned; }
        public void addEarned(long amount) { this.totalEarned += amount; }
        
        public long calculateDailyIncome() {
            long total = 0;
            for (Map.Entry<MineType, Integer> entry : mineLevels.entrySet()) {
                total += entry.getKey().getIncome(entry.getValue());
            }
            return total;
        }
    }
    
    public static MiningData getMiningData(UUID playerUUID) {
        return playerMines.computeIfAbsent(playerUUID, k -> new MiningData());
    }
    
    public static boolean purchaseMine(ServerPlayer player, MineType type) {
        MiningData data = getMiningData(player.getUUID());
        int currentLevel = data.getMineLevel(type);
        long cost = type.getCost(currentLevel);
        
        if (!CurrencyManager.canAfford(player, cost)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[MINING] Insufficient funds! Need: §6" + CurrencyManager.format(cost)));
            return false;
        }
        
        CurrencyManager.removeMoney(player, cost);
        data.setMineLevel(type, currentLevel + 1);
        
        if (currentLevel == 0) {
            player.sendSystemMessage(Component.literal(
                "§a§l[MINING] Purchased " + type.getDisplayName() + "!"));
        } else {
            player.sendSystemMessage(Component.literal(
                "§a§l[MINING] Upgraded " + type.getDisplayName() + " to Level " + (currentLevel + 1) + "!"));
        }
        
        player.sendSystemMessage(Component.literal(
            "§7Daily Income: §6+" + CurrencyManager.format(type.getIncome(currentLevel + 1))));
        
        return true;
    }
    
    public static void processDailyIncome(long currentDay, net.minecraft.server.MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> {
            MiningData data = getMiningData(player.getUUID());
            long baseIncome = data.calculateDailyIncome();
            
            // Apply worker bonus (20% income boost per mine if worker assigned with MINING skill 5+)
            long totalIncome = baseIncome;
            for (MineType mineType : MineType.values()) {
                if (data.hasMine(mineType)) {
                    String mineId = "MINE_" + mineType.name();
                    double workerBonus = com.shopmod.worker.WorkerManager.getWorkerBonus(player.getUUID(), mineId);
                    if (workerBonus >= 0.25) { // MINING skill level 5 gives 25% bonus
                        long mineIncome = mineType.getIncome(data.getMineLevel(mineType));
                        totalIncome += (long)(mineIncome * 0.20); // 20% bonus for this mine
                    }
                }
            }
            
            if (totalIncome > 0) {
                CurrencyManager.addMoney(player, totalIncome);
                data.addEarned(totalIncome);
                player.sendSystemMessage(Component.literal(
                    "§6§l[MINING] §aYour mines produced §6$" + CurrencyManager.format(totalIncome) + "!"));
            }
        });
    }
}
