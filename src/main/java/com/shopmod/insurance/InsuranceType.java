package com.shopmod.insurance;

/**
 * Types of insurance available to players
 */
public enum InsuranceType {
    PROPERTY_INSURANCE("Property Insurance", 500, 100_000,
        "Covers 100% tenant damage and property repairs"),
    
    FARM_INSURANCE("Farm Insurance", 300, 50_000,
        "Covers crop failures and farming disasters"),
    
    MINE_INSURANCE("Mine Insurance", 800, 150_000,
        "Covers mining equipment failures and accidents"),
    
    BUSINESS_INSURANCE("Business Insurance", 1_400, 250_000,
        "Complete coverage for all business operations (20% discount!)");
    
    private final String displayName;
    private final long monthlyPremium;
    private final long coverageAmount;
    private final String description;
    
    InsuranceType(String displayName, long monthlyPremium, long coverageAmount, String description) {
        this.displayName = displayName;
        this.monthlyPremium = monthlyPremium;
        this.coverageAmount = coverageAmount;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public long getMonthlyPremium() {
        return monthlyPremium;
    }
    
    public long getCoverageAmount() {
        return coverageAmount;
    }
    
    public String getDescription() {
        return description;
    }
}
