package com.shopmod.daily;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Daily Rewards System with streak tracking
 */
public class DailyRewardManager {
    private static final Map<UUID, DailyData> playerData = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    
    public static class DailyData {
        private LocalDate lastClaimDate = null;
        private int currentStreak = 0;
        private int longestStreak = 0;
        private int totalClaims = 0;
        private long totalRewardsEarned = 0;
        private boolean permanentBonus5Percent = false;
        private boolean permanentBonus10Percent = false;
        
        public LocalDate getLastClaimDate() { return lastClaimDate; }
        public void setLastClaimDate(LocalDate date) { this.lastClaimDate = date; }
        
        public int getCurrentStreak() { return currentStreak; }
        public void incrementStreak() { 
            this.currentStreak++;
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
        }
        public void resetStreak() { this.currentStreak = 0; }
        
        public int getLongestStreak() { return longestStreak; }
        public int getTotalClaims() { return totalClaims; }
        public void incrementTotalClaims() { this.totalClaims++; }
        
        public long getTotalRewardsEarned() { return totalRewardsEarned; }
        public void addReward(long amount) { this.totalRewardsEarned += amount; }
        
        public boolean hasPermanentBonus5Percent() { return permanentBonus5Percent; }
        public void unlockPermanentBonus5Percent() { this.permanentBonus5Percent = true; }
        
        public boolean hasPermanentBonus10Percent() { return permanentBonus10Percent; }
        public void unlockPermanentBonus10Percent() { this.permanentBonus10Percent = true; }
    }
    
