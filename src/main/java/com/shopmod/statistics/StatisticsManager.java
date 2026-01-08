package com.shopmod.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks comprehensive player statistics
 */
public class StatisticsManager {
    private static final Map<UUID, PlayerStatistics> playerStats = new ConcurrentHashMap<>();
    
    public static PlayerStatistics getStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, id -> new PlayerStatistics());
    }
    
    public static class PlayerStatistics {
        // Wealth stats
        private long totalMoneyEarned = 0;
        private long totalMoneySpent = 0;
        private long currentBalance = 0;
        private long highestBalance = 0;
        
        // Property stats
        private int propertiesOwnedCurrent = 0;
        private int propertiesOwnedLifetime = 0;
        private long totalPropertyIncome = 0;
        private int propertiesRentedOut = 0;
        
        // Farm & Mine stats
        private int farmsOwnedCurrent = 0;
        private int farmsOwnedLifetime = 0;
        private int minesOwnedCurrent = 0;
        private int minesOwnedLifetime = 0;
        private long farmIncomeTotal = 0;
        private long mineIncomeTotal = 0;
        
        // Business stats
        private int businessesOwnedCurrent = 0;
        private int businessesOwnedLifetime = 0;
        private long businessIncomeTotal = 0;
        private int businessUpgradesTotal = 0;
        
        // Worker stats
        private int workersHiredCurrent = 0;
        private int workersHiredLifetime = 0;
        private int workersTrained = 0;
        private long workerWagesTotal = 0;
        
        // Stock Market stats
        private int stockTradesMade = 0;
        private long stockProfitTotal = 0;
        private long stockLossTotal = 0;
        private int sharesOwnedCurrent = 0;
        private long dividendsEarnedTotal = 0;
        private long biggestStockWin = 0;
        private long biggestStockLoss = 0;
        
        // Gaming stats
        private int gamesPlayed = 0;
        private int gamesWon = 0;
        private int gamesLost = 0;
        private long gamingWinningsTotal = 0;
        private long gamingLossesTotal = 0;
        private int longestWinStreak = 0;
        private int currentWinStreak = 0;
        private long biggestGameWin = 0;
        private Map<String, Integer> gamesPlayedByType = new HashMap<>();
        
        // Lottery stats
        private int lotteryTicketsBought = 0;
        private int lotteryWins = 0;
        private long lotteryWinningsTotal = 0;
        private long lotterySpentTotal = 0;
        
        // Loan stats
        private int loansTaken = 0;
        private int loansRepaid = 0;
        private long totalBorrowed = 0;
        private long totalRepaid = 0;
        private long totalInterestPaid = 0;
        
        // Insurance stats
        private int insurancePolicies = 0;
        private int claimsFiled = 0;
        private long premiumsPaidTotal = 0;
        private long claimsReceivedTotal = 0;
        
        // Automation stats
        private int automationRuns = 0;
        private long automationSavings = 0;
        
        // Misc stats
        private int daysPlayed = 0;
        private long lastPlayDate = 0;
        
        // Wealth getters/setters
        public long getTotalMoneyEarned() { return totalMoneyEarned; }
        public void addMoneyEarned(long amount) { this.totalMoneyEarned += amount; }
        
        public long getTotalMoneySpent() { return totalMoneySpent; }
        public void addMoneySpent(long amount) { this.totalMoneySpent += amount; }
        
        public long getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(long balance) { 
            this.currentBalance = balance;
            if (balance > highestBalance) {
                highestBalance = balance;
            }
        }
        
        public long getHighestBalance() { return highestBalance; }
        
        // Property
        public int getPropertiesOwnedCurrent() { return propertiesOwnedCurrent; }
        public void setPropertiesOwnedCurrent(int count) { 
            this.propertiesOwnedCurrent = count;
            if (count > propertiesOwnedLifetime) {
                propertiesOwnedLifetime = count;
            }
        }
        
        public int getPropertiesOwnedLifetime() { return propertiesOwnedLifetime; }
        public long getTotalPropertyIncome() { return totalPropertyIncome; }
        public void addPropertyIncome(long amount) { this.totalPropertyIncome += amount; }
        
        public int getPropertiesRentedOut() { return propertiesRentedOut; }
        public void setPropertiesRentedOut(int count) { this.propertiesRentedOut = count; }
        
        // Farms & Mines
        public int getFarmsOwnedCurrent() { return farmsOwnedCurrent; }
        public void setFarmsOwnedCurrent(int count) {
            this.farmsOwnedCurrent = count;
            if (count > farmsOwnedLifetime) {
                farmsOwnedLifetime = count;
            }
        }
        
        public int getFarmsOwnedLifetime() { return farmsOwnedLifetime; }
        
        public int getMinesOwnedCurrent() { return minesOwnedCurrent; }
        public void setMinesOwnedCurrent(int count) {
            this.minesOwnedCurrent = count;
            if (count > minesOwnedLifetime) {
                minesOwnedLifetime = count;
            }
        }
        
        public int getMinesOwnedLifetime() { return minesOwnedLifetime; }
        
        public long getFarmIncomeTotal() { return farmIncomeTotal; }
        public void addFarmIncome(long amount) { this.farmIncomeTotal += amount; }
        
        public long getMineIncomeTotal() { return mineIncomeTotal; }
        public void addMineIncome(long amount) { this.mineIncomeTotal += amount; }
        
        // Business
        public int getBusinessesOwnedCurrent() { return businessesOwnedCurrent; }
        public void setBusinessesOwnedCurrent(int count) {
            this.businessesOwnedCurrent = count;
            if (count > businessesOwnedLifetime) {
                businessesOwnedLifetime = count;
            }
        }
        
        public int getBusinessesOwnedLifetime() { return businessesOwnedLifetime; }
        
        public long getBusinessIncomeTotal() { return businessIncomeTotal; }
        public void addBusinessIncome(long amount) { this.businessIncomeTotal += amount; }
        
        public int getBusinessUpgradesTotal() { return businessUpgradesTotal; }
        public void incrementBusinessUpgrades() { this.businessUpgradesTotal++; }
        
        // Workers
        public int getWorkersHiredCurrent() { return workersHiredCurrent; }
        public void setWorkersHiredCurrent(int count) {
            this.workersHiredCurrent = count;
            if (count > workersHiredLifetime) {
                workersHiredLifetime = count;
            }
        }
        
        public int getWorkersHiredLifetime() { return workersHiredLifetime; }
        
        public int getWorkersTrained() { return workersTrained; }
        public void incrementWorkersTrained() { this.workersTrained++; }
        
        public long getWorkerWagesTotal() { return workerWagesTotal; }
        public void addWorkerWages(long amount) { this.workerWagesTotal += amount; }
        
        // Stock Market
        public int getStockTradesMade() { return stockTradesMade; }
        public void incrementStockTrades() { this.stockTradesMade++; }
        
        public long getStockProfitTotal() { return stockProfitTotal; }
        public void addStockProfit(long amount) { 
            this.stockProfitTotal += amount;
            if (amount > biggestStockWin) {
                biggestStockWin = amount;
            }
        }
        
        public long getStockLossTotal() { return stockLossTotal; }
        public void addStockLoss(long amount) { 
            this.stockLossTotal += amount;
            if (amount > biggestStockLoss) {
                biggestStockLoss = amount;
            }
        }
        
        public int getSharesOwnedCurrent() { return sharesOwnedCurrent; }
        public void setSharesOwnedCurrent(int count) { this.sharesOwnedCurrent = count; }
        
        public long getDividendsEarnedTotal() { return dividendsEarnedTotal; }
        public void addDividends(long amount) { this.dividendsEarnedTotal += amount; }
        
        public long getBiggestStockWin() { return biggestStockWin; }
        public long getBiggestStockLoss() { return biggestStockLoss; }
        
        // Gaming
        public int getGamesPlayed() { return gamesPlayed; }
        public void incrementGamesPlayed() { this.gamesPlayed++; }
        
        public int getGamesWon() { return gamesWon; }
        public void incrementGamesWon() { 
            this.gamesWon++;
            this.currentWinStreak++;
            if (currentWinStreak > longestWinStreak) {
                longestWinStreak = currentWinStreak;
            }
        }
        
        public int getGamesLost() { return gamesLost; }
        public void incrementGamesLost() { 
            this.gamesLost++;
            this.currentWinStreak = 0;
        }
        
        public long getGamingWinningsTotal() { return gamingWinningsTotal; }
        public void addGamingWinnings(long amount) { 
            this.gamingWinningsTotal += amount;
            if (amount > biggestGameWin) {
                biggestGameWin = amount;
            }
        }
        
        public long getGamingLossesTotal() { return gamingLossesTotal; }
        public void addGamingLosses(long amount) { this.gamingLossesTotal += amount; }
        
        public int getLongestWinStreak() { return longestWinStreak; }
        public int getCurrentWinStreak() { return currentWinStreak; }
        public long getBiggestGameWin() { return biggestGameWin; }
        
        public Map<String, Integer> getGamesPlayedByType() { return gamesPlayedByType; }
        public void incrementGameType(String type) {
            gamesPlayedByType.put(type, gamesPlayedByType.getOrDefault(type, 0) + 1);
        }
        
        // Lottery
        public int getLotteryTicketsBought() { return lotteryTicketsBought; }
        public void incrementLotteryTickets(int count) { this.lotteryTicketsBought += count; }
        
        public int getLotteryWins() { return lotteryWins; }
        public void incrementLotteryWins() { this.lotteryWins++; }
        
        public long getLotteryWinningsTotal() { return lotteryWinningsTotal; }
        public void addLotteryWinnings(long amount) { this.lotteryWinningsTotal += amount; }
        
        public long getLotterySpentTotal() { return lotterySpentTotal; }
        public void addLotterySpent(long amount) { this.lotterySpentTotal += amount; }
        
        // Loans
        public int getLoansTaken() { return loansTaken; }
        public void incrementLoansTaken() { this.loansTaken++; }
        
        public int getLoansRepaid() { return loansRepaid; }
        public void incrementLoansRepaid() { this.loansRepaid++; }
        
        public long getTotalBorrowed() { return totalBorrowed; }
        public void addBorrowed(long amount) { this.totalBorrowed += amount; }
        
        public long getTotalRepaid() { return totalRepaid; }
        public void addRepaid(long amount) { this.totalRepaid += amount; }
        
        public long getTotalInterestPaid() { return totalInterestPaid; }
        public void addInterestPaid(long amount) { this.totalInterestPaid += amount; }
        
        // Insurance
        public int getInsurancePolicies() { return insurancePolicies; }
        public void setInsurancePolicies(int count) { this.insurancePolicies = count; }
        
        public int getClaimsFiled() { return claimsFiled; }
        public void incrementClaimsFiled() { this.claimsFiled++; }
        
        public long getPremiumsPaidTotal() { return premiumsPaidTotal; }
        public void addPremiumsPaid(long amount) { this.premiumsPaidTotal += amount; }
        
        public long getClaimsReceivedTotal() { return claimsReceivedTotal; }
        public void addClaimsReceived(long amount) { this.claimsReceivedTotal += amount; }
        
        // Automation
        public int getAutomationRuns() { return automationRuns; }
        public void incrementAutomationRuns() { this.automationRuns++; }
        
        public long getAutomationSavings() { return automationSavings; }
        public void addAutomationSavings(long amount) { this.automationSavings += amount; }
        
        // Misc
        public int getDaysPlayed() { return daysPlayed; }
        public void incrementDaysPlayed() { this.daysPlayed++; }
        
        public long getLastPlayDate() { return lastPlayDate; }
        public void updateLastPlayDate() { this.lastPlayDate = System.currentTimeMillis(); }
    }
}
