package com.shopmod.research;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Research/Upgrade types for player progression
 */
public enum ResearchType {
    // Economic Research
    BETTER_LOANS("Better Loan Terms", 50000, 0, ResearchCategory.ECONOMIC,
        "§7Reduce loan interest by 10%", Items.PAPER),
    BULK_DISCOUNTS("Bulk Purchase Discounts", 100000, 1, ResearchCategory.ECONOMIC,
        "§75% discount when buying 64+ items", Items.GOLD_INGOT),
    MARKET_INSIGHT("Market Insight", 250000, 2, ResearchCategory.ECONOMIC,
        "§7See price trends before buying", Items.SPYGLASS),
    TAX_HAVEN("Tax Haven", 500000, 3, ResearchCategory.ECONOMIC,
        "§7Reduce all transaction fees by 50%", Items.WRITABLE_BOOK),
    INSIDER_TRADING("Insider Trading", 1000000, 4, ResearchCategory.ECONOMIC,
        "§7Stock options show guaranteed profit", Items.KNOWLEDGE_BOOK),
    
    // Property Research
    EFFICIENT_MANAGEMENT("Efficient Management", 75000, 0, ResearchCategory.PROPERTY,
        "§7+10% income from all properties", Items.BRICK),
    PROPERTY_INSURANCE("Property Insurance", 150000, 1, ResearchCategory.PROPERTY,
        "§7Properties never lose value", Items.SHIELD),
    REAL_ESTATE_MOGUL("Real Estate Mogul", 300000, 2, ResearchCategory.PROPERTY,
        "§7+25% income from all properties", Items.NETHER_BRICK),
    URBAN_PLANNER("Urban Planner", 600000, 3, ResearchCategory.PROPERTY,
        "§750% cheaper property upgrades", Items.MAP),
    MEGA_DEVELOPER("Mega Developer", 1200000, 4, ResearchCategory.PROPERTY,
        "§7+50% income from all properties", Items.END_CRYSTAL),
    
    // Farming Research
    BETTER_SEEDS("Better Seeds", 25000, 0, ResearchCategory.FARMING,
        "§7+20% crop farm production", Items.WHEAT_SEEDS),
    AUTOMATED_FARMING("Automated Farming", 80000, 1, ResearchCategory.FARMING,
        "§7+30% all farm production", Items.REDSTONE),
    INDUSTRIAL_AGRICULTURE("Industrial Agriculture", 200000, 2, ResearchCategory.FARMING,
        "§7+50% all farm production", Items.HOPPER),
    BIOENGINEERING("Bioengineering", 400000, 3, ResearchCategory.FARMING,
        "§7Farms produce 2x resources", Items.GLOW_BERRIES),
    MEGA_FARM("Mega Farm Complex", 800000, 4, ResearchCategory.FARMING,
        "§7Farms produce 3x resources", Items.HAY_BLOCK),
    
    // Village Research
    VILLAGE_TRADE_ROUTES("Village Trade Routes", 100000, 0, ResearchCategory.VILLAGE,
        "§7Caravans arrive 2x faster", Items.MINECART),
    MINING_EFFICIENCY("Mining Efficiency", 200000, 1, ResearchCategory.VILLAGE,
        "§7Mining ops produce +50% ores", Items.IRON_PICKAXE),
    LOGISTICS_NETWORK("Logistics Network", 350000, 2, ResearchCategory.VILLAGE,
        "§7All village operations +30%", Items.COMPASS),
    INDUSTRIAL_COMPLEX("Industrial Complex", 700000, 3, ResearchCategory.VILLAGE,
        "§7Unlock mega village buildings", Items.BLAST_FURNACE),
    METROPOLIS("Metropolis", 1500000, 4, ResearchCategory.VILLAGE,
        "§7Village income doubled", Items.BEACON),
    
    // Special Research
    LUCKY_CHARM("Lucky Charm", 300000, 0, ResearchCategory.SPECIAL,
        "§7+5% better loot from all sources", Items.RABBIT_FOOT),
    MERCHANT_NETWORK("Merchant Network", 500000, 1, ResearchCategory.SPECIAL,
        "§7Unlock special auction items", Items.ENDER_PEARL),
    BLACK_MARKET_ACCESS("Black Market Access", 750000, 2, ResearchCategory.SPECIAL,
        "§7Unlock /blackmarket command", Items.WITHER_SKELETON_SKULL),
    MONEY_PRINTER("Money Printer", 2000000, 3, ResearchCategory.SPECIAL,
        "§7Generate $1k passive daily income", Items.EMERALD_BLOCK),
    FINANCIAL_EMPIRE("Financial Empire", 5000000, 4, ResearchCategory.SPECIAL,
        "§7All income sources +100%", Items.NETHER_STAR);
    
    private final String displayName;
    private final long cost;
    private final int tier; // 0-4, must research lower tiers first
    private final ResearchCategory category;
    private final String description;
    private final Item icon;
    
    ResearchType(String displayName, long cost, int tier, ResearchCategory category, String description, Item icon) {
        this.displayName = displayName;
        this.cost = cost;
        this.tier = tier;
        this.category = category;
        this.description = description;
        this.icon = icon;
    }
    
    public String getDisplayName() { return displayName; }
    public long getCost() { return cost; }
    public int getTier() { return tier; }
    public ResearchCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public Item getIcon() { return icon; }
    
    public enum ResearchCategory {
        ECONOMIC("§6Economic", "§7Improve your trading power"),
        PROPERTY("§e Property", "§7Boost real estate income"),
        FARMING("§aFarming", "§7Enhance farm production"),
        VILLAGE("§bVillage", "§7Upgrade village systems"),
        SPECIAL("§5Special", "§7Unique bonuses");
        
        private final String displayName;
        private final String description;
        
        ResearchCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
