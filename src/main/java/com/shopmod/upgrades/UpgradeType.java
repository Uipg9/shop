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
    ),
    
    HEALTH_BOOST(
        "Health Boost",
        "§c+2 max health (1 heart)",
        Items.GOLDEN_APPLE,
        50,      // +100 hearts max
        250,
        1.25,
        2.0      // 2 health points = 1 heart
    ),
    
    FLIGHT_TIME(
        "Flight Duration",
        "§e+5 seconds of flight",
        Items.ELYTRA,
        20,      // 100 seconds max
        1500,
        1.35,
        5.0      // 5 seconds per level
    ),
    
    LUCK_BOOST(
        "Luck",
        "§aGrants Luck effect",
        Items.RABBIT_FOOT,
        10,      // Max level
        500,
        1.28,
        1.0      // Luck level (rounded)
    ),
    
    REGENERATION(
        "Natural Regeneration",
        "§dFaster health regeneration",
        Items.GHAST_TEAR,
        3,       // Max level 3 (low power)
        600,
        1.40,
        1.0      // Regen level (rounded)
    ),
    
    FIRE_RESISTANCE(
        "Fire Resistance",
        "§6Permanent fire immunity",
        Items.MAGMA_CREAM,
        1,       // On/off toggle
        3000,
        1.0,
        1.0
    ),
    
    WATER_BREATHING(
        "Water Breathing",
        "§bPermanent water breathing",
        Items.PUFFERFISH,
        1,       // On/off toggle
        2000,
        1.0,
        1.0
    ),
    
    NIGHT_VISION(
        "Night Vision",
        "§ePermanent night vision",
        Items.GOLDEN_CARROT,
        1,       // On/off toggle
        1500,
        1.0,
        1.0
    ),
    
    KEEP_INVENTORY(
        "Keep Inventory",
        "§cKeep items on death",
        Items.TOTEM_OF_UNDYING,
        1,       // On/off toggle
        50000,   // Very expensive
        1.0,
        1.0
    ),
    
    KEEP_XP(
        "Keep XP",
        "§aKeep XP on death",
        Items.EXPERIENCE_BOTTLE,
        1,       // On/off toggle
        25000,
        1.0,
        1.0
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
            case HEALTH_BOOST -> String.format("§c+%.0f Hearts", benefit / 2.0);
            case FLIGHT_TIME -> String.format("§e+%.0f seconds", benefit);
            case LUCK_BOOST -> {
                int luckLevel = Math.min(10, (int) benefit);
                yield "§aLuck " + romanNumeral(luckLevel);
            }
            case REGENERATION -> {
                int regenLevel = Math.min(3, (int) benefit);
                yield "§dRegen " + romanNumeral(regenLevel);
            }
            case FIRE_RESISTANCE, WATER_BREATHING, NIGHT_VISION, KEEP_INVENTORY, KEEP_XP -> "§aActive";
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
