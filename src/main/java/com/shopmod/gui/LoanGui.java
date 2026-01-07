package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.loan.LoanManager;
import com.shopmod.shop.ShopTier;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.UUID;

/**
 * Loan GUI - Borrow money with credit scoring
 */
public class LoanGui extends SimpleGui {
    private final ServerPlayer player;
    private final UUID playerUUID;
    
    public LoanGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.playerUUID = player.getUUID();
        this.setTitle(Component.literal("§6§lBank Loans"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        // Credit score info
        int creditScore = LoanManager.calculateCreditScore(playerUUID);
        double interestRate = LoanManager.calculateInterestRate(creditScore);
        
        setSlot(4, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lCredit Score: " + creditScore))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Rating: " + getCreditRating(creditScore)))
            .addLoreLine(Component.literal("§7Interest Rate: §c" + String.format("%.1f%%", interestRate * 100)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Improve credit by:"))
            .addLoreLine(Component.literal("§7  • Investing in bank"))
            .addLoreLine(Component.literal("§7  • Making timely payments"))
        );
        
        // Current loan status
        if (LoanManager.hasActiveLoan(playerUUID)) {
            displayActiveLoan();
        } else {
            displayLoanOffers();
        }
        
        // Hub button
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void displayActiveLoan() {
        LoanManager.LoanData loan = LoanManager.getLoan(playerUUID);
        
        setSlot(13, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§6§lActive Loan"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Principal: §e$" + CurrencyManager.format(loan.getPrincipalAmount())))
            .addLoreLine(Component.literal("§7Remaining: §c$" + CurrencyManager.format(loan.getRemainingBalance())))
            .addLoreLine(Component.literal("§7Daily Payment: §6$" + CurrencyManager.format(loan.getDailyPayment())))
            .addLoreLine(Component.literal("§7Interest Rate: §e" + String.format("%.1f%%", loan.getInterestRate() * 100)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Paid: §a$" + CurrencyManager.format(loan.getTotalPaid())))
            .addLoreLine(Component.literal("§7Missed Payments: " + (loan.getMissedPayments() > 0 ? "§c" : "§a") + loan.getMissedPayments()))
        );
        
        // Pay now button
        setSlot(29, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§a§lPay Daily Payment"))
            .addLoreLine(Component.literal("§7Amount: §6$" + CurrencyManager.format(loan.getDailyPayment())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Remaining after: §e$" + 
                CurrencyManager.format(Math.max(0, loan.getRemainingBalance() - loan.getDailyPayment()))))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to pay"))
            .setCallback((index, type, action) -> {
                LoanManager.makeManualPayment(player, loan.getDailyPayment());
                updateDisplay();
            })
        );
        
        // Delay payment button
        if (!loan.isDelayRequested()) {
            setSlot(30, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("§e§lDelay Payment"))
                .addLoreLine(Component.literal("§7Skip today's payment"))
                .addLoreLine(Component.literal("§cFee: " + String.format("$%.0f", loan.getDailyPayment() * 0.10)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Can only delay once per day"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK §7to delay"))
                .setCallback((index, type, action) -> {
                    if (LoanManager.requestDelay(player)) {
                        player.sendSystemMessage(Component.literal("§e§l[LOAN] Payment delayed. Fee applied."));
                    } else {
                        player.sendSystemMessage(Component.literal("§c§l[LOAN] Already delayed today!"));
                    }
                    updateDisplay();
                })
            );
        } else {
            setSlot(30, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lDelay Used"))
                .addLoreLine(Component.literal("§7Already delayed today"))
            );
        }
        
        // Pay off button
        setSlot(31, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lPay Off Loan"))
            .addLoreLine(Component.literal("§7Pay remaining balance"))
            .addLoreLine(Component.literal("§7Amount: §6$" + CurrencyManager.format(loan.getRemainingBalance())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lCLICK §7to pay off"))
            .setCallback((index, type, action) -> {
                LoanManager.makeManualPayment(player, loan.getRemainingBalance());
                updateDisplay();
            })
        );
    }
    
    private void displayLoanOffers() {
        setSlot(13, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lNo Active Loan"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Choose a loan amount below"))
            .addLoreLine(Component.literal("§7to get started!"))
        );
        
        // Loan offers based on tier
        long maxLoan = LoanManager.getMaxLoanAmount(player);
        
        // Small loan (25% of max)
        long smallLoan = maxLoan / 4;
        setSlot(20, createLoanOffer(smallLoan, "Small Loan"));
        
        // Medium loan (50% of max)
        long mediumLoan = maxLoan / 2;
        setSlot(22, createLoanOffer(mediumLoan, "Medium Loan"));
        
        // Large loan (100% of max)
        setSlot(24, createLoanOffer(maxLoan, "Large Loan"));
    }
    
    private GuiElementBuilder createLoanOffer(long amount, String name) {
        int creditScore = LoanManager.calculateCreditScore(playerUUID);
        double interestRate = LoanManager.calculateInterestRate(creditScore);
        int loanTermDays = 30;
        long totalToRepay = (long)(amount * (1.0 + (interestRate * loanTermDays)));
        long dailyPayment = totalToRepay / loanTermDays;
        
        return new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§6§l" + name))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Amount: §e$" + CurrencyManager.format(amount)))
            .addLoreLine(Component.literal("§7Interest: §c" + String.format("%.1f%%", interestRate * 100)))
            .addLoreLine(Component.literal("§7Term: §e" + loanTermDays + " days"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Daily Payment: §6$" + CurrencyManager.format(dailyPayment)))
            .addLoreLine(Component.literal("§7Total Repay: §e$" + CurrencyManager.format(dailyPayment * loanTermDays)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lCLICK §7to take loan"))
            .setCallback((index, type, action) -> {
                if (LoanManager.takeLoan(player, amount, loanTermDays)) {
                    player.sendSystemMessage(Component.literal("§a§l[LOAN] Loan approved!"));
                    player.sendSystemMessage(Component.literal("§7Amount: §e$" + CurrencyManager.format(amount)));
                    updateDisplay();
                } else {
                    player.sendSystemMessage(Component.literal("§c§l[LOAN] Loan denied!"));
                }
            });
    }
    
    private String getCreditRating(int score) {
        if (score >= 90) return "§a§lExcellent";
        if (score >= 75) return "§2§lGood";
        if (score >= 50) return "§e§lFair";
        if (score >= 30) return "§6§lPoor";
        return "§c§lVery Poor";
    }
}
