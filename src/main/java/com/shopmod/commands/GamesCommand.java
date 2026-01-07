package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.GamesGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class GamesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("games")
            .executes(GamesCommand::executeGames));
        
        dispatcher.register(Commands.literal("game")
            .executes(GamesCommand::executeGames));
        
        dispatcher.register(Commands.literal("play")
            .executes(GamesCommand::executeGames));
    }
    
    private static int executeGames(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new GamesGui(player).open();
            return 1;
        }
        return 0;
    }
}
