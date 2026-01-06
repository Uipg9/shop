package com.shopmod.crates;

import java.util.*;

/**
 * Manages lucky crate gambling system
 * Daily changing odds and rewards
 */
public class LuckyCrateManager {
    private static final Random random = new Random();
    private static long lastDayUpdated = -1;
    
    // Current day's crate data
    private static List<CrateType> availableCrates = new ArrayList<>();
    
    public static class CrateType {
        public final String name;
        public final long cost;
        public final long minReward;
        public final long maxReward;
        public final double winChance; // 0.0 to 1.0
        public final String rarity;
        
        public CrateType(String name, long cost, long minReward, long maxReward, 
                        double winChance, String rarity) {
            this.name = name;
            this.cost = cost;
            this.minReward = minReward;
            this.maxReward = maxReward;
            this.winChance = winChance;
            this.rarity = rarity;
        }
        
        public long rollReward() {
            if (random.nextDouble() > winChance) {
                return 0; // Lost
            }
            return minReward + (long)(random.nextDouble() * (maxReward - minReward));
        }
        
        public String getDisplayName() {
            return rarity + name;
        }
        
        public String getOddsDisplay() {
            return String.format("§7Win Chance: §f%.1f%%", winChance * 100);
        }
        
        public String getRewardDisplay() {
            return String.format("§7Reward: §6$%,d §7- §6$%,d", minReward, maxReward);
        }
        
        public String getCostDisplay() {
            return String.format("§7Cost: §6$%,d", cost);
        }
    }
    
    /**
     * Update crates for the current Minecraft day
     */
    public static void updateDailyCrates(long currentDay) {
        if (lastDayUpdated == currentDay && !availableCrates.isEmpty()) {
            return; // Already updated today
        }
        
        lastDayUpdated = currentDay;
        availableCrates.clear();
        
        // Generate 5 random crate types for today
        Random dayRandom = new Random(currentDay * 12345L); // Seed based on day for consistency
        
        // Common Crate
        availableCrates.add(new CrateType(
            "Common Crate",
            100 + dayRandom.nextInt(100),  // $100-$200
            50 + dayRandom.nextInt(100),   // Reward: $50-$150
            200 + dayRandom.nextInt(200),  // to $200-$400
            0.45 + dayRandom.nextDouble() * 0.15,  // 45-60% win chance
            "§f"
        ));
        
        // Uncommon Crate
        availableCrates.add(new CrateType(
            "Uncommon Crate",
            500 + dayRandom.nextInt(300),  // $500-$800
            200 + dayRandom.nextInt(400),  // Reward: $200-$600
            1000 + dayRandom.nextInt(500), // to $1000-$1500
            0.35 + dayRandom.nextDouble() * 0.15,  // 35-50% win chance
            "§a"
        ));
        
        // Rare Crate
        availableCrates.add(new CrateType(
            "Rare Crate",
            2000 + dayRandom.nextInt(1000),  // $2000-$3000
            1000 + dayRandom.nextInt(2000),  // Reward: $1000-$3000
            5000 + dayRandom.nextInt(3000),  // to $5000-$8000
            0.25 + dayRandom.nextDouble() * 0.15,  // 25-40% win chance
            "§9"
        ));
        
        // Epic Crate
        availableCrates.add(new CrateType(
            "Epic Crate",
            10000 + dayRandom.nextInt(5000),   // $10k-$15k
            5000 + dayRandom.nextInt(10000),   // Reward: $5k-$15k
            25000 + dayRandom.nextInt(15000),  // to $25k-$40k
            0.15 + dayRandom.nextDouble() * 0.15,  // 15-30% win chance
            "§5"
        ));
        
        // Legendary Crate
        availableCrates.add(new CrateType(
            "Legendary Crate",
            50000 + dayRandom.nextInt(25000),   // $50k-$75k
            25000 + dayRandom.nextInt(50000),   // Reward: $25k-$75k
            150000 + dayRandom.nextInt(100000), // to $150k-$250k
            0.05 + dayRandom.nextDouble() * 0.15,  // 5-20% win chance
            "§6"
        ));
    }
    
    /**
     * Get all available crates for today
     */
    public static List<CrateType> getAvailableCrates() {
        return new ArrayList<>(availableCrates);
    }
    
    /**
     * Alias for getAvailableCrates (for compatibility)
     */
    public static List<CrateType> getAllCrates() {
        return getAvailableCrates();
    }
    
    /**
     * Get a specific crate by index
     */
    public static CrateType getCrate(int index) {
        if (index >= 0 && index < availableCrates.size()) {
            return availableCrates.get(index);
        }
        return null;
    }
    
    /**
     * Get the number of available crates
     */
    public static int getCrateCount() {
        return availableCrates.size();
    }
}
