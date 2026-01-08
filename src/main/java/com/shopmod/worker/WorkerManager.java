package com.shopmod.worker;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages all workers for all players
 */
public class WorkerManager {
    private static final Map<UUID, List<Worker>> playerWorkers = new ConcurrentHashMap<>();
    private static final long HIRING_FEE = 5000;
    private static final long TRAINING_COST = 1000;
    private static final int MAX_WORKERS_PER_PLAYER = 10;
    
    private static final String[] WORKER_NAMES = {
        "Alex", "Steve", "Bob", "Alice", "Charlie", "Diana", "Edward", "Fiona",
        "George", "Hannah", "Isaac", "Julia", "Kevin", "Laura", "Michael", "Nancy",
        "Oliver", "Patricia", "Quinn", "Rachel", "Samuel", "Teresa", "Ulysses", "Veronica",
        "Walter", "Xena", "Yuri", "Zoe", "Aaron", "Beth", "Carl", "Dana"
    };
    
    /**
     * Get all workers for a player
     */
    public static List<Worker> getPlayerWorkers(UUID playerUUID) {
        return playerWorkers.computeIfAbsent(playerUUID, k -> new ArrayList<>());
    }
    
    /**
     * Hire a new worker
     */
    public static Worker hireWorker(ServerPlayer player, WorkerType type, String name) {
        UUID playerUUID = player.getUUID();
        List<Worker> workers = getPlayerWorkers(playerUUID);
        
        // Check max workers
        if (workers.size() >= MAX_WORKERS_PER_PLAYER) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Maximum workers reached! (" + MAX_WORKERS_PER_PLAYER + ")"));
            return null;
        }
        
