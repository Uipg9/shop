package com.shopmod.business;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business Investment System
 * Purchase and manage businesses for passive daily income
 */
public class BusinessManager {
    private static final Map<UUID, List<Business>> playerBusinesses = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    
    public enum BusinessType {
        RESTAURANT("Restaurant", 500000, 5000),
        CASINO("Casino", 2000000, 20000),
        STOCK_BROKERAGE("Stock Brokerage", 1000000, 10000),
        INSURANCE_AGENCY("Insurance Agency", 750000, 8000),
        BANK_BRANCH("Bank Branch", 1500000, 15000),
        MINING_COMPANY("Mining Company", 1000000, 12000),
        FARM_CONGLOMERATE("Farm Conglomerate", 800000, 9000);
        
        private final String displayName;
        private final long purchaseCost;
        private final long baseDailyIncome;
        
        BusinessType(String displayName, long purchaseCost, long baseDailyIncome) {
            this.displayName = displayName;
            this.purchaseCost = purchaseCost;
            this.baseDailyIncome = baseDailyIncome;
        }
        
        public String getDisplayName() { return displayName; }
        public long getPurchaseCost() { return purchaseCost; }
        public long getBaseDailyIncome() { return baseDailyIncome; }
        
        public long getUpgradeCost(int currentLevel) {
            return (long)(purchaseCost * 2 * Math.pow(2, currentLevel - 1));
        }
        
        public long getDailyIncome(int level) {
            return (long)(baseDailyIncome * Math.pow(1.5, level - 1));
        }
    }
    
    public static class Business {
        private final UUID businessId;
        private final BusinessType type;
        private int level;
        private final long purchaseDate;
        private long lastCollection;
        private boolean active;
        private long totalEarned;
        
        public Business(BusinessType type, long purchaseDate) {
            this.businessId = UUID.randomUUID();
            this.type = type;
            this.level = 1;
            this.purchaseDate = purchaseDate;
            this.lastCollection = purchaseDate;
            this.active = true;
            this.totalEarned = 0;
        }
        
        public UUID getBusinessId() { return businessId; }
        public BusinessType getType() { return type; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public long getPurchaseDate() { return purchaseDate; }
        public long getLastCollection() { return lastCollection; }
        public void setLastCollection(long day) { this.lastCollection = day; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public long getTotalEarned() { return totalEarned; }
        public void addEarned(long amount) { this.totalEarned += amount; }
        
        public long getUncollectedIncome(long currentDay) {
            if (!active) return 0;
            long daysSinceCollection = Math.min(currentDay - lastCollection, 7); // Max 7 days stored
            return type.getDailyIncome(level) * daysSinceCollection;
        }
        
        public long getCurrentValue() {
            // Depreciation: businesses sell for 60% of total investment
            long totalInvestment = type.getPurchaseCost();
            for (int i = 1; i < level; i++) {
                totalInvestment += type.getUpgradeCost(i);
            }
            return (long)(totalInvestment * 0.6);
        }
    }
    
    /**
     * Get player's businesses
     */
    public static List<Business> getPlayerBusinesses(UUID playerUUID) {
        return playerBusinesses.computeIfAbsent(playerUUID, k -> new ArrayList<>());
    }
    
    /**
     * Buy a new business
     */
    public static boolean buyBusiness(ServerPlayer player, BusinessType type) {
        List<Business> businesses = getPlayerBusinesses(player.getUUID());
        
        // Check if already own this type
        if (businesses.stream().anyMatch(b -> b.getType() == type)) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] You already own a " + type.getDisplayName() + "!"));
            return false;
        }
        
        // Check funds
        long cost = type.getPurchaseCost();
        if (!CurrencyManager.canAfford(player, cost)) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Insufficient funds! Need " + CurrencyManager.format(cost)));
            return false;
        }
        
        // Purchase
        CurrencyManager.removeMoney(player, cost);
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        Business business = new Business(type, currentDay);
        businesses.add(business);
        
        player.sendSystemMessage(Component.literal("§a§l[BUSINESS] Purchased " + type.getDisplayName() + " for " + CurrencyManager.format(cost) + "!"));
        player.sendSystemMessage(Component.literal("§7Daily Income: §a+" + CurrencyManager.format(type.getBaseDailyIncome())));
        
