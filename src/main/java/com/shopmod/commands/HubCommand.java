package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.HubGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class HubCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hub")
            .executes(HubCommand::openHub));
        
        dispatcher.register(Commands.literal("menu")
            .executes(HubCommand::openHub));
        
        dispatcher.register(Commands.literal("gui")
            .executes(HubCommand::openHub));
    }
    
    private static int openHub(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new HubGui(player).open();
            return 1;
        }
        return 0;
    }
}
