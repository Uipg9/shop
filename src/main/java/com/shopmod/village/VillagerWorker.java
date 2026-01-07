package com.shopmod.village;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import java.util.Map;

/**
 * Enhanced villager worker types with resource production system
 * Tier 1 = Always available (Foundation)
 * Tier 2 = Village Level 2+ (Basic Industry)
 * Tier 3 = Village Level 3+ (Advanced Crafting)
 * Tier 4 = Village Level 4+ (Specialized)
 */
public enum VillagerWorker {
    // ===== TIER 1: FOUNDATION (Always Available) =====
    FARMER(
        "Farmer", "§aGrows crops to feed the village", Items.WHEAT,
        100L, 250L, 1,
        Map.of(), // No inputs - produces from nothing
        Map.of(ResourceType.FOOD, 3), // Produces 3 food/day
        true, 1.0
    ),
    
    LUMBERJACK(
        "Lumberjack", "§6Chops trees for building materials", Items.IRON_AXE,
        80L, 200L, 1,
        Map.of(),
        Map.of(ResourceType.WOOD, 3), // Produces 3 wood/day
        true, 1.0
    ),
    
    FISHERMAN(
        "Fisherman", "§bCatches fish as alternate food", Items.FISHING_ROD,
        70L, 180L, 1,
        Map.of(),
        Map.of(ResourceType.FISH, 2), // Produces 2 fish/day
        true, 0.9
    ),
    
    // ===== TIER 2: BASIC INDUSTRY (Village Level 2+) =====
    MINER(
        "Miner", "§8Mines ores for crafting", Items.DIAMOND_PICKAXE,
        200L, 500L, 2,
        Map.of(ResourceType.FOOD, 2, ResourceType.WOOD, 1), // Consumes 2 food + 1 wood
        Map.of(ResourceType.ORE, 2), // Produces 2 ore/day
        false, 1.2
    ),
    
    RANCHER(
        "Rancher", "§dRaises animals for leather and wool", Items.WHEAT,
        120L, 300L, 2,
        Map.of(ResourceType.FOOD, 3), // Consumes 3 food
        Map.of(ResourceType.LEATHER, 2, ResourceType.WOOL, 2), // Produces leather + wool
        false, 1.1
    ),
    
    // ===== TIER 3: ADVANCED CRAFTING (Village Level 3+) =====
    BLACKSMITH(
        "Blacksmith", "§7Crafts tools and armor", Items.ANVIL,
        150L, 400L, 3,
        Map.of(ResourceType.ORE, 2, ResourceType.WOOD, 1), // Consumes 2 ore + 1 wood
        Map.of(ResourceType.TOOLS, 1, ResourceType.ARMOR, 1), // Produces tools + armor
        false, 1.15
    ),
    
    MERCHANT(
        "Merchant", "§eTrades goods for profit", Items.EMERALD,
        180L, 600L, 3,
        Map.of(), // Consumes ANY 3 resources (special handling)
        Map.of(), // Produces money directly (special handling)
        false, 1.3
    ),
    
    // ===== TIER 4: SPECIALIZED (Village Level 4+) =====
    ENCHANTER(
        "Enchanter", "§5Creates enchanted items", Items.ENCHANTING_TABLE,
        250L, 1000L, 4,
        Map.of(ResourceType.WOOD, 1, ResourceType.ORE, 2), // Books + magic
        Map.of(ResourceType.ENCHANTED, 1), // Produces enchanted items
        false, 1.5
    );
    
    private final String displayName;
    private final String description;
    private final Item icon;
    private final long dailySalary;
    private final long hireCost;
    private final int requiredVillageLevel;
    private final Map<ResourceType, Integer> dailyInputs; // What worker consumes
    private final Map<ResourceType, Integer> dailyOutputs; // What worker produces
    private final boolean isTier1; // Tier 1 workers always available
    private final double productionMultiplier; // Bonus per level
    
    VillagerWorker(String displayName, String description, Item icon,
                   long dailySalary, long hireCost, int requiredVillageLevel,
                   Map<ResourceType, Integer> dailyInputs,
                   Map<ResourceType, Integer> dailyOutputs,
                   boolean isTier1, double productionMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.dailySalary = dailySalary;
        this.hireCost = hireCost;
        this.requiredVillageLevel = requiredVillageLevel;
        this.dailyInputs = dailyInputs;
        this.dailyOutputs = dailyOutputs;
        this.isTier1 = isTier1;
        this.productionMultiplier = productionMultiplier;
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
    
    public long getDailySalary() {
        return dailySalary;
    }
    
    public long getHireCost() {
        return hireCost;
    }
    
    public int getRequiredVillageLevel() {
        return requiredVillageLevel;
    }
    
    public Map<ResourceType, Integer> getDailyInputs() {
        return dailyInputs;
    }
    
    public Map<ResourceType, Integer> getDailyOutputs() {
        return dailyOutputs;
    }
    
    public boolean isTier1() {
        return isTier1;
    }
    
    public double getProductionMultiplier() {
        return productionMultiplier;
    }
    
    /**
     * Calculate output at a specific worker level
     */
    public Map<ResourceType, Integer> getOutputAtLevel(int level) {
        if (level <= 0) return Map.of();
        
        Map<ResourceType, Integer> scaled = new java.util.HashMap<>();
        double multiplier = Math.pow(productionMultiplier, level - 1);
        
        for (Map.Entry<ResourceType, Integer> entry : dailyOutputs.entrySet()) {
            int scaledAmount = (int) Math.ceil(entry.getValue() * multiplier);
            scaled.put(entry.getKey(), scaledAmount);
        }
        
        return scaled;
    }
    
    /**
     * Get estimated value produced per day at level 1
     */
    public long getEstimatedDailyValue() {
        long totalValue = 0;
        for (Map.Entry<ResourceType, Integer> entry : dailyOutputs.entrySet()) {
            totalValue += entry.getKey().getValuePerUnit() * entry.getValue();
        }
        return totalValue;
    }
    
    /**
     * Check if this worker is a producer (Tier 1) or requires inputs
     */
    public boolean isProducer() {
        return dailyInputs.isEmpty();
    }
}
