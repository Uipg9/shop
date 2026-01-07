package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.economy.PriceFluctuation;
import com.shopmod.shop.ItemPricing;
import com.shopmod.stocks.StockOptionsManager;
import net.minecraft.world.item.Item;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Stock Options Trading GUI
 */
public class StocksGui extends SimpleGui {
    private final ServerPlayer player;
    private boolean showingActive = false;
    
    public StocksGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§lStock Options Trading"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        StockOptionsManager.PlayerStockData data = StockOptionsManager.getPlayerStockData(player.getUUID());
        
        // Info bar
        setupInfoBar(data);
        
        // Toggle view
        setupToggle();
        
        if (showingActive) {
            displayActiveOptions(data);
        } else {
            displayAvailableOptions();
        }
    }
    
    private void setupInfoBar(StockOptionsManager.PlayerStockData data) {
        // Active options count
        setSlot(2, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lActive Options: " + data.getActiveOptions().size()))
            .addLoreLine(Component.literal("§7Your current positions"))
        );
        
        // Total profit
        setSlot(4, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lTotal Profit"))
            .addLoreLine(Component.literal(CurrencyManager.format(data.getTotalProfit())))
        );
        
        // Total loss
        setSlot(6, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c§lTotal Loss"))
            .addLoreLine(Component.literal(CurrencyManager.format(data.getTotalLoss())))
        );
        
        // Balance
        setSlot(8, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    private void setupToggle() {
        setSlot(49, new GuiElementBuilder(showingActive ? Items.ENDER_EYE : Items.ENDER_PEARL)
            .setName(Component.literal("§b§lView: " + (showingActive ? "Active Options" : "Available Options")))
            .addLoreLine(Component.literal("§7Click to toggle"))
            .setCallback((index, type, action) -> {
                showingActive = !showingActive;
                updateDisplay();
            })
        );
    }
    
    private void displayAvailableOptions() {
        List<Item> suggested = StockOptionsManager.getSuggestedOptions();
        
        int slot = 18;
        for (Item item : suggested) {
            long basePrice = ItemPricing.getBuyPrice(item);
            long currentPrice = PriceFluctuation.getAdjustedPrice(item, basePrice);
            long callPremium = currentPrice / 10; // 10% of price
            long putPremium = currentPrice / 10;
            
            GuiElementBuilder builder = new GuiElementBuilder(item)
                .setName(Component.literal("§e§l" + item.getDescriptionId()))
                .addLoreLine(Component.literal("§7Current Price: §6" + CurrencyManager.format(currentPrice)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a§lCALL Option (Price UP)"))
                .addLoreLine(Component.literal("§7Premium: §c" + CurrencyManager.format(callPremium)))
                .addLoreLine(Component.literal("§7Strike: §6" + CurrencyManager.format(currentPrice)))
                .addLoreLine(Component.literal("§7Expires: §e3 MC hours"))
                .addLoreLine(Component.literal("§a§lLEFT CLICK §7to buy CALL"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c§lPUT Option (Price DOWN)"))
                .addLoreLine(Component.literal("§7Premium: §c" + CurrencyManager.format(putPremium)))
                .addLoreLine(Component.literal("§c§lRIGHT CLICK §7to buy PUT"))
                .setCallback((index, type, action) -> {
                    if (type.isLeft) {
                        StockOptionsManager.buyCallOption(player, item, currentPrice, callPremium);
                    } else if (type.isRight) {
                        StockOptionsManager.buyPutOption(player, item, currentPrice, putPremium);
                    }
                    updateDisplay();
                });
            
            setSlot(slot, builder);
            slot++;
            if (slot >= 45) break;
        }
    }
    
    private void displayActiveOptions(StockOptionsManager.PlayerStockData data) {
        List<StockOptionsManager.StockOption> options = data.getActiveOptions();
        
        if (options.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Active Options"))
                .addLoreLine(Component.literal("§7Buy options from available tab"))
            );
            return;
        }
        
        long currentTime = player.level().getServer().overworld().getDayTime();
        
        int slot = 18;
        for (int i = 0; i < options.size() && slot < 45; i++) {
            StockOptionsManager.StockOption option = options.get(i);
            final int optionIndex = i;
            
            long basePrice = ItemPricing.getBuyPrice(option.getItem());
            long currentPrice = PriceFluctuation.getAdjustedPrice(option.getItem(), basePrice);
            long profit = option.calculateProfit();
            long timeRemaining = option.getExpirationTime() - currentTime;
            int minutesLeft = (int)(timeRemaining / 1200); // Approximate minutes
            
            boolean inProfit = profit > 0;
            
            GuiElementBuilder builder = new GuiElementBuilder(option.getItem())
                .setName(Component.literal((inProfit ? "§a" : "§c") + "§l" + option.getItem().getDescriptionId()))
                .addLoreLine(Component.literal("§7Type: " + (option.isCall() ? "§a§lCALL (UP)" : "§c§lPUT (DOWN)")))
                .addLoreLine(Component.literal("§7Strike: §6" + CurrencyManager.format(option.getStrikePrice())))
                .addLoreLine(Component.literal("§7Current: §6" + CurrencyManager.format(currentPrice)))
                .addLoreLine(Component.literal("§7Premium Paid: §c" + CurrencyManager.format(option.getPremium())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal((inProfit ? "§a§lPROFIT: +" : "§c§lLOSS: ") + 
                    CurrencyManager.format(Math.abs(profit))))
                .addLoreLine(Component.literal("§7Expires: §e~" + minutesLeft + " min"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§b§lCLICK §7to exercise early"))
                .setCallback((index, type, action) -> {
                    StockOptionsManager.exerciseOption(player, optionIndex);
                    updateDisplay();
                });
            
            if (inProfit) {
                builder.glow();
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
}
