package com.shopmod.insurance;

import java.time.LocalDateTime;

/**
 * Represents an insurance policy owned by a player
 */
public class InsurancePolicy {
    private final InsuranceType type;
    private final long monthlyPremium;
    private final long coverageAmount;
    private boolean active;
    private final LocalDateTime purchaseDate;
    private LocalDateTime lastPaymentDate;
    private int claimsCount;
    private double premiumMultiplier;  // Increases with claims
    private int missedPayments;
    
    public InsurancePolicy(InsuranceType type, long monthlyPremium, long coverageAmount) {
        this.type = type;
        this.monthlyPremium = monthlyPremium;
        this.coverageAmount = coverageAmount;
        this.active = true;
        this.purchaseDate = LocalDateTime.now();
        this.lastPaymentDate = LocalDateTime.now();
        this.claimsCount = 0;
        this.premiumMultiplier = 1.0;
        this.missedPayments = 0;
    }
    
    public InsuranceType getType() {
        return type;
    }
    
    public long getMonthlyPremium() {
        return (long)(monthlyPremium * premiumMultiplier);
    }
    
    public long getBasePremium() {
        return monthlyPremium;
    }
    
    public long getCoverageAmount() {
        return coverageAmount;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public LocalDateTime getLastPaymentDate() {
        return lastPaymentDate;
    }
    
    public void setLastPaymentDate(LocalDateTime date) {
        this.lastPaymentDate = date;
    }
    
    public int getClaimsCount() {
        return claimsCount;
    }
    
    public void incrementClaimsCount() {
        this.claimsCount++;
        // Increase premium by 10% per claim, max 200% (2.0x multiplier)
        this.premiumMultiplier = Math.min(2.0, 1.0 + (claimsCount * 0.10));
    }
    
    public double getPremiumMultiplier() {
        return premiumMultiplier;
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
}
