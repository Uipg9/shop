package com.shopmod.wand;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.shop.ItemPricing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sell Wand system - right-click chests to sell contents
 */
public class SellWandManager {
    private static final Map<UUID, WandData> playerWands = new ConcurrentHashMap<>();
    
    public static class WandData {
        private int level;
        private long totalSold;
        private long itemsSold;
        
        public WandData() {
            this.level = 1;
            this.totalSold = 0;
            this.itemsSold = 0;
        }
        
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public long getTotalSold() { return totalSold; }
        public void addSold(long amount) { this.totalSold += amount; }
        public long getItemsSold() { return itemsSold; }
        public void addItemsSold(long count) { this.itemsSold += count; }
        
        public double getSellMultiplier() {
            return 1.0 + (level * 0.05); // +5% per level
        }
        
        public long getUpgradeCost() {
            return (long)(50000 * Math.pow(1.5, level - 1));
        }
        
        public long getRequiredSales() {
            return (long)(100000 * Math.pow(1.8, level - 1));
        }
    }
    
    /**
     * Get player wand data
     */
    public static WandData getWandData(UUID playerUUID) {
        return playerWands.computeIfAbsent(playerUUID, k -> new WandData());
    }
    
    /**
     * Give sell wand to player
     */
    public static ItemStack createSellWand(ServerPlayer player) {
        WandData data = getWandData(player.getUUID());
        
        ItemStack wand = new ItemStack(Items.STICK);
        
        // Set custom name and lore
        wand.set(DataComponents.CUSTOM_NAME, Component.literal("§6§l⚡ Sell Wand §r§7(Level " + data.getLevel() + ")"));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Right-click chests to sell contents"));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Level: §e" + data.getLevel()));
        lore.add(Component.literal("§7Bonus: §a+" + (int)(data.getSellMultiplier() * 100 - 100) + "%"));
        lore.add(Component.literal("§7Total Sold: §6$" + CurrencyManager.format(data.getTotalSold())));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Use §e/wand §7to upgrade!"));
        
        wand.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));
        
        // Add NBT tag to identify as sell wand
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("SellWand", true);
        tag.putInt("WandLevel", data.getLevel());
        wand.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        
        return wand;
    }
    
    /**
     * Check if item is a sell wand
     */
    public static boolean isSellWand(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.STICK) return false;
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        
        CompoundTag tag = customData.copyTag();
        return tag.contains("SellWand");
    }
    
    /**
     * Upgrade wand level
     */
    public static boolean upgradeWand(ServerPlayer player) {
        WandData data = getWandData(player.getUUID());
        
        if (data.getLevel() >= 20) {
            player.sendSystemMessage(Component.literal("§c§l[WAND] Maximum level reached!"));
            return false;
        }
        
        long cost = data.getUpgradeCost();
        
        if (!CurrencyManager.canAfford(player, cost)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WAND] Insufficient funds! Need: §6" + CurrencyManager.format(cost)));
            return false;
        }
        
        // Check sales requirement
        if (data.getTotalSold() < data.getRequiredSales()) {
            player.sendSystemMessage(Component.literal(
                "§c§l[WAND] Need §6$" + CurrencyManager.format(data.getRequiredSales()) + 
                " §ctotal sales to upgrade!"));
            player.sendSystemMessage(Component.literal(
                "§7Current: §6$" + CurrencyManager.format(data.getTotalSold())));
            return false;
        }
        
        CurrencyManager.removeMoney(player, cost);
        data.setLevel(data.getLevel() + 1);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[WAND] Upgraded to Level " + data.getLevel() + "!"));
        player.sendSystemMessage(Component.literal(
            "§7Bonus: §a+" + (int)(data.getSellMultiplier() * 100 - 100) + "%"));
        
        return true;
    }
    
    /**
     * Calculate sell value for item
     */
    public static long calculateSellValue(ItemStack stack, double multiplier) {
        long basePrice = ItemPricing.getSellPrice(stack.getItem());
        return (long)(basePrice * stack.getCount() * multiplier);
    }
}
