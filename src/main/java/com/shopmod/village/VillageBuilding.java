package com.shopmod.village;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * Buildings that can be constructed in the village to provide bonuses
 * Each building has a cost, daily upkeep, and provides specific benefits
 */
public enum VillageBuilding {
    // Basic infrastructure
    HOUSE("House", Items.OAK_PLANKS, 
        "Provides housing for workers",
        Map.of(ResourceType.WOOD, 50),
        Map.of(ResourceType.WOOD, 2), // 2 wood/day upkeep
        1), // +1 worker slot
    
    // Storage buildings
    GRANARY("Granary", Items.HAY_BLOCK,
        "Stores 2x more food",
        Map.of(ResourceType.WOOD, 100, ResourceType.FOOD, 50),
        Map.of(ResourceType.WOOD, 1),
        0),
    
    WAREHOUSE("Warehouse", Items.CHEST,
        "Stores 2x more resources",
        Map.of(ResourceType.WOOD, 150, ResourceType.ORE, 20),
        Map.of(ResourceType.WOOD, 3),
        0),
    
    // Production buildings
    FARM_EXPANSION("Farm Expansion", Items.WHEAT,
        "Each farmer produces +1 food",
        Map.of(ResourceType.WOOD, 80, ResourceType.FOOD, 30),
        Map.of(ResourceType.FOOD, 5), // Needs seeds/fertilizer
        0),
    
    MINE_SHAFT("Mine Shaft", Items.IRON_PICKAXE,
        "Miner has 20% chance for bonus ore",
        Map.of(ResourceType.WOOD, 200, ResourceType.ORE, 50, ResourceType.TOOLS, 5),
        Map.of(ResourceType.WOOD, 4, ResourceType.TOOLS, 1),
        0),
    
    WORKSHOP("Workshop", Items.CRAFTING_TABLE,
        "All workers produce 10% more",
        Map.of(ResourceType.WOOD, 250, ResourceType.ORE, 30),
        Map.of(ResourceType.WOOD, 5, ResourceType.TOOLS, 1),
        0),
    
    // Economic buildings
    MARKET("Market", Items.EMERALD_BLOCK,
        "Sell resources for 2x value",
        Map.of(ResourceType.WOOD, 300, ResourceType.ORE, 50, ResourceType.TOOLS, 10),
        Map.of(ResourceType.FOOD, 10, ResourceType.WOOD, 3),
        0),
    
    TRADING_POST("Trading Post", Items.LECTERN,
        "Merchant generates +50% profit",
        Map.of(ResourceType.WOOD, 200, ResourceType.LEATHER, 20),
        Map.of(ResourceType.FOOD, 5),
        0),
    
    // Special buildings
    LIBRARY("Library", Items.BOOKSHELF,
        "Enchanter produces better books",
        Map.of(ResourceType.WOOD, 400, ResourceType.ORE, 100, ResourceType.ENCHANTED, 5),
        Map.of(ResourceType.WOOD, 3, ResourceType.FOOD, 2),
        0),
    
    BARRACKS("Barracks", Items.IRON_BLOCK,
        "Workers never go on strike",
        Map.of(ResourceType.WOOD, 500, ResourceType.ORE, 200, ResourceType.TOOLS, 20, ResourceType.ARMOR, 10),
        Map.of(ResourceType.FOOD, 20, ResourceType.WOOD, 10),
        0);
    
    private final String displayName;
    private final Item icon;
    private final String description;
    private final Map<ResourceType, Integer> buildCost;
    private final Map<ResourceType, Integer> dailyUpkeep;
    private final int workerSlots; // How many worker slots this building adds
    
    VillageBuilding(String displayName, Item icon, String description,
                    Map<ResourceType, Integer> buildCost,
                    Map<ResourceType, Integer> dailyUpkeep,
                    int workerSlots) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.buildCost = buildCost;
        this.dailyUpkeep = dailyUpkeep;
        this.workerSlots = workerSlots;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Item getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Map<ResourceType, Integer> getBuildCost() {
        return buildCost;
    }
    
    public Map<ResourceType, Integer> getDailyUpkeep() {
        return dailyUpkeep;
    }
    
    public int getWorkerSlots() {
        return workerSlots;
    }
    
    /**
     * Check if this building is a house (provides worker slots)
     */
    public boolean isHousing() {
        return workerSlots > 0;
    }
}
