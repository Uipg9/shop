package com.shopmod.pricing;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages item pricing for buying and selling
 * TODO: Load from config file and expand to all items
 */
public class ItemPricing {
    private static final Map<Item, PriceData> PRICES = new HashMap<>();
    
    static {
        // Initialize with some basic prices
        // Sell price is 80% of buy price (20% reduction)
        registerPrice(Items.DIRT, 1, 1);
        registerPrice(Items.COBBLESTONE, 1, 1);
        registerPrice(Items.OAK_LOG, 5, 4);
        registerPrice(Items.COAL, 10, 8);
        registerPrice(Items.IRON_INGOT, 50, 40);
        registerPrice(Items.GOLD_INGOT, 100, 80);
        registerPrice(Items.DIAMOND, 500, 400);
        registerPrice(Items.EMERALD, 250, 200);
        registerPrice(Items.NETHERITE_INGOT, 2000, 1600);
        
        // Add more items...
        // TODO: Generate prices for ALL items dynamically
    }
    
    private static void registerPrice(Item item, long buyPrice, long sellPrice) {
        PRICES.put(item, new PriceData(buyPrice, sellPrice));
    }
    
    /**
     * Gets all items that have prices
     */
    public static Map<Item, PriceData> getAllPricedItems() {
        return PRICES;
    }
    
    /**
     * Gets the buy price for an item (what player pays)
     */
    public static long getBuyPrice(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.buyPrice : 100; // Default price
    }
    
    /**
     * Gets the sell price for an item (what player receives)
     */
    public static long getSellPrice(Item item) {
        PriceData data = PRICES.get(item);
        return data != null ? data.sellPrice : 50; // Default price
    }
    
    /**
     * Checks if an item can be sold
     */
    public static boolean canSell(Item item) {
        return getSellPrice(item) > 0;
    }
    
    /**
     * Data class for item prices
     */
    public static class PriceData {
        private final long buyPrice;
        private final long sellPrice;
        
        public PriceData(long buyPrice, long sellPrice) {
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
        
        public long buyPrice() {
            return buyPrice;
        }
        
        public long sellPrice() {
            return sellPrice;
        }
    }
}
