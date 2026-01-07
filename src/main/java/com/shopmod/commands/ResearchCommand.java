package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shopmod.gui.ResearchGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /research command - Opens the Research & Upgrades GUI
 */
public class ResearchCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("research")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new ResearchGui(player).open();
                    return 1;
                })
        );
        
        // Alias
        dispatcher.register(
            Commands.literal("upgrades")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new ResearchGui(player).open();
                    return 1;
                })
        );
    }
}
