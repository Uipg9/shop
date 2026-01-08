package com.shopmod.loan;

import com.shopmod.ShopMod;
import com.shopmod.bank.BankManager;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.shop.ShopTier;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player loans with credit scoring and daily payments
 * Credit score improves with bank investments
 * Miss payments = penalties
 */
public class LoanManager {
    private static final Map<UUID, LoanData> activeLoanS = new ConcurrentHashMap<>();
    
    // Total loan interest rates (applied once over entire loan period)
    private static final double MIN_INTEREST_RATE = 0.20;  // 20% total interest (excellent credit - score 100)
    private static final double MAX_INTEREST_RATE = 0.50;  // 50% total interest (poor credit - score 20)
    
    // Credit score thresholds (based on total bank investments)
    private static final long EXCELLENT_CREDIT = 100000;  // $100k invested = excellent
    private static final long GOOD_CREDIT = 50000;        // $50k invested = good
    private static final long FAIR_CREDIT = 10000;        // $10k invested = fair
    
    // Loan limits by tier
    private static final Map<ShopTier, Long> TIER_LOAN_LIMITS = Map.of(
        ShopTier.STARTER, 5000L,
        ShopTier.FARMER, 15000L,
        ShopTier.ENGINEER, 50000L,
        ShopTier.MERCHANT, 150000L,
        ShopTier.NETHER_MASTER, 500000L,
        ShopTier.ELITE, 2000000L
    );
    
    // Payment delay fee
    private static final double DELAY_FEE_RATE = 0.10; // 10% of payment
    
    public static class LoanData {
        private long principalAmount;       // Original loan amount
        private long remainingBalance;      // What's left to pay
        private double interestRate;        // Daily interest rate
        private long dailyPayment;          // Required daily payment
        private long lastPaymentDay;        // Last day payment was made
        private int missedPayments;         // Consecutive missed payments
        private boolean delayRequested;     // Whether delay was requested for today
        private long totalPaid;             // Total amount paid so far
        
        public LoanData(long amount, double rate, long dailyPayment) {
            this.principalAmount = amount;
            this.remainingBalance = amount;
            this.interestRate = rate;
            this.dailyPayment = dailyPayment;
            this.lastPaymentDay = -1;
            this.missedPayments = 0;
            this.delayRequested = false;
            this.totalPaid = 0;
        }
        
        public long getPrincipalAmount() { return principalAmount; }
        public long getRemainingBalance() { return remainingBalance; }
        public double getInterestRate() { return interestRate; }
        public long getDailyPayment() { return dailyPayment; }
        public long getLastPaymentDay() { return lastPaymentDay; }
        public int getMissedPayments() { return missedPayments; }
        public boolean isDelayRequested() { return delayRequested; }
        public long getTotalPaid() { return totalPaid; }
        
        public void setRemainingBalance(long balance) { this.remainingBalance = balance; }
        public void setLastPaymentDay(long day) { this.lastPaymentDay = day; }
        public void setMissedPayments(int missed) { this.missedPayments = missed; }
        public void setDelayRequested(boolean delayed) { this.delayRequested = delayed; }
        public void addTotalPaid(long amount) { this.totalPaid += amount; }
    }
    
    /**
     * Calculate credit score (0-100) based on bank investments
     */
    public static int calculateCreditScore(UUID playerUUID) {
        long invested = BankManager.getBankData(playerUUID).getInvestedMoney();
        
        if (invested >= EXCELLENT_CREDIT) return 100;
        if (invested >= GOOD_CREDIT) return 80;
        if (invested >= FAIR_CREDIT) return 60;
        if (invested >= 1000) return 40;
        return 20; // Poor credit
    }
    
    /**
     * Calculate total loan interest rate based on credit score (not daily!)
     * Returns the total interest percentage to add to the principal
     */
    public static double calculateInterestRate(int creditScore) {
        // Linear interpolation between max and min rates
        // Score 100 = 20% total interest, Score 20 = 50% total interest
        double normalizedScore = creditScore / 100.0;
        return MAX_INTEREST_RATE - (normalizedScore * (MAX_INTEREST_RATE - MIN_INTEREST_RATE));
    }
    
    /**
     * Get maximum loan amount for player's tier
     */
    public static long getMaxLoanAmount(ServerPlayer player) {
        int highestTier = ShopMod.dataManager.getHighestUnlockedTier(player.getUUID());
        
        for (ShopTier tier : ShopTier.values()) {
            if (tier.getId() == highestTier) {
                return TIER_LOAN_LIMITS.getOrDefault(tier, 5000L);
            }
        }
        
        return 5000L; // Default to starter
    }
    
    /**
     * Check if player has an active loan
     */
    public static boolean hasActiveLoan(UUID playerUUID) {
        return activeLoanS.containsKey(playerUUID);
    }
    
