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
        COAL_MINE("Coal Mine", 25000, 300, 1.0),
        IRON_MINE("Iron Mine", 75000, 600, 1.5),
        GOLD_MINE("Gold Mine", 150000, 1200, 2.0),
        DIAMOND_MINE("Diamond Mine", 500000, 3000, 3.0),
        NETHERITE_MINE("Netherite Mine", 2000000, 10000, 5.0);
        
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
            long income = data.calculateDailyIncome();
            
            if (income > 0) {
                CurrencyManager.addMoney(player, income);
                data.addEarned(income);
                player.sendSystemMessage(Component.literal(
                    "§6§l[MINING] §aYour mines produced §6$" + CurrencyManager.format(income) + "!"));
            }
        });
    }
}
