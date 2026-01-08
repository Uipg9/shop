package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.gui.TeleportGui;
import com.shopmod.teleport.TeleportManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TeleportCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main GUI - changed to /tele to avoid vanilla conflict
        dispatcher.register(Commands.literal("tele")
            .executes(TeleportCommand::executeTeleport));
        
        dispatcher.register(Commands.literal("warp")
            .executes(TeleportCommand::executeTeleport)
            .then(Commands.argument("x", IntegerArgumentType.integer())
                .then(Commands.argument("y", IntegerArgumentType.integer())
                    .then(Commands.argument("z", IntegerArgumentType.integer())
                        .executes(TeleportCommand::executeTpCoords)))));
        
        // Also register /tp for backwards compatibility (coordinate teleport only)
        dispatcher.register(Commands.literal("tp")
            .then(Commands.argument("x", IntegerArgumentType.integer())
                .then(Commands.argument("y", IntegerArgumentType.integer())
                    .then(Commands.argument("z", IntegerArgumentType.integer())
                        .executes(TeleportCommand::executeTpCoords)))));
        
        // Set waypoint
        dispatcher.register(Commands.literal("setwaypoint")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(TeleportCommand::executeSetWaypoint)));
        
        dispatcher.register(Commands.literal("sethome")
            .executes(ctx -> executeSetWaypointNamed(ctx, "home")));
        
        // Teleport to waypoint
        dispatcher.register(Commands.literal("waypoint")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(TeleportCommand::executeTpWaypoint)));
        
        dispatcher.register(Commands.literal("home")
            .executes(ctx -> executeTpWaypointNamed(ctx, "home")));
    }
    
    private static int executeTeleport(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new TeleportGui(player).open();
            return 1;
        }
        return 0;
    }
    
    private static int executeTpCoords(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            int x = IntegerArgumentType.getInteger(context, "x");
            int y = IntegerArgumentType.getInteger(context, "y");
            int z = IntegerArgumentType.getInteger(context, "z");
            
            TeleportManager.teleportToCoords(player, x, y, z);
            return 1;
        }
        return 0;
    }
    
    private static int executeSetWaypoint(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            String name = StringArgumentType.getString(context, "name");
            TeleportManager.createWaypoint(player, name);
            return 1;
        }
        return 0;
    }
    
    private static int executeSetWaypointNamed(CommandContext<CommandSourceStack> context, String name) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            TeleportManager.createWaypoint(player, name);
            return 1;
        }
        return 0;
    }
    
    private static int executeTpWaypoint(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            String name = StringArgumentType.getString(context, "name");
            TeleportManager.teleportToWaypoint(player, name);
            return 1;
        }
        return 0;
    }
    
    private static int executeTpWaypointNamed(CommandContext<CommandSourceStack> context, String name) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            TeleportManager.teleportToWaypoint(player, name);
            return 1;
        }
        return 0;
    }
}
