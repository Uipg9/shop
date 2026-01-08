package com.shopmod.perks;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages perks and temporary boosters
 */
public class PerkManager {
    private static final Map<UUID, PlayerPerks> playerPerks = new ConcurrentHashMap<>();
    
    public static PlayerPerks getPerks(UUID playerId) {
        return playerPerks.computeIfAbsent(playerId, id -> new PlayerPerks());
    }
    
    public static PlayerPerks getPerks(ServerPlayer player) {
        return getPerks(player.getUUID());
    }
    
    public static class PlayerPerks {
        // Permanent perks
        private final Set<PermanentPerk> ownedPerks = new HashSet<>();
        
        // Active boosters (type -> expiry time)
        private final Map<TemporaryBooster, Long> activeBoosters = new HashMap<>();
        
        public boolean hasPerk(PermanentPerk perk) {
            return ownedPerks.contains(perk);
        }
        
        public void unlockPerk(PermanentPerk perk) {
            ownedPerks.add(perk);
        }
        
        public Set<PermanentPerk> getOwnedPerks() {
            return new HashSet<>(ownedPerks);
        }
        
        public void activateBooster(TemporaryBooster booster) {
            long duration = booster.getDurationMinutes() * 60 * 1000; // Convert to ms
            activeBoosters.put(booster, System.currentTimeMillis() + duration);
        }
        
        public boolean hasActiveBooster(TemporaryBooster booster) {
            Long expiry = activeBoosters.get(booster);
            if (expiry == null) return false;
            
            if (System.currentTimeMillis() > expiry) {
                activeBoosters.remove(booster);
                return false;
            }
            return true;
        }
        
        public long getBoosterTimeRemaining(TemporaryBooster booster) {
            Long expiry = activeBoosters.get(booster);
            if (expiry == null) return 0;
            
            long remaining = expiry - System.currentTimeMillis();
            return Math.max(0, remaining / 1000); // Return seconds
        }
        
        public Map<TemporaryBooster, Long> getActiveBoosters() {
            // Clean expired boosters
            activeBoosters.entrySet().removeIf(entry -> 
                System.currentTimeMillis() > entry.getValue());
            return new HashMap<>(activeBoosters);
        }
        
        // Calculate multipliers
        public double getIncomeMultiplier() {
            double multiplier = 1.0;
            
            if (hasPerk(PermanentPerk.GOLDEN_TOUCH)) multiplier += 0.05;
            if (hasActiveBooster(TemporaryBooster.INCOME_BOOSTER)) multiplier *= 2.0;
            
            // Double Down makes boosters 2x effective
            if (hasPerk(PermanentPerk.DOUBLE_DOWN) && hasActiveBooster(TemporaryBooster.INCOME_BOOSTER)) {
                multiplier *= 1.5; // Extra 50% on top of the 2x
            }
            
            return multiplier;
        }
        
        public double getXpMultiplier() {
            double multiplier = 1.0;
            
            if (hasPerk(PermanentPerk.MENTOR)) multiplier += 0.10;
            if (hasActiveBooster(TemporaryBooster.XP_BOOSTER)) multiplier *= 2.0;
            
            if (hasPerk(PermanentPerk.DOUBLE_DOWN) && hasActiveBooster(TemporaryBooster.XP_BOOSTER)) {
                multiplier *= 1.5;
            }
            
            return multiplier;
        }
        
        public double getLuckMultiplier() {
            double multiplier = 1.0;
            
            if (hasPerk(PermanentPerk.LUCKY_CHARM)) multiplier += 0.05;
            if (hasActiveBooster(TemporaryBooster.LUCK_BOOSTER)) multiplier += 0.10;
            
            return multiplier;
        }
        
        public double getCooldownReduction() {
            if (hasPerk(PermanentPerk.TIME_MASTER)) return 0.20; // 20% reduction
            return 0.0;
        }
        
        public double getPriceDiscount() {
            if (hasPerk(PermanentPerk.NEGOTIATOR)) return 0.10; // 10% discount
            return 0.0;
        }
        
        public double getWorkerEfficiencyBonus() {
            double bonus = 0.0;
            
            if (hasActiveBooster(TemporaryBooster.EFFICIENCY_BOOSTER)) bonus += 0.50;
            if (hasActiveBooster(TemporaryBooster.SPEED_BOOSTER)) bonus += 0.25;
            
            return bonus;
        }
        
