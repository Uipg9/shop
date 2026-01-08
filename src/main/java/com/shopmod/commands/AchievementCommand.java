package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.achievements.AchievementGui;
import com.shopmod.achievements.AchievementManager;
import com.shopmod.achievements.AchievementProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command for viewing achievements
 */
public class AchievementCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("achievements")
            .executes(AchievementCommand::openGui));
        
        dispatcher.register(Commands.literal("achieve")
            .executes(AchievementCommand::openGui));
        
        dispatcher.register(Commands.literal("achievements")
            .then(Commands.literal("list")
                .executes(AchievementCommand::listUnlocked)));
        
        dispatcher.register(Commands.literal("achievements")
            .then(Commands.literal("progress")
                .executes(AchievementCommand::showProgress)));
    }
    
    private static int openGui(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new AchievementGui(player).open();
        }
        return 1;
    }
    
    private static int listUnlocked(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            AchievementProgress progress = AchievementManager.getProgress(player);
            int unlocked = AchievementManager.getUnlockedCount(player);
            int total = AchievementManager.getTotalCount();
            
            player.sendSystemMessage(Component.literal("§6§l=== Your Achievements ==="));
            player.sendSystemMessage(Component.literal("§7Unlocked: §e" + unlocked + " §7/ §6" + total));
            player.sendSystemMessage(Component.literal("§7Progress: §a" + (total > 0 ? (unlocked * 100 / total) : 0) + "%"));
            
            if (unlocked == 0) {
                player.sendSystemMessage(Component.literal("§7No achievements unlocked yet!"));
                player.sendSystemMessage(Component.literal("§7Use §e/achievements §7to see available achievements."));
            }
        }
        return 1;
    }
    
    private static int showProgress(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new AchievementGui(player).open();
        }
        return 1;
    }
}
