package com.shopmod.gui;

import com.shopmod.teleport.TeleportManager;
import com.shopmod.teleport.TeleportManager.Waypoint;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.Map;

/**
 * Teleport GUI - Manage waypoints and teleport
 */
public class TeleportGui extends SimpleGui {
    private final ServerPlayer player;
    private final TeleportManager.TeleportData data;
    private int page = 0;
    private static final int WAYPOINTS_PER_PAGE = 21;
    
    public TeleportGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.data = TeleportManager.getTeleportData(player.getUUID());
        this.setTitle(Component.literal("§5§l✈ Teleport System"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.ENDER_PEARL)
            .setName(Component.literal("§5§lTeleport Statistics"))
            .addLoreLine(Component.literal("§7Waypoints: §e" + data.getWaypoints().size() + "/20"))
            .addLoreLine(Component.literal("§7Total Teleports: §e" + data.getTotalTeleports()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lFREE TELEPORTATION!"))
        );
        
        // Create waypoint button
        setSlot(2, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§e§l+ Create Waypoint"))
            .addLoreLine(Component.literal("§7Save your current location"))
            .addLoreLine(Component.literal("§7as a waypoint"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Use: §e/setwaypoint <name>"))
            .addLoreLine(Component.literal("§7Example: §e/setwaypoint home"))
        );
        
        // Quick coordinates teleport
        setSlot(6, new GuiElementBuilder(Items.COMPASS)
            .setName(Component.literal("§b§lCoordinates Teleport"))
            .addLoreLine(Component.literal("§7Teleport to specific coords"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Use: §e/tp <x> <y> <z>"))
            .addLoreLine(Component.literal("§7Example: §e/tp 100 64 -200"))
        );
        
        // Display waypoints
        Map<String, Waypoint> waypoints = data.getWaypoints();
        int startIndex = page * WAYPOINTS_PER_PAGE;
        int slot = 10;
        int count = 0;
        
        for (Map.Entry<String, Waypoint> entry : waypoints.entrySet()) {
            if (count >= startIndex && count < startIndex + WAYPOINTS_PER_PAGE) {
                addWaypointButton(slot, entry.getKey(), entry.getValue());
                slot++;
                if (slot % 9 == 0 || slot % 9 == 8) slot += 2;
                if (slot >= 44) break;
            }
            count++;
        }
        
        // Navigation
        int totalPages = (int)Math.ceil(waypoints.size() / (double)WAYPOINTS_PER_PAGE);
        if (page > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§l← Previous Page"))
                .setCallback((index, type, action) -> {
                    page--;
                    updateDisplay();
                })
            );
        }
        
        if (totalPages > 0) {
            setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§6§lPage " + (page + 1) + "/" + Math.max(1, totalPages)))
            );
        }
        
        if (page < totalPages - 1) {
            setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§lNext Page →"))
                .setCallback((index, type, action) -> {
                    page++;
                    updateDisplay();
                })
            );
        }
        
        // Hub button
        setSlot(52, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void addWaypointButton(int slot, String name, Waypoint waypoint) {
        net.minecraft.core.BlockPos pos = waypoint.getPosition();
        
        setSlot(slot, new GuiElementBuilder(Items.END_CRYSTAL)
            .setName(Component.literal("§d§l" + name))
            .addLoreLine(Component.literal("§7Location:"))
            .addLoreLine(Component.literal("§7  X: §e" + pos.getX()))
            .addLoreLine(Component.literal("§7  Y: §e" + pos.getY()))
            .addLoreLine(Component.literal("§7  Z: §e" + pos.getZ()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lLEFT CLICK §7to teleport"))
            .addLoreLine(Component.literal("§c§lRIGHT CLICK §7to delete"))
            .setCallback((index, type, action) -> {
                if (type.isLeft) {
                    TeleportManager.teleportToWaypoint(player, name);
                    close();
                } else if (type.isRight) {
                    TeleportManager.deleteWaypoint(player, name);
                    updateDisplay();
                }
            })
            .glow()
        );
    }
    
    private void updateDisplay() {
        setupDisplay();
    }
}
