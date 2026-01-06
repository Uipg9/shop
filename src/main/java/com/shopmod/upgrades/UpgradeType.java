package com.shopmod.upgrades;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

/**
 * Defines all available shop upgrades
 */
public enum UpgradeType {
    INCOME_MULTIPLIER(
        "Income Multiplier",
        "§6+1% to all income",
        Items.GOLD_INGOT,
        100,     // Max level
        100,     // Base cost
        1.15,    // Cost multiplier per level
        0.01     // Benefit per level (1%)
    ),
    
    MINING_SPEED(
        "Mining Speed",
        "§bGrants Haste effect",
        Items.DIAMOND_PICKAXE,
        50,      // Max level (Haste goes up to V normally)
        150,
        1.18,
        1.0      // Haste level (rounded)
    ),
    
    XP_MULTIPLIER(
        "XP Multiplier",
        "§a+1% to all XP gains",
        Items.EXPERIENCE_BOTTLE,
        100,
        120,
        1.16,
        0.01     // 1% per level
    ),
    
    SELL_PRICE_BOOST(
        "Sell Price Boost",
        "§e+0.5% to sell prices",
        Items.EMERALD,
        100,
        110,
        1.17,
        0.005    // 0.5% per level
    );
    
    private final String displayName;
    private final String description;
    private final Item icon;
    private final int maxLevel;
    private final long baseCost;
    private final double costMultiplier;
    private final double benefitPerLevel;
    
    UpgradeType(String displayName, String description, Item icon, int maxLevel, 
                long baseCost, double costMultiplier, double benefitPerLevel) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.benefitPerLevel = benefitPerLevel;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Item getIcon() {
        return icon;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Calculate cost for a specific level
     */
    public long getCostForLevel(int level) {
        if (level <= 0 || level > maxLevel) return 0;
        return (long) (baseCost * Math.pow(costMultiplier, level - 1));
    }
    
    /**
     * Get the benefit at a specific level
     */
    public double getBenefitAtLevel(int level) {
        return benefitPerLevel * level;
    }
    
    /**
     * Format the benefit for display
     */
    public String formatBenefit(int level) {
        double benefit = getBenefitAtLevel(level);
        
        return switch (this) {
            case INCOME_MULTIPLIER -> String.format("§6+%.0f%% Income", benefit * 100);
            case MINING_SPEED -> {
                int hasteLevel = Math.min(5, (int) Math.ceil(benefit / 10.0));
                yield "§bHaste " + romanNumeral(hasteLevel);
            }
            case XP_MULTIPLIER -> String.format("§a+%.0f%% XP", benefit * 100);
            case SELL_PRICE_BOOST -> String.format("§e+%.1f%% Sell Price", benefit * 100);
        };
    }
    
    private static String romanNumeral(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }
}
