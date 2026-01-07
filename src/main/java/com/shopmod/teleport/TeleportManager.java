package com.shopmod.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Teleport system - FREE waypoints and teleportation
 */
public class TeleportManager {
    private static final Map<UUID, TeleportData> playerData = new ConcurrentHashMap<>();
    
    public static class Waypoint {
        private final String name;
        private final BlockPos position;
        private final String dimension;
        private final long createdTime;
        
        public Waypoint(String name, BlockPos position, String dimension, long createdTime) {
            this.name = name;
            this.position = position;
            this.dimension = dimension;
            this.createdTime = createdTime;
        }
        
        public String getName() { return name; }
        public BlockPos getPosition() { return position; }
        public String getDimension() { return dimension; }
        public long getCreatedTime() { return createdTime; }
    }
    
    public static class TeleportData {
        private final Map<String, Waypoint> waypoints = new HashMap<>();
        private long totalTeleports = 0;
        
        public Map<String, Waypoint> getWaypoints() { return new HashMap<>(waypoints); }
        public void addWaypoint(String name, Waypoint waypoint) { waypoints.put(name, waypoint); }
        public void removeWaypoint(String name) { waypoints.remove(name); }
        public boolean hasWaypoint(String name) { return waypoints.containsKey(name); }
        public Waypoint getWaypoint(String name) { return waypoints.get(name); }
        public long getTotalTeleports() { return totalTeleports; }
        public void incrementTeleports() { totalTeleports++; }
    }
    
    public static TeleportData getTeleportData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> new TeleportData());
    }
    
    /**
     * Create a new waypoint at player's current location
     */
    public static boolean createWaypoint(ServerPlayer player, String name) {
        TeleportData data = getTeleportData(player.getUUID());
        
        if (data.hasWaypoint(name)) {
            player.sendSystemMessage(Component.literal("§c§l[TELEPORT] Waypoint '" + name + "' already exists!"));
            return false;
        }
        
        if (data.getWaypoints().size() >= 20) {
            player.sendSystemMessage(Component.literal("§c§l[TELEPORT] Maximum waypoints reached (20)!"));
            return false;
        }
        
        BlockPos pos = player.blockPosition();
        String dimension = player.level().dimension().toString();
        
        Waypoint waypoint = new Waypoint(name, pos, dimension, System.currentTimeMillis());
        data.addWaypoint(name, waypoint);
        
        player.sendSystemMessage(Component.literal("§a§l[TELEPORT] Waypoint '" + name + "' created!"));
        player.sendSystemMessage(Component.literal("§7Location: §e" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        
        return true;
    }
    
    /**
     * Teleport player to waypoint - FREE!
     */
    public static boolean teleportToWaypoint(ServerPlayer player, String name) {
        TeleportData data = getTeleportData(player.getUUID());
        
        if (!data.hasWaypoint(name)) {
            player.sendSystemMessage(Component.literal("§c§l[TELEPORT] Waypoint '" + name + "' not found!"));
            return false;
        }
        
        Waypoint waypoint = data.getWaypoint(name);
        BlockPos pos = waypoint.getPosition();
        
        // Teleport player
        player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        data.incrementTeleports();
        
        player.sendSystemMessage(Component.literal("§a§l[TELEPORT] Teleported to '" + name + "'!"));
        player.playSound(net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        
        return true;
    }
    
    /**
     * Delete a waypoint
     */
    public static boolean deleteWaypoint(ServerPlayer player, String name) {
        TeleportData data = getTeleportData(player.getUUID());
        
        if (!data.hasWaypoint(name)) {
            player.sendSystemMessage(Component.literal("§c§l[TELEPORT] Waypoint '" + name + "' not found!"));
            return false;
        }
        
        data.removeWaypoint(name);
        player.sendSystemMessage(Component.literal("§a§l[TELEPORT] Waypoint '" + name + "' deleted!"));
        
        return true;
    }
    
    /**
     * Quick teleport to coordinates - FREE!
     */
    public static void teleportToCoords(ServerPlayer player, int x, int y, int z) {
        player.teleportTo(x + 0.5, y, z + 0.5);
        
        TeleportData data = getTeleportData(player.getUUID());
        data.incrementTeleports();
        
        player.sendSystemMessage(Component.literal("§a§l[TELEPORT] Teleported to " + x + ", " + y + ", " + z + "!"));
        player.playSound(net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}
