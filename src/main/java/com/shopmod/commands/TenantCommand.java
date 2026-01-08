package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.TenantGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TenantCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tenant")
            .executes(TenantCommand::executeTenant));
        
        dispatcher.register(Commands.literal("tenants")
            .executes(TenantCommand::executeTenant));
    }
    
    private static int executeTenant(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new TenantGui(player).open();
            return 1;
        }
        return 0;
    }
}
