package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.VillageGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class VillageCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("village")
            .executes(VillageCommand::executeVillage));
        
        dispatcher.register(Commands.literal("villagers")
            .executes(VillageCommand::executeVillage));
    }
    
    private static int executeVillage(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new VillageGui(player).open();
            return 1;
        }
        return 0;
    }
}
