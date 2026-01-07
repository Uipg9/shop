package com.shopmod.farm;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import com.shopmod.village.ResourceType;

/**
 * Digital Farm Types - Automated resource generation farms
 * Buy once, pay daily salary, collect resources automatically
 */
public enum FarmType {
    // BASIC FARMS (Always Available) - 3x production boost
    CROP_FARM(
        "Crop Farm", "§aAutomated crop production", Items.WHEAT,
        5000L, 200L, // $5000 buy, $200/day salary
        ResourceType.FOOD, 30, 1 // 30/day (was 10)
    ),
    
    TREE_FARM(
        "Tree Farm", "§6Automated lumber production", Items.OAK_LOG,
        4000L, 150L,
        ResourceType.WOOD, 24, 1 // 24/day (was 8)
    ),
    
    FISH_FARM(
        "Fish Farm", "§bAutomated fish production", Items.COD,
        3000L, 100L,
        ResourceType.FISH, 18, 1 // 18/day (was 6)
    ),
    
    // ADVANCED FARMS (Level 2+) - 3x production boost
    IRON_FARM(
        "Iron Farm", "§7Automated iron golem farm", Items.IRON_INGOT,
        15000L, 800L,
        ResourceType.ORE, 15, 2 // 15/day (was 5)
    ),
    
    ANIMAL_FARM(
        "Animal Farm", "§dAutomated leather & wool production", Items.LEATHER,
        8000L, 400L,
        ResourceType.LEATHER, 12, 2 // 12/day (was 4)
    ),
    
    // EXPERT FARMS (Level 3+) - 3x production boost
    MOB_FARM(
        "Mob Farm", "§cAutomated hostile mob farm", Items.GUNPOWDER,
        25000L, 1200L,
        ResourceType.RARE, 9, 3 // 9/day (was 3)
    ),
    
    ENCHANT_FARM(
        "Enchanting Farm", "§5Automated enchanted item production", Items.ENCHANTING_TABLE,
        30000L, 1500L,
        ResourceType.ENCHANTED, 6, 3 // 6/day (was 2)
    );
    
    private final String displayName;
    private final String description;
    private final Item icon;
    private final long purchaseCost;
    private final long dailySalary;
    private final ResourceType outputResource;
    private final int dailyOutput;
    private final int requiredLevel;
    
    FarmType(String displayName, String description, Item icon,
             long purchaseCost, long dailySalary,
             ResourceType outputResource, int dailyOutput, int requiredLevel) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.purchaseCost = purchaseCost;
        this.dailySalary = dailySalary;
        this.outputResource = outputResource;
        this.dailyOutput = dailyOutput;
        this.requiredLevel = requiredLevel;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Item getIcon() { return icon; }
    public long getPurchaseCost() { return purchaseCost; }
    public long getDailySalary() { return dailySalary; }
    public ResourceType getOutputResource() { return outputResource; }
    public int getDailyOutput() { return dailyOutput; }
    public int getRequiredLevel() { return requiredLevel; }
}