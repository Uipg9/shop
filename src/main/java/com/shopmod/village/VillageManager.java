package com.shopmod.village;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced village manager with resource production, supply chains, buildings, and housing
 * Players manage an interconnected economy with workers consuming/producing resources
 */
public class VillageManager {
    private static final Map<UUID, Village> villages = new ConcurrentHashMap<>();
    
    // Base worker capacity (before houses)
    private static final int BASE_WORKER_SLOTS = 3;
    
    // Auto-manage mode reduces efficiency
    private static final double AUTO_MANAGE_EFFICIENCY = 0.70; // 30% penalty
    
    /**
     * Village data - contains workers, resources, buildings, and settings
     */
    public static class Village {
        // Workers
        private final Map<VillagerWorker, WorkerData> workers = new HashMap<>();
        
        // Resource storage
        private final Map<ResourceType, Long> resources = new HashMap<>();
        private final Map<ResourceType, Long> storageCapacity = new HashMap<>();
        
        // Buildings
        private final Map<VillageBuilding, Integer> buildings = new HashMap<>();
        
        // Village stats
        private int villageLevel = 1; // 1=Hamlet, 2=Village, 3=Town, 4=City
        private long lastProcessedDay = -1;
        private long totalEarningsAllTime = 0;
        private boolean autoManage = false; // Toggle for automation
        
        // Strike tracking
        private final Map<VillagerWorker, Integer> daysUnfed = new HashMap<>();
        
        public Village() {
            // Initialize resource storage with base capacities
            for (ResourceType type : ResourceType.values()) {
                resources.put(type, 0L);
                storageCapacity.put(type, getBaseStorageCapacity(type));
            }
        }
        
        private long getBaseStorageCapacity(ResourceType type) {
            return switch (type) {
                case FOOD, FISH -> 500L; // Lots of food storage
                case WOOD -> 300L;
                case ORE, LEATHER, WOOL -> 200L;
                case TOOLS, ARMOR -> 100L;
                case ENCHANTED, RARE -> 50L;
            };
        }
        
        // Getters
        public Map<VillagerWorker, WorkerData> getWorkers() { return workers; }
        public Map<ResourceType, Long> getResources() { return resources; }
        public Map<ResourceType, Long> getStorageCapacity() { return storageCapacity; }
        public Map<VillageBuilding, Integer> getBuildings() { return buildings; }
        public int getVillageLevel() { return villageLevel; }
        public void setVillageLevel(int level) { this.villageLevel = level; }
        public long getLastProcessedDay() { return lastProcessedDay; }
        public void setLastProcessedDay(long day) { this.lastProcessedDay = day; }
        public long getTotalEarningsAllTime() { return totalEarningsAllTime; }
        public void addEarnings(long amount) { this.totalEarningsAllTime += amount; }
        public boolean isAutoManage() { return autoManage; }
        public void setAutoManage(boolean auto) { this.autoManage = auto; }
        public Map<VillagerWorker, Integer> getDaysUnfed() { return daysUnfed; }
        
        /**
         * Get total worker slots (base + houses)
         */
        public int getTotalWorkerSlots() {
            int slots = BASE_WORKER_SLOTS;
            for (Map.Entry<VillageBuilding, Integer> entry : buildings.entrySet()) {
                slots += entry.getKey().getWorkerSlots() * entry.getValue();
            }
            return slots;
        }
        
        /**
         * Get total workers hired
         */
        public int getTotalWorkerCount() {
            return workers.values().stream()
                .mapToInt(WorkerData::getCount)
                .sum();
        }
        
        /**
         * Add resource to storage (respects capacity)
         */
        public void addResource(ResourceType type, long amount) {
            long current = resources.getOrDefault(type, 0L);
            long capacity = storageCapacity.getOrDefault(type, 0L);
            long newAmount = Math.min(current + amount, capacity);
            resources.put(type, newAmount);
        }
        
