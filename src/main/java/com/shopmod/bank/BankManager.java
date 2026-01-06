package com.shopmod.bank;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player bank storage and investments
 * Features:
 * - Item storage (like ender chest)
 * - Money investment with daily gains/losses
 * - Difficulty-scaled risk/reward
 */
public class BankManager {
    private static final Map<UUID, BankData> bankDataMap = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    // Investment parameters
    private static final double EASY_MAX_GAIN = 0.10;   // 10% max gain
    private static final double EASY_MAX_LOSS = 0.05;   // 5% max loss
    
    private static final double NORMAL_MAX_GAIN = 0.25;  // 25% max gain
    private static final double NORMAL_MAX_LOSS = 0.15;  // 15% max loss
    
    private static final double HARD_MAX_GAIN = 0.50;    // 50% max gain
    private static final double HARD_MAX_LOSS = 0.40;    // 40% max loss
    
    public static class BankData {
        private final List<ItemStack> storage = new ArrayList<>();
        private long investedMoney = 0;
        private long lastProcessedDay = -1;
        
        public BankData() {
            // Initialize with 27 slots (like chest)
            for (int i = 0; i < 27; i++) {
                storage.add(ItemStack.EMPTY);
            }
        }
        
        public List<ItemStack> getStorage() {
            return storage;
        }
        
        public long getInvestedMoney() {
            return investedMoney;
        }
        
        public void setInvestedMoney(long amount) {
            this.investedMoney = Math.max(0, amount);
        }
        
        public long getLastProcessedDay() {
            return lastProcessedDay;
        }
        
        public void setLastProcessedDay(long day) {
            this.lastProcessedDay = day;
        }
    }
    
    /**
     * Get or create bank data for a player
     */
    public static BankData getBankData(UUID playerUUID) {
        return bankDataMap.computeIfAbsent(playerUUID, k -> new BankData());
    }
    
    /**
     * Deposit money into investment
     */
    public static boolean depositMoney(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        
        long balance = CurrencyManager.getBalance(player);
        if (balance < amount) return false;
        
        BankData data = getBankData(player.getUUID());
        CurrencyManager.removeMoney(player, amount);
        data.setInvestedMoney(data.getInvestedMoney() + amount);
        
        return true;
    }
    
    /**
     * Withdraw money from investment
     */
    public static boolean withdrawMoney(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        
        BankData data = getBankData(player.getUUID());
        if (data.getInvestedMoney() < amount) return false;
        
        data.setInvestedMoney(data.getInvestedMoney() - amount);
        CurrencyManager.addMoney(player, amount);
        
        return true;
    }
    
    /**
     * Process daily investment returns
     * Call this when a new Minecraft day starts
     */
    public static void processDailyReturns(ServerPlayer player) {
        BankData data = getBankData(player.getUUID());
        long invested = data.getInvestedMoney();
        
        if (invested <= 0) {
            com.shopmod.ShopMod.LOGGER.info("Player " + player.getName().getString() + " has no investment, skipping daily returns");
            return;
        }
        
        long currentDay = player.level().getDayTime() / 24000L;
        if (data.getLastProcessedDay() >= currentDay) {
            com.shopmod.ShopMod.LOGGER.info("Player " + player.getName().getString() + " already processed for day " + currentDay);
            return;
        }
        
        data.setLastProcessedDay(currentDay);
        com.shopmod.ShopMod.LOGGER.info("Processing returns for " + player.getName().getString() + " - Day " + currentDay + ", Invested: $" + invested);
        
        // Get difficulty
        net.minecraft.world.Difficulty difficulty = player.level().getDifficulty();
        
        double maxGain, maxLoss;
        switch (difficulty) {
            case EASY -> {
                maxGain = EASY_MAX_GAIN;
                maxLoss = EASY_MAX_LOSS;
            }
            case HARD -> {
                maxGain = HARD_MAX_GAIN;
                maxLoss = HARD_MAX_LOSS;
            }
            default -> {  // NORMAL & PEACEFUL
                maxGain = NORMAL_MAX_GAIN;
                maxLoss = NORMAL_MAX_LOSS;
            }
        }
        
        // Random return between -maxLoss and +maxGain
        double returnRate = (random.nextDouble() * (maxGain + maxLoss)) - maxLoss;
        long changeAmount = (long)(invested * returnRate);
        
        long newAmount = Math.max(0, invested + changeAmount);
        data.setInvestedMoney(newAmount);
        
        // Notify player with action bar
        String message;
        if (changeAmount > 0) {
            message = String.format("§a§l[BANK] Investment earned §6$%,d§a! (%.1f%%)", 
                                   changeAmount, returnRate * 100);
        } else if (changeAmount < 0) {
            message = String.format("§c§l[BANK] Investment lost §6$%,d§c (%.1f%%)", 
                                   Math.abs(changeAmount), returnRate * 100);
        } else {
            message = "§e§l[BANK] Investment remained stable";
        }
        
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), true);
    }
    
    /**
     * Save bank data to NBT
     */
    public static CompoundTag saveBankData(UUID playerUUID, ServerPlayer player) {
        BankData data = getBankData(playerUUID);
        CompoundTag tag = new CompoundTag();
        
        // Save storage - simplified for now, items won't persist
        // TODO: Fix ItemStack serialization for 1.21.11
        ListTag storageList = new ListTag();
        tag.put("Storage", storageList);
        
        // Save investment data
        tag.putLong("InvestedMoney", data.investedMoney);
        tag.putLong("LastProcessedDay", data.lastProcessedDay);
        
        return tag;
    }
    
    /**
     * Load bank data from NBT
     */
    public static void loadBankData(UUID playerUUID, CompoundTag tag, ServerPlayer player) {
        BankData data = getBankData(playerUUID);
        
        // Clear existing storage
        for (int i = 0; i < data.storage.size(); i++) {
            data.storage.set(i, ItemStack.EMPTY);
        }
        
        // Load storage - simplified for now
        // TODO: Fix ItemStack serialization for 1.21.11
        
        // Load investment data - NBT persistence disabled for now due to API issues
        // Data starts at 0 from BankData constructor
        // TODO: Fix CompoundTag.getLong() Optional<Long> issue in 1.21.11
    }
}
