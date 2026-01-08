package com.shopmod.statistics;

import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

/**
 * Statistics Dashboard GUI
 */
public class StatisticsGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode viewMode = ViewMode.OVERVIEW;
    
    private enum ViewMode {
        OVERVIEW, WEALTH, INVESTMENTS, GAMING, WORKERS
    }
    
    public StatisticsGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§b§l📊 Statistics Dashboard"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            } else {
                setSlot(i, new GuiElementBuilder(Items.AIR));
            }
        }
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l" + player.getName().getString()))
            .addLoreLine(Component.literal("§7Your comprehensive stats"))
        );
        
        // View mode buttons
        setupViewModeButtons();
        
        // Display stats based on view mode
        switch (viewMode) {
            case OVERVIEW -> displayOverview();
            case WEALTH -> displayWealth();
            case INVESTMENTS -> displayInvestments();
            case GAMING -> displayGaming();
            case WORKERS -> displayWorkers();
        }
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    private void setupViewModeButtons() {
        setSlot(10, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal(viewMode == ViewMode.OVERVIEW ? "§a§l• OVERVIEW" : "§7OVERVIEW"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.OVERVIEW;
                setupDisplay();
            })
        );
        
        setSlot(11, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal(viewMode == ViewMode.WEALTH ? "§a§l• WEALTH" : "§6WEALTH"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.WEALTH;
                setupDisplay();
            })
        );
        
        setSlot(12, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal(viewMode == ViewMode.INVESTMENTS ? "§a§l• INVESTMENTS" : "§aINVESTMENTS"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.INVESTMENTS;
                setupDisplay();
            })
        );
        
        setSlot(13, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal(viewMode == ViewMode.GAMING ? "§a§l• GAMING" : "§eGAMING"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.GAMING;
                setupDisplay();
            })
        );
        
        setSlot(14, new GuiElementBuilder(Items.IRON_SHOVEL)
            .setName(Component.literal(viewMode == ViewMode.WORKERS ? "§a§l• WORKERS" : "§7WORKERS"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.WORKERS;
                setupDisplay();
            })
        );
    }
    
    private void displayOverview() {
        StatisticsManager.PlayerStatistics stats = StatisticsManager.getStats(player.getUUID());
        
        // Current Balance
        setSlot(19, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§6§lCurrent Balance"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
        
        // Total Earned
        setSlot(20, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lTotal Earned"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(stats.getTotalMoneyEarned())))
            .addLoreLine(Component.literal("§7All-time earnings"))
        );
        
        // Total Spent
        setSlot(21, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c§lTotal Spent"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(stats.getTotalMoneySpent())))
            .addLoreLine(Component.literal("§7All-time spending"))
        );
        
        // Properties
        setSlot(22, new GuiElementBuilder(Items.GRASS_BLOCK)
            .setName(Component.literal("§2§lProperties"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getPropertiesOwnedCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getPropertiesOwnedLifetime()))
            .addLoreLine(Component.literal("§7Income: §6" + CurrencyManager.format(stats.getTotalPropertyIncome())))
        );
        
        // Businesses
        setSlot(23, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§a§lBusinesses"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getBusinessesOwnedCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getBusinessesOwnedLifetime()))
            .addLoreLine(Component.literal("§7Income: §6" + CurrencyManager.format(stats.getBusinessIncomeTotal())))
        );
        
        // Games
        setSlot(24, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lGaming Stats"))
            .addLoreLine(Component.literal("§7Played: §e" + stats.getGamesPlayed()))
            .addLoreLine(Component.literal("§7Won: §a" + stats.getGamesWon()))
            .addLoreLine(Component.literal("§7Lost: §c" + stats.getGamesLost()))
            .addLoreLine(Component.literal("§7Win Rate: §e" + getWinRate(stats) + "%"))
        );
        
        // Workers
        setSlot(25, new GuiElementBuilder(Items.IRON_SHOVEL)
            .setName(Component.literal("§7§lWorkers"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getWorkersHiredCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getWorkersHiredLifetime()))
            .addLoreLine(Component.literal("§7Trained: §e" + stats.getWorkersTrained()))
        );
        
        // Fun Facts
        setSlot(31, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§d§l✨ Fun Facts"))
            .addLoreLine(Component.literal("§7Days Played: §e" + stats.getDaysPlayed()))
            .addLoreLine(Component.literal("§7Lottery Tickets: §e" + stats.getLotteryTicketsBought()))
            .addLoreLine(Component.literal("§7Stock Trades: §e" + stats.getStockTradesMade()))
            .addLoreLine(Component.literal("§7Automation Runs: §e" + stats.getAutomationRuns()))
        );
    }
    
    private void displayWealth() {
        StatisticsManager.PlayerStatistics stats = StatisticsManager.getStats(player.getUUID());
        
        setSlot(20, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§6§lCurrent Balance"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
        
        setSlot(21, new GuiElementBuilder(Items.DIAMOND_BLOCK)
            .setName(Component.literal("§b§lHighest Balance"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(stats.getHighestBalance())))
        );
        
        setSlot(22, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lTotal Earned"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(stats.getTotalMoneyEarned())))
        );
        
        setSlot(23, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c§lTotal Spent"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(stats.getTotalMoneySpent())))
        );
        
        setSlot(24, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lNet Worth"))
            .addLoreLine(Component.literal("§e" + CurrencyManager.format(stats.getTotalMoneyEarned() - stats.getTotalMoneySpent())))
            .addLoreLine(Component.literal("§7Earned - Spent"))
        );
        
        setSlot(29, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§6§lLoan Stats"))
            .addLoreLine(Component.literal("§7Taken: §e" + stats.getLoansTaken()))
            .addLoreLine(Component.literal("§7Repaid: §a" + stats.getLoansRepaid()))
            .addLoreLine(Component.literal("§7Borrowed: §6" + CurrencyManager.format(stats.getTotalBorrowed())))
            .addLoreLine(Component.literal("§7Interest: §c" + CurrencyManager.format(stats.getTotalInterestPaid())))
        );
        
        setSlot(30, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lLottery Stats"))
            .addLoreLine(Component.literal("§7Tickets: §e" + stats.getLotteryTicketsBought()))
            .addLoreLine(Component.literal("§7Wins: §a" + stats.getLotteryWins()))
            .addLoreLine(Component.literal("§7Spent: §c" + CurrencyManager.format(stats.getLotterySpentTotal())))
            .addLoreLine(Component.literal("§7Won: §a" + CurrencyManager.format(stats.getLotteryWinningsTotal())))
        );
        
        setSlot(31, new GuiElementBuilder(Items.SHIELD)
            .setName(Component.literal("§9§lInsurance Stats"))
            .addLoreLine(Component.literal("§7Policies: §e" + stats.getInsurancePolicies()))
            .addLoreLine(Component.literal("§7Claims Filed: §e" + stats.getClaimsFiled()))
            .addLoreLine(Component.literal("§7Premiums: §c" + CurrencyManager.format(stats.getPremiumsPaidTotal())))
            .addLoreLine(Component.literal("§7Claims Paid: §a" + CurrencyManager.format(stats.getClaimsReceivedTotal())))
        );
    }
    
    private void displayInvestments() {
        StatisticsManager.PlayerStatistics stats = StatisticsManager.getStats(player.getUUID());
        
        // Properties
        setSlot(19, new GuiElementBuilder(Items.GRASS_BLOCK)
            .setName(Component.literal("§2§lProperty Stats"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getPropertiesOwnedCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getPropertiesOwnedLifetime()))
            .addLoreLine(Component.literal("§7Rented: §e" + stats.getPropertiesRentedOut()))
            .addLoreLine(Component.literal("§7Total Income: §6" + CurrencyManager.format(stats.getTotalPropertyIncome())))
        );
        
        // Farms
        setSlot(20, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§6§lFarm Stats"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getFarmsOwnedCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getFarmsOwnedLifetime()))
            .addLoreLine(Component.literal("§7Total Income: §6" + CurrencyManager.format(stats.getFarmIncomeTotal())))
        );
        
        // Mines
        setSlot(21, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
            .setName(Component.literal("§8§lMine Stats"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getMinesOwnedCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getMinesOwnedLifetime()))
            .addLoreLine(Component.literal("§7Total Income: §6" + CurrencyManager.format(stats.getMineIncomeTotal())))
        );
        
        // Businesses
        setSlot(22, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§a§lBusiness Stats"))
            .addLoreLine(Component.literal("§7Current: §e" + stats.getBusinessesOwnedCurrent()))
            .addLoreLine(Component.literal("§7Lifetime: §e" + stats.getBusinessesOwnedLifetime()))
            .addLoreLine(Component.literal("§7Upgrades: §e" + stats.getBusinessUpgradesTotal()))
            .addLoreLine(Component.literal("§7Total Income: §6" + CurrencyManager.format(stats.getBusinessIncomeTotal())))
        );
        
        // Stock Market
        setSlot(23, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§b§lStock Market Stats"))
            .addLoreLine(Component.literal("§7Trades: §e" + stats.getStockTradesMade()))
            .addLoreLine(Component.literal("§7Current Shares: §e" + stats.getSharesOwnedCurrent()))
            .addLoreLine(Component.literal("§7Total Profit: §a" + CurrencyManager.format(stats.getStockProfitTotal())))
            .addLoreLine(Component.literal("§7Total Loss: §c" + CurrencyManager.format(stats.getStockLossTotal())))
            .addLoreLine(Component.literal("§7Dividends: §6" + CurrencyManager.format(stats.getDividendsEarnedTotal())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Biggest Win: §a" + CurrencyManager.format(stats.getBiggestStockWin())))
            .addLoreLine(Component.literal("§7Biggest Loss: §c" + CurrencyManager.format(stats.getBiggestStockLoss())))
        );
    }
    
    private void displayGaming() {
        StatisticsManager.PlayerStatistics stats = StatisticsManager.getStats(player.getUUID());
        
        setSlot(20, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§e§lOverall Gaming"))
            .addLoreLine(Component.literal("§7Games Played: §e" + stats.getGamesPlayed()))
            .addLoreLine(Component.literal("§7Games Won: §a" + stats.getGamesWon()))
            .addLoreLine(Component.literal("§7Games Lost: §c" + stats.getGamesLost()))
            .addLoreLine(Component.literal("§7Win Rate: §e" + getWinRate(stats) + "%"))
        );
        
        setSlot(21, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lWinnings"))
            .addLoreLine(Component.literal("§7Total Won: §a" + CurrencyManager.format(stats.getGamingWinningsTotal())))
            .addLoreLine(Component.literal("§7Total Lost: §c" + CurrencyManager.format(stats.getGamingLossesTotal())))
            .addLoreLine(Component.literal("§7Net: §e" + CurrencyManager.format(stats.getGamingWinningsTotal() - stats.getGamingLossesTotal())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Biggest Win: §a" + CurrencyManager.format(stats.getBiggestGameWin())))
        );
        
        setSlot(22, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§b§lWin Streaks"))
            .addLoreLine(Component.literal("§7Current Streak: §e" + stats.getCurrentWinStreak()))
            .addLoreLine(Component.literal("§7Longest Streak: §6" + stats.getLongestWinStreak()))
        );
    }
    
    private void displayWorkers() {
        StatisticsManager.PlayerStatistics stats = StatisticsManager.getStats(player.getUUID());
        
        setSlot(21, new GuiElementBuilder(Items.IRON_SHOVEL)
            .setName(Component.literal("§7§lWorker Stats"))
            .addLoreLine(Component.literal("§7Currently Hired: §e" + stats.getWorkersHiredCurrent()))
            .addLoreLine(Component.literal("§7Lifetime Hired: §e" + stats.getWorkersHiredLifetime()))
            .addLoreLine(Component.literal("§7Workers Trained: §e" + stats.getWorkersTrained()))
            .addLoreLine(Component.literal("§7Total Wages Paid: §6" + CurrencyManager.format(stats.getWorkerWagesTotal())))
        );
    }
    
    private int getWinRate(StatisticsManager.PlayerStatistics stats) {
        int total = stats.getGamesPlayed();
        if (total == 0) return 0;
        return (stats.getGamesWon() * 100) / total;
    }
}