        /**
         * Remove resource from storage (returns false if insufficient)
         */
        public boolean removeResource(ResourceType type, long amount) {
            long current = resources.getOrDefault(type, 0L);
            if (current < amount) return false;
            resources.put(type, current - amount);
            return true;
        }
        
        /**
         * Check if we have enough of a resource
         */
        public boolean hasResource(ResourceType type, long amount) {
            return resources.getOrDefault(type, 0L) >= amount;
        }
    }
    
    /**
     * Worker data - count, level, status, earnings
     */
    public static class WorkerData {
        private int count;
        private int level;
        private long totalEarned;
        private WorkerStatus status;
        
        public WorkerData() {
            this.count = 1;
            this.level = 1;
            this.totalEarned = 0;
            this.status = WorkerStatus.ACTIVE;
        }
        
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public long getTotalEarned() { return totalEarned; }
        public void addEarnings(long amount) { this.totalEarned += amount; }
        public WorkerStatus getStatus() { return status; }
        public void setStatus(WorkerStatus status) { this.status = status; }
    }
    
    /**
     * Worker status enum
     */
    public enum WorkerStatus {
        ACTIVE("§a✓ Active", 1.0),       // Working normally
        HUNGRY("§e⚠ Hungry", 0.5),       // 1 day unfed, 50% production
        ON_STRIKE("§c✖ On Strike", 0.0); // 2+ days unfed, 0% production
        
        private final String display;
        private final double efficiency;
        
        WorkerStatus(String display, double efficiency) {
            this.display = display;
            this.efficiency = efficiency;
        }
        
        public String getDisplay() { return display; }
        public double getEfficiency() { return efficiency; }
    }
    
    /**
     * Get or create village for player
     */
    public static Village getVillage(UUID playerUUID) {
        return villages.computeIfAbsent(playerUUID, k -> new Village());
    }
    
