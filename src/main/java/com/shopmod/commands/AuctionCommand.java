package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shopmod.gui.AuctionGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /auction command - Opens the Auction House GUI
 */
public class AuctionCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("auction")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new AuctionGui(player).open();
                    return 1;
                })
        );
        
        // Alias
        dispatcher.register(
            Commands.literal("ah")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new AuctionGui(player).open();
                    return 1;
                })
        );
    }
}
