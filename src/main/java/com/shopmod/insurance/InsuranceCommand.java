package com.shopmod.insurance;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

/**
 * Commands for insurance system
 */
public class InsuranceCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("insurance")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                new InsuranceGui(player).open();
                return 1;
            })
            .then(Commands.literal("buy")
                .then(Commands.argument("type", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        String typeStr = StringArgumentType.getString(context, "type").toUpperCase();
                        
                        try {
                            InsuranceType type = InsuranceType.valueOf(typeStr + "_INSURANCE");
                            InsuranceManager.purchasePolicy(player, type);
                        } catch (IllegalArgumentException e) {
                            player.sendSystemMessage(Component.literal(
                                "§c§l[INSURANCE] Invalid type! Options: property, farm, mine, business"));
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("claim")
                .then(Commands.argument("type", StringArgumentType.word())
                    .then(Commands.argument("amount", LongArgumentType.longArg(0))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String typeStr = StringArgumentType.getString(context, "type").toUpperCase();
                            long amount = LongArgumentType.getLong(context, "amount");
                            
                            try {
                                InsuranceType type = InsuranceType.valueOf(typeStr + "_INSURANCE");
                                InsuranceManager.fileClaim(player, type, 
                                    InsuranceClaim.ClaimType.OTHER, amount, "Manual claim via command");
                            } catch (IllegalArgumentException e) {
                                player.sendSystemMessage(Component.literal(
                                    "§c§l[INSURANCE] Invalid type! Options: property, farm, mine, business"));
                            }
                            return 1;
                        })
                    )
                )
            )
            .then(Commands.literal("list")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    player.sendSystemMessage(Component.literal("§9§l=== Your Insurance Policies ==="));
                    
                    long total = 0;
                    boolean hasAny = false;
                    for (InsurancePolicy policy : InsuranceManager.getPolicies(player.getUUID())) {
                        if (policy.isActive()) {
                            hasAny = true;
                            player.sendSystemMessage(Component.literal(
                                String.format("§a%s - §6$%,d §7/month", 
                                    policy.getType().getDisplayName(), policy.getMonthlyPremium())));
                            total += policy.getMonthlyPremium();
                        }
                    }
                    
                    if (!hasAny) {
                        player.sendSystemMessage(Component.literal("§7No active policies."));
                    } else {
                        player.sendSystemMessage(Component.literal(
                            String.format("§7Total Monthly Cost: §6$%,d", total)));
                    }
                    
                    return 1;
                })
            )
        );
        
        // Alias: /insure
        dispatcher.register(Commands.literal("insure")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                new InsuranceGui(player).open();
                return 1;
            })
        );
    }
}
