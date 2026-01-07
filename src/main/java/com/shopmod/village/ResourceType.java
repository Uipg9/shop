package com.shopmod.village;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Types of resources that can be produced and consumed by village workers
 */
public enum ResourceType {
    // Tier 1 - Foundation resources
    FOOD("Food", "🌾", Items.WHEAT, "Wheat, carrots, potatoes, bread"),
    WOOD("Wood", "🪵", Items.OAK_LOG, "Logs and planks for building"),
    FISH("Fish", "🐟", Items.COD, "Fresh fish from the ocean"),
    
    // Tier 2 - Basic materials
    ORE("Ore", "⛏️", Items.IRON_ORE, "Iron, gold, and rare ores"),
    LEATHER("Leather", "🐄", Items.LEATHER, "From ranched animals"),
    WOOL("Wool", "🐑", Items.WHITE_WOOL, "From sheep ranching"),
    
    // Tier 3 - Processed goods
    TOOLS("Tools", "⚒️", Items.IRON_PICKAXE, "Crafted tools and weapons"),
    ARMOR("Armor", "🛡️", Items.IRON_CHESTPLATE, "Crafted armor pieces"),
    
    // Tier 4 - Special items
    ENCHANTED("Enchanted", "✨", Items.ENCHANTED_BOOK, "Enchanted books and items"),
    RARE("Rare", "💎", Items.DIAMOND, "Rare valuable items");
    
    private final String displayName;
    private final String icon;
    private final Item representativeItem;
    private final String description;
    
    ResourceType(String displayName, String icon, Item representativeItem, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.representativeItem = representativeItem;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public Item getRepresentativeItem() {
        return representativeItem;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the monetary value per unit of this resource
     */
    public long getValuePerUnit() {
        return switch (this) {
            case FOOD -> 10;
            case WOOD -> 15;
            case FISH -> 12;
            case ORE -> 50;
            case LEATHER -> 30;
            case WOOL -> 25;
            case TOOLS -> 150;
            case ARMOR -> 200;
            case ENCHANTED -> 500;
            case RARE -> 1000;
        };
    }
}
