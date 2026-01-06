package com.shopmod.economy;

import net.minecraft.world.item.Item;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dynamic price fluctuations for items in the shop (stock market system)
 * Prices change based on time and random market conditions
 */
public class PriceFluctuation {
    private static final Map<Item, Double> priceMultipliers = new ConcurrentHashMap<>();
    private static final Map<Item, Double> previousMultipliers = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    // Base fluctuation range
    private static final double MIN_MULTIPLIER = 0.50;  // 50% of base price
    private static final double MAX_MULTIPLIER = 2.50;  // 250% of base price
    private static final double CHANGE_PER_HOUR = 0.15; // Max 15% change per hour
    
    /**
     * Update all price multipliers (call this every hour in-game or real-time)
     */
    public static void updatePrices() {
        for (Item item : new ArrayList<>(priceMultipliers.keySet())) {
            double current = priceMultipliers.getOrDefault(item, 1.0);
            previousMultipliers.put(item, current);
            
            // Random walk with bounds
            double change = (random.nextDouble() - 0.5) * CHANGE_PER_HOUR * 2;
            double newMultiplier = Math.max(MIN_MULTIPLIER, 
                                  Math.min(MAX_MULTIPLIER, current + change));
            
            priceMultipliers.put(item, newMultiplier);
        }
    }
    
    /**
     * Get current price multiplier for an item
     */
    public static double getPriceMultiplier(Item item) {
        return priceMultipliers.computeIfAbsent(item, k -> 
            0.8 + (random.nextDouble() * 0.4) // Start between 0.8-1.2
        );
    }
    
    /**
     * Get the price change direction and percentage
     * Returns a formatted string like "↑ +12.5%" or "↓ -8.3%"
     */
    public static String getPriceChangeDisplay(Item item) {
        double current = getPriceMultiplier(item);
        double previous = previousMultipliers.getOrDefault(item, current);
        
        if (Math.abs(current - previous) < 0.001) {
            return "§7→ 0.0%"; // No change
        }
        
        double percentChange = ((current - previous) / previous) * 100;
        
        if (percentChange > 0) {
            return String.format("§a↑ +%.1f%%", percentChange);
        } else {
            return String.format("§c↓ %.1f%%", percentChange);
        }
    }
    
    /**
     * Apply multiplier to a base price
     */
    public static long getAdjustedPrice(Item item, long basePrice) {
        return Math.max(1, (long)(basePrice * getPriceMultiplier(item)));
    }
    
    /**
     * Register an item for price tracking
     */
    public static void registerItem(Item item) {
        if (!priceMultipliers.containsKey(item)) {
            priceMultipliers.put(item, 0.9 + (random.nextDouble() * 0.2));
        }
    }
    
    /**
     * Get all tracked items
     */
    public static Set<Item> getTrackedItems() {
        return priceMultipliers.keySet();
    }
    
    /**
     * Force a market crash or boom for testing/events
     */
    public static void triggerMarketEvent(boolean crash) {
        double targetMultiplier = crash ? 0.6 : 1.8;
        
        for (Item item : priceMultipliers.keySet()) {
            double current = priceMultipliers.get(item);
            previousMultipliers.put(item, current);
            
            // Move 30% towards the target
            double newValue = current + ((targetMultiplier - current) * 0.3);
            priceMultipliers.put(item, Math.max(MIN_MULTIPLIER, 
                                      Math.min(MAX_MULTIPLIER, newValue)));
        }
    }
}