    /**
     * Hire a worker
     */
    public static boolean hireWorker(ServerPlayer player, VillagerWorker workerType) {
        Village village = getVillage(player.getUUID());
        
        // Check village level requirement
        if (workerType.getRequiredVillageLevel() > village.getVillageLevel()) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[VILLAGE] Requires Village Level %d! (Currently Level %d)",
                    workerType.getRequiredVillageLevel(), village.getVillageLevel())));
            return false;
        }
        
        // Check worker slot capacity
        if (village.getTotalWorkerCount() >= village.getTotalWorkerSlots()) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[VILLAGE] No worker slots! Build houses to expand. (%d/%d)",
                    village.getTotalWorkerCount(), village.getTotalWorkerSlots())));
            return false;
        }
        
        // Check hire cost
        long hireCost = workerType.getHireCost();
        long balance = CurrencyManager.getBalance(player);
        if (balance < hireCost) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[VILLAGE] Insufficient funds! Need %s, have %s",
                    CurrencyManager.format(hireCost), CurrencyManager.format(balance))));
            return false;
        }
        
        // Hire worker
        CurrencyManager.removeMoney(player, hireCost);
        WorkerData data = village.getWorkers().computeIfAbsent(workerType, k -> new WorkerData());
        if (data.getCount() == 0) {
            data.setCount(1);
        } else {
            data.setCount(data.getCount() + 1);
        }
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[VILLAGE] Hired %s! (Workers: %d/%d)",
                workerType.getDisplayName(), village.getTotalWorkerCount(), village.getTotalWorkerSlots())));
        
        return true;
    }
    
    /**
     * Fire a worker
     */
    public static boolean fireWorker(ServerPlayer player, VillagerWorker workerType) {
        Village village = getVillage(player.getUUID());
        WorkerData data = village.getWorkers().get(workerType);
        
        if (data == null || data.getCount() <= 0) {
            player.sendSystemMessage(Component.literal("§c§l[VILLAGE] No workers of this type!"));
            return false;
        }
        
        // Refund 50% of hire cost
        long refund = workerType.getHireCost() / 2;
        CurrencyManager.addMoney(player, refund);
        
        data.setCount(data.getCount() - 1);
        if (data.getCount() == 0) {
            village.getWorkers().remove(workerType);
        }
        
        player.sendSystemMessage(Component.literal(
            String.format("§6§l[VILLAGE] Fired %s! Refunded %s (50%%)",
                workerType.getDisplayName(), CurrencyManager.format(refund))));
        
        return true;
    }
    
    /**
     * Upgrade worker level
     */
    public static boolean upgradeWorker(ServerPlayer player, VillagerWorker workerType) {
        Village village = getVillage(player.getUUID());
        WorkerData data = village.getWorkers().get(workerType);
        
        if (data == null || data.getCount() <= 0) {
            player.sendSystemMessage(Component.literal("§c§l[VILLAGE] No workers of this type!"));
            return false;
        }
        
        if (data.getLevel() >= 50) {
            player.sendSystemMessage(Component.literal("§c§l[VILLAGE] Maximum level reached!"));
            return false;
        }
        
        // Calculate upgrade cost
        long upgradeCost = workerType.getHireCost() * (data.getLevel() + 1);
        long balance = CurrencyManager.getBalance(player);
        if (balance < upgradeCost) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[VILLAGE] Insufficient funds! Need %s",
                    CurrencyManager.format(upgradeCost))));
            return false;
        }
        
        // Upgrade
        CurrencyManager.removeMoney(player, upgradeCost);
        data.setLevel(data.getLevel() + 1);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[VILLAGE] Upgraded %s to Level %d!",
                workerType.getDisplayName(), data.getLevel())));
        
        return true;
    }
    
    /**
     * Build a building
     */
    public static boolean buildBuilding(ServerPlayer player, VillageBuilding building) {
        Village village = getVillage(player.getUUID());
        
        // Check resource costs
        for (Map.Entry<ResourceType, Integer> cost : building.getBuildCost().entrySet()) {
            if (!village.hasResource(cost.getKey(), cost.getValue())) {
                player.sendSystemMessage(Component.literal(
                    String.format("§c§l[VILLAGE] Insufficient %s! Need %d, have %d",
                        cost.getKey().getDisplayName(), cost.getValue(),
                        village.getResources().getOrDefault(cost.getKey(), 0L))));
                return false;
            }
        }
        
        // Deduct resources
        for (Map.Entry<ResourceType, Integer> cost : building.getBuildCost().entrySet()) {
            village.removeResource(cost.getKey(), cost.getValue());
        }
        
        // Build
        int count = village.getBuildings().getOrDefault(building, 0);
        village.getBuildings().put(building, count + 1);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[VILLAGE] Built %s! (Total: %d)",
                building.getDisplayName(), count + 1)));
        
        if (building.isHousing()) {
            player.sendSystemMessage(Component.literal(
                String.format("§e§l[VILLAGE] Worker slots: %d/%d",
                    village.getTotalWorkerCount(), village.getTotalWorkerSlots())));
        }
        
        return true;
    }
    
    /**
     * Toggle auto-manage mode
     */
    public static void toggleAutoManage(ServerPlayer player) {
        Village village = getVillage(player.getUUID());
        village.setAutoManage(!village.isAutoManage());
        
        if (village.isAutoManage()) {
            player.sendSystemMessage(Component.literal(
                "§e§l[VILLAGE] Auto-Manage ENABLED - 30% efficiency penalty, zero micromanagement"));
        } else {
            player.sendSystemMessage(Component.literal(
                "§a§l[VILLAGE] Auto-Manage DISABLED - 100% efficiency, manual resource management"));
        }
    }
    
    /**
     * Process daily village production, consumption, and upkeep
     */
    public static void processDailyVillage(ServerPlayer player, long currentDay) {
        Village village = getVillage(player.getUUID());
        
        // Skip if already processed
        if (village.getLastProcessedDay() >= currentDay) {
            return;
        }
        village.setLastProcessedDay(currentDay);
        
        if (village.getWorkers().isEmpty()) {
            return; // No workers, nothing to process
        }
        
        // === PHASE 1: PAY BUILDING UPKEEP ===
        boolean upkeepFailed = processUpkeep(village, player);
        
        // === PHASE 2: FEED WORKERS ===
        processFoodConsumption(village, player);
        
        // === PHASE 3: PRODUCE RESOURCES ===
        long totalMoneyEarned = 0;
        Map<ResourceType, Long> totalProduced = new HashMap<>();
        
        for (Map.Entry<VillagerWorker, WorkerData> entry : village.getWorkers().entrySet()) {
            VillagerWorker workerType = entry.getKey();
            WorkerData data = entry.getValue();
            
            if (data.getCount() <= 0) continue;
            
            // Calculate efficiency
            double efficiency = data.getStatus().getEfficiency();
            if (village.isAutoManage()) {
                efficiency *= AUTO_MANAGE_EFFICIENCY; // 30% penalty in auto mode
            }
            
            // Apply workshop bonus (+10%)
            if (village.getBuildings().getOrDefault(VillageBuilding.WORKSHOP, 0) > 0) {
                efficiency *= 1.10;
            }
            
            // Check if we have input resources
            boolean canProduce = true;
            if (!village.isAutoManage()) {
                for (Map.Entry<ResourceType, Integer> input : workerType.getDailyInputs().entrySet()) {
                    int needed = input.getValue() * data.getCount();
                    if (!village.hasResource(input.getKey(), needed)) {
                        canProduce = false;
                        break;
                    }
                }
            }
            
            if (!canProduce && !village.isAutoManage()) {
                // Worker idles - no production
                continue;
            }
            
            // Consume inputs (unless auto-manage, then it's "magic")
            if (!village.isAutoManage()) {
                for (Map.Entry<ResourceType, Integer> input : workerType.getDailyInputs().entrySet()) {
                    int needed = input.getValue() * data.getCount();
                    village.removeResource(input.getKey(), needed);
                }
            }
            
            // Produce outputs
            Map<ResourceType, Integer> outputs = workerType.getOutputAtLevel(data.getLevel());
            for (Map.Entry<ResourceType, Integer> output : outputs.entrySet()) {
                int produced = (int) (output.getValue() * data.getCount() * efficiency);
                village.addResource(output.getKey(), produced);
                totalProduced.merge(output.getKey(), (long) produced, Long::sum);
            }
            
            // Special handling for Merchant (converts resources to money)
            if (workerType == VillagerWorker.MERCHANT) {
                long merchantProfit = processMerchant(village, data, efficiency);
                totalMoneyEarned += merchantProfit;
            }
            
            // Pay salaries
            long totalSalaries = workerType.getDailySalary() * data.getCount();
            CurrencyManager.removeMoney(player, totalSalaries);
        }
        
        // Add merchant money to player
        if (totalMoneyEarned > 0) {
            CurrencyManager.addMoney(player, totalMoneyEarned);
            village.addEarnings(totalMoneyEarned);
        }
        
        // === PHASE 4: SEND SUMMARY ===
        sendDailySummary(player, village, totalProduced, totalMoneyEarned, upkeepFailed);
    }
    
    /**
     * Process building upkeep costs
     */
    private static boolean processUpkeep(Village village, ServerPlayer player) {
        boolean failed = false;
        
        for (Map.Entry<VillageBuilding, Integer> entry : village.getBuildings().entrySet()) {
            VillageBuilding building = entry.getKey();
            int count = entry.getValue();
            
            for (Map.Entry<ResourceType, Integer> upkeep : building.getDailyUpkeep().entrySet()) {
                int needed = upkeep.getValue() * count;
                
                if (!village.hasResource(upkeep.getKey(), needed)) {
                    // In auto-manage mode, ignore upkeep
                    if (!village.isAutoManage()) {
                        player.sendSystemMessage(Component.literal(
                            String.format("§c§l[VILLAGE] Building upkeep failed! %s needs %d %s",
                                building.getDisplayName(), needed, upkeep.getKey().getDisplayName())));
                        failed = true;
                    }
                } else if (!village.isAutoManage()) {
                    village.removeResource(upkeep.getKey(), needed);
                }
            }
        }
        
        return failed;
    }
    
    /**
     * Process worker food consumption
     */
    private static void processFoodConsumption(Village village, ServerPlayer player) {
        int totalWorkers = village.getTotalWorkerCount();
        int foodNeeded = totalWorkers; // 1 food per worker
        
        // Check if we have enough food (or fish)
        long totalFood = village.getResources().getOrDefault(ResourceType.FOOD, 0L) +
                        village.getResources().getOrDefault(ResourceType.FISH, 0L);
        
        if (totalFood >= foodNeeded || village.isAutoManage()) {
            // Feed workers (prioritize food, then fish)
            if (!village.isAutoManage()) {
                long foodUsed = Math.min(foodNeeded, village.getResources().getOrDefault(ResourceType.FOOD, 0L));
                village.removeResource(ResourceType.FOOD, foodUsed);
                
                long remaining = foodNeeded - foodUsed;
                if (remaining > 0) {
                    village.removeResource(ResourceType.FISH, remaining);
                }
            }
            
            // Reset all workers to ACTIVE
            for (WorkerData data : village.getWorkers().values()) {
                data.setStatus(WorkerStatus.ACTIVE);
            }
            village.getDaysUnfed().clear();
            
        } else {
            // Not enough food - workers go hungry/strike
            for (Map.Entry<VillagerWorker, WorkerData> entry : village.getWorkers().entrySet()) {
                VillagerWorker workerType = entry.getKey();
                WorkerData data = entry.getValue();
                
                int days = village.getDaysUnfed().getOrDefault(workerType, 0) + 1;
                village.getDaysUnfed().put(workerType, days);
                
                if (days >= 2) {
                    data.setStatus(WorkerStatus.ON_STRIKE);
                } else {
                    data.setStatus(WorkerStatus.HUNGRY);
                }
            }
            
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[VILLAGE] FOOD SHORTAGE! Need %d food, have %d. Workers are %s!",
                    foodNeeded, totalFood, totalFood > 0 ? "HUNGRY" : "ON STRIKE")));
        }
    }
    
    /**
     * Process merchant - sells 3 random resources for profit
     */
    private static long processMerchant(Village village, WorkerData data, double efficiency) {
        long totalProfit = 0;
        
        for (int i = 0; i < data.getCount(); i++) {
            // Find 3 resources to sell
            List<ResourceType> available = new ArrayList<>();
            for (Map.Entry<ResourceType, Long> entry : village.getResources().entrySet()) {
                if (entry.getValue() >= 3) {
                    available.add(entry.getKey());
                }
            }
            
            if (available.size() < 3) continue; // Not enough resources
            
            // Pick 3 random
            Collections.shuffle(available);
            for (int j = 0; j < 3 && j < available.size(); j++) {
                ResourceType type = available.get(j);
                village.removeResource(type, 1);
                long value = (long) (type.getValuePerUnit() * efficiency);
                
                // Market bonus (2x value)
                if (village.getBuildings().getOrDefault(VillageBuilding.MARKET, 0) > 0) {
                    value *= 2;
                }
                
                // Trading post bonus (+50%)
                if (village.getBuildings().getOrDefault(VillageBuilding.TRADING_POST, 0) > 0) {
                    value = (long) (value * 1.5);
                }
                
                totalProfit += value;
            }
        }
        
        return totalProfit;
    }
    
    /**
     * Send daily summary to player
     */
    private static void sendDailySummary(ServerPlayer player, Village village,
                                         Map<ResourceType, Long> produced, long money, boolean upkeepFailed) {
        player.sendSystemMessage(Component.literal("§6§l[VILLAGE] Daily Report:"));
        
        if (!produced.isEmpty()) {
            player.sendSystemMessage(Component.literal("§a§lProduced:"));
            for (Map.Entry<ResourceType, Long> entry : produced.entrySet()) {
                player.sendSystemMessage(Component.literal(
                    String.format("  §7%s %s: §e+%d", 
                        entry.getKey().getIcon(), entry.getKey().getDisplayName(), entry.getValue())));
            }
        }
        
        if (money > 0) {
            player.sendSystemMessage(Component.literal(
                String.format("§a§lMerchant Sales: §6+%s", CurrencyManager.format(money))));
        }
        
        if (upkeepFailed) {
            player.sendSystemMessage(Component.literal(
                "§c§l⚠ Building upkeep failed! Some buildings may not function."));
        }
        
        if (village.isAutoManage()) {
            player.sendSystemMessage(Component.literal(
                "§e(Auto-Manage Mode: -30% efficiency)"));
        }
        
        // Check for village level up
        checkVillageLevelUp(player, village);
    }
    
    /**
     * Check if village can level up and do so
     */
    private static void checkVillageLevelUp(ServerPlayer player, Village village) {
        int currentLevel = village.getVillageLevel();
        
        boolean canLevelUp = switch (currentLevel) {
            case 1 -> checkLevelUp2Requirements(village); // Hamlet → Village
            case 2 -> checkLevelUp3Requirements(village); // Village → Town
            case 3 -> checkLevelUp4Requirements(village); // Town → City
            default -> false;
        };
        
        if (canLevelUp) {
            village.setVillageLevel(currentLevel + 1);
            String levelName = switch (currentLevel + 1) {
                case 2 -> "Village";
                case 3 -> "Town";
                case 4 -> "City";
                default -> "Unknown";
            };
            
            player.sendSystemMessage(Component.literal(
                String.format("§6§l§k!!!§r §6§lVILLAGE UPGRADED TO %s!§r §6§l§k!!!§r", levelName.toUpperCase())));
            player.sendSystemMessage(Component.literal(
                String.format("§e§lNew workers unlocked! Check /village to hire.")));
        }
    }
    
    /**
     * Level 1 → 2 (Hamlet → Village)
     * Requirements: 50 Food, 30 Wood, 3+ days operated
     */
    private static boolean checkLevelUp2Requirements(Village village) {
        return village.getResources().getOrDefault(ResourceType.FOOD, 0L) >= 50 &&
               village.getResources().getOrDefault(ResourceType.WOOD, 0L) >= 30 &&
               village.getLastProcessedDay() >= 3;
    }
    
    /**
     * Level 2 → 3 (Village → Town)
     * Requirements: 200 Food, 100 Wood, 50 Ore, 5+ workers, 1+ house
     */
    private static boolean checkLevelUp3Requirements(Village village) {
        return village.getResources().getOrDefault(ResourceType.FOOD, 0L) >= 200 &&
               village.getResources().getOrDefault(ResourceType.WOOD, 0L) >= 100 &&
               village.getResources().getOrDefault(ResourceType.ORE, 0L) >= 50 &&
               village.getTotalWorkerCount() >= 5 &&
               village.getBuildings().getOrDefault(VillageBuilding.HOUSE, 0) >= 1;
    }
    
    /**
     * Level 3 → 4 (Town → City)
     * Requirements: All Tier 3 workers hired, 10+ total workers, 3+ houses
     */
    private static boolean checkLevelUp4Requirements(Village village) {
        boolean hasBlacksmith = village.getWorkers().containsKey(VillagerWorker.BLACKSMITH) &&
                               village.getWorkers().get(VillagerWorker.BLACKSMITH).getCount() > 0;
        boolean hasMerchant = village.getWorkers().containsKey(VillagerWorker.MERCHANT) &&
                             village.getWorkers().get(VillagerWorker.MERCHANT).getCount() > 0;
        
        return hasBlacksmith && hasMerchant &&
               village.getTotalWorkerCount() >= 10 &&
               village.getBuildings().getOrDefault(VillageBuilding.HOUSE, 0) >= 3;
    }
    
    /**
     * Get village level name
     */
    public static String getVillageLevelName(int level) {
        return switch (level) {
            case 1 -> "Hamlet";
            case 2 -> "Village";
            case 3 -> "Town";
            case 4 -> "City";
            default -> "Settlement";
        };
    }
}