    /**
     * Get player's active loan
     */
    public static LoanData getLoan(UUID playerUUID) {
        return activeLoanS.get(playerUUID);
    }
    
    /**
     * Take out a new loan
     */
    public static boolean takeLoan(ServerPlayer player, long amount, int durationDays) {
        if (hasActiveLoan(player.getUUID())) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYou already have an active loan! Pay it off first."));
            return false;
        }
        
        long maxAmount = getMaxLoanAmount(player);
        if (amount > maxAmount) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYour tier only allows loans up to " + CurrencyManager.format(maxAmount)));
            return false;
        }
        
        if (amount < 100) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cMinimum loan amount is $100"));
            return false;
        }
        
        int creditScore = calculateCreditScore(player.getUUID());
        double totalInterestRate = calculateInterestRate(creditScore);
        
        // Calculate total amount to repay (principal + total interest)
        // Interest is applied ONCE over the entire loan period, not daily!
        long totalInterest = (long)(amount * totalInterestRate);
        long totalToRepay = amount + totalInterest;
        long dailyPayment = totalToRepay / durationDays;
        
        LoanData loan = new LoanData(amount, totalInterestRate, dailyPayment);
        activeLoanS.put(player.getUUID(), loan);
        
        CurrencyManager.addMoney(player, amount);
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a§l=== LOAN APPROVED ==="));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aLoan Amount: §6" + CurrencyManager.format(amount)));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aCredit Score: §e" + creditScore + "/100"));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aTotal Interest: §e" + String.format("%.1f%%", totalInterestRate * 100) + " §7(" + CurrencyManager.format(totalInterest) + ")"));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aDaily Payment: §6" + CurrencyManager.format(dailyPayment)));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§aTotal To Repay: §6" + CurrencyManager.format(totalToRepay)));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§7Use /loan to view your loan status"));
        
        return true;
    }
    
    /**
     * Process daily loan payment
     */
    public static void processDailyPayments(ServerPlayer player, long currentDay) {
        LoanData loan = getLoan(player.getUUID());
        if (loan == null) return;
        
        // Skip if already paid today
        if (loan.getLastPaymentDay() >= currentDay) return;
        
        // Check for delay request
        if (loan.isDelayRequested()) {
            loan.setDelayRequested(false);
            loan.setLastPaymentDay(currentDay);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§e§l[LOAN] Payment delayed for today (fee applied)"));
            return;
        }
        
        long payment = loan.getDailyPayment();
        
        // Apply penalties for missed payments
        if (loan.getMissedPayments() == 1) {
            payment *= 2; // Double interest
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c§l[LOAN] MISSED PAYMENT! Interest doubled to " + CurrencyManager.format(payment)));
        } else if (loan.getMissedPayments() >= 2) {
            payment *= 3; // Triple interest + penalty
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§4§l[LOAN] MULTIPLE MISSED PAYMENTS! Penalty applied: " + CurrencyManager.format(payment)));
        }
        
        long balance = CurrencyManager.getBalance(player);
        
        // Try to make payment from wallet first, then from bank investments if needed
        if (balance >= payment) {
            // Make payment from wallet
            CurrencyManager.removeMoney(player, payment);
            loan.setRemainingBalance(loan.getRemainingBalance() - payment);
            loan.addTotalPaid(payment);
            loan.setLastPaymentDay(currentDay);
            loan.setMissedPayments(0);
            
            if (loan.getRemainingBalance() <= 0) {
                // Loan paid off!
                activeLoanS.remove(player.getUUID());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a§l[LOAN] PAID OFF! You're debt-free!"));
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a§l[LOAN] Payment made: §6" + CurrencyManager.format(payment)));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Remaining: §6" + CurrencyManager.format(loan.getRemainingBalance())));
            }
        } else {
            // Check if we can cover shortfall from bank investments
            long shortfall = payment - balance;
            long bankInvestment = BankManager.getBankData(player.getUUID()).getInvestedMoney();
            
            if (bankInvestment >= shortfall) {
                // Auto-withdraw from bank to cover loan payment
                CurrencyManager.removeMoney(player, balance); // Use all wallet money
                BankManager.withdrawMoney(player, shortfall); // Withdraw remainder from bank
                CurrencyManager.removeMoney(player, shortfall); // Take the withdrawn amount for payment
                
                loan.setRemainingBalance(loan.getRemainingBalance() - payment);
                loan.addTotalPaid(payment);
                loan.setLastPaymentDay(currentDay);
                loan.setMissedPayments(0);
                
                if (loan.getRemainingBalance() <= 0) {
                    // Loan paid off!
                    activeLoanS.remove(player.getUUID());
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a§l[LOAN] PAID OFF! You're debt-free!"));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a§l[LOAN] Payment made: §6" + CurrencyManager.format(payment)));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§e§l[LOAN] Auto-withdrew §6" + CurrencyManager.format(shortfall) + " §efrom bank investments"));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7Remaining loan: §6" + CurrencyManager.format(loan.getRemainingBalance())));
                }
            } else {
                // Still can't afford - missed payment
                loan.setMissedPayments(loan.getMissedPayments() + 1);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c§l[LOAN] PAYMENT MISSED! Insufficient funds in wallet and bank."));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Needed: §6" + CurrencyManager.format(payment) + 
                    " §7Available: §6" + CurrencyManager.format(balance + bankInvestment)));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c§l[LOAN] Penalties will apply tomorrow."));
            }
        }
    }
    
    /**
     * Request payment delay for today (costs fee)
     */
    public static boolean requestDelay(ServerPlayer player) {
        LoanData loan = getLoan(player.getUUID());
        if (loan == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYou don't have an active loan!"));
            return false;
        }
        
        if (loan.isDelayRequested()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYou've already requested a delay for today!"));
            return false;
        }
        
        long fee = (long)(loan.getDailyPayment() * DELAY_FEE_RATE);
        
        if (!CurrencyManager.canAfford(player, fee)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYou can't afford the delay fee of " + CurrencyManager.format(fee)));
            return false;
        }
        
        CurrencyManager.removeMoney(player, fee);
        loan.setDelayRequested(true);
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a§l[LOAN] Payment delayed! Fee: §6" + CurrencyManager.format(fee)));
        
        return true;
    }
    
    /**
     * Make manual loan payment with auto-withdrawal from bank if needed
     */
    public static void makeManualPayment(ServerPlayer player, long amount) {
        LoanData loan = getLoan(player.getUUID());
        if (loan == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c§l[LOAN] You don't have an active loan!"));
            return;
        }
        
        if (amount <= 0) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c§l[LOAN] Payment amount must be positive!"));
            return;
        }
        
        // Cap payment to remaining balance
        long actualPayment = Math.min(amount, loan.getRemainingBalance());
        long walletBalance = CurrencyManager.getBalance(player);
        
        // Try to make payment from wallet first, then from bank investments if needed
        if (walletBalance >= actualPayment) {
            // Pay from wallet
            CurrencyManager.removeMoney(player, actualPayment);
            loan.setRemainingBalance(loan.getRemainingBalance() - actualPayment);
            loan.addTotalPaid(actualPayment);
            
            if (loan.getRemainingBalance() <= 0) {
                // Loan paid off!
                activeLoanS.remove(player.getUUID());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a§l[LOAN] CONGRATULATIONS! Loan fully paid off!"));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Total paid: §6" + CurrencyManager.format(loan.getTotalPaid())));
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a§l[LOAN] Payment made: §6" + CurrencyManager.format(actualPayment)));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Remaining balance: §6" + CurrencyManager.format(loan.getRemainingBalance())));
            }
        } else {
            // Check if we can cover shortfall from bank investments
            long shortfall = actualPayment - walletBalance;
            long bankInvestment = BankManager.getBankData(player.getUUID()).getInvestedMoney();
            
            if (bankInvestment >= shortfall) {
                // Auto-withdraw from bank to cover payment
                CurrencyManager.removeMoney(player, walletBalance); // Use all wallet money
                BankManager.withdrawMoney(player, shortfall); // Withdraw remainder from bank
                CurrencyManager.removeMoney(player, shortfall); // Take the withdrawn amount for payment
                
                loan.setRemainingBalance(loan.getRemainingBalance() - actualPayment);
                loan.addTotalPaid(actualPayment);
                
                if (loan.getRemainingBalance() <= 0) {
                    // Loan paid off!
                    activeLoanS.remove(player.getUUID());
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a§l[LOAN] CONGRATULATIONS! Loan fully paid off!"));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§e§l[LOAN] Auto-withdrew §6" + CurrencyManager.format(shortfall) + " §efrom bank investments"));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7Total paid: §6" + CurrencyManager.format(loan.getTotalPaid())));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a§l[LOAN] Payment made: §6" + CurrencyManager.format(actualPayment)));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§e§l[LOAN] Auto-withdrew §6" + CurrencyManager.format(shortfall) + " §efrom bank investments"));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7Remaining balance: §6" + CurrencyManager.format(loan.getRemainingBalance())));
                }
            } else {
                // Can't afford payment
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c§l[LOAN] Insufficient funds! Need §6" + CurrencyManager.format(actualPayment)));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Available: §6" + CurrencyManager.format(walletBalance + bankInvestment) + 
                    " §7(Wallet: §6" + CurrencyManager.format(walletBalance) + 
                    " §7+ Bank: §6" + CurrencyManager.format(bankInvestment) + "§7)"));
            }
        }
    }
}
