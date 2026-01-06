package com.shopmod.shop;

import net.minecraft.ChatFormatting;

/**
 * Shop tier system - players unlock categories by paying
 */
public enum ShopTier {
    STARTER(0, "Starter", "Basic survival essentials", ChatFormatting.GREEN, 0),
    FARMER(1, "Farmer", "Crops, farming, and animals", ChatFormatting.YELLOW, 2000),
    ENGINEER(2, "Engineer", "Redstone and mechanisms", ChatFormatting.RED, 5000),
    MERCHANT(3, "Merchant", "Precious minerals and trading", ChatFormatting.AQUA, 10000),
    NETHER_MASTER(4, "Nether Master", "Nether items and potions", ChatFormatting.DARK_RED, 25000),
    ELITE(5, "Elite", "End-game and rare items", ChatFormatting.LIGHT_PURPLE, 50000);
    
    private final int id;
    private final String name;
    private final String description;
    private final ChatFormatting color;
    private final long unlockCost;
    
    ShopTier(int id, String name, String description, ChatFormatting color, long unlockCost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.unlockCost = unlockCost;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ChatFormatting getColor() {
        return color;
    }
    
    public long getUnlockCost() {
        return unlockCost;
    }
    
    public boolean isFree() {
        return unlockCost == 0;
    }
    
    public String getDisplayName() {
        return color + name;
    }
    
    public String getIcon() {
        return switch (this) {
            case STARTER -> "🌱";
            case FARMER -> "🌾";
            case ENGINEER -> "⚙";
            case MERCHANT -> "💎";
            case NETHER_MASTER -> "🔥";
            case ELITE -> "⭐";
        };
    }
    
    public static ShopTier getById(int id) {
        for (ShopTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return STARTER;
    }
}
