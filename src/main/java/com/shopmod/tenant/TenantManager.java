package com.shopmod.tenant;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.property.PropertyManager;
import com.shopmod.property.PropertyType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tenant Management System - Track property renters and their relationships
 */
public class TenantManager {
    private static final Map<UUID, TenantData> playerTenants = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    // Tenant names pool
    private static final String[] FIRST_NAMES = {
        "John", "Emma", "Michael", "Sophia", "William", "Olivia", "James", "Ava",
        "Robert", "Isabella", "David", "Mia", "Richard", "Charlotte", "Joseph", "Amelia"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas"
    };
    
    public static class TenantInfo {
        private String name;
        private PropertyType propertyType;
        private long rentAmount;
        private int relationshipScore; // 0-100, affects events
        private long daysRented;
        private long totalPaidRent;
        private long lastEventDay;
        
        public TenantInfo(String name, PropertyType propertyType, long rentAmount) {
            this.name = name;
            this.propertyType = propertyType;
            this.rentAmount = rentAmount;
            this.relationshipScore = 50; // Start at neutral
            this.daysRented = 0;
            this.totalPaidRent = 0;
            this.lastEventDay = -1;
        }
        
        public String getName() { return name; }
        public PropertyType getPropertyType() { return propertyType; }
        public long getRentAmount() { return rentAmount; }
        public void setRentAmount(long amount) { this.rentAmount = amount; }
        public int getRelationshipScore() { return relationshipScore; }
        public void setRelationshipScore(int score) { this.relationshipScore = Math.max(0, Math.min(100, score)); }
        public void adjustRelationship(int amount) { setRelationshipScore(relationshipScore + amount); }
        public long getDaysRented() { return daysRented; }
        public void incrementDaysRented() { this.daysRented++; }
        public long getTotalPaidRent() { return totalPaidRent; }
        public void addPaidRent(long amount) { this.totalPaidRent += amount; }
        public long getLastEventDay() { return lastEventDay; }
        public void setLastEventDay(long day) { this.lastEventDay = day; }
    }
    
    public static class TenantData {
        private final Map<String, TenantInfo> tenants = new HashMap<>();
        private long lastProcessedDay = -1;
        
        public Map<String, TenantInfo> getTenants() { return tenants; }
        public void addTenant(String id, TenantInfo tenant) { tenants.put(id, tenant); }
        public void removeTenant(String id) { tenants.remove(id); }
        public TenantInfo getTenant(String id) { return tenants.get(id); }
        public long getLastProcessedDay() { return lastProcessedDay; }
        public void setLastProcessedDay(long day) { this.lastProcessedDay = day; }
    }
    
    public static TenantData getTenantData(UUID playerUUID) {
        return playerTenants.computeIfAbsent(playerUUID, k -> new TenantData());
    }
    
    /**
     * Generate a random tenant name
     */
    public static String generateTenantName() {
        String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String last = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return first + " " + last;
    }
    
    /**
     * Rent out a property to a tenant
     */
    public static boolean rentOutProperty(ServerPlayer player, PropertyType propertyType) {
        PropertyManager.PlayerProperties props = PropertyManager.getPlayerProperties(player.getUUID());
        PropertyManager.PropertyData propData = props.getProperties().get(propertyType);
        
        if (propData == null || propData.getQuantity() <= 0) {
            player.sendSystemMessage(Component.literal("§c§l[TENANT] You don't own this property type!"));
            return false;
        }
        
        if (propData.isRented()) {
            player.sendSystemMessage(Component.literal("§c§l[TENANT] This property is already rented!"));
            return false;
        }
        
        // Create tenant
        String tenantName = generateTenantName();
        long rentAmount = (long)(propertyType.getDailyIncome() * 1.5); // 50% bonus
        String tenantId = propertyType.name() + "_" + player.getUUID();
        
        TenantInfo tenant = new TenantInfo(tenantName, propertyType, rentAmount);
        getTenantData(player.getUUID()).addTenant(tenantId, tenant);
        
        // Mark property as rented
        propData.setRented(true);
        propData.setRenterName(tenantName);
        
        player.sendSystemMessage(Component.literal("§a§l[TENANT] Property rented to " + tenantName + "!"));
        player.sendSystemMessage(Component.literal("§7Rent: §6" + CurrencyManager.format(rentAmount) + "/day §7(+50% bonus)"));
        
        return true;
    }
    
    /**
     * Evict a tenant from property
     */
    public static boolean evictTenant(ServerPlayer player, PropertyType propertyType) {
        String tenantId = propertyType.name() + "_" + player.getUUID();
        TenantData data = getTenantData(player.getUUID());
        TenantInfo tenant = data.getTenant(tenantId);
        
        if (tenant == null) {
            player.sendSystemMessage(Component.literal("§c§l[TENANT] No tenant found for this property!"));
            return false;
        }
        
        // Remove tenant
        data.removeTenant(tenantId);
        
        // Unmark property as rented
        PropertyManager.PlayerProperties props = PropertyManager.getPlayerProperties(player.getUUID());
        PropertyManager.PropertyData propData = props.getProperties().get(propertyType);
        if (propData != null) {
            propData.setRented(false);
            propData.setRenterName(null);
        }
        
        player.sendSystemMessage(Component.literal("§c§l[TENANT] Evicted " + tenant.getName() + "!"));
        
        return true;
    }
    
