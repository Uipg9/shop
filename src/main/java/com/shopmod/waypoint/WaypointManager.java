package com.shopmod.waypoint;

import com.shopmod.teleport.TeleportManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

/**
 * Enhanced Waypoint Manager with custom waypoints
 * Note: Basic functionality already exists in TeleportManager,
 * this extends it with additional features
 */
public class WaypointManager {
    
    /**
     * Save a custom waypoint with validation
     */
    public static boolean saveCustomWaypoint(ServerPlayer player, String name, BlockPos pos) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            player.sendSystemMessage(Component.literal("§c§l[WAYPOINT] Waypoint name cannot be empty!"));
            return false;
        }
        
        if (name.length() > 20) {
            player.sendSystemMessage(Component.literal("§c§l[WAYPOINT] Waypoint name too long (max 20 characters)!"));
            return false;
        }
        
        // Use TeleportManager for actual storage
        return TeleportManager.createWaypoint(player, name);
    }
    
    /**
     * Get all waypoints for a player
     */
    public static Map<String, TeleportManager.Waypoint> getWaypoints(UUID playerUUID) {
        return TeleportManager.getTeleportData(playerUUID).getWaypoints();
    }
    
    /**
     * Teleport to waypoint by name
     */
    public static boolean teleportToWaypoint(ServerPlayer player, String name) {
        return TeleportManager.teleportToWaypoint(player, name);
    }
    
    /**
     * Delete waypoint by name
     */
    public static boolean deleteWaypoint(ServerPlayer player, String name) {
        return TeleportManager.deleteWaypoint(player, name);
    }
    
    /**
     * Teleport to coordinates
     */
    public static void teleportToCoordinates(ServerPlayer player, int x, int y, int z) {
        TeleportManager.teleportToCoords(player, x, y, z);
    }
}
