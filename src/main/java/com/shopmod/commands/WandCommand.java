package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.SellWandGui;
import com.shopmod.wand.SellWandManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class WandCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wand")
            .executes(WandCommand::executeWand));
        
        dispatcher.register(Commands.literal("sellwand")
            .executes(WandCommand::executeWand));
    }
    
    private static int executeWand(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new SellWandGui(player).open();
            return 1;
        }
        return 0;
    }
}