        // Check if name already exists
        if (workers.stream().anyMatch(w -> w.getName().equalsIgnoreCase(name))) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] A worker named '" + name + "' already exists!"));
            return null;
        }
        
        // Check funds
        if (!CurrencyManager.canAfford(player, HIRING_FEE)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Insufficient funds! Need " + CurrencyManager.format(HIRING_FEE)));
            return null;
        }
        
        // Deduct hiring fee
        CurrencyManager.removeMoney(player, HIRING_FEE);
        
        // Create worker
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        Worker worker = new Worker(name, type, currentDay);
        workers.add(worker);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[WORKERS] Hired " + name + " as " + type.getDisplayName() + "!"));
        player.sendSystemMessage(Component.literal(
            "§7Daily Salary: §6" + CurrencyManager.format(worker.getDailySalary())));
        
        return worker;
    }
    
    /**
     * Fire a worker
     */
    public static boolean fireWorker(ServerPlayer player, UUID workerId) {
        List<Worker> workers = getPlayerWorkers(player.getUUID());
        
        Worker worker = workers.stream()
            .filter(w -> w.getWorkerId().equals(workerId))
            .findFirst()
            .orElse(null);
        
        if (worker == null) {
            player.sendSystemMessage(Component.literal("§c§l[WORKERS] Worker not found!"));
            return false;
        }
        
        workers.remove(worker);
        
        player.sendSystemMessage(Component.literal(
            "§e§l[WORKERS] Fired " + worker.getName() + " (" + worker.getType().getDisplayName() + ")"));
        
        return true;
    }
    
    /**
     * Fire a worker by name
     */
    public static boolean fireWorkerByName(ServerPlayer player, String name) {
        List<Worker> workers = getPlayerWorkers(player.getUUID());
        
        Worker worker = workers.stream()
            .filter(w -> w.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
        
        if (worker == null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Worker '" + name + "' not found!"));
            return false;
        }
        
        return fireWorker(player, worker.getWorkerId());
    }
    
    /**
     * Assign worker to a target (farm/mine/property)
     */
    public static boolean assignWorker(ServerPlayer player, UUID workerId, String targetId) {
        Worker worker = getWorkerById(player.getUUID(), workerId);
        
        if (worker == null) {
            player.sendSystemMessage(Component.literal("§c§l[WORKERS] Worker not found!"));
            return false;
        }
        
        worker.setAssignedTo(targetId);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[WORKERS] Assigned " + worker.getName() + " to " + targetId));
        
        return true;
    }
    
    /**
     * Unassign worker
     */
    public static boolean unassignWorker(ServerPlayer player, UUID workerId) {
        Worker worker = getWorkerById(player.getUUID(), workerId);
        
        if (worker == null) {
            player.sendSystemMessage(Component.literal("§c§l[WORKERS] Worker not found!"));
            return false;
        }
        
        worker.setAssignedTo(null);
        
        player.sendSystemMessage(Component.literal(
            "§e§l[WORKERS] Unassigned " + worker.getName()));
        
        return true;
    }
    
    /**
     * Train a worker in a specific skill
     */
    public static boolean trainWorker(ServerPlayer player, UUID workerId, WorkerSkill skill) {
        Worker worker = getWorkerById(player.getUUID(), workerId);
        
        if (worker == null) {
            player.sendSystemMessage(Component.literal("§c§l[WORKERS] Worker not found!"));
            return false;
        }
        
        // Check if skill is applicable
        if (!skill.isApplicableTo(worker.getType())) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] " + worker.getName() + " cannot learn " + skill.getDisplayName() + "!"));
            return false;
        }
        
        // Check if already max level
        int currentLevel = worker.getSkillLevel(skill);
        if (currentLevel >= 10) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] " + skill.getDisplayName() + " is already at maximum level!"));
            return false;
        }
        
        // Check cooldown (1 day per skill)
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        if (worker.getLastTrainingDay() >= currentDay) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] " + worker.getName() + " needs rest! Can train again tomorrow."));
            return false;
        }
        
        // Check funds
        if (!CurrencyManager.canAfford(player, TRAINING_COST)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Insufficient funds! Need " + CurrencyManager.format(TRAINING_COST)));
            return false;
        }
        
        // Deduct cost and train
        CurrencyManager.removeMoney(player, TRAINING_COST);
        worker.setSkillLevel(skill, currentLevel + 1);
        worker.setLastTrainingDay(currentDay);
        worker.addExperience(100);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[WORKERS] " + worker.getName() + "'s " + skill.getDisplayName() + 
            " improved to Level " + (currentLevel + 1) + "!"));
        
        return true;
    }
    
    /**
     * Get total daily salaries for a player
     */
    public static long getDailySalaries(UUID playerUUID) {
        return getPlayerWorkers(playerUUID).stream()
            .mapToLong(Worker::getDailySalary)
            .sum();
    }
    
    /**
     * Process daily payments for all workers
     */
    public static void processDailyPayments(long currentDay, net.minecraft.server.MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> {
            UUID playerUUID = player.getUUID();
            List<Worker> workers = getPlayerWorkers(playerUUID);
            
            if (workers.isEmpty()) {
                return;
            }
            
            long totalSalaries = getDailySalaries(playerUUID);
            long balance = CurrencyManager.getBalance(player);
            
            if (balance >= totalSalaries) {
                // Pay all workers
                CurrencyManager.removeMoney(player, totalSalaries);
                
                // Increase loyalty for all workers
                workers.forEach(w -> w.adjustLoyalty(1));
                
                player.sendSystemMessage(Component.literal(
                    "§6§l[WORKERS] Paid salaries: -" + CurrencyManager.format(totalSalaries)));
            } else {
                // Can't afford - decrease loyalty
                workers.forEach(w -> w.adjustLoyalty(-5));
                
                player.sendSystemMessage(Component.literal(
                    "§c§l[WORKERS] Missed payroll! Worker loyalty decreased."));
            }
        });
    }
    
    /**
     * Process weekly loyalty updates and quit checks
     */
    public static void processWeeklyUpdates(long currentDay, net.minecraft.server.MinecraftServer server) {
        Random random = new Random();
        
        server.getPlayerList().getPlayers().forEach(player -> {
            UUID playerUUID = player.getUUID();
            List<Worker> workers = getPlayerWorkers(playerUUID);
            List<Worker> toRemove = new ArrayList<>();
            
            for (Worker worker : workers) {
                // Check if worker quits (loyalty below 20 = 10% chance)
                if (worker.getLoyalty() < 20) {
                    if (random.nextInt(100) < 10) {
                        player.sendSystemMessage(Component.literal(
                            "§c§l[WORKERS] " + worker.getName() + " quit due to low morale!"));
                        toRemove.add(worker);
                    }
                }
            }
            
            // Remove workers who quit
            workers.removeAll(toRemove);
        });
    }
    
    /**
     * Get worker by ID
     */
    public static Worker getWorkerById(UUID playerUUID, UUID workerId) {
        return getPlayerWorkers(playerUUID).stream()
            .filter(w -> w.getWorkerId().equals(workerId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get worker by name
     */
    public static Worker getWorkerByName(UUID playerUUID, String name) {
        return getPlayerWorkers(playerUUID).stream()
            .filter(w -> w.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get workers assigned to a specific target
     */
    public static List<Worker> getWorkersForTarget(UUID playerUUID, String targetId) {
        return getPlayerWorkers(playerUUID).stream()
            .filter(w -> targetId.equals(w.getAssignedTo()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get efficiency bonus for a target (max bonus from assigned workers)
     */
    public static double getWorkerBonus(UUID playerUUID, String targetId) {
        List<Worker> assignedWorkers = getWorkersForTarget(playerUUID, targetId);
        
        if (assignedWorkers.isEmpty()) {
            return 0.0;
        }
        
        // Return the highest bonus among assigned workers
        return assignedWorkers.stream()
            .mapToDouble(Worker::getEfficiencyBonus)
            .max()
            .orElse(0.0);
    }
    
    /**
     * Generate a random worker name
     */
    public static String generateRandomName() {
        Random random = new Random();
        return WORKER_NAMES[random.nextInt(WORKER_NAMES.length)];
    }
    
    /**
     * Get hiring fee
     */
    public static long getHiringFee() {
        return HIRING_FEE;
    }
    
    /**
     * Get training cost
     */
    public static long getTrainingCost() {
        return TRAINING_COST;
    }
}
