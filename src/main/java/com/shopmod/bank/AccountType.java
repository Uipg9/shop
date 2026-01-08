package com.shopmod.bank;

/**
 * Types of bank accounts
 */
public enum AccountType {
    CHECKING("Checking Account", "Free unlimited transactions, no interest"),
    SAVINGS("Savings Account", "Earns risky investment returns"),
    INVESTMENT("Investment Account", "Holds stock portfolio value"),
    CREDIT("Credit Card", "Borrow up to $50,000 at 10% monthly interest");
    
    private final String displayName;
    private final String description;
    
    AccountType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
