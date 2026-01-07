package com.shopmod.property;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.research.ResearchManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Property/Real Estate management system
 * Buy properties for passive daily income
 */
public class PropertyManager {
    private static final Map<UUID, PlayerProperties> playerProperties = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    
    // Digital villager names for renters
    private static final String[] VILLAGER_NAMES = {
        "Bob", "Alice", "Charlie", "Diana", "Edward", "Fiona",
        "George", "Hannah", "Isaac", "Julia", "Kevin", "Laura",
        "Michael", "Nancy", "Oliver", "Patricia", "Quinn", "Rachel",
        "Samuel", "Teresa", "Ulysses", "Veronica", "Walter", "Xena"
    };
    
    public static class PlayerProperties {
        private final Map<PropertyType, PropertyData> properties = new HashMap<>();
        private int propertyLevel = 1; // Unlocks higher tier properties
        private long lastProcessedDay = -1;
        
        public Map<PropertyType, PropertyData> getProperties() { return properties; }
        public int getPropertyLevel() { return propertyLevel; }
        public void setPropertyLevel(int level) { this.propertyLevel = level; }
        public long getLastProcessedDay() { return lastProcessedDay; }
        public void setLastProcessedDay(long day) { this.lastProcessedDay = day; }
        
        public long getTotalDailyIncome() {
            return properties.values().stream()
                .mapToLong(prop -> prop.getPropertyType().getDailyIncome() * prop.getQuantity())
                .sum();
        }
        
        public int getTotalProperties() {
            return properties.values().stream()
                .mapToInt(PropertyData::getQuantity)
                .sum();
        }
    }
    
    public static class PropertyData {
        private final PropertyType propertyType;
        private int quantity;
        private long totalEarned;
        private boolean isRented; // Rented out to digital villagers
        private String renterName; // Name of digital villager
        private int daysUntilRepair; // Days until repair is needed
        
        public PropertyData(PropertyType type) {
            this.propertyType = type;
            this.quantity = 1;
            this.totalEarned = 0;
            this.isRented = false;
            this.renterName = "";
            this.daysUntilRepair = -1;
        }
        
        public PropertyType getPropertyType() { return propertyType; }
        public int getQuantity() { return quantity; }
        public void addQuantity(int amount) { this.quantity += amount; }
        public long getTotalEarned() { return totalEarned; }
        public void addEarnings(long amount) { this.totalEarned += amount; }
        public boolean isRented() { return isRented; }
        public void setRented(boolean rented) { this.isRented = rented; }
        public String getRenterName() { return renterName; }
        public void setRenterName(String name) { this.renterName = name; }
        public int getDaysUntilRepair() { return daysUntilRepair; }
        public void setDaysUntilRepair(int days) { this.daysUntilRepair = days; }
    }
    
    /**
     * Get player's properties
     */
    public static PlayerProperties getPlayerProperties(UUID playerUUID) {
        return playerProperties.computeIfAbsent(playerUUID, k -> new PlayerProperties());
    }
    
