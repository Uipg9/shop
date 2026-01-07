package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shopmod.gui.StocksGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /stocks command - Opens the Stock Options Trading GUI
 */
public class StocksCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("stocks")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new StocksGui(player).open();
                    return 1;
                })
        );
        
        // Alias
        dispatcher.register(
            Commands.literal("options")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new StocksGui(player).open();
                    return 1;
                })
        );
    }
}
