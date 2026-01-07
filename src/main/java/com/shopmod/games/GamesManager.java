package com.shopmod.games;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mini-games system for earning starter income
 */
public class GamesManager {
    private static final Map<UUID, GameData> playerData = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    
    public static class GameData {
        private long totalEarned = 0;
        private int gamesPlayed = 0;
        private long lastPlayTime = 0;
        
        public long getTotalEarned() { return totalEarned; }
        public void addEarned(long amount) { this.totalEarned += amount; }
        public int getGamesPlayed() { return gamesPlayed; }
        public void incrementGames() { this.gamesPlayed++; }
        public long getLastPlayTime() { return lastPlayTime; }
        public void setLastPlayTime(long time) { this.lastPlayTime = time; }
    }
    
    public static GameData getGameData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> new GameData());
    }
    
    /**
     * NUMBER GUESS - Guess a number 1-10
     */
    public static void playNumberGuess(ServerPlayer player, int guess) {
        GameData data = getGameData(player.getUUID());
        long entryCost = 200;
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        // Charge entry fee
        CurrencyManager.removeMoney(player, entryCost);
        
        int number = RANDOM.nextInt(10) + 1;
        long reward = 0;
        
        if (guess == number) {
            reward = 5000; // Exact match: $5,000
            player.sendSystemMessage(Component.literal("§a§l[GAME] 🎉 PERFECT! The number was " + number + "!"));
        } else if (Math.abs(guess - number) == 1) {
            reward = 2000; // Off by 1: $2,000
            player.sendSystemMessage(Component.literal("§e§l[GAME] So close! The number was " + number + "."));
        } else {
            reward = 500; // Wrong: $500 consolation
            player.sendSystemMessage(Component.literal("§c§l[GAME] Not quite. The number was " + number + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.incrementGames();
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
    }
    
    /**
     * COIN FLIP - 50/50 chance
     */
    public static void playCoinFlip(ServerPlayer player, boolean headsChoice) {
        GameData data = getGameData(player.getUUID());
        long entryCost = 500;
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        // Charge entry fee
        CurrencyManager.removeMoney(player, entryCost);
        
        boolean result = RANDOM.nextBoolean(); // true = heads, false = tails
        String resultStr = result ? "Heads" : "Tails";
        String choiceStr = headsChoice ? "Heads" : "Tails";
        
        long reward;
        if (result == headsChoice) {
            reward = 3000; // Win: $3,000
            player.sendSystemMessage(Component.literal("§a§l[GAME] 🪙 " + resultStr + "! You WIN!"));
        } else {
            reward = 500; // Lose: $500 consolation
            player.sendSystemMessage(Component.literal("§c§l[GAME] 🪙 " + resultStr + ". You guessed " + choiceStr + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.incrementGames();
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
    }
    
    /**
     * DICE ROLL - Roll for rewards
     */
    public static void playDiceRoll(ServerPlayer player) {
        GameData data = getGameData(player.getUUID());
        long entryCost = 1000;
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        // Charge entry fee
        CurrencyManager.removeMoney(player, entryCost);
        
        int roll = RANDOM.nextInt(6) + 1;
        long reward;
        
        if (roll == 6) {
            reward = 10000; // Jackpot: $10,000
            player.sendSystemMessage(Component.literal("§d§l[GAME] 🎲 You rolled a 6! JACKPOT!"));
        } else if (roll >= 4) {
            reward = 4000; // Good roll: $4,000
            player.sendSystemMessage(Component.literal("§a§l[GAME] 🎲 You rolled a " + roll + "! Nice!"));
        } else {
            reward = 1000; // Low roll: $1,000
            player.sendSystemMessage(Component.literal("§e§l[GAME] 🎲 You rolled a " + roll + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.incrementGames();
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
    }
    
    /**
     * HIGH-LOW - Guess if next number is higher or lower
     */
    public static void playHighLow(ServerPlayer player, int currentNumber, boolean guessHigher) {
        GameData data = getGameData(player.getUUID());
        long entryCost = 300;
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        // Charge entry fee
        CurrencyManager.removeMoney(player, entryCost);
        
        int nextNumber = RANDOM.nextInt(10) + 1;
        boolean correct = (guessHigher && nextNumber > currentNumber) || (!guessHigher && nextNumber < currentNumber);
        
        long reward;
        if (nextNumber == currentNumber) {
            reward = 8000; // Same number (rare): $8,000
            player.sendSystemMessage(Component.literal("§d§l[GAME] Same number (" + nextNumber + ")! RARE!"));
        } else if (correct) {
            reward = 3500; // Correct: $3,500
            String dir = guessHigher ? "HIGHER" : "LOWER";
            player.sendSystemMessage(Component.literal("§a§l[GAME] Correct! " + nextNumber + " is " + dir + "!"));
        } else {
            reward = 750; // Wrong: $750
            player.sendSystemMessage(Component.literal("§c§l[GAME] Wrong. Next was " + nextNumber + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.incrementGames();
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
    }
    
    /**
     * LUCKY SLOTS - Slot machine style
     */
    public static void playLuckySlots(ServerPlayer player) {
        GameData data = getGameData(player.getUUID());
        long entryCost = 2000;
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        // Charge entry fee
        CurrencyManager.removeMoney(player, entryCost);
        
        String[] symbols = {"🍒", "🍋", "🍊", "⭐", "💎", "7️⃣"};
        int s1 = RANDOM.nextInt(symbols.length);
        int s2 = RANDOM.nextInt(symbols.length);
        int s3 = RANDOM.nextInt(symbols.length);
        
        long reward;
        if (s1 == s2 && s2 == s3) {
            // Triple match!
            if (s1 == 5) { // 777
                reward = 50000; // MEGA JACKPOT: $50,000!
                player.sendSystemMessage(Component.literal("§d§l[SLOTS] 🎰 7️⃣ 7️⃣ 7️⃣ MEGA JACKPOT!!!"));
            } else if (s1 == 4) { // 💎💎💎
                reward = 25000; // Big win: $25,000
                player.sendSystemMessage(Component.literal("§b§l[SLOTS] 🎰 💎 💎 💎 DIAMONDS!!!"));
            } else {
                reward = 15000; // Triple: $15,000
                player.sendSystemMessage(Component.literal("§a§l[SLOTS] 🎰 " + symbols[s1] + " " + symbols[s2] + " " + symbols[s3] + " TRIPLE!"));
            }
        } else if (s1 == s2 || s2 == s3 || s1 == s3) {
            reward = 5000; // Pair: $5,000
            player.sendSystemMessage(Component.literal("§e§l[SLOTS] 🎰 " + symbols[s1] + " " + symbols[s2] + " " + symbols[s3] + " Pair!"));
        } else {
            reward = 1000; // No match: $1,000
            player.sendSystemMessage(Component.literal("§7§l[SLOTS] 🎰 " + symbols[s1] + " " + symbols[s2] + " " + symbols[s3]));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.incrementGames();
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
    }
}
