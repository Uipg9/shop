package com.shopmod.automation;

/**
 * Stores automation preferences for a player
 */
public class AutomationSettings {
    // Automation toggles
    public boolean autoPayLoans = false;
    public boolean autoCollectFarms = false;
    public boolean autoDepositWallet = false;
    public boolean autoSellHarvests = false;
    public boolean autoInvestDividends = false;
    
    // Deposit threshold (only auto-deposit when wallet exceeds this)
    public long depositThreshold = 10_000;  // $10K default
    
    public AutomationSettings() {}
    
    /**
     * Check if any automation is enabled
     */
    public boolean hasAnyEnabled() {
        return autoPayLoans || autoCollectFarms || autoDepositWallet || 
               autoSellHarvests || autoInvestDividends;
    }
    
    /**
     * Count enabled automations
     */
    public int countEnabled() {
        int count = 0;
        if (autoPayLoans) count++;
        if (autoCollectFarms) count++;
        if (autoDepositWallet) count++;
        if (autoSellHarvests) count++;
        if (autoInvestDividends) count++;
        return count;
    }
}
