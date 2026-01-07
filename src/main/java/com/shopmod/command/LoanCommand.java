package com.shopmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.loan.LoanManager;
import com.shopmod.gui.LoanGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Loan command - calculator, take loans, view status, request delays
 */
public class LoanCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("loan")
            .executes(LoanCommand::openGui)
            .then(Commands.literal("gui")
                .executes(LoanCommand::openGui))
            .then(Commands.literal("status")
                .executes(LoanCommand::showStatus))
            .then(Commands.literal("calculator")
                .executes(LoanCommand::showCalculator))
            .then(Commands.literal("calc")
                .executes(LoanCommand::showCalculator))
            .then(Commands.literal("take")
                .then(Commands.argument("amount", LongArgumentType.longArg(100))
                    .then(Commands.argument("days", IntegerArgumentType.integer(1, 365))
                        .executes(LoanCommand::takeLoan))))
            .then(Commands.literal("delay")
                .executes(LoanCommand::requestDelay))
            .then(Commands.literal("pay")
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                    .executes(LoanCommand::makePayment))
                .executes(LoanCommand::makeFullPayment))
            .then(Commands.literal("help")
                .executes(LoanCommand::showHelp))
        );
    }
    
    private static int openGui(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        new LoanGui(player).open();
        return 1;
    }
    
    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        if (!LoanManager.hasActiveLoan(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§e§l=== NO ACTIVE LOAN ==="));
            player.sendSystemMessage(Component.literal("§7You don't have any active loans."));
            player.sendSystemMessage(Component.literal("§7Use §b/loan calculator §7to see loan options"));
            player.sendSystemMessage(Component.literal("§7Use §b/loan take <amount> <days> §7to take a loan"));
            return 1;
        }
        
        LoanManager.LoanData loan = LoanManager.getLoan(player.getUUID());
        int creditScore = LoanManager.calculateCreditScore(player.getUUID());
        
        player.sendSystemMessage(Component.literal("§6§l=== ACTIVE LOAN ==="));
        player.sendSystemMessage(Component.literal("§7Original Amount: §6" + 
            CurrencyManager.format(loan.getPrincipalAmount())));
        player.sendSystemMessage(Component.literal("§7Remaining Balance: §c" + 
            CurrencyManager.format(loan.getRemainingBalance())));
        player.sendSystemMessage(Component.literal("§7Daily Payment: §6" + 
            CurrencyManager.format(loan.getDailyPayment())));
        player.sendSystemMessage(Component.literal("§7Interest Rate: §e" + 
            String.format("%.1f%%", loan.getInterestRate() * 100) + " per day"));
        player.sendSystemMessage(Component.literal("§7Total Paid So Far: §a" + 
            CurrencyManager.format(loan.getTotalPaid())));
        player.sendSystemMessage(Component.literal("§7Credit Score: §e" + creditScore + "/100"));
        
        if (loan.getMissedPayments() > 0) {
            player.sendSystemMessage(Component.literal("§c§lWARNING: " + loan.getMissedPayments() + 
                " missed payment(s)! Penalties apply!"));
        }
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Commands:"));
        player.sendSystemMessage(Component.literal("§8• §b/loan delay §7- Delay today's payment (10% fee)"));
        player.sendSystemMessage(Component.literal("§8• §b/loan calculator §7- View loan calculator"));
        
        return 1;
    }
    
    private static int showCalculator(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        int creditScore = LoanManager.calculateCreditScore(player.getUUID());
        double interestRate = LoanManager.calculateInterestRate(creditScore);
        long maxLoan = LoanManager.getMaxLoanAmount(player);
        
        player.sendSystemMessage(Component.literal("§6§l=== LOAN CALCULATOR ==="));
        player.sendSystemMessage(Component.literal("§7Your Credit Score: §e" + creditScore + "/100"));
        player.sendSystemMessage(Component.literal("§7Your Interest Rate: §e" + 
            String.format("%.1f%%", interestRate * 100) + " per day"));
        player.sendSystemMessage(Component.literal("§7Maximum Loan: §6" + CurrencyManager.format(maxLoan)));
        player.sendSystemMessage(Component.literal(""));
        
        player.sendSystemMessage(Component.literal("§e§lLOAN EXAMPLES:"));
        
        // Show examples at different amounts and durations
        long[] amounts = {1000L, 5000L, 10000L, maxLoan / 4, maxLoan / 2};
        int[] durations = {7, 14, 30};
        
        for (long amount : amounts) {
            if (amount > maxLoan) continue;
            
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6Loan: " + CurrencyManager.format(amount)));
            
            for (int days : durations) {
                long totalToRepay = (long)(amount * (1.0 + (interestRate * days)));
                long dailyPayment = totalToRepay / days;
                long totalInterest = totalToRepay - amount;
                
                player.sendSystemMessage(Component.literal(
                    "  §7" + days + " days: §6" + CurrencyManager.format(dailyPayment) + 
                    "§7/day (§c+" + CurrencyManager.format(totalInterest) + " §7interest)"));
            }
        }
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7To take a loan: §b/loan take <amount> <days>"));
        player.sendSystemMessage(Component.literal("§8Example: /loan take 5000 14"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7💡 Tip: Invest in §b/bank §7to improve your credit score!"));
        
        return 1;
    }
    
    private static int takeLoan(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        long amount = LongArgumentType.getLong(ctx, "amount");
        int days = IntegerArgumentType.getInteger(ctx, "days");
        
        LoanManager.takeLoan(player, amount, days);
        return 1;
    }
    
    private static int requestDelay(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        LoanManager.requestDelay(player);
        return 1;
    }
    
    private static int showHelp(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        player.sendSystemMessage(Component.literal("§6§l=== LOAN SYSTEM HELP ==="));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§e§lCommands:"));
        player.sendSystemMessage(Component.literal("§b/loan §7- View your loan status"));
        player.sendSystemMessage(Component.literal("§b/loan calculator §7- See loan rates and examples"));
        player.sendSystemMessage(Component.literal("§b/loan take <amount> <days> §7- Take out a loan"));
        player.sendSystemMessage(Component.literal("§b/loan pay <amount> §7- Make manual payment"));
        player.sendSystemMessage(Component.literal("§b/loan pay §7- Pay off entire loan"));
        player.sendSystemMessage(Component.literal("§b/loan delay §7- Delay today's payment (10% fee)"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§e§lHow It Works:"));
        player.sendSystemMessage(Component.literal("§7• Your credit score depends on bank investments"));
        player.sendSystemMessage(Component.literal("§7• Better credit = lower interest rates"));
        player.sendSystemMessage(Component.literal("§7• Payments are due every Minecraft day"));
        player.sendSystemMessage(Component.literal("§7• Payments auto-withdraw from bank if wallet insufficient"));
        player.sendSystemMessage(Component.literal("§7• Miss 1 payment = double interest next day"));
        player.sendSystemMessage(Component.literal("§7• Miss 2+ payments = triple interest + penalty"));
        player.sendSystemMessage(Component.literal("§7• Can delay 1 day for 10% fee (request in advance)"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7💡 Invest in §b/bank §7to improve your credit score!"));
        
        return 1;
    }
    
    private static int makePayment(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        long amount = LongArgumentType.getLong(ctx, "amount");
        LoanManager.makeManualPayment(player, amount);
        return 1;
    }
    
    private static int makeFullPayment(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        if (!LoanManager.hasActiveLoan(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§c§l[LOAN] You don't have an active loan!"));
            return 0;
        }
        
        LoanManager.LoanData loan = LoanManager.getLoan(player.getUUID());
        LoanManager.makeManualPayment(player, loan.getRemainingBalance());
        return 1;
    }
}