        return true;
    }
    
    /**
     * Sell a business
     */
    public static boolean sellBusiness(ServerPlayer player, UUID businessId) {
        List<Business> businesses = getPlayerBusinesses(player.getUUID());
        
        Business business = businesses.stream()
            .filter(b -> b.getBusinessId().equals(businessId))
            .findFirst()
            .orElse(null);
        
        if (business == null) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Business not found!"));
            return false;
        }
        
        // Collect any uncollected income first
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        long uncollected = business.getUncollectedIncome(currentDay);
        if (uncollected > 0) {
            CurrencyManager.addMoney(player, uncollected);
            business.addEarned(uncollected);
        }
        
        // Sell for 60% of value
        long saleValue = business.getCurrentValue();
        CurrencyManager.addMoney(player, saleValue);
        
        businesses.remove(business);
        
        player.sendSystemMessage(Component.literal("§e§l[BUSINESS] Sold " + business.getType().getDisplayName() + " for " + CurrencyManager.format(saleValue)));
        if (uncollected > 0) {
            player.sendSystemMessage(Component.literal("§7Collected uncollected income: §a+" + CurrencyManager.format(uncollected)));
        }
        
        return true;
    }
    
    /**
     * Upgrade a business
     */
    public static boolean upgradeBusiness(ServerPlayer player, UUID businessId) {
        List<Business> businesses = getPlayerBusinesses(player.getUUID());
        
        Business business = businesses.stream()
            .filter(b -> b.getBusinessId().equals(businessId))
            .findFirst()
            .orElse(null);
        
        if (business == null) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Business not found!"));
            return false;
        }
        
        // Check max level
        if (business.getLevel() >= 5) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Already at maximum level (5)!"));
            return false;
        }
        
        // Check funds
        long cost = business.getType().getUpgradeCost(business.getLevel());
        if (!CurrencyManager.canAfford(player, cost)) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Insufficient funds! Need " + CurrencyManager.format(cost)));
            return false;
        }
        
        // Upgrade
        CurrencyManager.removeMoney(player, cost);
        business.setLevel(business.getLevel() + 1);
        
        long newIncome = business.getType().getDailyIncome(business.getLevel());
        player.sendSystemMessage(Component.literal("§a§l[BUSINESS] Upgraded " + business.getType().getDisplayName() + " to Level " + business.getLevel() + "!"));
        player.sendSystemMessage(Component.literal("§7New Daily Income: §a+" + CurrencyManager.format(newIncome)));
        
        return true;
    }
    
    /**
     * Collect income from a specific business
     */
    public static boolean collectIncome(ServerPlayer player, UUID businessId) {
        List<Business> businesses = getPlayerBusinesses(player.getUUID());
        
        Business business = businesses.stream()
            .filter(b -> b.getBusinessId().equals(businessId))
            .findFirst()
            .orElse(null);
        
        if (business == null) {
            player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Business not found!"));
            return false;
        }
        
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        long income = business.getUncollectedIncome(currentDay);
        
        if (income <= 0) {
            player.sendSystemMessage(Component.literal("§e§l[BUSINESS] No income to collect yet!"));
            return false;
        }
        
        // Apply synergy bonus
        double synergyBonus = getSynergyBonus(player.getUUID());
        if (synergyBonus > 0) {
            income = (long)(income * (1.0 + synergyBonus));
        }
        
        CurrencyManager.addMoney(player, income);
        business.setLastCollection(currentDay);
        business.addEarned(income);
        
        player.sendSystemMessage(Component.literal("§a§l[BUSINESS] Collected " + CurrencyManager.format(income) + " from " + business.getType().getDisplayName() + "!"));
        if (synergyBonus > 0) {
            player.sendSystemMessage(Component.literal("§7(+" + (int)(synergyBonus * 100) + "% synergy bonus applied)"));
        }
        
        return true;
    }
    
    /**
     * Get total daily income from all businesses
     */
    public static long getDailyIncome(UUID playerUUID) {
        List<Business> businesses = getPlayerBusinesses(playerUUID);
        long total = 0;
        
        for (Business business : businesses) {
            if (business.isActive()) {
                total += business.getType().getDailyIncome(business.getLevel());
            }
        }
        
        // Apply synergy bonus
        double synergyBonus = getSynergyBonus(playerUUID);
        if (synergyBonus > 0) {
            total = (long)(total * (1.0 + synergyBonus));
        }
        
        return total;
    }
    
    /**
     * Calculate synergy bonus based on number of business types owned
     */
    private static double getSynergyBonus(UUID playerUUID) {
        List<Business> businesses = getPlayerBusinesses(playerUUID);
        Set<BusinessType> uniqueTypes = new HashSet<>();
        
        for (Business business : businesses) {
            uniqueTypes.add(business.getType());
        }
        
        int count = uniqueTypes.size();
        
        if (count >= 7) {
            return 1.0; // 100% bonus - all types
        } else if (count >= 5) {
            return 0.5; // 50% bonus
        } else if (count >= 3) {
            return 0.2; // 20% bonus
        }
        
        return 0.0; // No bonus
    }
    
    /**
     * Process daily income for all players (called by server tick)
     */
    public static void processDailyIncome(long currentDay, net.minecraft.server.MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> {
            List<Business> businesses = getPlayerBusinesses(player.getUUID());
            long totalIncome = 0;
            
            for (Business business : businesses) {
                if (business.isActive()) {
                    long income = business.getType().getDailyIncome(business.getLevel());
                    business.setLastCollection(currentDay);
                    business.addEarned(income);
                    totalIncome += income;
                }
            }
            
            if (totalIncome > 0) {
                // Apply synergy bonus
                double synergyBonus = getSynergyBonus(player.getUUID());
                if (synergyBonus > 0) {
                    totalIncome = (long)(totalIncome * (1.0 + synergyBonus));
                }
                
                CurrencyManager.addMoney(player, totalIncome);
                player.sendSystemMessage(Component.literal("§6§l[BUSINESS] §aYour businesses earned §6" + CurrencyManager.format(totalIncome) + "!"));
                
                if (synergyBonus > 0) {
                    player.sendSystemMessage(Component.literal("§7(+" + (int)(synergyBonus * 100) + "% synergy bonus)"));
                }
            }
        });
    }
    
    /**
     * Get synergy bonus percentage for display
     */
    public static int getSynergyBonusPercent(UUID playerUUID) {
        return (int)(getSynergyBonus(playerUUID) * 100);
    }
}
