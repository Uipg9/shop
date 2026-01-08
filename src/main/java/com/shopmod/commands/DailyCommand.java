package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.daily.DailyRewardGui;
import com.shopmod.daily.DailyRewardManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command for daily rewards
 */
public class DailyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("daily")
            .executes(DailyCommand::openGui));
        
        dispatcher.register(Commands.literal("daily")
            .then(Commands.literal("claim")
                .executes(DailyCommand::quickClaim)));
    }
    
    private static int openGui(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new DailyRewardGui(player).open();
        }
        return 1;
    }
    
    private static int quickClaim(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            DailyRewardManager.claimReward(player);
        }
        return 1;
    }
}
