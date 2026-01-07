package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shopmod.gui.PropertyGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /property command - Opens the Property/Real Estate GUI
 */
public class PropertyCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("property")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new PropertyGui(player).open();
                    return 1;
                })
        );
        
        // Alias
        dispatcher.register(
            Commands.literal("realestate")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new PropertyGui(player).open();
                    return 1;
                })
        );
    }
}
