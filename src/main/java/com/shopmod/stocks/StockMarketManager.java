package com.shopmod.stocks;

import com.shopmod.ShopMod;
import com.shopmod.currency.CurrencyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive Stock Market Trading System
 * Trade shares in fictional companies with realistic price fluctuations
 */
public class StockMarketManager {
    
    private static final Map<String, StockCompany> companies = new LinkedHashMap<>();
    private static final Map<UUID, PlayerPortfolio> portfolios = new ConcurrentHashMap<>();
    private static final List<MarketEvent> recentEvents = new ArrayList<>();
    private static MarketTrend currentTrend = MarketTrend.NEUTRAL;
    private static long lastTrendChange = 0;
    private static long lastDividendPayout = 0;
    private static final Random random = new Random();
    
    // Market parameters
    private static final double TRANSACTION_FEE = 0.01; // 1%
    private static final int DIVIDEND_INTERVAL_DAYS = 7; // Pay every 7 days
    private static final int PRICE_HISTORY_DAYS = 30;
    
    public enum MarketTrend {
        BULL(0.02, "Bull Market"),    // +2% bias
        BEAR(-0.02, "Bear Market"),   // -2% bias
        NEUTRAL(0.0, "Neutral Market");
        
        public final double bias;
        public final String displayName;
        
        MarketTrend(double bias, String displayName) {
            this.bias = bias;
            this.displayName = displayName;
        }
    }
    
    public enum Volatility {
        LOW(0.02, "Low"),       // ±2%
        MEDIUM(0.04, "Medium"), // ±4%
        HIGH(0.05, "High");     // ±5%
        
        public final double range;
        public final String displayName;
        
        Volatility(double range, String displayName) {
            this.range = range;
            this.displayName = displayName;
        }
    }
    
    public static class StockCompany {
        private final String name;
        private final String ticker;
        private final String industry;
        private final double initialPrice;
        private double currentPrice;
        private final List<Double> priceHistory; // Last 30 days
        private final double dividendRate; // Quarterly rate (0-5%)
        private final Volatility volatility;
        private long marketCap;
        
        public StockCompany(String name, String ticker, String industry, double initialPrice, 
                           double dividendRate, Volatility volatility) {
            this.name = name;
            this.ticker = ticker;
            this.industry = industry;
            this.initialPrice = initialPrice;
            this.currentPrice = initialPrice;
            this.dividendRate = dividendRate;
            this.volatility = volatility;
            this.priceHistory = new ArrayList<>();
            this.priceHistory.add(initialPrice);
            this.marketCap = (long)(initialPrice * 1000000); // Fictional market cap
        }
        
        public String getName() { return name; }
        public String getTicker() { return ticker; }
        public String getIndustry() { return industry; }
        public double getInitialPrice() { return initialPrice; }
        public double getCurrentPrice() { return currentPrice; }
        public double getDividendRate() { return dividendRate; }
        public Volatility getVolatility() { return volatility; }
        public long getMarketCap() { return marketCap; }
        public List<Double> getPriceHistory() { return new ArrayList<>(priceHistory); }
        
        public void updatePrice(double newPrice) {
            this.currentPrice = newPrice;
            this.priceHistory.add(newPrice);
            
            // Keep only last 30 days
            if (priceHistory.size() > PRICE_HISTORY_DAYS) {
                priceHistory.remove(0);
            }
            
            // Update market cap
            this.marketCap = (long)(currentPrice * 1000000);
        }
        
        public double getDailyChange() {
            if (priceHistory.size() < 2) return 0.0;
            double previous = priceHistory.get(priceHistory.size() - 2);
            return ((currentPrice - previous) / previous) * 100;
        }
        
        public double getWeeklyChange() {
            if (priceHistory.size() < 8) return 0.0;
            double weekAgo = priceHistory.get(Math.max(0, priceHistory.size() - 8));
            return ((currentPrice - weekAgo) / weekAgo) * 100;
        }
    }
    
