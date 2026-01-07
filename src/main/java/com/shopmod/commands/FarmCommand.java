package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shopmod.gui.FarmGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /farm command - Opens the Digital Farm System GUI
 */
public class FarmCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("farm")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new FarmGui(player).open();
                    return 1;
                })
        );
    }
}