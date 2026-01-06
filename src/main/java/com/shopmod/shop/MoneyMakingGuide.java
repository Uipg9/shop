package com.shopmod.shop;

import java.util.ArrayList;
import java.util.List;

/**
 * Money-making guide showing profitable strategies
 */
public class MoneyMakingGuide {
    
    public static class Strategy {
        public final String title;
        public final List<String> steps;
        public final long estimatedProfit;
        public final ShopTier requiredTier;
        
        public Strategy(String title, List<String> steps, long estimatedProfit, ShopTier requiredTier) {
            this.title = title;
            this.steps = steps;
            this.estimatedProfit = estimatedProfit;
            this.requiredTier = requiredTier;
        }
    }
    
    private static final List<Strategy> STRATEGIES = new ArrayList<>();
    
    static {
        // Starter strategies
        STRATEGIES.add(new Strategy(
            "§aBasic Farming",
            List.of(
                "§71. Buy Wheat Seeds ($2)",
                "§72. Plant and wait to grow",
                "§73. Sell Wheat ($5)",
                "§7Profit: §a$3 per wheat"
            ),
            3,
            ShopTier.STARTER
        ));
        
        STRATEGIES.add(new Strategy(
            "§aCarrot Farming",
            List.of(
                "§71. Buy 1 Carrot ($3)",
                "§72. Plant - grows 2-4 carrots",
                "§73. Sell back (avg 3 carrots @ $3 = $9)",
                "§7Profit: §a$6 per harvest"
            ),
            6,
            ShopTier.STARTER
        ));
        
        // Tier 1 strategies
        STRATEGIES.add(new Strategy(
            "§eMelon Farming",
            List.of(
                "§71. Buy Melon Seeds ($20)",
                "§72. Grow and harvest (9 slices)",
                "§73. Sell slices (9 × $3 = $27)",
                "§7Profit: §a$7 per melon"
            ),
            7,
            ShopTier.FARMER
        ));
        
        STRATEGIES.add(new Strategy(
            "§ePumpkin Farming",
            List.of(
                "§71. Buy Pumpkin Seeds ($15)",
                "§72. Grow and harvest",
                "§73. Sell Pumpkin ($25)",
                "§7Profit: §a$10 per pumpkin"
            ),
            10,
            ShopTier.FARMER
        ));
        
        STRATEGIES.add(new Strategy(
            "§eTree Farm",
            List.of(
                "§71. Buy Oak Sapling ($10)",
                "§72. Plant and wait to grow",
                "§73. Chop down (4-6 logs avg)",
                "§74. Sell logs (5 × $5 = $25)",
                "§7Profit: §a$15 per tree + saplings"
            ),
            15,
            ShopTier.FARMER
        ));
        
        // Tier 2 strategies
        STRATEGIES.add(new Strategy(
            "§cRedstone Mining to Iron",
            List.of(
                "§71. Mine naturally or buy Coal ($10)",
                "§72. Use as fuel for smelting",
                "§73. Buy Raw Iron ($35), smelt",
                "§74. Sell Iron Ingot ($50)",
                "§7Profit: §a$15 per ingot (minus fuel)"
            ),
            12,
            ShopTier.ENGINEER
        ));
        
        // Tier 3 strategies
        STRATEGIES.add(new Strategy(
            "§bIron Smelting",
            List.of(
                "§71. Buy Raw Iron ($35)",
                "§72. Smelt with coal/charcoal",
                "§73. Sell Iron Ingot ($50)",
                "§7Profit: §a$15 per ingot"
            ),
            15,
            ShopTier.MERCHANT
        ));
        
        STRATEGIES.add(new Strategy(
            "§bGold Smelting",
            List.of(
                "§71. Buy Raw Gold ($80)",
                "§72. Smelt with coal",
                "§73. Sell Gold Ingot ($100)",
                "§7Profit: §a$20 per ingot"
            ),
            20,
            ShopTier.MERCHANT
        ));
        
        STRATEGIES.add(new Strategy(
            "§bDiamond Mining",
            List.of(
                "§71. Mine diamonds naturally",
                "§72. Sell Diamond ($500 each)",
                "§7OR craft diamond tools/armor",
                "§7and sell at premium prices!"
            ),
            500,
            ShopTier.MERCHANT
        ));
        
        // Tier 4 strategies
        STRATEGIES.add(new Strategy(
            "§4Nether Wart Farming",
            List.of(
                "§71. Buy Nether Wart ($25)",
                "§72. Plant in soul sand",
                "§73. Harvest (2-4 per plant)",
                "§74. Sell back (avg 3 × $25 = $75)",
                "§7Profit: §a$50 per harvest"
            ),
            50,
            ShopTier.NETHER_MASTER
        ));
        
        STRATEGIES.add(new Strategy(
            "§4Potion Brewing",
            List.of(
                "§71. Buy ingredients from shop",
                "§72. Brew potions with effects",
                "§73. Sell to players!",
                "§7(Player-to-player trading)"
            ),
            0,
            ShopTier.NETHER_MASTER
        ));
        
        // Tier 5 strategies
        STRATEGIES.add(new Strategy(
            "§dEnd-Game Trading",
            List.of(
                "§71. Farm End Cities/Nether",
                "§72. Collect rare items naturally",
                "§73. Sell Elytra, Shulker Shells",
                "§7Huge profits from exploration!"
            ),
            15000,
            ShopTier.ELITE
        ));
        
        STRATEGIES.add(new Strategy(
            "§dNetherite Crafting",
            List.of(
                "§71. Buy Ancient Debris ($1,500)",
                "§72. Smelt to Netherite Scrap",
                "§73. Combine 4 scraps + 4 gold",
                "§74. Sell Netherite Ingot ($5,000)",
                "§7Profit: §a$500 per ingot (small)"
            ),
            500,
            ShopTier.ELITE
        ));
    }
    
    public static List<Strategy> getAllStrategies() {
        return new ArrayList<>(STRATEGIES);
    }
    
    public static List<Strategy> getStrategiesForTier(ShopTier tier) {
        List<Strategy> result = new ArrayList<>();
        for (Strategy strategy : STRATEGIES) {
            if (strategy.requiredTier == tier) {
                result.add(strategy);
            }
        }
        return result;
    }
    
    public static List<String> getGeneralTips() {
        return List.of(
            "§e⭐ General Tips:",
            "",
            "§71. Start with farming - low investment, steady profit",
            "§72. Save up to unlock Tier 1 ($2,000) quickly",
            "§73. Use profits to buy better seeds/materials",
            "§74. Automation helps! Use hoppers & farms",
            "§75. Mine naturally to get ores for free",
            "§76. Sell items back at 80% if you need quick cash",
            "§77. Save for higher tiers - better items = more profit",
            "",
            "§6💰 Starting Money: $1,000",
            "§aTier 0 is FREE - basics to get started",
            "§aTier 1: $2,000 - Farming expansion",
            "§aTier 2: $5,000 - Redstone & automation",
            "§aTier 3: $10,000 - Precious minerals",
            "§aTier 4: $25,000 - Nether items",
            "§aTier 5: $50,000 - Elite end-game items"
        );
    }
}
