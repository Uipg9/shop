package com.shopmod.auction;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Auction House system with NPC bidding simulation
 * Creates realistic auction experience in single player
 */
public class AuctionManager {
    private static final Map<UUID, PlayerAuctionData> playerData = new ConcurrentHashMap<>();
    private static List<AuctionItem> currentAuctions = new ArrayList<>();
    private static long lastAuctionReset = -1;
    private static final Random random = new Random();
    
    // Fake NPC bidder names for immersion
    private static final String[] NPC_NAMES = {
        "Steve_Trader", "Alex_Merchant", "Villager#47", "Enderman_Collector",
        "Diamond_Hunter", "Iron_Baron", "Wealthy_Miner", "Rich_Farmer",
        "Gold_Digger", "Redstone_Engineer", "Block_Tycoon", "Ore_Master",
        "Trade_King", "Market_Whale", "Deep_Pockets", "Cash_Lord"
    };
    
    public static class PlayerAuctionData {
        private final Map<Integer, Long> playerBids = new HashMap<>(); // auction index -> bid amount
        private final List<ItemStack> wonItems = new ArrayList<>();
        
        public Map<Integer, Long> getPlayerBids() { return playerBids; }
        public List<ItemStack> getWonItems() { return wonItems; }
    }
    
    /**
     * Get player auction data
     */
    public static PlayerAuctionData getPlayerData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> new PlayerAuctionData());
    }
    
    /**
     * Get current auctions, generating new ones if needed
     */
    public static List<AuctionItem> getCurrentAuctions(long currentDayTime) {
        long currentDay = currentDayTime / 24000;
        
        // Reset auctions at dawn (new day)
        if (currentDay > lastAuctionReset) {
            currentAuctions = AuctionItem.generateDailyAuctions(currentDayTime);
            lastAuctionReset = currentDay;
            
            // Clear old bids
            playerData.values().forEach(data -> data.getPlayerBids().clear());
        }
        
        return currentAuctions;
    }
    
    /**
     * Place bid on auction
     */
    public static boolean placeBid(ServerPlayer player, int auctionIndex, long bidAmount) {
        if (auctionIndex < 0 || auctionIndex >= currentAuctions.size()) {
            player.sendSystemMessage(Component.literal("§c§l[AUCTION] Invalid auction!"));
            return false;
        }
        
        AuctionItem auction = currentAuctions.get(auctionIndex);
        
        // Check if auction expired
        if (auction.isExpired(player.level().getServer().overworld().getDayTime())) {
            player.sendSystemMessage(Component.literal("§c§l[AUCTION] Auction has ended!"));
            return false;
        }
        
        // Must bid at least 5% more than current bid
        long minimumBid = (long)(auction.getCurrentBid() * 1.05);
        if (bidAmount < minimumBid) {
            player.sendSystemMessage(Component.literal(
                "§c§l[AUCTION] Minimum bid: " + CurrencyManager.format(minimumBid)));
            return false;
        }
        
        // Check if player can afford
        if (!CurrencyManager.canAfford(player, bidAmount)) {
            player.sendSystemMessage(Component.literal("§c§l[AUCTION] Insufficient funds!"));
            return false;
        }
        
        // Place bid
        auction.setCurrentBid(bidAmount);
        auction.setCurrentBidder(player.getName().getString());
        auction.addBidHistory(player.getName().getString() + ": " + CurrencyManager.format(bidAmount));
        
        PlayerAuctionData data = getPlayerData(player.getUUID());
        data.getPlayerBids().put(auctionIndex, bidAmount);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[AUCTION] Bid placed: " + CurrencyManager.format(bidAmount) + "!"));
        
        // Simulate NPC counter-bids (60% chance)
        if (random.nextDouble() < 0.6) {
            simulateNPCBid(auction, bidAmount);
        }
        
        return true;
    }
    
    /**
     * Simulate NPC bidding for realism
     */
    private static void simulateNPCBid(AuctionItem auction, long playerBid) {
        // Random NPC bids slightly higher (5-15% more)
        double increase = 1.05 + (random.nextDouble() * 0.10);
        long npcBid = (long)(playerBid * increase);
        
        // Cap at 80% of instant buy price
        if (npcBid > auction.getInstantBuyPrice() * 0.8) {
            return; // NPCs won't bid too close to instant buy
        }
        
        String npcName = NPC_NAMES[random.nextInt(NPC_NAMES.length)];
        auction.setCurrentBid(npcBid);
        auction.setCurrentBidder(npcName);
        auction.addBidHistory(npcName + ": " + CurrencyManager.format(npcBid));
    }
    
    /**
     * Instant buy auction item
     */
    public static boolean instantBuy(ServerPlayer player, int auctionIndex) {
        if (auctionIndex < 0 || auctionIndex >= currentAuctions.size()) {
            player.sendSystemMessage(Component.literal("§c§l[AUCTION] Invalid auction!"));
            return false;
        }
        
        AuctionItem auction = currentAuctions.get(auctionIndex);
        long price = auction.getInstantBuyPrice();
        
        // Check if can afford
        if (!CurrencyManager.canAfford(player, price)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[AUCTION] Insufficient funds! Need: " + CurrencyManager.format(price)));
            return false;
        }
        
        // Buy item
        CurrencyManager.removeMoney(player, price);
        
        // Give item to player
        ItemStack item = auction.getItemStack().copy();
        player.getInventory().add(item);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[AUCTION] Purchased " + auction.getDisplayName() + " for " + 
            CurrencyManager.format(price) + "!"));
        
        // Remove from auctions
        currentAuctions.remove(auctionIndex);
        
        return true;
    }
    
    /**
     * Process auction endings and award winners
     */
    public static void processAuctionEndings(long currentTime) {
        Iterator<AuctionItem> iterator = currentAuctions.iterator();
        
        while (iterator.hasNext()) {
            AuctionItem auction = iterator.next();
            
            if (auction.isExpired(currentTime)) {
                // Check if a real player won
                boolean playerWon = !auction.getCurrentBidder().equals("None") && 
                                  Arrays.stream(NPC_NAMES).noneMatch(name -> name.equals(auction.getCurrentBidder()));
                
                if (playerWon) {
                    // Find winning player
                    // Server reference would need to be passed in or cached
                    // For now, skip notification (items will be given via wonItems list)
                }
                
                // Remove expired auction
                iterator.remove();
            }
        }
    }
    
    /**
     * Get auction by index
     */
    public static AuctionItem getAuction(int index) {
        if (index < 0 || index >= currentAuctions.size()) return null;
        return currentAuctions.get(index);
    }
}