    /**
     * Purchase property
     */
    public static boolean purchaseProperty(ServerPlayer player, PropertyType propertyType, int quantity) {
        if (quantity <= 0) return false;
        
        PlayerProperties props = getPlayerProperties(player.getUUID());
        
        // Check level requirement
        if (propertyType.getRequiredLevel() > props.getPropertyLevel()) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] Requires level " + propertyType.getRequiredLevel() + "! Current: " + props.getPropertyLevel()));
            player.sendSystemMessage(Component.literal(
                "§7Upgrade your property level with /property upgrade"));
            return false;
        }
        
        // Calculate cost
        long totalCost = propertyType.getPurchaseCost() * quantity;
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, totalCost)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] Insufficient funds! Need: " + CurrencyManager.format(totalCost)));
            return false;
        }
        
        // Deduct money
        CurrencyManager.removeMoney(player, totalCost);
        
        // Add property
        PropertyData data = props.getProperties().computeIfAbsent(propertyType, PropertyData::new);
        if (data.getQuantity() > 0) {
            data.addQuantity(quantity);
        } else {
            data.quantity = quantity;
        }
        
        player.sendSystemMessage(Component.literal(
            "§a§l[PROPERTY] Purchased " + quantity + "x " + propertyType.getDisplayName() + "!"));
        player.sendSystemMessage(Component.literal(
            "§7Daily income: §6+" + CurrencyManager.format(propertyType.getDailyIncome() * quantity) + "/day"));
        player.sendSystemMessage(Component.literal(
            "§7Total daily income: §6+" + CurrencyManager.format(props.getTotalDailyIncome()) + "/day"));
        
        return true;
    }
    
    /**
     * Upgrade property level
     */
    public static boolean upgradePropertyLevel(ServerPlayer player) {
        PlayerProperties props = getPlayerProperties(player.getUUID());
        int currentLevel = props.getPropertyLevel();
        
        if (currentLevel >= 5) {
            player.sendSystemMessage(Component.literal("§c§l[PROPERTY] Maximum level reached!"));
            return false;
        }
        
        // Cost increases exponentially: $50k, $200k, $500k, $1M
        long[] costs = {50000L, 200000L, 500000L, 1000000L, 5000000L};
        long upgradeCost = costs[currentLevel - 1];
        
        if (!CurrencyManager.canAfford(player, upgradeCost)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] Insufficient funds! Need: " + CurrencyManager.format(upgradeCost)));
            return false;
        }
        
        CurrencyManager.removeMoney(player, upgradeCost);
        props.setPropertyLevel(currentLevel + 1);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[PROPERTY] Level upgraded to " + props.getPropertyLevel() + "!"));
        player.sendSystemMessage(Component.literal(
            "§7New properties unlocked! Use /property to browse"));
        
        return true;
    }
    
    /**
     * Process daily property income (called from server tick with server instance)
     */
    public static void processDailyIncome(long currentDay, net.minecraft.server.MinecraftServer server) {
        for (Map.Entry<UUID, PlayerProperties> entry : playerProperties.entrySet()) {
            UUID playerUUID = entry.getKey();
            PlayerProperties props = entry.getValue();
            
            // Skip if already processed today
            if (props.getLastProcessedDay() >= currentDay) continue;
            
            // Calculate total income with research bonuses and rental bonuses
            long totalIncome = 0;
            
            for (PropertyData propData : props.getProperties().values()) {
                long baseIncome = propData.getPropertyType().getDailyIncome() * propData.getQuantity();
                
                // If rented, +50% income but check for repairs
                if (propData.isRented()) {
                    baseIncome = (long)(baseIncome * 1.5); // +50% rent bonus
                    
                    // Check repair countdown
                    int daysLeft = propData.getDaysUntilRepair();
                    if (daysLeft > 0) {
                        propData.setDaysUntilRepair(daysLeft - 1);
                    } else if (daysLeft == 0) {
                        // Repair needed!
                        long repairCost = (long)(propData.getPropertyType().getPurchaseCost() * 0.15);
                        
                        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                        if (player != null) {
                            if (CurrencyManager.canAfford(player, repairCost)) {
                                CurrencyManager.removeMoney(player, repairCost);
                                player.sendSystemMessage(Component.literal(
                                    "§c§l[PROPERTY] Repair bill: -" + CurrencyManager.format(repairCost)));
                                player.sendSystemMessage(Component.literal(
                                    "§7" + propData.getRenterName() + " caused damage to your " + 
                                    propData.getPropertyType().getDisplayName()));
                            } else {
                                // Can't afford - evict renter
                                player.sendSystemMessage(Component.literal(
                                    "§c§l[PROPERTY] Can't afford repairs! " + propData.getRenterName() + " evicted."));
                                propData.setRented(false);
                                propData.setRenterName("");
                                propData.setDaysUntilRepair(-1);
                            }
                        }
                        
                        // Reset countdown
                        propData.setDaysUntilRepair(7 + RANDOM.nextInt(8));
                    }
                }
                
                // Apply research multiplier
                double multiplier = ResearchManager.getPropertyIncomeMultiplier(playerUUID);
                long finalIncome = (long)(baseIncome * multiplier);
                
                totalIncome += finalIncome;
                propData.addEarnings(finalIncome);
            }
            
            if (totalIncome > 0) {
                // Find player
                if (server != null) {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    if (player != null) {
                        // Award income
                        CurrencyManager.addMoney(player, totalIncome);
                        
                        player.sendSystemMessage(Component.literal(
                            "§e§l[PROPERTY] Daily income: §6+" + CurrencyManager.format(totalIncome)));
                    }
                }
            }
            
            props.setLastProcessedDay(currentDay);
        }
    }
    
    /**
     * Rent out property to digital villager
     */
    public static boolean rentOutProperty(ServerPlayer player, PropertyType propertyType) {
        PlayerProperties props = getPlayerProperties(player.getUUID());
        PropertyData data = props.getProperties().get(propertyType);
        
        if (data == null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] You don't own this property!"));
            return false;
        }
        
        if (data.isRented()) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] This property is already rented!"));
            return false;
        }
        
        // Generate random villager name
        String renterName = VILLAGER_NAMES[RANDOM.nextInt(VILLAGER_NAMES.length)];
        data.setRented(true);
        data.setRenterName(renterName);
        
        // Set repair countdown (7-14 days)
        data.setDaysUntilRepair(7 + RANDOM.nextInt(8));
        
        player.sendSystemMessage(Component.literal(
            "§a§l[PROPERTY] Rented out to " + renterName + "!"));
        player.sendSystemMessage(Component.literal(
            "§7They will pay §6+50% rent §7but property may need repairs."));
        
        return true;
    }
    
    /**
     * Evict renter from property
     */
    public static boolean evictRenter(ServerPlayer player, PropertyType propertyType) {
        PlayerProperties props = getPlayerProperties(player.getUUID());
        PropertyData data = props.getProperties().get(propertyType);
        
        if (data == null || !data.isRented()) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] No renter to evict!"));
            return false;
        }
        
        String oldRenter = data.getRenterName();
        data.setRented(false);
        data.setRenterName("");
        data.setDaysUntilRepair(-1);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[PROPERTY] Evicted " + oldRenter + "!"));
        player.sendSystemMessage(Component.literal(
            "§7Property income back to normal."));
        
        return true;
    }
    
    /**
     * Sell property
     */
    public static boolean sellProperty(ServerPlayer player, PropertyType propertyType, int quantity) {
        PlayerProperties props = getPlayerProperties(player.getUUID());
        PropertyData data = props.getProperties().get(propertyType);
        
        if (data == null || data.getQuantity() < quantity) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PROPERTY] You don't own enough of this property!"));
            return false;
        }
        
        // Sell for 70% of purchase price
        long sellPrice = (long)(propertyType.getPurchaseCost() * 0.7 * quantity);
        
        data.addQuantity(-quantity);
        if (data.getQuantity() <= 0) {
            props.getProperties().remove(propertyType);
        }
        
        CurrencyManager.addMoney(player, sellPrice);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[PROPERTY] Sold " + quantity + "x " + propertyType.getDisplayName() + 
            " for §6" + CurrencyManager.format(sellPrice)));
        
        return true;
    }
}
