package com.shopmod.upgrades;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Applies upgrade effects to players periodically
 */
public class UpgradeEffectApplier {
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 20 * 10; // Every 10 seconds
    
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            
            if (tickCounter >= UPDATE_INTERVAL) {
                tickCounter = 0;
                
                // Apply effects to all online players
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    UpgradeManager.applyMiningSpeed(player);
                    UpgradeManager.applyHealthBoost(player);
                    UpgradeManager.applyLuck(player);
                    UpgradeManager.applyRegeneration(player);
                    UpgradeManager.applyFireResistance(player);
                    UpgradeManager.applyWaterBreathing(player);
                    UpgradeManager.applyNightVision(player);
                }
            }
        });
    }
}
