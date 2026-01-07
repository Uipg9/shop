package com.shopmod.property;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

/**
 * Property/Real Estate types - Buy land, buildings, villages for passive income
 */
public enum PropertyType {
    // LAND PLOTS
    SMALL_PLOT("Small Plot", "§710x10 land plot", Items.GRASS_BLOCK,
        10000L, 50L, 1, // $10k buy, $50/day income, level 1
        PropertyCategory.LAND),
    
    MEDIUM_PLOT("Medium Plot", "§e25x25 land plot", Items.DIRT,
        50000L, 300L, 1,
        PropertyCategory.LAND),
    
    LARGE_PLOT("Large Plot", "§650x50 land plot", Items.PODZOL,
        200000L, 1500L, 2,
        PropertyCategory.LAND),
    
    MEGA_PLOT("Mega Plot", "§d100x100 land plot", Items.MYCELIUM,
        1000000L, 10000L, 3,
        PropertyCategory.LAND),
    
    // BUILDINGS
    SHOP_BUILDING("Shop Building", "§bRetail storefront", Items.EMERALD_BLOCK,
        75000L, 500L, 1,
        PropertyCategory.BUILDING),
    
    WAREHOUSE_BUILDING("Warehouse", "§7Storage facility", Items.CHEST,
        100000L, 800L, 2,
        PropertyCategory.BUILDING),
    
    FACTORY("Factory", "§8Manufacturing plant", Items.FURNACE,
        250000L, 2000L, 2,
        PropertyCategory.BUILDING),
    
    SKYSCRAPER("Skyscraper", "§bMassive office tower", Items.GLASS,
        500000L, 5000L, 3,
        PropertyCategory.BUILDING),
    
    MALL("Shopping Mall", "§5Retail complex", Items.GOLD_BLOCK,
        750000L, 8000L, 3,
        PropertyCategory.BUILDING),
    
    // VILLAGES/TOWNS
    HAMLET("Hamlet", "§7Small settlement (5 villagers)", Items.OAK_DOOR,
        150000L, 1000L, 1,
        PropertyCategory.SETTLEMENT),
    
    VILLAGE("Village", "§a20 villagers", Items.BELL,
        500000L, 4000L, 2,
        PropertyCategory.SETTLEMENT),
    
    TOWN("Town", "§950 villagers", Items.LODESTONE,
        1500000L, 12000L, 3,
        PropertyCategory.SETTLEMENT),
    
    CITY("City", "§b100 villagers", Items.BEACON,
        5000000L, 40000L, 4,
        PropertyCategory.SETTLEMENT),
    
    METROPOLIS("Metropolis", "§5500 villagers", Items.END_CRYSTAL,
        20000000L, 200000L, 5,
        PropertyCategory.SETTLEMENT);
    
    private final String displayName;
    private final String description;
    private final Item icon;
    private final long purchaseCost;
    private final long dailyIncome;
    private final int requiredLevel; // Player tier requirement
    private final PropertyCategory category;
    
    PropertyType(String displayName, String description, Item icon,
                 long purchaseCost, long dailyIncome, int requiredLevel,
                 PropertyCategory category) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.purchaseCost = purchaseCost;
        this.dailyIncome = dailyIncome;
        this.requiredLevel = requiredLevel;
        this.category = category;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Item getIcon() { return icon; }
    public long getPurchaseCost() { return purchaseCost; }
    public long getDailyIncome() { return dailyIncome; }
    public int getRequiredLevel() { return requiredLevel; }
    public PropertyCategory getCategory() { return category; }
    
    public enum PropertyCategory {
        LAND("Land Plots"),
        BUILDING("Buildings"),
        SETTLEMENT("Settlements");
        
        private final String displayName;
        
        PropertyCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
}
