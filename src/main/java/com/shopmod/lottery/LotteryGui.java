package com.shopmod.lottery;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.gui.HubGui;
import com.shopmod.lottery.LotteryManager.*;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.*;

/**
 * Lottery GUI - 9x6 interface
 * View modes: BUY_TICKETS, MY_TICKETS, LAST_DRAW, JACKPOT_INFO
 */
public class LotteryGui extends SimpleGui {
    private final ServerPlayer player;
    private final PlayerLotteryData data;
    private ViewMode currentView = ViewMode.JACKPOT_INFO;
    private final List<Integer> selectedNumbers = new ArrayList<>();
    private long currentDay;
    
    private enum ViewMode {
        BUY_TICKETS, MY_TICKETS, LAST_DRAW, JACKPOT_INFO
    }
    
    public LotteryGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.data = LotteryManager.getPlayerData(player.getUUID());
        this.currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        this.setTitle(Component.literal("§6§l✦ Lottery ✦"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        // Background
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        // Tabs
        setupTabs();
        
        // Display based on current view
        switch (currentView) {
            case BUY_TICKETS:
                showBuyTickets();
                break;
            case MY_TICKETS:
                showMyTickets();
                break;
            case LAST_DRAW:
                showLastDraw();
                break;
            case JACKPOT_INFO:
                showJackpotInfo();
                break;
        }
        
        // Navigation buttons
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
        
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void setupTabs() {
        setSlot(45, new GuiElementBuilder(currentView == ViewMode.JACKPOT_INFO ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
            .setName(Component.literal("§6§lJackpot Info"))
            .addLoreLine(Component.literal("§7View current jackpot"))
            .addLoreLine(Component.literal("§7and draw information"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.JACKPOT_INFO;
                updateDisplay();
            })
        );
        
        setSlot(46, new GuiElementBuilder(currentView == ViewMode.BUY_TICKETS ? Items.PAPER : Items.MAP)
            .setName(Component.literal("§e§lBuy Tickets"))
            .addLoreLine(Component.literal("§7Purchase lottery tickets"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.BUY_TICKETS;
                selectedNumbers.clear();
                updateDisplay();
            })
        );
        
        setSlot(47, new GuiElementBuilder(currentView == ViewMode.MY_TICKETS ? Items.WRITABLE_BOOK : Items.BOOK)
            .setName(Component.literal("§a§lMy Tickets"))
            .addLoreLine(Component.literal("§7View your active tickets"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.MY_TICKETS;
                updateDisplay();
            })
        );
        
        setSlot(48, new GuiElementBuilder(currentView == ViewMode.LAST_DRAW ? Items.CLOCK : Items.COMPASS)
            .setName(Component.literal("§d§lLast Draw"))
            .addLoreLine(Component.literal("§7View last draw results"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.LAST_DRAW;
                updateDisplay();
            })
        );
    }
    
    private void showJackpotInfo() {
        this.setTitle(Component.literal("§6§l✦ Lottery - Jackpot Info ✦"));
        
        long daysUntilDraw = LotteryManager.getDaysUntilDraw(currentDay);
        long jackpot = LotteryManager.getCurrentJackpot();
        
        setSlot(22, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§6§l§nCURRENT JACKPOT"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§l" + CurrencyManager.format(jackpot)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Match all 6 numbers to win!"))
            .glow()
        );
        
        setSlot(20, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal("§e§lNext Draw"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7In §e" + daysUntilDraw + " days"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Draws every 7 days"))
        );
        
        setSlot(24, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§a§lTicket Cost"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6$10,000 §7per ticket"))
            .addLoreLine(Component.literal("§7Max 10 tickets per player"))
        );
        
        setSlot(29, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lPrize Tiers"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6§lMatch 6: §a§lJACKPOT"))
            .addLoreLine(Component.literal("§e§lMatch 5: §a$100,000"))
            .addLoreLine(Component.literal("§e§lMatch 4: §a$25,000"))
            .addLoreLine(Component.literal("§e§lMatch 3: §a$5,000"))
            .addLoreLine(Component.literal("§e§lMatch 2: §a$1,000"))
            .addLoreLine(Component.literal("§e§lMatch 1: §aFREE TICKET"))
        );
        
        setSlot(31, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§d§lOdds"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Match 6: " + LotteryManager.getOdds(6)))
            .addLoreLine(Component.literal("§7Match 5: " + LotteryManager.getOdds(5)))
            .addLoreLine(Component.literal("§7Match 4: " + LotteryManager.getOdds(4)))
            .addLoreLine(Component.literal("§7Match 3: " + LotteryManager.getOdds(3)))
            .addLoreLine(Component.literal("§7Match 2: " + LotteryManager.getOdds(2)))
            .addLoreLine(Component.literal("§7Match 1: " + LotteryManager.getOdds(1)))
        );
        
        setSlot(33, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lYour Stats"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Active Tickets: §e" + data.getActiveTickets().size()))
            .addLoreLine(Component.literal("§7Total Spent: §c" + CurrencyManager.format(data.getTotalSpent())))
            .addLoreLine(Component.literal("§7Total Won: §a" + CurrencyManager.format(data.getTotalWon())))
            .addLoreLine(Component.literal("§7Tickets Played: §e" + data.getTicketsPlayed()))
        );
    }
    
    private void showBuyTickets() {
        this.setTitle(Component.literal("§e§l✦ Lottery - Buy Tickets ✦"));
        
        setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance: §a" + CurrencyManager.format(CurrencyManager.getBalance(player))))
            .addLoreLine(Component.literal("§7Ticket Cost: §6$10,000"))
            .addLoreLine(Component.literal("§7Active Tickets: §e" + data.getActiveTickets().size() + "/10"))
        );
        
        // Quick buy button
        setSlot(13, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§a§l§n⭐ QUICK BUY"))
            .addLoreLine(Component.literal("§7Auto-generate random numbers"))
            .addLoreLine(Component.literal("§7and purchase instantly!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6Cost: $10,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to purchase"))
            .glow()
            .setCallback((index, type, action) -> {
                if (LotteryManager.buyTicket(player)) {
                    updateDisplay();
                }
            })
        );
        
        // Manual selection info
        StringBuilder sb = new StringBuilder("§7Numbers: §e");
        for (int i = 0; i < selectedNumbers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(selectedNumbers.get(i));
        }
        
        GuiElementBuilder manualBuilder = new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§e§lManual Selection"))
            .addLoreLine(Component.literal("§7Select 6 numbers from 1-50"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Selected: §e" + selectedNumbers.size() + "/6"));
        
        if (selectedNumbers.size() > 0) {
            manualBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(sb.toString()));
        }
        
        setSlot(22, manualBuilder);
        
        // Number grid (1-50) - displayed in rows
        int[] slots = {10, 11, 12, 14, 15, 16, 19, 20, 21, 23, 24, 25, 28, 29, 30, 32, 33, 34};
        int numberIndex = 0;
        
        for (int slot : slots) {
            if (numberIndex >= 50) break;
            
            final int number = numberIndex + 1;
            boolean isSelected = selectedNumbers.contains(number);
            
            setSlot(slot, new GuiElementBuilder(isSelected ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal((isSelected ? "§a" : "§7") + "§l" + number))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(isSelected ? "§a§lSELECTED" : "§7Click to select"))
                .setCallback((index, type, action) -> {
                    if (isSelected) {
                        selectedNumbers.remove((Integer) number);
                    } else {
                        if (selectedNumbers.size() < 6) {
                            selectedNumbers.add(number);
                            Collections.sort(selectedNumbers);
                        } else {
                            player.sendSystemMessage(Component.literal("§c§l[LOTTERY] Already selected 6 numbers!"));
                        }
                    }
                    updateDisplay();
                })
            );
            
            numberIndex++;
        }
        
        // Purchase with selected numbers button
        if (selectedNumbers.size() == 6) {
            setSlot(40, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("§a§l§nPURCHASE TICKET"))
                .addLoreLine(Component.literal("§7Numbers: §e" + formatNumbers(selectedNumbers)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: $10,000"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK to purchase"))
                .glow()
                .setCallback((index, type, action) -> {
                    if (LotteryManager.buyTicket(player, new ArrayList<>(selectedNumbers))) {
                        selectedNumbers.clear();
                        updateDisplay();
                    }
                })
            );
        }
    }
    
    private void showMyTickets() {
        this.setTitle(Component.literal("§a§l✦ Lottery - My Tickets ✦"));
        
        List<LotteryTicket> tickets = data.getActiveTickets();
        
        if (tickets.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§7§lNo Active Tickets"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Buy tickets for the next draw!"))
            );
        } else {
            int[] displaySlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
            
            for (int i = 0; i < Math.min(tickets.size(), displaySlots.length); i++) {
                LotteryTicket ticket = tickets.get(i);
                
                setSlot(displaySlots[i], new GuiElementBuilder(Items.PAPER)
                    .setName(Component.literal("§e§lTicket #" + (i + 1)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Numbers: §e" + formatNumbers(ticket.getNumbers())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Purchased: Day " + ticket.getPurchaseDay()))
                );
            }
            
            setSlot(31, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("§6§lTotal Active Tickets"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e" + tickets.size() + " tickets"))
                .addLoreLine(Component.literal("§7Value: §6" + CurrencyManager.format(tickets.size() * 10000)))
            );
        }
    }
    
    private void showLastDraw() {
        this.setTitle(Component.literal("§d§l✦ Lottery - Last Draw ✦"));
        
        List<Integer> lastWinning = LotteryManager.getLastWinningNumbers();
        long lastDrawDay = LotteryManager.getLastDrawDay();
        
        if (lastWinning.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§7§lNo Draw Yet"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7First draw on Day 7"))
            );
        } else {
            setSlot(13, new GuiElementBuilder(Items.DIAMOND)
                .setName(Component.literal("§e§l§nWinning Numbers"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6§l" + formatNumbers(lastWinning)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Day " + lastDrawDay))
                .glow()
            );
            
            Map<Integer, Integer> winners = LotteryManager.getLastDrawWinners();
            
            setSlot(29, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("§a§lWinner Breakdown"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Match 6: §e" + winners.getOrDefault(6, 0) + " winner(s)"))
                .addLoreLine(Component.literal("§eMatch 5: §7" + winners.getOrDefault(5, 0) + " winner(s)"))
                .addLoreLine(Component.literal("§eMatch 4: §7" + winners.getOrDefault(4, 0) + " winner(s)"))
                .addLoreLine(Component.literal("§eMatch 3: §7" + winners.getOrDefault(3, 0) + " winner(s)"))
                .addLoreLine(Component.literal("§eMatch 2: §7" + winners.getOrDefault(2, 0) + " winner(s)"))
                .addLoreLine(Component.literal("§eMatch 1: §7" + winners.getOrDefault(1, 0) + " winner(s)"))
            );
        }
    }
    
    private String formatNumbers(List<Integer> numbers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(numbers.get(i));
        }
        return sb.toString();
    }
}