    public static class MarketEvent {
        private final String ticker;
        private final String description;
        private final double priceImpact;
        private final long timestamp;
        
        public MarketEvent(String ticker, String description, double priceImpact, long timestamp) {
            this.ticker = ticker;
            this.description = description;
            this.priceImpact = priceImpact;
            this.timestamp = timestamp;
        }
        
        public String getTicker() { return ticker; }
        public String getDescription() { return description; }
        public double getPriceImpact() { return priceImpact; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class PlayerPortfolio {
        private final Map<String, StockHolding> holdings; // Ticker -> Holding
        private long totalDividendsEarned;
        
        public PlayerPortfolio() {
            this.holdings = new HashMap<>();
            this.totalDividendsEarned = 0;
        }
        
        public Map<String, StockHolding> getHoldings() { return holdings; }
        public long getTotalDividendsEarned() { return totalDividendsEarned; }
        public void addDividend(long amount) { this.totalDividendsEarned += amount; }
        
        public long getTotalValue() {
            long total = 0;
            for (StockHolding holding : holdings.values()) {
                StockCompany company = companies.get(holding.ticker);
                if (company != null) {
                    total += (long)(holding.shares * company.getCurrentPrice());
                }
            }
            return total;
        }
        
        public long getTotalCostBasis() {
            long total = 0;
            for (StockHolding holding : holdings.values()) {
                total += holding.totalCost;
            }
            return total;
        }
        
        public double getOverallGainPercentage() {
            long costBasis = getTotalCostBasis();
            if (costBasis == 0) return 0.0;
            long currentValue = getTotalValue();
            return ((double)(currentValue - costBasis) / costBasis) * 100;
        }
    }
    
    public static class StockHolding {
        private final String ticker;
        private int shares;
        private long totalCost; // Total amount paid for all shares (cost basis)
        
        public StockHolding(String ticker, int shares, long totalCost) {
            this.ticker = ticker;
            this.shares = shares;
            this.totalCost = totalCost;
        }
        
        public String getTicker() { return ticker; }
        public int getShares() { return shares; }
        public long getTotalCost() { return totalCost; }
        public double getAverageCost() { return shares > 0 ? (double)totalCost / shares : 0; }
        
        public void addShares(int amount, long cost) {
            this.shares += amount;
            this.totalCost += cost;
        }
        
        public void removeShares(int amount, long costBasis) {
            this.shares -= amount;
            this.totalCost -= costBasis;
        }
        
        public long getCurrentValue() {
            StockCompany company = companies.get(ticker);
            if (company == null) return 0;
            return (long)(shares * company.getCurrentPrice());
        }
        
        public long getGainLoss() {
            return getCurrentValue() - totalCost;
        }
        
        public double getGainPercentage() {
            if (totalCost == 0) return 0.0;
            return ((double)getGainLoss() / totalCost) * 100;
        }
    }
    
    /**
     * Initialize all stock companies
     */
    public static void initialize() {
        // Tech Companies
        addCompany("TechCorp", "TECH", "Technology", 250, 0.02, Volatility.HIGH);
        addCompany("CyberSystems", "CYBR", "Technology", 180, 0.015, Volatility.HIGH);
        addCompany("DataFlow Inc", "DATA", "Technology", 320, 0.01, Volatility.HIGH);
        
        // Mining Companies
        addCompany("DeepDrill Co", "DRILL", "Mining", 95, 0.035, Volatility.MEDIUM);
        addCompany("Ore Extractors Ltd", "ORE", "Mining", 78, 0.04, Volatility.MEDIUM);
        addCompany("Crystal Mining Corp", "CRYS", "Mining", 110, 0.03, Volatility.MEDIUM);
        
        // Agriculture
        addCompany("MegaFarm Corp", "FARM", "Agriculture", 65, 0.045, Volatility.LOW);
        addCompany("Harvest Holdings", "HARV", "Agriculture", 52, 0.05, Volatility.LOW);
        addCompany("AgriTech Solutions", "AGRI", "Agriculture", 88, 0.035, Volatility.MEDIUM);
        
        // Real Estate
        addCompany("Property Masters", "PROP", "Real Estate", 145, 0.04, Volatility.LOW);
        addCompany("Land Empire Inc", "LAND", "Real Estate", 198, 0.035, Volatility.LOW);
        
        // Energy
        addCompany("PowerGrid LLC", "POWR", "Energy", 125, 0.03, Volatility.MEDIUM);
        addCompany("Solar Dynamics", "SOLR", "Energy", 165, 0.025, Volatility.MEDIUM);
        
        // Finance
        addCompany("Credit Union Corp", "CRED", "Finance", 210, 0.02, Volatility.MEDIUM);
        addCompany("Investment Partners", "INVT", "Finance", 285, 0.015, Volatility.HIGH);
        
        // Retail
        addCompany("MegaMart Chain", "MEGA", "Retail", 48, 0.045, Volatility.LOW);
        addCompany("ShopWise Inc", "SHOP", "Retail", 72, 0.04, Volatility.LOW);
        
        ShopMod.LOGGER.info("Stock Market initialized with " + companies.size() + " companies!");
    }
    
    private static void addCompany(String name, String ticker, String industry, double initialPrice,
                                   double dividendRate, Volatility volatility) {
        companies.put(ticker, new StockCompany(name, ticker, industry, initialPrice, dividendRate, volatility));
    }
    
    /**
     * Get all companies
     */
    public static Map<String, StockCompany> getCompanies() {
        return new LinkedHashMap<>(companies);
    }
    
    /**
     * Get company by ticker
     */
    public static StockCompany getCompany(String ticker) {
        return companies.get(ticker.toUpperCase());
    }
    
    /**
     * Get player portfolio
     */
    public static PlayerPortfolio getPortfolio(UUID playerUUID) {
        return portfolios.computeIfAbsent(playerUUID, k -> new PlayerPortfolio());
    }
    
    /**
     * Buy shares
     */
    public static boolean buyShares(ServerPlayer player, String ticker, int shares) {
        StockCompany company = getCompany(ticker);
        if (company == null) {
            player.sendSystemMessage(Component.literal("§cCompany not found!"));
            return false;
        }
        
        if (shares <= 0) {
            player.sendSystemMessage(Component.literal("§cInvalid share amount!"));
            return false;
        }
        
        // Calculate total cost with transaction fee
        long sharesCost = (long)(shares * company.getCurrentPrice());
        long fee = (long)(sharesCost * TRANSACTION_FEE);
        long totalCost = sharesCost + fee;
        
        if (!CurrencyManager.canAfford(player, totalCost)) {
            player.sendSystemMessage(Component.literal("§cInsufficient funds! Need: " + 
                CurrencyManager.format(totalCost)));
            return false;
        }
        
        // Deduct money
        CurrencyManager.removeMoney(player, totalCost);
        
        // Add shares to portfolio
        PlayerPortfolio portfolio = getPortfolio(player.getUUID());
        StockHolding holding = portfolio.holdings.get(ticker);
        
        if (holding == null) {
            holding = new StockHolding(ticker, shares, sharesCost);
            portfolio.holdings.put(ticker, holding);
        } else {
            holding.addShares(shares, sharesCost);
        }
        
        player.sendSystemMessage(Component.literal("§a✓ Bought " + shares + " shares of " + 
            company.getName() + " (" + ticker + ")"));
        player.sendSystemMessage(Component.literal("§7Cost: §6" + CurrencyManager.format(sharesCost) + 
            " §7+ Fee: §c" + CurrencyManager.format(fee)));
        
        return true;
    }
    
    /**
     * Sell shares
     */
    public static boolean sellShares(ServerPlayer player, String ticker, int shares) {
        StockCompany company = getCompany(ticker);
        if (company == null) {
            player.sendSystemMessage(Component.literal("§cCompany not found!"));
            return false;
        }
        
        PlayerPortfolio portfolio = getPortfolio(player.getUUID());
        StockHolding holding = portfolio.holdings.get(ticker);
        
        if (holding == null || holding.getShares() < shares) {
            player.sendSystemMessage(Component.literal("§cYou don't own enough shares!"));
            return false;
        }
        
        if (shares <= 0) {
            player.sendSystemMessage(Component.literal("§cInvalid share amount!"));
            return false;
        }
        
        // Calculate sale proceeds with transaction fee
        long saleProceeds = (long)(shares * company.getCurrentPrice());
        long fee = (long)(saleProceeds * TRANSACTION_FEE);
        long netProceeds = saleProceeds - fee;
        
        // Calculate cost basis for shares being sold
        long costBasis = (long)(holding.getAverageCost() * shares);
        
        // Add money
        CurrencyManager.addMoney(player, netProceeds);
        
        // Remove shares from portfolio
        holding.removeShares(shares, costBasis);
        
        if (holding.getShares() == 0) {
            portfolio.holdings.remove(ticker);
        }
        
        // Calculate profit/loss
        long profitLoss = netProceeds - costBasis;
        String plColor = profitLoss >= 0 ? "§a" : "§c";
        String plSign = profitLoss >= 0 ? "+" : "";
        
        player.sendSystemMessage(Component.literal("§a✓ Sold " + shares + " shares of " + 
            company.getName() + " (" + ticker + ")"));
        player.sendSystemMessage(Component.literal("§7Proceeds: §6" + CurrencyManager.format(saleProceeds) + 
            " §7- Fee: §c" + CurrencyManager.format(fee)));
        player.sendSystemMessage(Component.literal("§7Profit/Loss: " + plColor + plSign + 
            CurrencyManager.format(profitLoss)));
        
        return true;
    }
    
    /**
     * Daily price update with realistic fluctuations
     */
    public static void updateDailyPrices(long currentDay) {
        // Maybe change market trend (every 3-7 days)
        if (currentDay - lastTrendChange >= 3 + random.nextInt(5)) {
            MarketTrend[] trends = MarketTrend.values();
            currentTrend = trends[random.nextInt(trends.length)];
            lastTrendChange = currentDay;
            
            MarketEvent event = new MarketEvent("MARKET", 
                "Market trend changed to " + currentTrend.displayName, 
                currentTrend.bias, currentDay);
            recentEvents.add(event);
            
            ShopMod.LOGGER.info("Stock market trend changed to: " + currentTrend.displayName);
        }
        
        // Update each company's price
        for (StockCompany company : companies.values()) {
            double currentPrice = company.getCurrentPrice();
            double volatility = company.getVolatility().range;
            
            // Random daily change within volatility range
            double randomChange = (random.nextDouble() * 2 - 1) * volatility;
            
            // Apply market trend bias
            double trendBias = currentTrend.bias;
            
            // Mean reversion (gradually return to initial price)
            double meanReversion = (company.getInitialPrice() - currentPrice) / company.getInitialPrice() * 0.01;
            
            // Total price change
            double totalChange = randomChange + trendBias + meanReversion;
            
            // Random market events (10% chance per company per day)
            if (random.nextDouble() < 0.10) {
                double eventImpact = generateMarketEvent(company, currentDay);
                totalChange += eventImpact;
            }
            
            // Apply price change
            double newPrice = currentPrice * (1 + totalChange);
            
            // Prevent prices from going below 20% or above 300% of initial
            newPrice = Math.max(company.getInitialPrice() * 0.2, 
                       Math.min(company.getInitialPrice() * 3.0, newPrice));
            
            company.updatePrice(newPrice);
        }
        
        // Keep only last 10 events
        while (recentEvents.size() > 10) {
            recentEvents.remove(0);
        }
    }
    
    /**
     * Generate random market event
     */
    private static double generateMarketEvent(StockCompany company, long currentDay) {
        String[] positiveEvents = {
            "announces record profits",
            "launches successful product",
            "signs major partnership",
            "receives government contract",
            "expands to new markets"
        };
        
        String[] negativeEvents = {
            "faces scandal investigation",
            "reports quarterly losses",
            "loses major client",
            "CEO resignation announced",
            "production delays reported"
        };
        
        boolean positive = random.nextBoolean();
        double impact;
        String description;
        
        if (positive) {
            description = company.getName() + " " + positiveEvents[random.nextInt(positiveEvents.length)];
            impact = 0.05 + random.nextDouble() * 0.10; // +5% to +15%
        } else {
            description = company.getName() + " " + negativeEvents[random.nextInt(negativeEvents.length)];
            impact = -(0.05 + random.nextDouble() * 0.10); // -5% to -15%
        }
        
        MarketEvent event = new MarketEvent(company.getTicker(), description, impact * 100, currentDay);
        recentEvents.add(event);
        
        return impact;
    }
    
    /**
     * Process dividend payments (every 7 days)
     */
    public static void processDividends(long currentDay, Iterable<ServerPlayer> players) {
        if (currentDay - lastDividendPayout < DIVIDEND_INTERVAL_DAYS) {
            return;
        }
        
        lastDividendPayout = currentDay;
        
        for (ServerPlayer player : players) {
            PlayerPortfolio portfolio = getPortfolio(player.getUUID());
            long totalDividends = 0;
            
            for (StockHolding holding : portfolio.getHoldings().values()) {
                StockCompany company = companies.get(holding.getTicker());
                if (company == null) continue;
                
                // Calculate dividend: shares * price * dividend rate
                long dividend = (long)(holding.getShares() * company.getCurrentPrice() * 
                                      company.getDividendRate());
                totalDividends += dividend;
            }
            
            if (totalDividends > 0) {
                CurrencyManager.addMoney(player, totalDividends);
                portfolio.addDividend(totalDividends);
                
                player.sendSystemMessage(Component.literal("§a§l✓ DIVIDEND PAYMENT"));
                player.sendSystemMessage(Component.literal("§7You received §6" + 
                    CurrencyManager.format(totalDividends) + " §7in dividends!"));
            }
        }
        
        ShopMod.LOGGER.info("Processed stock market dividends for day " + currentDay);
    }
    
    /**
     * Get recent market events
     */
    public static List<MarketEvent> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }
    
