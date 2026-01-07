package com.shopmod.stocks;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.economy.PriceFluctuation;
import com.shopmod.shop.ItemPricing;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stock Options and Futures trading system
 * Bet on price movements for high risk/reward
 */
public class StockOptionsManager {
    private static final Map<UUID, PlayerStockData> playerStocks = new ConcurrentHashMap<>();
    private static long lastOptionsReset = -1;
    private static final Random random = new Random();
    
    public static class PlayerStockData {
        private final List<StockOption> activeOptions = new ArrayList<>();
        private long totalProfit = 0;
        private long totalLoss = 0;
        
        public List<StockOption> getActiveOptions() { return activeOptions; }
        public long getTotalProfit() { return totalProfit; }
        public void addProfit(long amount) { this.totalProfit += amount; }
        public long getTotalLoss() { return totalLoss; }
        public void addLoss(long amount) { this.totalLoss += amount; }
    }
    
    public static class StockOption {
        private final Item item;
        private final long strikePrice;
        private final long premium; // Cost to buy option
        private final boolean isCall; // true = bet price goes UP, false = bet price goes DOWN
        private final long expirationTime; // MC ticks
        private final long purchaseTime;
        
        public StockOption(Item item, long strikePrice, long premium, boolean isCall, long expirationTime, long purchaseTime) {
            this.item = item;
            this.strikePrice = strikePrice;
            this.premium = premium;
            this.isCall = isCall;
            this.expirationTime = expirationTime;
            this.purchaseTime = purchaseTime;
        }
        
        public Item getItem() { return item; }
        public long getStrikePrice() { return strikePrice; }
        public long getPremium() { return premium; }
        public boolean isCall() { return isCall; }
        public long getExpirationTime() { return expirationTime; }
        public long getPurchaseTime() { return purchaseTime; }
        
        public boolean isExpired(long currentTime) {
            return currentTime >= expirationTime;
        }
        
        public long calculateProfit() {
            long basePrice = ItemPricing.getBuyPrice(item);
            long currentPrice = PriceFluctuation.getAdjustedPrice(item, basePrice);
            
            if (isCall) {
                // Call option: profit if price goes UP
                if (currentPrice > strikePrice) {
                    return (currentPrice - strikePrice) * 10 - premium; // 10x multiplier
                }
            } else {
                // Put option: profit if price goes DOWN
                if (currentPrice < strikePrice) {
                    return (strikePrice - currentPrice) * 10 - premium; // 10x multiplier
                }
            }
            return -premium; // Lost premium
        }
    }
    
    /**
     * Get player stock data
     */
    public static PlayerStockData getPlayerStockData(UUID playerUUID) {
        return playerStocks.computeIfAbsent(playerUUID, k -> new PlayerStockData());
    }
    
    /**
     * Buy call option (bet price goes UP)
     */
    public static boolean buyCallOption(ServerPlayer player, Item item, long strikePrice, long premium) {
        return buyOption(player, item, strikePrice, premium, true);
    }
    
    /**
     * Buy put option (bet price goes DOWN)
     */
    public static boolean buyPutOption(ServerPlayer player, Item item, long strikePrice, long premium) {
        return buyOption(player, item, strikePrice, premium, false);
    }
    
    private static boolean buyOption(ServerPlayer player, Item item, long strikePrice, long premium, boolean isCall) {
        // Check if can afford premium
        if (!CurrencyManager.canAfford(player, premium)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[STOCKS] Insufficient funds! Premium: " + CurrencyManager.format(premium)));
            return false;
        }
        
        // Deduct premium
        CurrencyManager.removeMoney(player, premium);
        
        // Create option expiring in 3 MC hours (3600 ticks)
        long currentTime = player.level().getServer().overworld().getDayTime();
        long expirationTime = currentTime + 3600;
        
        StockOption option = new StockOption(item, strikePrice, premium, isCall, expirationTime, currentTime);
        
        PlayerStockData data = getPlayerStockData(player.getUUID());
        data.getActiveOptions().add(option);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[STOCKS] " + (isCall ? "CALL" : "PUT") + " option purchased!"));
        player.sendSystemMessage(Component.literal(
            "§7Item: §e" + item.getDescriptionId()));
        player.sendSystemMessage(Component.literal(
            "§7Strike: §6" + CurrencyManager.format(strikePrice)));
        player.sendSystemMessage(Component.literal(
            "§7Premium: §c-" + CurrencyManager.format(premium)));
        player.sendSystemMessage(Component.literal(
            "§7Expires in: §e~3 MC hours"));
        
        return true;
    }
    
    /**
     * Exercise option early (take profit/loss now)
     */
    public static boolean exerciseOption(ServerPlayer player, int optionIndex) {
        PlayerStockData data = getPlayerStockData(player.getUUID());
        
        if (optionIndex < 0 || optionIndex >= data.getActiveOptions().size()) {
            player.sendSystemMessage(Component.literal("§c§l[STOCKS] Invalid option!"));
            return false;
        }
        
        StockOption option = data.getActiveOptions().get(optionIndex);
        long profit = option.calculateProfit();
        
        // Remove option
        data.getActiveOptions().remove(optionIndex);
        
        if (profit > 0) {
            CurrencyManager.addMoney(player, profit);
            data.addProfit(profit);
            player.sendSystemMessage(Component.literal(
                "§a§l[STOCKS] PROFIT: +" + CurrencyManager.format(profit) + "!"));
        } else {
            data.addLoss(-profit);
            player.sendSystemMessage(Component.literal(
                "§c§l[STOCKS] LOSS: " + CurrencyManager.format(-profit)));
        }
        
        return true;
    }
    
    /**
     * Process expired options
     */
    public static void processExpiredOptions(long currentTime, net.minecraft.server.MinecraftServer server) {
        for (Map.Entry<UUID, PlayerStockData> entry : playerStocks.entrySet()) {
            UUID playerUUID = entry.getKey();
            PlayerStockData data = entry.getValue();
            
            Iterator<StockOption> iterator = data.getActiveOptions().iterator();
            while (iterator.hasNext()) {
                StockOption option = iterator.next();
                
                if (option.isExpired(currentTime)) {
                    long profit = option.calculateProfit();
                    
                    ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                    
                    if (profit > 0) {
                        CurrencyManager.addMoney(player, profit);
                        data.addProfit(profit);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(
                                "§a§l[STOCKS] Option expired - PROFIT: +" + CurrencyManager.format(profit)));
                        }
                    } else {
                        data.addLoss(-profit);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(
                                "§c§l[STOCKS] Option expired - LOSS: " + CurrencyManager.format(-profit)));
                        }
                    }
                    
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * Get suggested options (hot stocks)
     */
    public static List<Item> getSuggestedOptions() {
        List<Item> suggestions = new ArrayList<>();
        List<Item> allItems = new ArrayList<>(ItemPricing.getAllPrices().keySet());
        
        // Pick 10 random items
        for (int i = 0; i < 10 && i < allItems.size(); i++) {
            suggestions.add(allItems.get(random.nextInt(allItems.size())));
        }
        
        return suggestions;
    }
}
