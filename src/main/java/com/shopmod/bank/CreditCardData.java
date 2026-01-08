package com.shopmod.bank;

import java.time.LocalDateTime;

/**
 * Credit card account data
 */
public class CreditCardData {
    private static final long CREDIT_LIMIT = 50_000;
    private static final double MONTHLY_INTEREST_RATE = 0.10;  // 10% per month
    private static final double LATE_PENALTY = 0.20;  // 20% extra on late payment
    private static final double MIN_PAYMENT_RATE = 0.05;  // 5% minimum payment
    private static final long MIN_PAYMENT_AMOUNT = 100;  // Or $100, whichever is higher
    
    private long balance;  // Amount borrowed (owed)
    private LocalDateTime lastPaymentDate;
    private LocalDateTime lastInterestDate;
    private int missedPayments;
    private long totalBorrowed;
    private long totalPaid;
    
    public CreditCardData() {
        this.balance = 0;
        this.lastPaymentDate = LocalDateTime.now();
        this.lastInterestDate = LocalDateTime.now();
        this.missedPayments = 0;
        this.totalBorrowed = 0;
        this.totalPaid = 0;
    }
    
    public long getBalance() {
        return balance;
    }
    
    public void setBalance(long balance) {
        this.balance = Math.max(0, Math.min(CREDIT_LIMIT, balance));
    }
    
    public long getCreditLimit() {
        return CREDIT_LIMIT;
    }
    
    public long getAvailableCredit() {
        return CREDIT_LIMIT - balance;
    }
    
    public double getMonthlyInterestRate() {
        return MONTHLY_INTEREST_RATE;
    }
    
    public long getMinimumPayment() {
        if (balance == 0) return 0;
        return Math.max(MIN_PAYMENT_AMOUNT, (long)(balance * MIN_PAYMENT_RATE));
    }
    
    public LocalDateTime getLastPaymentDate() {
        return lastPaymentDate;
    }
    
    public void setLastPaymentDate(LocalDateTime date) {
        this.lastPaymentDate = date;
    }
    
    public LocalDateTime getLastInterestDate() {
        return lastInterestDate;
    }
    
    public void setLastInterestDate(LocalDateTime date) {
        this.lastInterestDate = date;
    }
    
    public int getMissedPayments() {
        return missedPayments;
    }
    
    public void incrementMissedPayments() {
        this.missedPayments++;
    }
    
    public void resetMissedPayments() {
        this.missedPayments = 0;
    }
    
    public long getTotalBorrowed() {
        return totalBorrowed;
    }
    
    public void addTotalBorrowed(long amount) {
        this.totalBorrowed += amount;
    }
    
    public long getTotalPaid() {
        return totalPaid;
    }
    
    public void addTotalPaid(long amount) {
        this.totalPaid += amount;
    }
    
    /**
     * Borrow money from credit
     */
    public boolean borrow(long amount) {
        if (amount <= 0) return false;
        if (getAvailableCredit() < amount) return false;
        
        balance += amount;
        totalBorrowed += amount;
        return true;
    }
    
    /**
     * Make a payment toward credit card balance
     */
    public boolean pay(long amount) {
        if (amount <= 0) return false;
        if (amount > balance) amount = balance;  // Can't overpay
        
        balance -= amount;
        totalPaid += amount;
        lastPaymentDate = LocalDateTime.now();
        resetMissedPayments();
        return true;
    }
    
    /**
     * Apply monthly interest
     */
    public void applyMonthlyInterest(boolean isLate) {
        if (balance == 0) return;
        
        double rate = MONTHLY_INTEREST_RATE;
        if (isLate) {
            rate += LATE_PENALTY;  // 30% total if late
        }
        
        long interest = (long)(balance * rate);
        balance = Math.min(CREDIT_LIMIT, balance + interest);
        lastInterestDate = LocalDateTime.now();
    }
}