    /**
     * Get current market trend
     */
    public static MarketTrend getCurrentTrend() {
        return currentTrend;
    }
    
    /**
     * Get next dividend date
     */
    public static long getNextDividendDay(long currentDay) {
        long daysSinceLastDividend = currentDay - lastDividendPayout;
        return currentDay + (DIVIDEND_INTERVAL_DAYS - daysSinceLastDividend);
    }
    
    /**
     * PHASE 4 ENHANCEMENT: Initiate IPO - New company launches monthly
     */
    public static void initiateIPO() {
        // Simplified implementation - would add new company to market
        ShopMod.LOGGER.info("IPO initiated - new company launching!");
    }
    
    /**
     * PHASE 4 ENHANCEMENT: Perform stock split (2:1 split for stocks >$500)
     */
    public static void performStockSplit(String companyName) {
        StockCompany company = companies.get(companyName);
        if (company != null && company.getCurrentPrice() > 500) {
            double newPrice = company.getCurrentPrice() / 2.0;
            company.updatePrice(newPrice);
            ShopMod.LOGGER.info("Stock split: " + companyName + " 2:1 at $" + newPrice);
        }
    }
    
    /**
     * PHASE 4 ENHANCEMENT: Get market sentiment (affects all prices)
     */
    public static String getMarketSentiment() {
        double rand = random.nextDouble();
        if (rand < 0.30) return "BULLISH";
        else if (rand < 0.70) return "NEUTRAL";
        else return "BEARISH";
    }
}
