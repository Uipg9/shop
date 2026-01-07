package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.MiningGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class MiningCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mining")
            .executes(MiningCommand::executeMining));
        
        dispatcher.register(Commands.literal("mines")
            .executes(MiningCommand::executeMining));
    }
    
    private static int executeMining(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new MiningGui(player).open();
            return 1;
        }
        return 0;
    }
}
