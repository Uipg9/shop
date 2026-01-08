package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.statistics.StatisticsGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command for viewing statistics
 */
public class StatisticsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("statistics")
            .executes(StatisticsCommand::openGui));
        
        dispatcher.register(Commands.literal("stats")
            .executes(StatisticsCommand::openGui));
    }
    
    private static int openGui(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new StatisticsGui(player).open();
        }
        return 1;
    }
}
