package com.shopmod.achievements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks a player's achievement progress
 */
public class AchievementProgress {
    // General stats
    private long currentBalance = 0;
    private long totalEarned = 0;
    private long totalSpent = 0;
    private long highestBalance = 0;
    
    // Properties & Investments
    private int propertiesOwned = 0;
    private int propertiesOwnedLifetime = 0;
    private int farmsOwned = 0;
    private int farmsOwnedLifetime = 0;
    private int minesOwned = 0;
    private int minesOwnedLifetime = 0;
    
    // Business
    private int businessesOwned = 0;
    private int businessesOwnedLifetime = 0;
    private Set<String> businessTypesOwned = new HashSet<>();
    
    // Workers
    private int workersHired = 0;
    private int workersHiredLifetime = 0;
    private int workersTrained = 0;
    
    // Stock Market
    private int stockTradesMade = 0;
    private long stockProfitTotal = 0;
    private int longestHoldDays = 0;
    
    // Gaming
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int consecutiveWins = 0;
    private int blackjackCount = 0;
    private int jackpotWins = 0;
    
    // Lottery
    private int lotteryTicketsBought = 0;
    private int lotteryWins = 0;
    private boolean lotteryJackpotWon = false;
    
    // Completed achievements
    private Set<String> completedAchievements = new HashSet<>();
    
    // Custom counters for specific achievements
    private Map<String, Long> customCounters = new HashMap<>();
    
    // Getters and setters
    public long getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(long balance) { 
        this.currentBalance = balance;
        if (balance > highestBalance) {
            highestBalance = balance;
        }
    }
    
    public long getTotalEarned() { return totalEarned; }
    public void addEarned(long amount) { this.totalEarned += amount; }
    
    public long getTotalSpent() { return totalSpent; }
    public void addSpent(long amount) { this.totalSpent += amount; }
    
    public long getHighestBalance() { return highestBalance; }
    
    public int getPropertiesOwned() { return propertiesOwned; }
    public void setPropertiesOwned(int count) { 
        this.propertiesOwned = count;
        if (count > propertiesOwnedLifetime) {
            propertiesOwnedLifetime = count;
        }
    }
    
    public int getPropertiesOwnedLifetime() { return propertiesOwnedLifetime; }
    
    public int getFarmsOwned() { return farmsOwned; }
    public void setFarmsOwned(int count) { 
        this.farmsOwned = count;
        if (count > farmsOwnedLifetime) {
            farmsOwnedLifetime = count;
        }
    }
    
    public int getFarmsOwnedLifetime() { return farmsOwnedLifetime; }
    
    public int getMinesOwned() { return minesOwned; }
    public void setMinesOwned(int count) { 
        this.minesOwned = count;
        if (count > minesOwnedLifetime) {
            minesOwnedLifetime = count;
        }
    }
    
    public int getMinesOwnedLifetime() { return minesOwnedLifetime; }
    
    public int getBusinessesOwned() { return businessesOwned; }
    public void setBusinessesOwned(int count) { 
        this.businessesOwned = count;
        if (count > businessesOwnedLifetime) {
            businessesOwnedLifetime = count;
        }
    }
    
    public int getBusinessesOwnedLifetime() { return businessesOwnedLifetime; }
    
    public Set<String> getBusinessTypesOwned() { return businessTypesOwned; }
    public void addBusinessType(String type) { businessTypesOwned.add(type); }
    
    public int getWorkersHired() { return workersHired; }
    public void setWorkersHired(int count) { 
        this.workersHired = count;
        if (count > workersHiredLifetime) {
            workersHiredLifetime = count;
        }
    }
    
    public int getWorkersHiredLifetime() { return workersHiredLifetime; }
    
    public int getWorkersTrained() { return workersTrained; }
    public void incrementWorkersTrained() { this.workersTrained++; }
    
    public int getStockTradesMade() { return stockTradesMade; }
    public void incrementStockTrades() { this.stockTradesMade++; }
    
    public long getStockProfitTotal() { return stockProfitTotal; }
    public void addStockProfit(long profit) { this.stockProfitTotal += profit; }
    
    public int getLongestHoldDays() { return longestHoldDays; }
    public void updateLongestHold(int days) { 
        if (days > longestHoldDays) {
            longestHoldDays = days;
        }
    }
    
    public int getGamesPlayed() { return gamesPlayed; }
    public void incrementGamesPlayed() { this.gamesPlayed++; }
    
    public int getGamesWon() { return gamesWon; }
    public void incrementGamesWon() { 
        this.gamesWon++;
        this.consecutiveWins++;
    }
    
    public void resetConsecutiveWins() { this.consecutiveWins = 0; }
    
    public int getConsecutiveWins() { return consecutiveWins; }
    
    public int getBlackjackCount() { return blackjackCount; }
    public void incrementBlackjack() { this.blackjackCount++; }
    
    public int getJackpotWins() { return jackpotWins; }
    public void incrementJackpotWins() { this.jackpotWins++; }
    
    public int getLotteryTicketsBought() { return lotteryTicketsBought; }
    public void incrementLotteryTickets() { this.lotteryTicketsBought++; }
    
    public int getLotteryWins() { return lotteryWins; }
    public void incrementLotteryWins() { this.lotteryWins++; }
    
    public boolean isLotteryJackpotWon() { return lotteryJackpotWon; }
    public void setLotteryJackpotWon() { this.lotteryJackpotWon = true; }
    
    public Set<String> getCompletedAchievements() { return completedAchievements; }
    public void completeAchievement(String id) { completedAchievements.add(id); }
    public boolean isAchievementCompleted(String id) { return completedAchievements.contains(id); }
    
    public long getCustomCounter(String key) { return customCounters.getOrDefault(key, 0L); }
    public void setCustomCounter(String key, long value) { customCounters.put(key, value); }
    public void incrementCustomCounter(String key) { 
        customCounters.put(key, customCounters.getOrDefault(key, 0L) + 1);
    }
}
