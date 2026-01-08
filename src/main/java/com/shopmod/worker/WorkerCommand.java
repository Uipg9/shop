package com.shopmod.worker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.currency.CurrencyManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Commands for managing workers
 */
public class WorkerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("workers")
            .executes(context -> openGui(context))
        );
        
        dispatcher.register(Commands.literal("worker")
            .then(Commands.literal("hire")
                .then(Commands.argument("type", StringArgumentType.word())
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(context -> hire(context,
                            StringArgumentType.getString(context, "type"),
                            StringArgumentType.getString(context, "name")))
                    )
                )
            )
            .then(Commands.literal("fire")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(context -> fire(context,
                        StringArgumentType.getString(context, "name")))
                )
            )
            .then(Commands.literal("assign")
                .then(Commands.argument("name", StringArgumentType.word())
                    .then(Commands.argument("target", StringArgumentType.greedyString())
                        .executes(context -> assign(context,
                            StringArgumentType.getString(context, "name"),
                            StringArgumentType.getString(context, "target")))
                    )
                )
            )
            .then(Commands.literal("list")
                .executes(context -> list(context))
            )
            .then(Commands.literal("stats")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(context -> stats(context,
                        StringArgumentType.getString(context, "name")))
                )
            )
        );
    }
    
    private static int openGui(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new WorkerGui(player).open();
        }
        return 1;
    }
    
    private static int hire(CommandContext<CommandSourceStack> context, String typeStr, String name) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        WorkerType type;
        try {
            type = switch (typeStr.toLowerCase()) {
                case "farm", "farmhand", "farm_hand" -> WorkerType.FARM_HAND;
                case "miner", "mine" -> WorkerType.MINER;
                case "property", "manager", "property_manager" -> WorkerType.PROPERTY_MANAGER;
                default -> null;
            };
            
            if (type == null) {
                player.sendSystemMessage(Component.literal(
                    "§c§l[WORKERS] Invalid type! Use: farm, miner, or property"));
                return 0;
            }
            
            Worker worker = WorkerManager.hireWorker(player, type, name);
            return worker != null ? 1 : 0;
            
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Error hiring worker: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int fire(CommandContext<CommandSourceStack> context, String name) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        return WorkerManager.fireWorkerByName(player, name) ? 1 : 0;
    }
    
    private static int assign(CommandContext<CommandSourceStack> context, String name, String target) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        Worker worker = WorkerManager.getWorkerByName(player.getUUID(), name);
        if (worker == null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Worker '" + name + "' not found!"));
            return 0;
        }
        
        return WorkerManager.assignWorker(player, worker.getWorkerId(), target) ? 1 : 0;
    }
    
    private static int list(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        List<Worker> workers = WorkerManager.getPlayerWorkers(player.getUUID());
        
        if (workers.isEmpty()) {
            player.sendSystemMessage(Component.literal("§e§l[WORKERS] No workers hired"));
            return 0;
        }
        
        player.sendSystemMessage(Component.literal("§6§l=== Your Workers ==="));
        player.sendSystemMessage(Component.literal("§7Total: §e" + workers.size() + "/10"));
        player.sendSystemMessage(Component.literal("§7Daily Salaries: §6" + 
            CurrencyManager.format(WorkerManager.getDailySalaries(player.getUUID()))));
        player.sendSystemMessage(Component.literal(""));
        
        for (Worker worker : workers) {
            String loyaltyColor = worker.getLoyalty() >= 80 ? "§a" : 
                                 worker.getLoyalty() >= 60 ? "§e" : 
                                 worker.getLoyalty() >= 40 ? "§6" : 
                                 worker.getLoyalty() >= 20 ? "§c" : "§4";
            
            player.sendSystemMessage(Component.literal(
                "§e" + worker.getName() + " §7(" + worker.getType().getDisplayName() + 
                ") - " + loyaltyColor + worker.getLoyalty() + "% §7loyalty"));
            player.sendSystemMessage(Component.literal(
                "  §7Salary: §6" + CurrencyManager.format(worker.getDailySalary()) + 
                " §7| Assignment: " + (worker.isAssigned() ? "§a" + worker.getAssignedTo() : "§c✗ None")));
        }
        
        return 1;
    }
    
    private static int stats(CommandContext<CommandSourceStack> context, String name) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        
        Worker worker = WorkerManager.getWorkerByName(player.getUUID(), name);
        if (worker == null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WORKERS] Worker '" + name + "' not found!"));
            return 0;
        }
        
        player.sendSystemMessage(Component.literal("§6§l=== " + worker.getName() + " ==="));
        player.sendSystemMessage(Component.literal("§7Type: §f" + worker.getType().getDisplayName()));
        player.sendSystemMessage(Component.literal("§7Loyalty: §e" + worker.getLoyalty() + "%"));
        player.sendSystemMessage(Component.literal("§7Experience: §b" + worker.getExperience()));
        player.sendSystemMessage(Component.literal("§7Daily Salary: §6" + CurrencyManager.format(worker.getDailySalary())));
        player.sendSystemMessage(Component.literal("§7Assignment: " + 
            (worker.isAssigned() ? "§a" + worker.getAssignedTo() : "§c✗ None")));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§a§lSkills:"));
        
        for (WorkerSkill skill : WorkerSkill.values()) {
            if (skill.isApplicableTo(worker.getType())) {
                int level = worker.getSkillLevel(skill);
                player.sendSystemMessage(Component.literal(
                    "  §7" + skill.getDisplayName() + ": §e" + level + "/10"));
            }
        }
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Efficiency Bonus: §a+" + 
            String.format("%.1f", worker.getEfficiencyBonus() * 100) + "%"));
        
        return 1;
    }
}
