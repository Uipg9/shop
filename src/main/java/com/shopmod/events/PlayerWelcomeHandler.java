package com.shopmod.events;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Handles welcome messages for players joining the server
 * Informs players about the Village Web Dashboard
 */
public class PlayerWelcomeHandler {
    
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Delay the message by 3 seconds to avoid interference with other join messages
            server.execute(() -> {
                try {
                    Thread.sleep(3000);
                    server.execute(() -> {
                        sendWelcomeMessage(handler.getPlayer());
                    });
                } catch (InterruptedException e) {
                    // If interrupted, send immediately
                    sendWelcomeMessage(handler.getPlayer());
                }
            });
        });
    }
    
    private static void sendWelcomeMessage(net.minecraft.server.level.ServerPlayer player) {
        // Header line
        Component header = Component.literal("🏘️ ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("Village Management").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal(" 🏘️").withStyle(ChatFormatting.YELLOW));
        
        // Info line
        Component info = Component.literal("📱 Manage your village from any web browser!")
            .withStyle(ChatFormatting.GREEN);
        
        // Link line  
        Component link = Component.literal("🌐 Open in browser: ")
            .withStyle(ChatFormatting.AQUA)
            .append(Component.literal("http://localhost:8080").withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));
        
        // Features line
        Component features = Component.literal("✨ Hire workers, manage resources, build structures & more!")
            .withStyle(ChatFormatting.LIGHT_PURPLE);
        
        // Footer line
        Component footer = Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            .withStyle(ChatFormatting.GRAY);
        
        // Send all messages
        player.sendSystemMessage(Component.empty()); // Empty line
        player.sendSystemMessage(footer);
        player.sendSystemMessage(header);
        player.sendSystemMessage(info);
        player.sendSystemMessage(link);
        player.sendSystemMessage(features);
        player.sendSystemMessage(footer);
        player.sendSystemMessage(Component.empty()); // Empty line
    }
}