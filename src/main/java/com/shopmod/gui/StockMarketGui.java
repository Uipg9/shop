package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.stocks.StockMarketManager;
import com.shopmod.stocks.StockMarketManager.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

import java.util.*;

/**
 * Comprehensive Stock Market Trading GUI
 */
public class StockMarketGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode currentView = ViewMode.MARKET;
    private String selectedTicker = null;
    
    private enum ViewMode {
        MARKET,      // All companies listing
        TRADING,     // Individual company trading
        PORTFOLIO,   // Player's holdings
        NEWS         // Market news and events
    }
    
    public StockMarketGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§l⚡ Stock Market Exchange"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        // Background border
        for (int i = 0; i < 9; i++) {
            setSlot(i, createBorderItem());
            setSlot(45 + i, createBorderItem());
        }
        
        // Navigation
        setupNavigation();
        
        // Display based on view mode
        switch (currentView) {
            case MARKET:
                displayMarketView();
                break;
            case TRADING:
                displayTradingView();
                break;
            case PORTFOLIO:
                displayPortfolioView();
                break;
            case NEWS:
                displayNewsView();
                break;
        }
    }
    
    private GuiElementBuilder createBorderItem() {
        return new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
            .setName(Component.literal(""));
    }
    
    private void setupNavigation() {
        // Market View button
        setSlot(0, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal(currentView == ViewMode.MARKET ? "§a§l● Market" : "§7Market"))
            .addLoreLine(Component.literal("§7View all companies"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.MARKET;
                selectedTicker = null;
                updateDisplay();
            })
        );
        
        // Portfolio View button
        setSlot(1, new GuiElementBuilder(Items.CHEST)
            .setName(Component.literal(currentView == ViewMode.PORTFOLIO ? "§a§l● Portfolio" : "§7Portfolio"))
            .addLoreLine(Component.literal("§7Your holdings"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.PORTFOLIO;
                selectedTicker = null;
                updateDisplay();
            })
        );
        
        // News View button
        setSlot(2, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal(currentView == ViewMode.NEWS ? "§a§l● News" : "§7News"))
            .addLoreLine(Component.literal("§7Market events"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.NEWS;
                selectedTicker = null;
                updateDisplay();
            })
        );
        
        // Player info
        long balance = CurrencyManager.getBalance(player);
        PlayerPortfolio portfolio = StockMarketManager.getPortfolio(player.getUUID());
        
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l" + player.getName().getString()))
            .addLoreLine(Component.literal("§7Cash: §6" + CurrencyManager.format(balance)))
            .addLoreLine(Component.literal("§7Portfolio: §6" + CurrencyManager.format(portfolio.getTotalValue())))
            .addLoreLine(Component.literal("§7Total: §6" + CurrencyManager.format(balance + portfolio.getTotalValue())))
        );
        
        // Market trend indicator
        MarketTrend trend = StockMarketManager.getCurrentTrend();
        Item trendItem = trend == MarketTrend.BULL ? Items.GREEN_WOOL : 
                        trend == MarketTrend.BEAR ? Items.RED_WOOL : Items.YELLOW_WOOL;
        
        setSlot(8, new GuiElementBuilder(trendItem)
            .setName(Component.literal("§6§lMarket: §f" + trend.displayName))
            .addLoreLine(Component.literal("§7Trend: " + String.format("%.1f%%", trend.bias * 100)))
        );
    }
    
    private void displayMarketView() {
        setSlot(3, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§6§lStock Market"))
            .addLoreLine(Component.literal("§7Click any company to trade"))
            .addLoreLine(Component.literal("§7Transaction fee: §c1%"))
        );
        
        Map<String, StockCompany> companies = StockMarketManager.getCompanies();
        List<StockCompany> companyList = new ArrayList<>(companies.values());
        
        int slot = 9;
        for (int i = 0; i < Math.min(36, companyList.size()); i++) {
            StockCompany company = companyList.get(i);
            
            // Skip border slots
            if (slot % 9 == 0 || slot % 9 == 8 || slot >= 45) {
                slot++;
                if (slot % 9 == 0 || slot % 9 == 8) slot++;
            }
            
            displayCompanyItem(slot, company);
            slot++;
        }
    }
    
    private void displayCompanyItem(int slot, StockCompany company) {
        double dailyChange = company.getDailyChange();
        String changeColor = dailyChange > 0 ? "§a" : dailyChange < 0 ? "§c" : "§7";
        String arrow = dailyChange > 0 ? "▲" : dailyChange < 0 ? "▼" : "■";
        
        Item icon = getIndustryIcon(company.getIndustry());
        
        GuiElementBuilder builder = new GuiElementBuilder(icon)
            .setName(Component.literal("§e§l" + company.getName()))
            .addLoreLine(Component.literal("§7Ticker: §f" + company.getTicker()))
            .addLoreLine(Component.literal("§7Industry: §f" + company.getIndustry()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Price: §6$" + String.format("%.2f", company.getCurrentPrice())))
            .addLoreLine(Component.literal("§7Change: " + changeColor + arrow + " " + 
                String.format("%.2f%%", Math.abs(dailyChange))))
            .addLoreLine(Component.literal("§7Dividend: §a" + String.format("%.1f%%", company.getDividendRate() * 100)))
            .addLoreLine(Component.literal("§7Volatility: §f" + company.getVolatility().displayName))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to trade"))
            .setCallback((index, type, action) -> {
                selectedTicker = company.getTicker();
                currentView = ViewMode.TRADING;
                updateDisplay();
            });
        
        setSlot(slot, builder);
    }
    
    private void displayTradingView() {
        if (selectedTicker == null) {
            currentView = ViewMode.MARKET;
            updateDisplay();
            return;
        }
        
        StockCompany company = StockMarketManager.getCompany(selectedTicker);
        if (company == null) {
            currentView = ViewMode.MARKET;
            updateDisplay();
            return;
        }
        
        PlayerPortfolio portfolio = StockMarketManager.getPortfolio(player.getUUID());
        StockHolding holding = portfolio.getHoldings().get(selectedTicker);
        
        // Company info
        setSlot(13, new GuiElementBuilder(getIndustryIcon(company.getIndustry()))
            .setName(Component.literal("§e§l" + company.getName()))
            .addLoreLine(Component.literal("§7Ticker: §f" + company.getTicker()))
            .addLoreLine(Component.literal("§7Industry: §f" + company.getIndustry()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current Price: §6$" + String.format("%.2f", company.getCurrentPrice())))
            .addLoreLine(Component.literal("§7Daily Change: §f" + String.format("%.2f%%", company.getDailyChange())))
            .addLoreLine(Component.literal("§7Weekly Change: §f" + String.format("%.2f%%", company.getWeeklyChange())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Dividend Rate: §a" + String.format("%.1f%%", company.getDividendRate() * 100)))
            .addLoreLine(Component.literal("§7Volatility: §f" + company.getVolatility().displayName))
        );
        
        // Price history (text-based)
        setSlot(22, displayPriceHistory(company));
        
        // Your holdings
        if (holding != null) {
            long currentValue = holding.getCurrentValue();
            long gainLoss = holding.getGainLoss();
            String glColor = gainLoss >= 0 ? "§a" : "§c";
            String glSign = gainLoss >= 0 ? "+" : "";
            
            setSlot(31, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("§6§lYour Holdings"))
                .addLoreLine(Component.literal("§7Shares: §f" + holding.getShares()))
                .addLoreLine(Component.literal("§7Avg Cost: §6$" + String.format("%.2f", holding.getAverageCost())))
                .addLoreLine(Component.literal("§7Total Cost: §6" + CurrencyManager.format(holding.getTotalCost())))
                .addLoreLine(Component.literal("§7Current Value: §6" + CurrencyManager.format(currentValue)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Gain/Loss: " + glColor + glSign + CurrencyManager.format(gainLoss)))
                .addLoreLine(Component.literal("§7Return: " + glColor + String.format("%.2f%%", holding.getGainPercentage())))
            );
        } else {
            setSlot(31, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Holdings"))
                .addLoreLine(Component.literal("§7You don't own any shares"))
                .addLoreLine(Component.literal("§7of this company yet."))
            );
        }
        
        // Buy buttons
        setupBuyButton(10, company, 1, "Buy 1 Share");
        setupBuyButton(11, company, 10, "Buy 10 Shares");
        setupBuyButton(12, company, 100, "Buy 100 Shares");
        
        // Sell buttons (only if holding)
        if (holding != null && holding.getShares() > 0) {
            int maxShares = holding.getShares();
            setupSellButton(19, company, Math.min(1, maxShares), "Sell 1 Share");
            setupSellButton(20, company, Math.min(10, maxShares), "Sell 10 Shares");
            setupSellButton(21, company, maxShares, "Sell ALL (" + maxShares + ")");
        } else {
            setSlot(19, createDisabledButton("No shares to sell"));
            setSlot(20, createDisabledButton("No shares to sell"));
            setSlot(21, createDisabledButton("No shares to sell"));
        }
        
        // Back button
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§7⬅ Back to Market"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.MARKET;
                selectedTicker = null;
                updateDisplay();
            })
        );
    }
    
    private GuiElementBuilder displayPriceHistory(StockCompany company) {
        List<Double> history = company.getPriceHistory();
        GuiElementBuilder builder = new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§6§l7-Day Price History"));
        
        int days = Math.min(7, history.size());
        for (int i = 0; i < days; i++) {
            int index = history.size() - days + i;
            double price = history.get(index);
            String trend = "";
            
            if (i > 0) {
                double prev = history.get(index - 1);
                if (price > prev) trend = "§a▲";
                else if (price < prev) trend = "§c▼";
                else trend = "§7■";
            }
            
            builder.addLoreLine(Component.literal("§7Day -" + (days - i - 1) + ": §6$" + 
                String.format("%.2f", price) + " " + trend));
        }
        
        return builder;
    }
    
    private void setupBuyButton(int slot, StockCompany company, int shares, String label) {
        long sharesCost = (long)(shares * company.getCurrentPrice());
        long fee = (long)(sharesCost * 0.01);
        long totalCost = sharesCost + fee;
        
        boolean canAfford = CurrencyManager.canAfford(player, totalCost);
        
        GuiElementBuilder builder = new GuiElementBuilder(canAfford ? Items.LIME_DYE : Items.GRAY_DYE)
            .setName(Component.literal((canAfford ? "§a§l" : "§7§l") + label))
            .addLoreLine(Component.literal("§7Shares: §f" + shares))
            .addLoreLine(Component.literal("§7Price: §6$" + String.format("%.2f", company.getCurrentPrice())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Shares Cost: §6" + CurrencyManager.format(sharesCost)))
            .addLoreLine(Component.literal("§7Fee (1%): §c" + CurrencyManager.format(fee)))
            .addLoreLine(Component.literal("§7Total: §6§l" + CurrencyManager.format(totalCost)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(canAfford ? "§e§lCLICK to buy" : "§c§lInsufficient funds"));
        
        if (canAfford) {
            builder.setCallback((index, type, action) -> {
                if (StockMarketManager.buyShares(player, company.getTicker(), shares)) {
                    updateDisplay();
                }
            });
        }
        
        setSlot(slot, builder);
    }
    
    private void setupSellButton(int slot, StockCompany company, int shares, String label) {
        if (shares <= 0) {
            setSlot(slot, createDisabledButton("No shares"));
            return;
        }
        
        long saleProceeds = (long)(shares * company.getCurrentPrice());
        long fee = (long)(saleProceeds * 0.01);
        long netProceeds = saleProceeds - fee;
        
        GuiElementBuilder builder = new GuiElementBuilder(Items.RED_DYE)
            .setName(Component.literal("§c§l" + label))
            .addLoreLine(Component.literal("§7Shares: §f" + shares))
            .addLoreLine(Component.literal("§7Price: §6$" + String.format("%.2f", company.getCurrentPrice())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Sale Value: §6" + CurrencyManager.format(saleProceeds)))
            .addLoreLine(Component.literal("§7Fee (1%): §c" + CurrencyManager.format(fee)))
            .addLoreLine(Component.literal("§7Net: §a§l" + CurrencyManager.format(netProceeds)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to sell"))
            .setCallback((index, type, action) -> {
                if (StockMarketManager.sellShares(player, company.getTicker(), shares)) {
                    updateDisplay();
                }
            });
        
        setSlot(slot, builder);
    }
    
    private GuiElementBuilder createDisabledButton(String reason) {
        return new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c" + reason));
    }
    
    private void displayPortfolioView() {
        PlayerPortfolio portfolio = StockMarketManager.getPortfolio(player.getUUID());
        
        long totalValue = portfolio.getTotalValue();
        long costBasis = portfolio.getTotalCostBasis();
        long gainLoss = totalValue - costBasis;
        double gainPercent = portfolio.getOverallGainPercentage();
        String glColor = gainLoss >= 0 ? "§a" : "§c";
        String glSign = gainLoss >= 0 ? "+" : "";
        
        // Portfolio summary
        setSlot(4, new GuiElementBuilder(Items.CHEST)
            .setName(Component.literal("§6§lYour Portfolio"))
            .addLoreLine(Component.literal("§7Total Value: §6" + CurrencyManager.format(totalValue)))
            .addLoreLine(Component.literal("§7Cost Basis: §6" + CurrencyManager.format(costBasis)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Gain/Loss: " + glColor + glSign + CurrencyManager.format(gainLoss)))
            .addLoreLine(Component.literal("§7Return: " + glColor + String.format("%.2f%%", gainPercent)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Dividends Earned: §a" + 
                CurrencyManager.format(portfolio.getTotalDividendsEarned())))
        );
        
        // Display holdings
        List<StockHolding> holdings = new ArrayList<>(portfolio.getHoldings().values());
        
        if (holdings.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Holdings"))
                .addLoreLine(Component.literal("§7You haven't bought any stocks yet."))
                .addLoreLine(Component.literal("§7Visit the Market to start trading!"))
            );
        } else {
            int slot = 9;
            for (StockHolding holding : holdings) {
                // Skip border slots
                if (slot % 9 == 0 || slot % 9 == 8 || slot >= 45) {
                    slot++;
                    if (slot % 9 == 0 || slot % 9 == 8) slot++;
                }
                if (slot >= 45) break;
                
                displayHoldingItem(slot, holding);
                slot++;
            }
        }
    }
    
    private void displayHoldingItem(int slot, StockHolding holding) {
        StockCompany company = StockMarketManager.getCompany(holding.getTicker());
        if (company == null) return;
        
        long currentValue = holding.getCurrentValue();
        long gainLoss = holding.getGainLoss();
        String glColor = gainLoss >= 0 ? "§a" : "§c";
        String glSign = gainLoss >= 0 ? "+" : "";
        
        Item icon = getIndustryIcon(company.getIndustry());
        
        GuiElementBuilder builder = new GuiElementBuilder(icon)
            .setName(Component.literal("§e§l" + company.getName()))
            .addLoreLine(Component.literal("§7Ticker: §f" + holding.getTicker()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Shares: §f" + holding.getShares()))
            .addLoreLine(Component.literal("§7Avg Cost: §6$" + String.format("%.2f", holding.getAverageCost())))
            .addLoreLine(Component.literal("§7Current Price: §6$" + String.format("%.2f", company.getCurrentPrice())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Cost: §6" + CurrencyManager.format(holding.getTotalCost())))
            .addLoreLine(Component.literal("§7Current Value: §6" + CurrencyManager.format(currentValue)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Gain/Loss: " + glColor + glSign + CurrencyManager.format(gainLoss)))
            .addLoreLine(Component.literal("§7Return: " + glColor + String.format("%.2f%%", holding.getGainPercentage())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to trade"))
            .setCallback((index, type, action) -> {
                selectedTicker = holding.getTicker();
                currentView = ViewMode.TRADING;
                updateDisplay();
            });
        
        setSlot(slot, builder);
    }
    
    private void displayNewsView() {
        setSlot(4, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§6§lMarket News & Events"))
            .addLoreLine(Component.literal("§7Recent market activity"))
        );
        
        List<MarketEvent> events = StockMarketManager.getRecentEvents();
        
        if (events.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("§7No recent events"))
                .addLoreLine(Component.literal("§7Check back later!"))
            );
        } else {
            int slot = 9;
            for (int i = events.size() - 1; i >= 0 && slot < 45; i--) {
                MarketEvent event = events.get(i);
                
                // Skip border slots
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                    if (slot % 9 == 0 || slot % 9 == 8) slot++;
                }
                if (slot >= 45) break;
                
                displayEventItem(slot, event);
                slot++;
            }
        }
        
        // Market trend info
        MarketTrend trend = StockMarketManager.getCurrentTrend();
        setSlot(49, new GuiElementBuilder(Items.COMPASS)
            .setName(Component.literal("§6§lCurrent Market Trend"))
            .addLoreLine(Component.literal("§f" + trend.displayName))
            .addLoreLine(Component.literal("§7Bias: §f" + String.format("%.1f%%", trend.bias * 100)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7This affects all stock prices."))
        );
    }
    
    private void displayEventItem(int slot, MarketEvent event) {
        double impact = event.getPriceImpact();
        String impactColor = impact > 0 ? "§a" : impact < 0 ? "§c" : "§7";
        String impactSign = impact > 0 ? "+" : "";
        Item icon = impact > 0 ? Items.LIME_DYE : impact < 0 ? Items.RED_DYE : Items.GRAY_DYE;
        
        setSlot(slot, new GuiElementBuilder(icon)
            .setName(Component.literal("§e§l" + event.getTicker()))
            .addLoreLine(Component.literal("§7" + event.getDescription()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Impact: " + impactColor + impactSign + 
                String.format("%.1f%%", impact)))
        );
    }
    
    private Item getIndustryIcon(String industry) {
        switch (industry.toLowerCase()) {
            case "technology": return Items.REDSTONE;
            case "mining": return Items.DIAMOND_PICKAXE;
            case "agriculture": return Items.WHEAT;
            case "real estate": return Items.BRICK;
            case "energy": return Items.COAL;
            case "finance": return Items.GOLD_INGOT;
            case "retail": return Items.CHEST;
            default: return Items.PAPER;
        }
    }
}
