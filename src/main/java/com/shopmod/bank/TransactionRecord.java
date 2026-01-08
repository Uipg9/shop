package com.shopmod.bank;

import java.time.LocalDateTime;

/**
 * Represents a single bank transaction
 */
public class TransactionRecord {
    private final TransactionType type;
    private final long amount;
    private final long balanceAfter;
    private final String description;
    private final LocalDateTime timestamp;
    private final AccountType accountType;
    
    public enum TransactionType {
        DEPOSIT("Deposit", "§a"),
        WITHDRAW("Withdrawal", "§c"),
        TRANSFER("Transfer", "§e"),
        LOAN_PAYMENT("Loan Payment", "§6"),
        INTEREST("Interest", "§a"),
        DIVIDEND("Dividend", "§a"),
        PURCHASE("Purchase", "§c"),
        INSURANCE_PREMIUM("Insurance", "§c"),
        CREDIT_BORROW("Credit Borrow", "§c"),
        CREDIT_PAYMENT("Credit Payment", "§a"),
        AUTO_DEPOSIT("Auto-Deposit", "§b"),
        AUTO_PAYMENT("Auto-Payment", "§d");
        
        private final String displayName;
        private final String color;
        
        TransactionType(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    public TransactionRecord(TransactionType type, long amount, long balanceAfter, 
                           String description, AccountType accountType) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.accountType = accountType;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public long getBalanceAfter() {
        return balanceAfter;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    /**
     * Get formatted display string
     */
    public String getFormattedString() {
        return String.format("%s%s: §6$%,d §7- %s (Balance: §6$%,d§7)",
            type.getColor(), type.getDisplayName(), amount, description, balanceAfter);
    }
}