    /**
     * Adjust rent amount for a tenant
     */
    public static boolean adjustRent(ServerPlayer player, PropertyType propertyType, boolean increase) {
        String tenantId = propertyType.name() + "_" + player.getUUID();
        TenantInfo tenant = getTenantData(player.getUUID()).getTenant(tenantId);
        
        if (tenant == null) {
            player.sendSystemMessage(Component.literal("§c§l[TENANT] No tenant found!"));
            return false;
        }
        
        long adjustment = (long)(tenant.getRentAmount() * 0.10); // 10% adjustment
        
        if (increase) {
            tenant.setRentAmount(tenant.getRentAmount() + adjustment);
            tenant.adjustRelationship(-5); // Tenant unhappy
            player.sendSystemMessage(Component.literal("§e§l[TENANT] Rent increased by §6" + 
                CurrencyManager.format(adjustment) + " §e(Relationship: -5)"));
        } else {
            tenant.setRentAmount(Math.max(100, tenant.getRentAmount() - adjustment));
            tenant.adjustRelationship(5); // Tenant happy
            player.sendSystemMessage(Component.literal("§a§l[TENANT] Rent decreased by §6" + 
                CurrencyManager.format(adjustment) + " §a(Relationship: +5)"));
        }
        
        player.sendSystemMessage(Component.literal("§7New rent: §6" + CurrencyManager.format(tenant.getRentAmount()) + "/day"));
        
        return true;
    }
    
    /**
     * Process daily tenant events and rent collection
     */
    public static void processDailyTenants(ServerPlayer player, long currentDay) {
        TenantData data = getTenantData(player.getUUID());
        
        if (data.getLastProcessedDay() >= currentDay) {
            return; // Already processed
        }
        
        for (TenantInfo tenant : data.getTenants().values()) {
            // Collect rent
            CurrencyManager.addMoney(player, tenant.getRentAmount());
            tenant.addPaidRent(tenant.getRentAmount());
            tenant.incrementDaysRented();
            
            // Random events (15% chance per day)
            if (random.nextDouble() < 0.15 && tenant.getLastEventDay() < currentDay - 2) {
                processRandomEvent(player, tenant);
                tenant.setLastEventDay(currentDay);
            }
        }
        
        data.setLastProcessedDay(currentDay);
    }
    
    /**
     * Process random tenant event
     */
    private static void processRandomEvent(ServerPlayer player, TenantInfo tenant) {
        int relationshipScore = tenant.getRelationshipScore();
        double eventRoll = random.nextDouble();
        
        // Better relationship = more positive events
        double positiveThreshold = 0.3 + (relationshipScore / 100.0 * 0.4); // 30-70% chance
        
        if (eventRoll < positiveThreshold) {
            // Positive event - gift
            processPositiveEvent(player, tenant);
        } else {
            // Negative event - damage or request
            processNegativeEvent(player, tenant);
        }
    }
    
    private static void processPositiveEvent(ServerPlayer player, TenantInfo tenant) {
        int eventType = random.nextInt(3);
        
        switch (eventType) {
            case 0: // Money gift
                long gift = tenant.getRentAmount() * (1 + random.nextInt(3)); // 1-3x rent
                CurrencyManager.addMoney(player, gift);
                player.sendSystemMessage(Component.literal("§a§l[TENANT] " + tenant.getName() + 
                    " gave you a gift of §6" + CurrencyManager.format(gift) + "§a!"));
                tenant.adjustRelationship(5);
                break;
                
            case 1: // Item gift (diamonds)
                int diamondCount = 1 + random.nextInt(3);
                ItemStack diamonds = new ItemStack(Items.DIAMOND, diamondCount);
                if (player.getInventory().add(diamonds)) {
                    player.sendSystemMessage(Component.literal("§a§l[TENANT] " + tenant.getName() + 
                        " gave you " + diamondCount + " diamonds!"));
                } else {
                    // Inventory full, convert to money
                    CurrencyManager.addMoney(player, diamondCount * 500L);
                    player.sendSystemMessage(Component.literal("§a§l[TENANT] " + tenant.getName() + 
                        " gave you §6$" + (diamondCount * 500) + " §a(inventory full)!"));
                }
                tenant.adjustRelationship(5);
                break;
                
            case 2: // Bonus rent payment
                long bonus = tenant.getRentAmount();
                CurrencyManager.addMoney(player, bonus);
                player.sendSystemMessage(Component.literal("§a§l[TENANT] " + tenant.getName() + 
                    " paid double rent today! (+§6" + CurrencyManager.format(bonus) + "§a)"));
                tenant.adjustRelationship(3);
                break;
        }
    }
    
    private static void processNegativeEvent(ServerPlayer player, TenantInfo tenant) {
        int eventType = random.nextInt(2);
        
        switch (eventType) {
            case 0: // Property damage
                long repairCost = tenant.getRentAmount() * (1 + random.nextInt(2)); // 1-2x rent
                CurrencyManager.removeMoney(player, repairCost);
                player.sendSystemMessage(Component.literal("§c§l[TENANT] " + tenant.getName() + 
                    " caused property damage! Repair cost: §6" + CurrencyManager.format(repairCost)));
                tenant.adjustRelationship(-3);
                break;
                
            case 1: // Rent reduction request
                long reduction = (long)(tenant.getRentAmount() * 0.10);
                tenant.setRentAmount(tenant.getRentAmount() - reduction);
                player.sendSystemMessage(Component.literal("§e§l[TENANT] " + tenant.getName() + 
                    " requested rent reduction. New rent: §6" + CurrencyManager.format(tenant.getRentAmount())));
                tenant.adjustRelationship(5);
                break;
        }
    }
}
