package com.shopmod.blackmarket;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.shop.ItemPricing;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Black Market system - Risky trades with potential scams
 */
public class BlackMarketManager {
    private static final Random RANDOM = new Random();
    private static final double SCAM_CHANCE = 0.15; // 15% chance to get scammed
    private static final List<BlackMarketDeal> DAILY_DEALS = new ArrayList<>();
    private static long lastRefresh = -1;
    
    /**
     * Generate daily black market deals
     */
    public static void generateDailyDeals() {
        DAILY_DEALS.clear();
        
        // 5 high-risk, high-reward deals
        for (int i = 0; i < 5; i++) {
            DAILY_DEALS.add(BlackMarketDeal.generateDeal());
        }
    }
    
    /**
     * Get current deals (refresh if needed)
     */
    public static List<BlackMarketDeal> getCurrentDeals(long currentDay) {
        if (lastRefresh != currentDay) {
            generateDailyDeals();
            lastRefresh = currentDay;
        }
        return new ArrayList<>(DAILY_DEALS);
    }
    
    /**
     * Attempt to purchase from black market
     */
    public static boolean purchase(ServerPlayer player, BlackMarketDeal deal) {
        long balance = CurrencyManager.getBalance(player);
        
        // Check cost
        if (balance < deal.getCost()) {
            player.sendSystemMessage(Component.literal("§cNot enough money!"));
            return false;
        }
        
        // Charge money
        CurrencyManager.removeMoney(player, deal.getCost());
        
        // Roll for scam
        if (RANDOM.nextDouble() < SCAM_CHANCE) {
            player.sendSystemMessage(Component.literal("§4§l[SCAMMED!]"));
            player.sendSystemMessage(Component.literal("§cThe deal was fake! You lost " + 
                CurrencyManager.format(deal.getCost()) + "!"));
            player.sendSystemMessage(Component.literal("§7(This is why it's called the BLACK market...)"));
            return false;
        }
        
        // Success! Give item
        ItemStack stack = deal.getReward().copy();
        
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        
        player.sendSystemMessage(Component.literal("§a§l[BLACK MARKET]"));
        player.sendSystemMessage(Component.literal("§7Deal successful! Obtained §e" + 
            deal.getRewardName()));
        
        return true;
    }
    
    /**
     * A black market deal
     */
    public static class BlackMarketDeal {
        private final String name;
        private final ItemStack reward;
        private final long cost;
        private final double discountPercent; // How much "cheaper" than normal
        
        public BlackMarketDeal(String name, ItemStack reward, long cost, double discountPercent) {
            this.name = name;
            this.reward = reward;
            this.cost = cost;
            this.discountPercent = discountPercent;
        }
        
        public String getName() { return name; }
        public String getRewardName() { return reward.getHoverName().getString(); }
        public ItemStack getReward() { return reward; }
        public long getCost() { return cost; }
        public double getDiscountPercent() { return discountPercent; }
        
        public static BlackMarketDeal generateDeal() {
            // Generate a random valuable item at 40-70% discount
            List<Item> valuable = Arrays.asList(
                Items.DIAMOND_BLOCK,
                Items.NETHERITE_INGOT,
                Items.NETHERITE_BLOCK,
                Items.NETHER_STAR,
                Items.ELYTRA,
                Items.SHULKER_BOX,
                Items.ENCHANTED_GOLDEN_APPLE,
                Items.TOTEM_OF_UNDYING,
                Items.DRAGON_HEAD,
                Items.BEACON
            );
            
            Item item = valuable.get(RANDOM.nextInt(valuable.size()));
            int quantity = 1 + RANDOM.nextInt(3); // 1-3 items
            
            // Calculate discounted price (40-70% off)
            double discountPercent = 40 + RANDOM.nextDouble() * 30;
            long normalPrice = ItemPricing.getBuyPrice(item) * quantity;
            long discountedPrice = (long)(normalPrice * (1.0 - discountPercent / 100.0));
            
            ItemStack stack = new ItemStack(item, quantity);
            
            String dealName = "§5" + quantity + "x " + item.getDescriptionId() + 
                " §7(§a" + (int)discountPercent + "% OFF§7)";
            
            return new BlackMarketDeal(dealName, stack, discountedPrice, discountPercent);
        }
    }
}
