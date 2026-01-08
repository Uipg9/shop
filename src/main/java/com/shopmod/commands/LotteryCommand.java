package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.lottery.LotteryGui;
import com.shopmod.lottery.LotteryManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LotteryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lottery")
            .executes(LotteryCommand::openLottery)
            .then(Commands.literal("buy")
                .executes(LotteryCommand::buyTicket))
            .then(Commands.literal("info")
                .executes(LotteryCommand::showInfo))
        );
    }
    
    private static int openLottery(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            new LotteryGui(player).open();
            return 1;
        }
        return 0;
    }
    
    private static int buyTicket(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            LotteryManager.buyTicket(player);
            return 1;
        }
        return 0;
    }
    
    private static int showInfo(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
            long daysUntilDraw = LotteryManager.getDaysUntilDraw(currentDay);
            long jackpot = LotteryManager.getCurrentJackpot();
            
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            player.sendSystemMessage(Component.literal("§6§l✦ LOTTERY INFO ✦"));
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            player.sendSystemMessage(Component.literal("§7Current Jackpot: §6§l" + CurrencyManager.format(jackpot)));
            player.sendSystemMessage(Component.literal("§7Next Draw: §e" + daysUntilDraw + " days"));
            player.sendSystemMessage(Component.literal("§7Ticket Cost: §6$10,000"));
            player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            
            return 1;
        }
        return 0;
    }
}
