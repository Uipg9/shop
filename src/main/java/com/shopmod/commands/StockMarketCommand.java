package com.shopmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.gui.StockMarketGui;
import com.shopmod.stocks.StockMarketManager;
import com.shopmod.stocks.StockMarketManager.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Stock Market command - Trading and portfolio management
 */
public class StockMarketCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main command - opens GUI
        dispatcher.register(
            Commands.literal("stockmarket")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new StockMarketGui(player).open();
                    return 1;
                })
                // /stockmarket buy <ticker> <amount>
                .then(Commands.literal("buy")
                    .then(Commands.argument("ticker", StringArgumentType.word())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String ticker = StringArgumentType.getString(context, "ticker").toUpperCase();
                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                
                                StockMarketManager.buyShares(player, ticker, amount);
                                return 1;
                            })
                        )
                    )
                )
                // /stockmarket sell <ticker> <amount>
                .then(Commands.literal("sell")
                    .then(Commands.argument("ticker", StringArgumentType.word())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                String ticker = StringArgumentType.getString(context, "ticker").toUpperCase();
                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                
                                StockMarketManager.sellShares(player, ticker, amount);
                                return 1;
                            })
                        )
                    )
                )
                // /stockmarket info <ticker>
                .then(Commands.literal("info")
                    .then(Commands.argument("ticker", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String ticker = StringArgumentType.getString(context, "ticker").toUpperCase();
                            
                            StockCompany company = StockMarketManager.getCompany(ticker);
                            if (company == null) {
                                player.sendSystemMessage(Component.literal("§cCompany not found: " + ticker));
                                return 0;
                            }
                            
                            player.sendSystemMessage(Component.literal("§6§l" + company.getName() + " (" + ticker + ")"));
                            player.sendSystemMessage(Component.literal("§7Industry: §f" + company.getIndustry()));
                            player.sendSystemMessage(Component.literal("§7Current Price: §6$" + 
                                String.format("%.2f", company.getCurrentPrice())));
                            player.sendSystemMessage(Component.literal("§7Daily Change: §f" + 
                                String.format("%.2f%%", company.getDailyChange())));
                            player.sendSystemMessage(Component.literal("§7Dividend Rate: §a" + 
                                String.format("%.1f%%", company.getDividendRate() * 100)));
                            player.sendSystemMessage(Component.literal("§7Volatility: §f" + 
                                company.getVolatility().displayName));
                            player.sendSystemMessage(Component.literal("§7Market Cap: §6" + 
                                CurrencyManager.format(company.getMarketCap())));
                            
                            return 1;
                        })
                    )
                )
                // /stockmarket portfolio
                .then(Commands.literal("portfolio")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        PlayerPortfolio portfolio = StockMarketManager.getPortfolio(player.getUUID());
                        
                        player.sendSystemMessage(Component.literal("§6§l=== YOUR PORTFOLIO ==="));
                        
                        if (portfolio.getHoldings().isEmpty()) {
                            player.sendSystemMessage(Component.literal("§7You don't own any stocks."));
                        } else {
                            for (StockHolding holding : portfolio.getHoldings().values()) {
                                StockCompany company = StockMarketManager.getCompany(holding.getTicker());
                                if (company == null) continue;
                                
                                long gainLoss = holding.getGainLoss();
                                String glColor = gainLoss >= 0 ? "§a" : "§c";
                                String glSign = gainLoss >= 0 ? "+" : "";
                                
                                player.sendSystemMessage(Component.literal("§e" + company.getName() + " (" + 
                                    holding.getTicker() + ")"));
                                player.sendSystemMessage(Component.literal("  §7Shares: §f" + holding.getShares() + 
                                    " §7@ §6$" + String.format("%.2f", holding.getAverageCost())));
                                player.sendSystemMessage(Component.literal("  §7Value: §6" + 
                                    CurrencyManager.format(holding.getCurrentValue()) + " " + glColor + "(" + 
                                    glSign + CurrencyManager.format(gainLoss) + ")"));
                            }
                            
                            player.sendSystemMessage(Component.literal(""));
                            player.sendSystemMessage(Component.literal("§7Total Value: §6" + 
                                CurrencyManager.format(portfolio.getTotalValue())));
                            player.sendSystemMessage(Component.literal("§7Total Return: " + 
                                (portfolio.getOverallGainPercentage() >= 0 ? "§a" : "§c") + 
                                String.format("%.2f%%", portfolio.getOverallGainPercentage())));
                        }
                        
                        return 1;
                    })
                )
                // /stockmarket list
                .then(Commands.literal("list")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        
                        player.sendSystemMessage(Component.literal("§6§l=== STOCK MARKET ==="));
                        player.sendSystemMessage(Component.literal("§7Market: §f" + 
                            StockMarketManager.getCurrentTrend().displayName));
                        player.sendSystemMessage(Component.literal(""));
                        
                        Map<String, StockCompany> companies = StockMarketManager.getCompanies();
                        for (StockCompany company : companies.values()) {
                            double change = company.getDailyChange();
                            String changeColor = change > 0 ? "§a" : change < 0 ? "§c" : "§7";
                            String arrow = change > 0 ? "▲" : change < 0 ? "▼" : "■";
                            
                            player.sendSystemMessage(Component.literal("§e" + company.getTicker() + " §7- " + 
                                company.getName() + " §6$" + String.format("%.2f", company.getCurrentPrice()) + 
                                " " + changeColor + arrow + " " + String.format("%.2f%%", Math.abs(change))));
                        }
                        
                        player.sendSystemMessage(Component.literal(""));
                        player.sendSystemMessage(Component.literal("§7Use §f/stockmarket §7to open GUI"));
                        
                        return 1;
                    })
                )
        );
        
        // Alias: /market
        dispatcher.register(
            Commands.literal("market")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    new StockMarketGui(player).open();
                    return 1;
                })
        );
    }
}
