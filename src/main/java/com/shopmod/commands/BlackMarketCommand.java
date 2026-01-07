package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.shopmod.gui.BlackMarketGui;
import com.shopmod.research.ResearchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /blackmarket command - Opens the Black Market GUI
 */
public class BlackMarketCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("blackmarket")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    // Check if player has researched access
                    if (!ResearchManager.hasBlackMarketAccess(player.getUUID())) {
                        player.sendSystemMessage(Component.literal("§c§lACCESS DENIED"));
                        player.sendSystemMessage(Component.literal("§7Research §5Black Market Access §7in /research"));
                        return 0;
                    }
                    
                    new BlackMarketGui(player).open();
                    return 1;
                })
        );
    }
}
