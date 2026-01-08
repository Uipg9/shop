package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.business.BusinessGui;
import com.shopmod.business.BusinessManager;
import com.shopmod.currency.CurrencyManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class BusinessCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("business")
            .executes(BusinessCommand::openBusiness)
            .then(Commands.literal("buy")
                .then(Commands.argument("type", StringArgumentType.word())
                    .executes(BusinessCommand::buyBusiness)))
            .then(Commands.literal("collect")
                .executes(BusinessCommand::collectAll))
            .then(Commands.literal("list")
                .executes(BusinessCommand::listBusinesses))
        );
    }
    
    private static int openBusiness(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new BusinessGui(player).open();
            return 1;
        }
        return 0;
    }
    
    private static int buyBusiness(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            String typeStr = StringArgumentType.getString(context, "type").toUpperCase();
            
            try {
                BusinessManager.BusinessType type = BusinessManager.BusinessType.valueOf(typeStr);
                BusinessManager.buyBusiness(player, type);
            } catch (IllegalArgumentException e) {
                player.sendSystemMessage(Component.literal("§c§l[BUSINESS] Invalid business type!"));
                player.sendSystemMessage(Component.literal("§7Available: RESTAURANT, CASINO, STOCK_BROKERAGE, INSURANCE_AGENCY, BANK_BRANCH, MINING_COMPANY, FARM_CONGLOMERATE"));
            }
            return 1;
        }
        return 0;
    }
    
    private static int collectAll(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            var businesses = BusinessManager.getPlayerBusinesses(player.getUUID());
            
            if (businesses.isEmpty()) {
                player.sendSystemMessage(Component.literal("§c§l[BUSINESS] You don't own any businesses!"));
                return 0;
            }
            
            for (var business : businesses) {
                BusinessManager.collectIncome(player, business.getBusinessId());
            }
            
            return 1;
        }
        return 0;
    }
    
    private static int listBusinesses(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            var businesses = BusinessManager.getPlayerBusinesses(player.getUUID());
            
            if (businesses.isEmpty()) {
                player.sendSystemMessage(Component.literal("§c§l[BUSINESS] You don't own any businesses!"));
                return 0;
            }
            
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            player.sendSystemMessage(Component.literal("§6§l✦ YOUR BUSINESSES ✦"));
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            
            for (var business : businesses) {
                long income = business.getType().getDailyIncome(business.getLevel());
                player.sendSystemMessage(Component.literal("§a" + business.getType().getDisplayName() + " §7(Level " + business.getLevel() + ")"));
                player.sendSystemMessage(Component.literal("  §7Daily: §a+" + CurrencyManager.format(income)));
            }
            
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§7Total Daily Income: §a+" + CurrencyManager.format(BusinessManager.getDailyIncome(player.getUUID()))));
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            
            return 1;
        }
        return 0;
    }
}
