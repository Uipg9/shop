package com.shopmod.research;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * Manages research purchases and applies bonuses
 */
public class ResearchManager {
    private static final Map<UUID, Set<ResearchType>> playerResearch = new HashMap<>();
    
    /**
     * Check if player has researched something
     */
    public static boolean hasResearch(UUID playerId, ResearchType research) {
        return playerResearch.getOrDefault(playerId, new HashSet<>()).contains(research);
    }
    
    /**
     * Purchase research
     */
    public static boolean purchaseResearch(ServerPlayer player, ResearchType research) {
        UUID playerId = player.getUUID();
        
        // Check if already researched
        if (hasResearch(playerId, research)) {
            player.sendSystemMessage(Component.literal("§cYou already have this research!"));
            return false;
        }
        
        // Check tier requirements - must have all lower tier research in same category
        if (research.getTier() > 0) {
            boolean hasPrerequisites = true;
            for (ResearchType other : ResearchType.values()) {
                if (other.getCategory() == research.getCategory() && 
                    other.getTier() < research.getTier() && 
                    !hasResearch(playerId, other)) {
                    hasPrerequisites = false;
                    break;
                }
            }
            
            if (!hasPrerequisites) {
                player.sendSystemMessage(Component.literal("§cYou must research lower tier " + 
                    research.getCategory().getDisplayName() + " §ctech first!"));
                return false;
            }
        }
        
        // Check cost
        long balance = CurrencyManager.getBalance(player);
        if (balance < research.getCost()) {
            player.sendSystemMessage(Component.literal("§cNot enough money! Need " + 
                CurrencyManager.format(research.getCost())));
            return false;
        }
        
        // Purchase
        CurrencyManager.removeMoney(player, research.getCost());
        playerResearch.computeIfAbsent(playerId, k -> new HashSet<>()).add(research);
        
        player.sendSystemMessage(Component.literal("§a§lResearch Complete!"));
        player.sendSystemMessage(Component.literal("§7Unlocked: " + research.getDisplayName()));
        player.sendSystemMessage(Component.literal(research.getDescription()));
        
        return true;
    }
    
    /**
     * Get all research for a player
     */
    public static Set<ResearchType> getPlayerResearch(UUID playerId) {
        return new HashSet<>(playerResearch.getOrDefault(playerId, new HashSet<>()));
    }
    
    /**
     * Get loan interest multiplier (1.0 = normal, 0.9 = 10% off)
     */
    public static double getLoanInterestMultiplier(UUID playerId) {
        return hasResearch(playerId, ResearchType.BETTER_LOANS) ? 0.9 : 1.0;
    }
    
    /**
     * Get bulk discount (returns discount percentage)
     */
    public static double getBulkDiscount(UUID playerId, int quantity) {
        if (hasResearch(playerId, ResearchType.BULK_DISCOUNTS) && quantity >= 64) {
            return 0.05; // 5% off
        }
        return 0.0;
    }
    
    /**
     * Get property income multiplier
     */
    public static double getPropertyIncomeMultiplier(UUID playerId) {
        double multiplier = 1.0;
        if (hasResearch(playerId, ResearchType.EFFICIENT_MANAGEMENT)) multiplier += 0.10;
        if (hasResearch(playerId, ResearchType.REAL_ESTATE_MOGUL)) multiplier += 0.25;
        if (hasResearch(playerId, ResearchType.MEGA_DEVELOPER)) multiplier += 0.50;
        if (hasResearch(playerId, ResearchType.FINANCIAL_EMPIRE)) multiplier *= 2.0; // Doubles AFTER additions
        return multiplier;
    }
    
    /**
     * Get farm production multiplier
     */
    public static double getFarmProductionMultiplier(UUID playerId) {
        double multiplier = 1.0;
        if (hasResearch(playerId, ResearchType.BETTER_SEEDS)) multiplier += 0.20;
        if (hasResearch(playerId, ResearchType.AUTOMATED_FARMING)) multiplier += 0.30;
        if (hasResearch(playerId, ResearchType.INDUSTRIAL_AGRICULTURE)) multiplier += 0.50;
        if (hasResearch(playerId, ResearchType.BIOENGINEERING)) multiplier *= 2.0;
        if (hasResearch(playerId, ResearchType.MEGA_FARM)) multiplier *= 3.0;
        if (hasResearch(playerId, ResearchType.FINANCIAL_EMPIRE)) multiplier *= 2.0;
        return multiplier;
    }
    
    /**
     * Get property upgrade cost multiplier (0.5 = 50% off)
     */
    public static double getPropertyUpgradeCostMultiplier(UUID playerId) {
        return hasResearch(playerId, ResearchType.URBAN_PLANNER) ? 0.5 : 1.0;
    }
    
    /**
     * Get village production multiplier
     */
    public static double getVillageProductionMultiplier(UUID playerId) {
        double multiplier = 1.0;
        if (hasResearch(playerId, ResearchType.MINING_EFFICIENCY)) multiplier += 0.50;
        if (hasResearch(playerId, ResearchType.LOGISTICS_NETWORK)) multiplier += 0.30;
        if (hasResearch(playerId, ResearchType.METROPOLIS)) multiplier *= 2.0;
        if (hasResearch(playerId, ResearchType.FINANCIAL_EMPIRE)) multiplier *= 2.0;
        return multiplier;
    }
    
    /**
     * Get transaction fee multiplier (0.5 = 50% off)
     */
    public static double getTransactionFeeMultiplier(UUID playerId) {
        return hasResearch(playerId, ResearchType.TAX_HAVEN) ? 0.5 : 1.0;
    }
    
    /**
     * Has black market access
     */
    public static boolean hasBlackMarketAccess(UUID playerId) {
        return hasResearch(playerId, ResearchType.BLACK_MARKET_ACCESS);
    }
    
    /**
     * Get daily passive income from Money Printer
     */
    public static long getDailyPassiveIncome(UUID playerId) {
        return hasResearch(playerId, ResearchType.MONEY_PRINTER) ? 1000 : 0;
    }
    
    /**
     * Apply daily passive research income
     */
    public static void applyDailyIncome(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            long income = getDailyPassiveIncome(player.getUUID());
            if (income > 0) {
                CurrencyManager.addMoney(player, income);
                player.sendSystemMessage(Component.literal("§a§l[Money Printer] +" + 
                    CurrencyManager.format(income)));
            }
        }
    }
}
