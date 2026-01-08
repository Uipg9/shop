package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.perks.PerkManager;
import com.shopmod.perks.PerkShopGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Command for perk shop
 */
public class PerkCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("perks")
            .executes(PerkCommand::openShop));
        
        dispatcher.register(Commands.literal("perks")
            .then(Commands.literal("active")
                .executes(PerkCommand::showActive)));
    }
    
    private static int openShop(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new PerkShopGui(player).open();
        }
        return 1;
    }
    
    private static int showActive(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            PerkManager.PlayerPerks perks = PerkManager.getPerks(player);
            Map<PerkManager.TemporaryBooster, Long> active = perks.getActiveBoosters();
            
            player.sendSystemMessage(Component.literal("§d§l=== Active Boosters ==="));
            
            if (active.isEmpty()) {
                player.sendSystemMessage(Component.literal("§7No active boosters."));
            } else {
                for (Map.Entry<PerkManager.TemporaryBooster, Long> entry : active.entrySet()) {
                    long remaining = perks.getBoosterTimeRemaining(entry.getKey());
                    player.sendSystemMessage(Component.literal("§e" + entry.getKey().getName() + 
                        " §7- §a" + formatTime(remaining)));
                }
            }
            
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§7Owned Perks: §d" + perks.getOwnedPerks().size()));
        }
        return 1;
    }
    
    private static String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes < 60) return minutes + "m " + secs + "s";
        long hours = minutes / 60;
        long mins = minutes % 60;
        return hours + "h " + mins + "m";
    }
}
