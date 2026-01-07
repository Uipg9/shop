package com.shopmod.auction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

/**
 * Auction item with starting bid, current bid, and item details
 */
public class AuctionItem {
    private final ItemStack itemStack;
    private final String displayName;
    private final long startingBid;
    private long currentBid;
    private final long instantBuyPrice;
    private final long auctionEndTime; // MC ticks
    private String currentBidder;
    private final List<String> bidHistory;
    private final boolean isRare;
    private final int tier; // Shop tier requirement (0 = any)
    
    public AuctionItem(ItemStack itemStack, String displayName, long startingBid, 
                      long instantBuyPrice, long auctionEndTime, boolean isRare, int tier) {
        this.itemStack = itemStack;
        this.displayName = displayName;
        this.startingBid = startingBid;
        this.currentBid = startingBid;
        this.instantBuyPrice = instantBuyPrice;
        this.auctionEndTime = auctionEndTime;
        this.currentBidder = "None";
        this.bidHistory = new ArrayList<>();
        this.isRare = isRare;
        this.tier = tier;
    }
    
    public ItemStack getItemStack() { return itemStack; }
    public String getDisplayName() { return displayName; }
    public long getStartingBid() { return startingBid; }
    public long getCurrentBid() { return currentBid; }
    public void setCurrentBid(long bid) { this.currentBid = bid; }
    public long getInstantBuyPrice() { return instantBuyPrice; }
    public long getAuctionEndTime() { return auctionEndTime; }
    public String getCurrentBidder() { return currentBidder; }
    public void setCurrentBidder(String bidder) { this.currentBidder = bidder; }
    public List<String> getBidHistory() { return bidHistory; }
    public void addBidHistory(String entry) { bidHistory.add(entry); }
    public boolean isRare() { return isRare; }
    public int getTier() { return tier; }
    
    public boolean isExpired(long currentTime) {
        return currentTime >= auctionEndTime;
    }
    
    public long getTimeRemaining(long currentTime) {
        return Math.max(0, auctionEndTime - currentTime);
    }
    
    /**
     * Generate random auction items for the day
     */
    public static List<AuctionItem> generateDailyAuctions(long currentDayTime) {
        List<AuctionItem> auctions = new ArrayList<>();
        Random random = new Random(currentDayTime); // Seed by day for consistency
        
        long auctionEndTime = currentDayTime + 18000; // End at midnight (18000 ticks = ~15 mins)
        
        // Generate 40 auction items of various types
        
        // Common items (20 items) - Basic materials and tools
        for (int i = 0; i < 20; i++) {
            Item item = getRandomCommonItem(random);
            int amount = 16 + random.nextInt(48); // 16-64 items
            long baseValue = 100 + random.nextInt(400);
            
            ItemStack stack = new ItemStack(item, amount);
            auctions.add(new AuctionItem(
                stack,
                amount + "x " + item.toString(),
                baseValue,
                (long)(baseValue * 1.3), // 30% markup for instant buy
                auctionEndTime,
                false,
                0
            ));
        }
        
        // Uncommon enchanted items (10 items)
        for (int i = 0; i < 10; i++) {
            ItemStack stack = getRandomEnchantedTool(random, 1, 2);
            long baseValue = 1000 + random.nextInt(4000);
            
            auctions.add(new AuctionItem(
                stack,
                stack.getHoverName().getString(),
                baseValue,
                (long)(baseValue * 1.4),
                auctionEndTime,
                true,
                1
            ));
        }
        
        // Rare enchanted items (5 items) - Higher tier or better enchants
        for (int i = 0; i < 5; i++) {
            ItemStack stack = getRandomEnchantedTool(random, 2, 3);
            long baseValue = 5000 + random.nextInt(10000);
            
            auctions.add(new AuctionItem(
                stack,
                stack.getHoverName().getString(),
                baseValue,
                (long)(baseValue * 1.5),
                auctionEndTime,
                true,
                2
            ));
        }
        
        // Very rare items (3 items) - Diamond/Netherite with max enchants
        for (int i = 0; i < 3; i++) {
            ItemStack stack = getRandomMaxEnchantedItem(random);
            long baseValue = 15000 + random.nextInt(35000);
            
            auctions.add(new AuctionItem(
                stack,
                stack.getHoverName().getString(),
                baseValue,
                (long)(baseValue * 1.6),
                auctionEndTime,
                true,
                3
            ));
        }
        
        // Epic items (2 items) - Elytra, Netherite blocks, etc.
        for (int i = 0; i < 2; i++) {
            ItemStack stack = getRandomEpicItem(random);
            long baseValue = 50000 + random.nextInt(50000);
            
            auctions.add(new AuctionItem(
                stack,
                stack.getHoverName().getString(),
                baseValue,
                (long)(baseValue * 1.8),
                auctionEndTime,
                true,
                4
            ));
        }
        
        return auctions;
    }
    
    private static Item getRandomCommonItem(Random random) {
        Item[] items = {
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.EMERALD,
            Items.COAL, Items.REDSTONE, Items.LAPIS_LAZULI, Items.COPPER_INGOT,
            Items.OAK_LOG, Items.SPRUCE_LOG, Items.STONE, Items.COBBLESTONE,
            Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEEF,
            Items.LEATHER, Items.WHITE_WOOL, Items.STRING, Items.ARROW
        };
        return items[random.nextInt(items.length)];
    }
    
    private static ItemStack getRandomEnchantedTool(Random random, int minLevel, int maxLevel) {
        Item[] tools = {
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE,
            Items.IRON_SWORD, Items.IRON_PICKAXE, Items.BOW, Items.CROSSBOW,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS, Items.SHIELD
        };
        
        ItemStack stack = new ItemStack(tools[random.nextInt(tools.length)]);
        // Add enchantments (simplified - in real implementation would use proper enchantment API)
        return stack;
    }
    
    private static ItemStack getRandomMaxEnchantedItem(Random random) {
        Item[] items = {
            Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE
        };
        
        ItemStack stack = new ItemStack(items[random.nextInt(items.length)]);
        // Add max enchantments
        return stack;
    }
    
    private static ItemStack getRandomEpicItem(Random random) {
        Item[] items = {
            Items.ELYTRA, Items.NETHERITE_BLOCK, Items.BEACON,
            Items.ENCHANTED_GOLDEN_APPLE, Items.TOTEM_OF_UNDYING,
            Items.DRAGON_HEAD, Items.SHULKER_BOX
        };
        
        int amount = random.nextInt(3) + 1; // 1-3 items
        return new ItemStack(items[random.nextInt(items.length)], amount);
    }
}
