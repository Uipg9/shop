package com.shopmod.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/**
 * Utility class for getting proper item display names
 */
public class ItemNameHelper {
    
    /**
     * Get the proper display name for an item
     */
    public static String getItemName(Item item) {
        return new ItemStack(item).getHoverName().getString();
    }
    
    /**
     * Get the proper display name for an item stack
     */
    public static String getItemName(ItemStack stack) {
        return stack.getHoverName().getString();
    }
    
    /**
     * Get formatted display name with count
     */
    public static String getFormattedName(ItemStack stack) {
        String name = stack.getHoverName().getString();
        if (stack.getCount() > 1) {
            return stack.getCount() + "x " + name;
        }
        return name;
    }
}
