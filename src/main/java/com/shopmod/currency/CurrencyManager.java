package com.shopmod.currency;

import com.shopmod.ShopMod;
import com.shopmod.data.ShopDataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Helper class for currency operations.
 * Provides convenient methods for managing player money.
 * 
 * Wallet balance earns 10% interest per Minecraft night.
 * Bank investments are risky (see BankManager).
 */
public class CurrencyManager {
    private static final String CURRENCY_SYMBOL = "$";
    private static final String CURRENCY_NAME = "Dollar";
    private static final String CURRENCY_NAME_PLURAL = "Dollars";
    
    // Interest rate for wallet balance (not bank investments)
    private static final double WALLET_INTEREST_RATE = 0.10;  // 10% per night
    
    /**
     * Gets a player's current balance
     */
    public static long getBalance(ServerPlayer player) {
        if (ShopMod.dataManager == null) return 0;
        return ShopMod.dataManager.getBalance(player);
    }
    
    /**
     * Adds money to a player's account
     */
    public static void addMoney(ServerPlayer player, long amount) {
        if (ShopMod.dataManager != null) {
            ShopMod.dataManager.addMoney(player, amount);
        }
    }
    
    /**
     * Removes money from a player's account
     * @return true if successful, false if insufficient funds
     */
    public static boolean removeMoney(ServerPlayer player, long amount) {
        if (ShopMod.dataManager == null) return false;
        return ShopMod.dataManager.removeMoney(player, amount);
    }
    
    /**
     * Checks if a player can afford an amount
     */
    public static boolean canAfford(ServerPlayer player, long amount) {
        return getBalance(player) >= amount;
    }
    
    /**
     * Formats a currency amount for display
     * Example: 1234567 -> "$1,234,567"
     */
    public static String format(long amount) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        return CURRENCY_SYMBOL + formatter.format(amount);
    }
    
    /**
     * Creates a formatted Component for displaying currency
     */
    public static Component formatComponent(long amount) {
        return Component.literal(format(amount));
    }
    
    /**
     * Gets the currency name (singular or plural)
     */
    public static String getCurrencyName(long amount) {
        return amount == 1 ? CURRENCY_NAME : CURRENCY_NAME_PLURAL;
    }
    
    /**
     * Sends a balance update message to a player
     */
    public static void sendBalanceMessage(ServerPlayer player) {
        long balance = getBalance(player);
        player.sendSystemMessage(
            Component.literal("Balance: ")
                .append(Component.literal(format(balance))
                    .withStyle(style -> style.withColor(0x00FF00)))
        );
    }
    
    /**
     * Sends a money received message to a player
     */
    public static void sendMoneyReceivedMessage(ServerPlayer player, long amount, String reason) {
        player.sendSystemMessage(
            Component.literal("+ ")
                .append(Component.literal(format(amount))
                    .withStyle(style -> style.withColor(0x00FF00)))
                .append(Component.literal(" (" + reason + ")"))
        );
    }
    
    /**
     * Sends a money spent message to a player
     */
    public static void sendMoneySpentMessage(ServerPlayer player, long amount, String reason) {
        player.sendSystemMessage(
            Component.literal("- ")
                .append(Component.literal(format(amount))
                    .withStyle(style -> style.withColor(0xFF0000)))
                .append(Component.literal(" (" + reason + ")"))
        );
    }
    
    /**
     * Sends an insufficient funds message to a player
     */
    public static void sendInsufficientFundsMessage(ServerPlayer player, long required) {
        long balance = getBalance(player);
        long needed = required - balance;
        
        player.sendSystemMessage(
            Component.literal("Insufficient funds! You need ")
                .append(Component.literal(format(needed))
                    .withStyle(style -> style.withColor(0xFF0000)))
                .append(Component.literal(" more."))
                .withStyle(style -> style.withColor(0xFF5555))
        );
    }
    
    /**
     * Process daily wallet interest (10% per night)
     * Called by ShopMod daily event system
     */
    public static void processDailyInterest(ServerPlayer player) {
        long balance = getBalance(player);
        
        if (balance <= 0) {
            return;
        }
        
        long interest = (long)(balance * WALLET_INTEREST_RATE);
        
        if (interest > 0) {
            addMoney(player, interest);
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format("§a§l[WALLET] Earned §6$%,d§a interest! (10%% on $%,d)", 
                                interest, balance)
                ), 
                true
            );
        }
    }
}
