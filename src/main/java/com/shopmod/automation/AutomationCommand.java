package com.shopmod.automation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

/**
 * Commands for automation system
 */
public class AutomationCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("automation")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                new AutomationGui(player).open();
                return 1;
            })
            .then(Commands.literal("toggle")
                .then(Commands.argument("setting", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        String setting = StringArgumentType.getString(context, "setting");
                        AutomationManager.toggleSetting(player, setting);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("run")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    AutomationManager.processManualAutomation(player);
                    return 1;
                })
            )
        );
        
        // Alias: /auto
        dispatcher.register(Commands.literal("auto")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                new AutomationGui(player).open();
                return 1;
            })
        );
    }
}