        public boolean hasVipStatus() {
            return hasPerk(PermanentPerk.VIP_STATUS);
        }
    }
    
    public enum TemporaryBooster {
        INCOME_BOOSTER("Income Booster", 50000, 60, "2x income for 1 hour"),
        XP_BOOSTER("XP Booster", 30000, 60, "2x job XP for 1 hour"),
        LUCK_BOOSTER("Luck Booster", 75000, 30, "Better odds for 30 min"),
        SPEED_BOOSTER("Speed Booster", 40000, 60, "Faster actions for 1 hour"),
        EFFICIENCY_BOOSTER("Efficiency Booster", 100000, 60, "Workers +50% for 1 hour");
        
        private final String name;
        private final long price;
        private final int durationMinutes;
        private final String description;
        
        TemporaryBooster(String name, long price, int durationMinutes, String description) {
            this.name = name;
            this.price = price;
            this.durationMinutes = durationMinutes;
            this.description = description;
        }
        
        public String getName() { return name; }
        public long getPrice() { return price; }
        public int getDurationMinutes() { return durationMinutes; }
        public String getDescription() { return description; }
    }
    
    public enum PermanentPerk {
        GOLDEN_TOUCH("Golden Touch", 5000000, "Permanent +5% all income"),
        MENTOR("Mentor", 3000000, "Permanent +10% job XP"),
        LUCKY_CHARM("Lucky Charm", 10000000, "Permanent +5% better game odds"),
        TIME_MASTER("Time Master", 7000000, "Cooldowns -20%"),
        NEGOTIATOR("Negotiator", 4000000, "Better prices everywhere (-10%)"),
        VIP_STATUS("VIP Status", 15000000, "Access to exclusive features"),
        DOUBLE_DOWN("Double Down", 20000000, "All boosters 2x effective");
        
        private final String name;
        private final long price;
        private final String description;
        
        PermanentPerk(String name, long price, String description) {
            this.name = name;
            this.price = price;
            this.description = description;
        }
        
        public String getName() { return name; }
        public long getPrice() { return price; }
        public String getDescription() { return description; }
    }
    
    /**
     * Purchase a temporary booster
     */
    public static boolean purchaseBooster(ServerPlayer player, TemporaryBooster booster) {
        PlayerPerks perks = getPerks(player);
        
        if (perks.hasActiveBooster(booster)) {
            player.sendSystemMessage(Component.literal("§cYou already have this booster active!"));
            return false;
        }
        
        if (!CurrencyManager.canAfford(player, booster.getPrice())) {
            player.sendSystemMessage(Component.literal("§cInsufficient funds! Need: §6" + 
                CurrencyManager.format(booster.getPrice())));
            return false;
        }
        
        CurrencyManager.removeMoney(player, booster.getPrice());
        perks.activateBooster(booster);
        
        player.sendSystemMessage(Component.literal("§a§l✓ Booster Activated!"));
        player.sendSystemMessage(Component.literal("§e" + booster.getName()));
        player.sendSystemMessage(Component.literal("§7Duration: §a" + booster.getDurationMinutes() + " minutes"));
        player.sendSystemMessage(Component.literal("§7" + booster.getDescription()));
        
        return true;
    }
    
    /**
     * Purchase a permanent perk
     */
    public static boolean purchasePerk(ServerPlayer player, PermanentPerk perk) {
        PlayerPerks perks = getPerks(player);
        
        if (perks.hasPerk(perk)) {
            player.sendSystemMessage(Component.literal("§cYou already own this perk!"));
            return false;
        }
        
        if (!CurrencyManager.canAfford(player, perk.getPrice())) {
            player.sendSystemMessage(Component.literal("§cInsufficient funds! Need: §6" + 
                CurrencyManager.format(perk.getPrice())));
            return false;
        }
        
        CurrencyManager.removeMoney(player, perk.getPrice());
        perks.unlockPerk(perk);
        
        player.sendSystemMessage(Component.literal("§a§l✓ Perk Unlocked!"));
        player.sendSystemMessage(Component.literal("§d" + perk.getName()));
        player.sendSystemMessage(Component.literal("§7" + perk.getDescription()));
        
        return true;
    }
}
