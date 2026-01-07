package com.shopmod.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple teleportation system - /sethome and /home
 * Does not require OP permissions
 */
public class TeleportManager {
    private static final Map<UUID, HomeData> playerHomes = new ConcurrentHashMap<>();
    
    public static class HomeData {
        private BlockPos position;
        private String dimensionKey;
        
        public HomeData(BlockPos position, String dimensionKey) {
            this.position = position;
            this.dimensionKey = dimensionKey;
        }
        
        public BlockPos getPosition() { return position; }
        public String getDimensionKey() { return dimensionKey; }
    }
    
    /**
     * Set player's home location
     */
    public static void setHome(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        String dimension = player.level().dimension().toString();
        
        playerHomes.put(player.getUUID(), new HomeData(pos, dimension));
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a§lHOME SET! §7Use §b/home §7to teleport here"));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§7Location: §e" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
    }
    
    /**
     * Teleport player to their home
     */
    public static boolean teleportHome(ServerPlayer player) {
        HomeData home = playerHomes.get(player.getUUID());
        
        if (home == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYou haven't set a home yet! Use §b/sethome§c first."));
            return false;
        }
        
        String currentDimension = player.level().dimension().toString();
        
        if (!currentDimension.equals(home.getDimensionKey())) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cYour home is in a different dimension!"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7Home dimension: §e" + home.getDimensionKey()));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7Current dimension: §e" + currentDimension));
            return false;
        }
        
        BlockPos homePos = home.getPosition();
        player.teleportTo(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5);
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a§lWelcome home!"));
        
        return true;
    }
    
    /**
     * Check if player has a home set
     */
    public static boolean hasHome(UUID playerUUID) {
        return playerHomes.containsKey(playerUUID);
    }
    
    /**
     * Get player's home location
     */
    public static HomeData getHome(UUID playerUUID) {
        return playerHomes.get(playerUUID);
    }
}
