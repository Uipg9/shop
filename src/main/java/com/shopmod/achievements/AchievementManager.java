package com.shopmod.achievements;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all achievements and player progress
 */
public class AchievementManager {
    private static final Map<UUID, AchievementProgress> playerProgress = new ConcurrentHashMap<>();
    private static final Map<String, Achievement> achievements = new LinkedHashMap<>();
    
    static {
        registerAllAchievements();
    }
    
    /**
     * Get or create progress for a player
     */
    public static AchievementProgress getProgress(ServerPlayer player) {
        return playerProgress.computeIfAbsent(player.getUUID(), id -> new AchievementProgress());
    }
    
    /**
     * Get progress by UUID
     */
    public static AchievementProgress getProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, id -> new AchievementProgress());
    }
    
    /**
     * Register all achievements
     */
    private static void registerAllAchievements() {
        // WEALTH ACHIEVEMENTS
        register(new Achievement(
            "first_10k", "First Steps", "Reach $10,000 balance",
            AchievementCategory.WEALTH,
            new AchievementRequirement(AchievementRequirement.RequirementType.BALANCE_REACHED, 10000,
                p -> p.getHighestBalance()),
            5000, "Starter Badge", new ItemStack(Items.GOLD_NUGGET)
        ));
        
        register(new Achievement(
            "fifty_k", "Growing Wealth", "Reach $50,000 balance",
            AchievementCategory.WEALTH,
            new AchievementRequirement(AchievementRequirement.RequirementType.BALANCE_REACHED, 50000,
                p -> p.getHighestBalance()),
            10000, "Bronze Badge", new ItemStack(Items.GOLD_INGOT)
        ));
        
        register(new Achievement(
            "hundred_k", "Six Figures", "Reach $100,000 balance",
            AchievementCategory.WEALTH,
            new AchievementRequirement(AchievementRequirement.RequirementType.BALANCE_REACHED, 100000,
                p -> p.getHighestBalance()),
            15000, "Silver Badge", new ItemStack(Items.IRON_BLOCK)
        ));
        
        register(new Achievement(
            "millionaire", "Millionaire", "Reach $1,000,000 balance",
            AchievementCategory.WEALTH,
            new AchievementRequirement(AchievementRequirement.RequirementType.BALANCE_REACHED, 1000000,
                p -> p.getHighestBalance()),
            50000, "+5% Income Bonus", new ItemStack(Items.GOLD_BLOCK)
        ));
        
        register(new Achievement(
            "multi_millionaire", "Multi-Millionaire", "Reach $10,000,000 balance",
            AchievementCategory.WEALTH,
            new AchievementRequirement(AchievementRequirement.RequirementType.BALANCE_REACHED, 10000000,
                p -> p.getHighestBalance()),
            100000, "+10% Income Bonus", new ItemStack(Items.DIAMOND_BLOCK)
        ));
        
        register(new Achievement(
            "billionaire", "Billionaire", "Reach $1,000,000,000 balance",
            AchievementCategory.WEALTH,
            new AchievementRequirement(AchievementRequirement.RequirementType.BALANCE_REACHED, 1000000000,
                p -> p.getHighestBalance()),
            500000, "Legendary Badge +20% Income", new ItemStack(Items.NETHERITE_BLOCK)
        ));
        
        // PROPERTY ACHIEVEMENTS
        register(new Achievement(
            "first_property", "Property Owner", "Buy your first property",
            AchievementCategory.PROPERTY,
            new AchievementRequirement(AchievementRequirement.RequirementType.PROPERTIES_OWNED, 1,
                p -> (long) p.getPropertiesOwnedLifetime()),
            5000, "Landlord Badge", new ItemStack(Items.OAK_DOOR)
        ));
        
        register(new Achievement(
            "property_portfolio", "Portfolio Manager", "Own 5 properties",
            AchievementCategory.PROPERTY,
            new AchievementRequirement(AchievementRequirement.RequirementType.PROPERTIES_OWNED, 5,
                p -> (long) p.getPropertiesOwned()),
            15000, "+5% Property Income", new ItemStack(Items.BRICK)
        ));
        
        register(new Achievement(
            "real_estate_empire", "Real Estate Empire", "Own all property types",
            AchievementCategory.PROPERTY,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 7,
                p -> (long) p.getCustomCounter("unique_property_types")),
            30000, "+10% Property Income", new ItemStack(Items.GRASS_BLOCK)
        ));
        
        register(new Achievement(
            "slum_lord", "Slum Lord", "Own 20+ properties",
            AchievementCategory.PROPERTY,
            new AchievementRequirement(AchievementRequirement.RequirementType.PROPERTIES_OWNED, 20,
                p -> (long) p.getPropertiesOwned()),
            50000, "+15% Property Income", new ItemStack(Items.EMERALD_BLOCK)
        ));
        
        // BUSINESS ACHIEVEMENTS
        register(new Achievement(
            "first_business", "Entrepreneur", "Start your first business",
            AchievementCategory.BUSINESS,
            new AchievementRequirement(AchievementRequirement.RequirementType.BUSINESSES_OWNED, 1,
                p -> (long) p.getBusinessesOwnedLifetime()),
            10000, "Business Badge", new ItemStack(Items.EMERALD)
        ));
        
        register(new Achievement(
            "business_owner", "Business Owner", "Own 3 businesses",
            AchievementCategory.BUSINESS,
            new AchievementRequirement(AchievementRequirement.RequirementType.BUSINESSES_OWNED, 3,
                p -> (long) p.getBusinessesOwned()),
            25000, "+5% Business Income", new ItemStack(Items.DIAMOND)
        ));
        
        register(new Achievement(
            "tycoon", "Tycoon", "Own all 7 business types",
            AchievementCategory.BUSINESS,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 7,
                p -> (long) p.getBusinessTypesOwned().size()),
            100000, "+20% Business Income", new ItemStack(Items.NETHERITE_INGOT)
        ));
        
        // JOB ACHIEVEMENTS
        register(new Achievement(
            "first_job", "First Job", "Complete your first job",
            AchievementCategory.JOBS,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 1,
                p -> p.getCustomCounter("jobs_completed")),
            3000, "Worker Badge", new ItemStack(Items.IRON_PICKAXE)
        ));
        
        register(new Achievement(
            "jack_of_all_trades", "Jack of All Trades", "Try all major features",
            AchievementCategory.JOBS,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 10,
                p -> p.getCustomCounter("features_tried")),
            20000, "+5% All Income", new ItemStack(Items.ENCHANTED_BOOK)
        ));
        
        // STOCK MARKET ACHIEVEMENTS
        register(new Achievement(
            "day_trader", "Day Trader", "Make 100 stock trades",
            AchievementCategory.STOCK_MARKET,
            new AchievementRequirement(AchievementRequirement.RequirementType.STOCK_TRADES, 100,
                p -> (long) p.getStockTradesMade()),
            25000, "Trader Badge", new ItemStack(Items.PAPER)
        ));
        
        register(new Achievement(
            "diamond_hands", "Diamond Hands", "Hold stock for 30+ days",
            AchievementCategory.STOCK_MARKET,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 30,
                p -> (long) p.getLongestHoldDays()),
            30000, "+10% Stock Gains", new ItemStack(Items.DIAMOND)
        ));
        
        register(new Achievement(
            "warren_buffet", "Warren Buffet", "Earn $5M total stock profit",
            AchievementCategory.STOCK_MARKET,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 5000000,
                p -> p.getStockProfitTotal()),
            100000, "Legendary Investor Badge", new ItemStack(Items.NETHER_STAR)
        ));
        
        // GAMING ACHIEVEMENTS
        register(new Achievement(
            "lucky_seven", "Lucky Seven", "Win 7 games in a row",
            AchievementCategory.GAMING,
            new AchievementRequirement(AchievementRequirement.RequirementType.CONSECUTIVE_WINS, 7,
                p -> (long) p.getConsecutiveWins()),
            20000, "+5% Game Odds", new ItemStack(Items.GOLD_INGOT)
        ));
        
        register(new Achievement(
            "blackjack_pro", "Blackjack Pro", "Get 10 blackjacks",
            AchievementCategory.GAMING,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 10,
                p -> (long) p.getBlackjackCount()),
            15000, "Blackjack Master Badge", new ItemStack(Items.HEART_OF_THE_SEA)
        ));
        
        register(new Achievement(
            "jackpot_winner", "Jackpot Winner", "Win any jackpot",
            AchievementCategory.GAMING,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 1,
                p -> (long) p.getJackpotWins()),
            50000, "Lucky Badge", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
        ));
        
        register(new Achievement(
            "gambler", "Gambler", "Play 100 games",
            AchievementCategory.GAMING,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 100,
                p -> (long) p.getGamesPlayed()),
            10000, "Gambler Badge", new ItemStack(Items.GOLD_BLOCK)
        ));
        
        // WORKER ACHIEVEMENTS
        register(new Achievement(
            "boss", "Boss", "Hire 5 workers",
            AchievementCategory.WORKER,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 5,
                p -> (long) p.getWorkersHiredLifetime()),
            15000, "+10% Worker Efficiency", new ItemStack(Items.IRON_SHOVEL)
        ));
        
        register(new Achievement(
            "ceo", "CEO", "Train 10 workers",
            AchievementCategory.WORKER,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 10,
                p -> (long) p.getWorkersTrained()),
            30000, "+20% Worker Efficiency", new ItemStack(Items.DIAMOND_SHOVEL)
        ));
        
        // LOTTERY ACHIEVEMENTS
        register(new Achievement(
            "lottery_winner", "Lucky Winner", "Win any lottery prize",
            AchievementCategory.LOTTERY,
            new AchievementRequirement(AchievementRequirement.RequirementType.LOTTERY_WON, 1,
                p -> (long) p.getLotteryWins()),
            10000, "Lottery Badge", new ItemStack(Items.GOLD_NUGGET)
        ));
        
        register(new Achievement(
            "jackpot_king", "Jackpot King", "Win the lottery jackpot",
            AchievementCategory.LOTTERY,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 1,
                p -> p.isLotteryJackpotWon() ? 1L : 0L),
            100000, "Ultimate Luck Badge", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
        ));
        
        // FARM & MINE ACHIEVEMENTS
        register(new Achievement(
            "farmer", "Farmer", "Own 10 farms",
            AchievementCategory.FARM_MINE,
            new AchievementRequirement(AchievementRequirement.RequirementType.FARMS_OWNED, 10,
                p -> (long) p.getFarmsOwned()),
            15000, "+10% Farm Income", new ItemStack(Items.WHEAT)
        ));
        
        register(new Achievement(
            "miner", "Miner", "Own 10 mines",
            AchievementCategory.FARM_MINE,
            new AchievementRequirement(AchievementRequirement.RequirementType.MINES_OWNED, 10,
                p -> (long) p.getMinesOwned()),
            15000, "+10% Mine Income", new ItemStack(Items.DIAMOND_PICKAXE)
        ));
        
        register(new Achievement(
            "industrialist", "Industrialist", "Own 20 farms and 20 mines",
            AchievementCategory.FARM_MINE,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 1,
                p -> (p.getFarmsOwned() >= 20 && p.getMinesOwned() >= 20) ? 1L : 0L),
            50000, "+20% Production", new ItemStack(Items.NETHERITE_PICKAXE)
        ));
        
        // MISC ACHIEVEMENTS
        register(new Achievement(
            "spender", "Big Spender", "Spend $5,000,000 total",
            AchievementCategory.MISC,
            new AchievementRequirement(AchievementRequirement.RequirementType.CUSTOM, 5000000,
                p -> p.getTotalSpent()),
            25000, "Spender Badge", new ItemStack(Items.GOLD_BLOCK)
        ));
        
        register(new Achievement(
            "earner", "Money Maker", "Earn $10,000,000 total",
            AchievementCategory.MISC,
            new AchievementRequirement(AchievementRequirement.RequirementType.MONEY_EARNED, 10000000,
                p -> p.getTotalEarned()),
            50000, "Earner Badge", new ItemStack(Items.EMERALD_BLOCK)
        ));
    }
    
    /**
     * Register an achievement
     */
    private static void register(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }
    
    /**
     * Get all achievements
     */
    public static Collection<Achievement> getAllAchievements() {
        return achievements.values();
    }
    
    /**
     * Get achievements by category
     */
    public static List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.getCategory() == category) {
                result.add(achievement);
            }
        }
        return result;
    }
    
    /**
     * Get an achievement by ID
     */
    public static Achievement getAchievement(String id) {
        return achievements.get(id);
    }
    
    /**
     * Check and unlock achievements for a player
     */
    public static void checkAchievements(ServerPlayer player) {
        AchievementProgress progress = getProgress(player);
        
        for (Achievement achievement : achievements.values()) {
            // Skip if already completed
            if (progress.isAchievementCompleted(achievement.getId())) {
                continue;
            }
            
            // Check if requirements are met
            if (achievement.isCompleted(progress)) {
                unlockAchievement(player, achievement);
            }
        }
    }
    
    /**
     * Unlock an achievement for a player
     */
    private static void unlockAchievement(ServerPlayer player, Achievement achievement) {
        AchievementProgress progress = getProgress(player);
        progress.completeAchievement(achievement.getId());
        
        // Award cash reward
        CurrencyManager.addMoney(player, achievement.getCashReward());
        
        // Play sound
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Send achievement notification
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l✦ ACHIEVEMENT UNLOCKED ✦"));
        player.sendSystemMessage(Component.literal("§e" + achievement.getName()));
        player.sendSystemMessage(Component.literal("§7" + achievement.getDescription()));
        player.sendSystemMessage(Component.literal("§aReward: §6" + CurrencyManager.format(achievement.getCashReward())));
        if (achievement.getBonusReward() != null && !achievement.getBonusReward().isEmpty()) {
            player.sendSystemMessage(Component.literal("§dBonus: §f" + achievement.getBonusReward()));
        }
        player.sendSystemMessage(Component.literal(""));
    }
    
    /**
     * Get unlocked count for a player
     */
    public static int getUnlockedCount(ServerPlayer player) {
        return getProgress(player).getCompletedAchievements().size();
    }
    
    /**
     * Get total achievement count
     */
    public static int getTotalCount() {
        return achievements.size();
    }
}