    public static DailyData getData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, id -> new DailyData());
    }
    
    public static DailyData getData(ServerPlayer player) {
        return getData(player.getUUID());
    }
    
    /**
     * Check if player can claim today's reward
     */
    public static boolean canClaimToday(ServerPlayer player) {
        DailyData data = getData(player);
        LocalDate today = LocalDate.now();
        
        if (data.getLastClaimDate() == null) {
            return true; // First time claiming
        }
        
        return !data.getLastClaimDate().equals(today);
    }
    
    /**
     * Claim today's reward
     */
    public static void claimReward(ServerPlayer player) {
        if (!canClaimToday(player)) {
            player.sendSystemMessage(Component.literal("§cYou've already claimed today's reward!"));
            player.sendSystemMessage(Component.literal("§7Come back tomorrow for your next reward."));
            return;
        }
        
        DailyData data = getData(player);
        LocalDate today = LocalDate.now();
        LocalDate lastClaim = data.getLastClaimDate();
        
        // Check if streak continues
        if (lastClaim != null && lastClaim.plusDays(1).equals(today)) {
            // Streak continues
            data.incrementStreak();
        } else if (lastClaim != null) {
            // Streak broken
            data.resetStreak();
            data.incrementStreak(); // Start new streak at 1
        } else {
            // First claim ever
            data.incrementStreak();
        }
        
        data.setLastClaimDate(today);
        data.incrementTotalClaims();
        
        int streak = data.getCurrentStreak();
        int dayInCycle = ((streak - 1) % 7) + 1; // 1-7
        
        // Calculate rewards based on day in cycle
        long cashReward = getCashReward(dayInCycle);
        MysteryBox box = getMysteryBox(dayInCycle);
        boolean freeInsurance = (dayInCycle == 7);
        
        // Give cash reward
        CurrencyManager.addMoney(player, cashReward);
        data.addReward(cashReward);
        
        // Check for milestone bonuses
        if (streak == 30 && !data.hasPermanentBonus5Percent()) {
            data.unlockPermanentBonus5Percent();
        }
        if (streak == 100 && !data.hasPermanentBonus10Percent()) {
            data.unlockPermanentBonus10Percent();
        }
        
        // Play sound
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Send reward message
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l✦ DAILY REWARD CLAIMED ✦"));
        player.sendSystemMessage(Component.literal("§e§lDay " + dayInCycle + " Reward"));
        player.sendSystemMessage(Component.literal("§7Streak: §a" + streak + " day" + (streak > 1 ? "s" : "")));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§a+ §6" + CurrencyManager.format(cashReward)));
        
        if (box != null) {
            player.sendSystemMessage(Component.literal("§a+ §d" + box.getName() + " Mystery Box"));
            giveMysteryBox(player, box);
        }
        
        if (freeInsurance) {
            player.sendSystemMessage(Component.literal("§a+ §bFree Insurance Claim"));
        }
        
        // Milestone rewards
        if (streak == 30) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§d§l✦ 30-DAY MILESTONE ✦"));
            player.sendSystemMessage(Component.literal("§aPermanent +5% Income Boost Unlocked!"));
        }
        
        if (streak == 100) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§d§l✦ 100-DAY MILESTONE ✦"));
            player.sendSystemMessage(Component.literal("§aPermanent +10% Income Boost Unlocked!"));
            player.sendSystemMessage(Component.literal("§a+ Legendary Mystery Box"));
            giveMysteryBox(player, MysteryBox.LEGENDARY);
        }
        
        player.sendSystemMessage(Component.literal(""));
    }
    
    private static long getCashReward(int day) {
        return switch (day) {
            case 1 -> 5000;
            case 2 -> 10000;
            case 3 -> 20000;
            case 4 -> 30000;
            case 5 -> 50000;
            case 6 -> 75000;
            case 7 -> 100000;
            default -> 5000;
        };
    }
    
    private static MysteryBox getMysteryBox(int day) {
        return switch (day) {
            case 3 -> MysteryBox.COMMON;
            case 5 -> MysteryBox.RARE;
            case 7 -> MysteryBox.EPIC;
            default -> null;
        };
    }
    
    public enum MysteryBox {
        COMMON("Common", 0.60),
        RARE("Rare", 0.30),
        EPIC("Epic", 0.08),
        LEGENDARY("Legendary", 0.02);
        
        private final String name;
        private final double baseChance;
        
        MysteryBox(String name, double baseChance) {
            this.name = name;
            this.baseChance = baseChance;
        }
        
        public String getName() { return name; }
        public double getBaseChance() { return baseChance; }
    }
    
    /**
     * Give mystery box rewards
     */
    private static void giveMysteryBox(ServerPlayer player, MysteryBox box) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§d§l✦ OPENING " + box.getName().toUpperCase() + " MYSTERY BOX ✦"));
        
        switch (box) {
            case COMMON -> {
                long cash = 10000 + RANDOM.nextInt(40000);
                CurrencyManager.addMoney(player, cash);
                player.sendSystemMessage(Component.literal("§a+ §6" + CurrencyManager.format(cash)));
                
                // Basic items
                if (RANDOM.nextBoolean()) {
                    giveItem(player, new ItemStack(Items.DIAMOND, 5 + RANDOM.nextInt(11)));
                    player.sendSystemMessage(Component.literal("§a+ Diamonds"));
                }
            }
            case RARE -> {
                long cash = 50000 + RANDOM.nextInt(50000);
                CurrencyManager.addMoney(player, cash);
                player.sendSystemMessage(Component.literal("§a+ §6" + CurrencyManager.format(cash)));
                
                // Items (skip enchantments for now - API changed in 1.21)
                if (RANDOM.nextBoolean()) {
                    ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
                    giveItem(player, pickaxe);
                    player.sendSystemMessage(Component.literal("§a+ Diamond Pickaxe"));
                }
            }
            case EPIC -> {
                long cash = 100000 + RANDOM.nextInt(150000);
                CurrencyManager.addMoney(player, cash);
                player.sendSystemMessage(Component.literal("§a+ §6" + CurrencyManager.format(cash)));
                
                player.sendSystemMessage(Component.literal("§a+ Worker Efficiency Boost (24h)"));
                // TODO: Apply worker boost
            }
            case LEGENDARY -> {
                long cash = 250000 + RANDOM.nextInt(750000);
                CurrencyManager.addMoney(player, cash);
                player.sendSystemMessage(Component.literal("§a+ §6" + CurrencyManager.format(cash)));
                
                // Rare items
                giveItem(player, new ItemStack(Items.NETHERITE_INGOT, 5));
                player.sendSystemMessage(Component.literal("§a+ Netherite Ingots"));
                player.sendSystemMessage(Component.literal("§a+ Permanent Luck Boost"));
            }
        }
        
        player.sendSystemMessage(Component.literal(""));
    }
    
    private static void giveItem(ServerPlayer player, ItemStack item) {
        if (!player.getInventory().add(item)) {
            player.drop(item, false);
        }
    }
    
    /**
     * Get income boost multiplier from daily rewards
     */
    public static double getIncomeBoostMultiplier(ServerPlayer player) {
        DailyData data = getData(player);
        double multiplier = 1.0;
        
        if (data.hasPermanentBonus5Percent()) {
            multiplier += 0.05;
        }
        if (data.hasPermanentBonus10Percent()) {
            multiplier += 0.10;
        }
        
        return multiplier;
    }
}
