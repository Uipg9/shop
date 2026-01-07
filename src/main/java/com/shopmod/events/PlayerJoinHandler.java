package com.shopmod.events;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles player join events - shows welcome message
 */
public class PlayerJoinHandler {
    
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            
            // Send welcome message after a short delay
            server.execute(() -> {
                try {
                    Thread.sleep(2000); // Wait 2 seconds for client to fully load
                    
                    player.sendSystemMessage(Component.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("  §6§l✦ Welcome to the Shop Mod! ✦"));
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("  §7Type §e§l/hub §7to access all features!"));
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("  §7Quick commands:"));
                    player.sendSystemMessage(Component.literal("  §8• §e/shop §7- Main marketplace"));
                    player.sendSystemMessage(Component.literal("  §8• §e/property §7- Real estate system"));
                    player.sendSystemMessage(Component.literal("  §8• §e/auction §7- Auction house"));
                    player.sendSystemMessage(Component.literal("  §8• §e/wand §7- Get your sell wand"));
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("  §7Earn money by: §aMining §8• §aChopping §8• §aKilling mobs"));
                    player.sendSystemMessage(Component.literal(""));
                    player.sendSystemMessage(Component.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
    }
}
