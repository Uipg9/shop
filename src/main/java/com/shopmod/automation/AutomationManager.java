package com.shopmod.automation;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.loan.LoanManager;
import com.shopmod.farm.FarmManager;
import com.shopmod.bank.BankManager;
import com.shopmod.insurance.InsuranceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages automation for all repetitive tasks
 * Tracks settings per player and processes daily automation
 */
public class AutomationManager {
    private static final Map<UUID, AutomationSettings> settingsMap = new ConcurrentHashMap<>();
    private static final Map<UUID, LinkedList<AutomationNotification>> notificationsMap = new ConcurrentHashMap<>();
    private static final int MAX_NOTIFICATIONS = 10;
    
    // Statistics tracking (daily)
    private static final Map<UUID, AutomationStats> statsMap = new ConcurrentHashMap<>();
    
    public static class AutomationStats {
        public long moneyAutoDeposited = 0;
        public int itemsAutoSold = 0;
        public long loansAutoPaid = 0;
        public int farmsAutoCollected = 0;
        public long dividendsAutoInvested = 0;
        
        public void reset() {
            moneyAutoDeposited = 0;
            itemsAutoSold = 0;
            loansAutoPaid = 0;
            farmsAutoCollected = 0;
            dividendsAutoInvested = 0;
        }
    }
    
    /**
     * Get or create automation settings for a player
     */
    public static AutomationSettings getSettings(UUID playerUUID) {
        return settingsMap.computeIfAbsent(playerUUID, k -> new AutomationSettings());
    }
    
    /**
     * Get automation statistics for a player
     */
    public static AutomationStats getStats(UUID playerUUID) {
        return statsMap.computeIfAbsent(playerUUID, k -> new AutomationStats());
    }
    
    /**
     * Get recent notifications for a player
     */
    public static List<AutomationNotification> getNotifications(UUID playerUUID) {
        return notificationsMap.computeIfAbsent(playerUUID, k -> new LinkedList<>());
    }
    
    /**
     * Add a notification and keep only the last 10
     */
    public static void addNotification(UUID playerUUID, String action, String details, long amount) {
        LinkedList<AutomationNotification> notifications = 
            (LinkedList<AutomationNotification>) notificationsMap.computeIfAbsent(playerUUID, k -> new LinkedList<>());
        
        notifications.addFirst(new AutomationNotification(action, details, amount));
        
        // Keep only last 10
        while (notifications.size() > MAX_NOTIFICATIONS) {
            notifications.removeLast();
        }
    }
    
    /**
     * Toggle a specific automation setting
     */
    public static void toggleSetting(ServerPlayer player, String settingName) {
        AutomationSettings settings = getSettings(player.getUUID());
        
        switch (settingName.toLowerCase()) {
            case "loans" -> {
                settings.autoPayLoans = !settings.autoPayLoans;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Pay Loans: " + (settings.autoPayLoans ? "§aON" : "§cOFF")));
            }
            case "farms" -> {
                settings.autoCollectFarms = !settings.autoCollectFarms;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Collect Farms: " + (settings.autoCollectFarms ? "§aON" : "§cOFF")));
            }
            case "deposit" -> {
                settings.autoDepositWallet = !settings.autoDepositWallet;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Deposit Wallet: " + (settings.autoDepositWallet ? "§aON" : "§cOFF")));
            }
            case "sell" -> {
                settings.autoSellHarvests = !settings.autoSellHarvests;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Sell Harvests: " + (settings.autoSellHarvests ? "§aON" : "§cOFF")));
            }
            case "invest" -> {
                settings.autoInvestDividends = !settings.autoInvestDividends;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Invest Dividends: " + (settings.autoInvestDividends ? "§aON" : "§cOFF")));
            }
            default -> player.sendSystemMessage(Component.literal(
                "§c§l[AUTOMATION] Unknown setting: " + settingName));
        }
    }
    
    /**
     * Process all daily automation for a player
     * Called from ShopMod daily processing
     */
    public static void processDailyAutomation(ServerPlayer player) {
        AutomationSettings settings = getSettings(player.getUUID());
        AutomationStats stats = getStats(player.getUUID());
        
        if (!settings.hasAnyEnabled()) {
            return;  // No automation enabled
        }
        
        // Auto-pay loans
        if (settings.autoPayLoans) {
            processAutoPayLoans(player, stats);
        }
        
        // Auto-collect farms
        if (settings.autoCollectFarms) {
            processAutoCollectFarms(player, stats);
        }
        
        // Auto-deposit wallet
        if (settings.autoDepositWallet) {
            processAutoDeposit(player, settings, stats);
        }
        
        // Send summary notification
        if (settings.hasAnyEnabled()) {
            player.sendSystemMessage(Component.literal(
                String.format("§a§l[AUTOMATION] Daily automation complete! %d tasks processed.", 
                    settings.countEnabled())));
        }
    }
    
    /**
     * Process manual "Run All Now" command
     */
    public static void processManualAutomation(ServerPlayer player) {
        AutomationSettings settings = getSettings(player.getUUID());
        AutomationStats stats = getStats(player.getUUID());
        
        if (!settings.hasAnyEnabled()) {
            player.sendSystemMessage(Component.literal(
                "§c§l[AUTOMATION] No automation enabled!"));
            return;
        }
        
        player.sendSystemMessage(Component.literal(
            "§e§l[AUTOMATION] Running all automation now..."));
        
        int tasksRun = 0;
        
        if (settings.autoPayLoans) {
            processAutoPayLoans(player, stats);
            tasksRun++;
        }
        
        if (settings.autoCollectFarms) {
            processAutoCollectFarms(player, stats);
            tasksRun++;
        }
        
        if (settings.autoDepositWallet) {
            processAutoDeposit(player, settings, stats);
            tasksRun++;
        }
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[AUTOMATION] Complete! %d tasks executed.", tasksRun)));
    }
    
    private static void processAutoPayLoans(ServerPlayer player, AutomationStats stats) {
        // Check if player has active loans
        if (LoanManager.getActiveLoan(player.getUUID()) == null) {
            return;
        }
        
        long balance = CurrencyManager.getBalance(player);
        long payment = LoanManager.getDailyPayment(player.getUUID());
        
        if (balance >= payment && payment > 0) {
            if (CurrencyManager.removeMoney(player, payment)) {
                stats.loansAutoPaid += payment;
                addNotification(player.getUUID(), "Loan Payment", "Auto-paid daily installment", payment);
            }
        }
    }
    
    private static void processAutoCollectFarms(ServerPlayer player, AutomationStats stats) {
        // Try to collect from all farms
        int collected = FarmManager.collectAllFarms(player);
        
        if (collected > 0) {
            stats.farmsAutoCollected += collected;
            addNotification(player.getUUID(), "Farm Collection", 
                String.format("Collected %d items", collected), 0);
        }
    }
    
    private static void processAutoDeposit(ServerPlayer player, AutomationSettings settings, AutomationStats stats) {
        long balance = CurrencyManager.getBalance(player);
        
        if (balance > settings.depositThreshold) {
            long depositAmount = balance - (settings.depositThreshold / 2);  // Leave half the threshold
            
            if (BankManager.depositMoney(player, depositAmount)) {
                stats.moneyAutoDeposited += depositAmount;
                addNotification(player.getUUID(), "Bank Deposit", 
                    "Moved excess wallet to bank", depositAmount);
            }
        }
    }
    
    /**
     * Reset daily statistics at midnight
     */
    public static void resetDailyStats(UUID playerUUID) {
        AutomationStats stats = getStats(playerUUID);
        stats.reset();
    }
}
