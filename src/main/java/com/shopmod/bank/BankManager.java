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
 * - Money investment with risky daily gains/losses
 * - Difficulty-scaled risk/reward
 * - NOTE: Regular wallet balance (not invested) earns 10% per night - see CurrencyManager
 */
public class BankManager {
    private static final Map<UUID, BankData> bankDataMap = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    // Investment parameters - risky returns
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
        private int storageLevel = 0; // 0 = base 27 slots, 1 = 36, 2 = 45, 3 = 54
        
        // New account types
        private long checkingBalance = 0;
        private long investmentBalance = 0;  // Stock portfolio value
        private final CreditCardData creditCard = new CreditCardData();
        private final java.util.LinkedList<TransactionRecord> transactionHistory = new java.util.LinkedList<>();
        private static final int MAX_TRANSACTION_HISTORY = 100;
        
        public BankData() {
            // Initialize with 27 slots (like chest)
            for (int i = 0; i < 27; i++) {
                storage.add(ItemStack.EMPTY);
            }
        }
        
        public List<ItemStack> getStorage() {
            return storage;
        }
        
        public int getStorageSize() {
            return 27 + (storageLevel * 9);
        }
        
        public int getStorageLevel() {
            return storageLevel;
        }
        
        public void setStorageLevel(int level) {
            this.storageLevel = level;
            // Expand storage if needed
            int targetSize = 27 + (level * 9);
            while (storage.size() < targetSize) {
                storage.add(ItemStack.EMPTY);
            }
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
        
        // New account methods
        public long getCheckingBalance() {
            return checkingBalance;
        }
        
        public void setCheckingBalance(long balance) {
            this.checkingBalance = Math.max(0, balance);
        }
        
        public long getInvestmentBalance() {
            return investmentBalance;
        }
        
        public void setInvestmentBalance(long balance) {
            this.investmentBalance = Math.max(0, balance);
        }
        
        public CreditCardData getCreditCard() {
            return creditCard;
        }
        
        public java.util.List<TransactionRecord> getTransactionHistory() {
            return transactionHistory;
        }
        
        public void addTransaction(TransactionRecord transaction) {
            transactionHistory.addFirst(transaction);
            while (transactionHistory.size() > MAX_TRANSACTION_HISTORY) {
                transactionHistory.removeLast();
            }
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
     * Upgrade bank storage space
     * Costs: Level 1 ($5k), Level 2 ($15k), Level 3 ($50k)
     * Slots: 27 -> 36 -> 45 -> 54
     */
    public static boolean upgradeStorage(ServerPlayer player) {
        BankData data = getBankData(player.getUUID());
        int currentLevel = data.getStorageLevel();
        
        if (currentLevel >= 3) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c§l[BANK] Maximum storage level reached!"));
            return false;
        }
        
        // Calculate upgrade cost
        long cost = switch (currentLevel) {
            case 0 -> 5_000;   // 27 -> 36 slots
            case 1 -> 15_000;  // 36 -> 45 slots
            case 2 -> 50_000;  // 45 -> 54 slots
            default -> 0;
        };
        
        long balance = CurrencyManager.getBalance(player);
        if (balance < cost) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                String.format("§c§l[BANK] Insufficient funds! Need §6$%,d§c, have §6$%,d§c", 
                    cost, balance)));
            return false;
        }
        
        // Process upgrade
        CurrencyManager.removeMoney(player, cost);
        data.setStorageLevel(currentLevel + 1);
        
        int newSlots = data.getStorageSize();
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            String.format("§a§l[BANK] Storage upgraded to §6Level %d§a! (%d slots)", 
                currentLevel + 1, newSlots)));
        
        return true;
    }
    
    /**
     * Get upgrade cost for next level
     */
    public static long getUpgradeCost(int currentLevel) {
        return switch (currentLevel) {
            case 0 -> 5_000;
            case 1 -> 15_000;
            case 2 -> 50_000;
            default -> 0;
        };
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
    
    // ===== NEW ACCOUNT SYSTEM METHODS =====
    
    /**
     * Deposit money into checking account
     */
    public static boolean depositToChecking(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        
        long balance = CurrencyManager.getBalance(player);
        if (balance < amount) return false;
        
        BankData data = getBankData(player.getUUID());
        CurrencyManager.removeMoney(player, amount);
        data.setCheckingBalance(data.getCheckingBalance() + amount);
        
        // Record transaction
        data.addTransaction(new TransactionRecord(
            TransactionRecord.TransactionType.DEPOSIT,
            amount,
            data.getCheckingBalance(),
            "Deposit to checking",
            AccountType.CHECKING
        ));
        
        return true;
    }
    
    /**
     * Withdraw money from checking account
     */
    public static boolean withdrawFromChecking(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        
        BankData data = getBankData(player.getUUID());
        if (data.getCheckingBalance() < amount) return false;
        
        data.setCheckingBalance(data.getCheckingBalance() - amount);
        CurrencyManager.addMoney(player, amount);
        
        // Record transaction
        data.addTransaction(new TransactionRecord(
            TransactionRecord.TransactionType.WITHDRAW,
            amount,
            data.getCheckingBalance(),
            "Withdrawal from checking",
            AccountType.CHECKING
        ));
        
        return true;
    }
    
    /**
     * Transfer between accounts
     */
    public static boolean transferBetweenAccounts(ServerPlayer player, AccountType from, AccountType to, long amount) {
        if (amount <= 0) return false;
        
        BankData data = getBankData(player.getUUID());
        
        // Determine source and destination balances
        long sourceBalance = switch (from) {
            case CHECKING -> data.getCheckingBalance();
            case SAVINGS -> data.getInvestedMoney();
            case INVESTMENT -> data.getInvestmentBalance();
            case CREDIT -> 0;  // Can't transfer from credit
        };
        
        if (sourceBalance < amount) return false;
        
        // Perform transfer
        switch (from) {
            case CHECKING -> data.setCheckingBalance(data.getCheckingBalance() - amount);
            case SAVINGS -> data.setInvestedMoney(data.getInvestedMoney() - amount);
            case INVESTMENT -> data.setInvestmentBalance(data.getInvestmentBalance() - amount);
            case CREDIT -> {} // Can't transfer from credit
        }
        
        switch (to) {
            case CHECKING -> data.setCheckingBalance(data.getCheckingBalance() + amount);
            case SAVINGS -> data.setInvestedMoney(data.getInvestedMoney() + amount);
            case INVESTMENT -> data.setInvestmentBalance(data.getInvestmentBalance() + amount);
            case CREDIT -> {} // Can't transfer to credit
        }
        
        // Record transaction
        data.addTransaction(new TransactionRecord(
            TransactionRecord.TransactionType.TRANSFER,
            amount,
            sourceBalance - amount,
            String.format("Transfer from %s to %s", from.getDisplayName(), to.getDisplayName()),
            from
        ));
        
        return true;
    }
    
    // ===== CREDIT CARD METHODS =====
    
    /**
     * Borrow money from credit card
     */
    public static boolean borrowFromCredit(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        
        BankData data = getBankData(player.getUUID());
        CreditCardData credit = data.getCreditCard();
        
        if (credit.getAvailableCredit() < amount) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                String.format("§c§l[BANK] Insufficient credit! Available: §6$%,d", 
                    credit.getAvailableCredit())));
            return false;
        }
        
        if (credit.borrow(amount)) {
            CurrencyManager.addMoney(player, amount);
            
            // Record transaction
            data.addTransaction(new TransactionRecord(
                TransactionRecord.TransactionType.CREDIT_BORROW,
                amount,
                credit.getBalance(),
                "Credit card borrow",
                AccountType.CREDIT
            ));
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                String.format("§a§l[BANK] Borrowed §6$%,d§a! New balance: §6$%,d", 
                    amount, credit.getBalance())));
            return true;
        }
        
        return false;
    }
    
    /**
     * Pay credit card balance
     */
    public static boolean payCreditBalance(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        
        BankData data = getBankData(player.getUUID());
        CreditCardData credit = data.getCreditCard();
        
        if (credit.getBalance() == 0) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§e§l[BANK] No credit card balance to pay!"));
            return false;
        }
        
        // Can't pay more than balance
        amount = Math.min(amount, credit.getBalance());
        
        // Try to pay from wallet
        if (!CurrencyManager.canAfford(player, amount)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                String.format("§c§l[BANK] Insufficient funds! Need §6$%,d", amount)));
            return false;
        }
        
        if (CurrencyManager.removeMoney(player, amount)) {
            credit.pay(amount);
            
            // Record transaction
            data.addTransaction(new TransactionRecord(
                TransactionRecord.TransactionType.CREDIT_PAYMENT,
                amount,
                credit.getBalance(),
                "Credit card payment",
                AccountType.CREDIT
            ));
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                String.format("§a§l[BANK] Paid §6$%,d§a! Remaining: §6$%,d", 
                    amount, credit.getBalance())));
            return true;
        }
        
        return false;
    }
    
    /**
     * Get available credit
     */
    public static long getCreditAvailable(UUID playerUUID) {
        return getBankData(playerUUID).getCreditCard().getAvailableCredit();
    }
    
    /**
     * Process monthly credit card interest
     * Called from ShopMod monthly processing
     */
    public static void processCreditCardInterest(ServerPlayer player) {
        BankData data = getBankData(player.getUUID());
        CreditCardData credit = data.getCreditCard();
        
        if (credit.getBalance() == 0) return;
        
        long balanceBefore = credit.getBalance();
        
        // Check if payment is late (more than 30 days since last payment)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime lastPayment = credit.getLastPaymentDate();
        long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastPayment, now);
        
        boolean isLate = daysSince > 30;
        credit.applyMonthlyInterest(isLate);
        
        long interest = credit.getBalance() - balanceBefore;
        
        // Record transaction
        data.addTransaction(new TransactionRecord(
            TransactionRecord.TransactionType.INTEREST,
            interest,
            credit.getBalance(),
            isLate ? "Credit interest + LATE FEE" : "Credit card interest",
            AccountType.CREDIT
        ));
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            String.format("§c§l[BANK] Credit card interest charged: §6$%,d %s", 
                interest, isLate ? "§c(LATE PENALTY!)" : "")));
    }
}
