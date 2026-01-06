package com.shopmod.gui;

import com.shopmod.shop.EnchantmentShop;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Simple enchanting GUI - drag book onto item to enchant
 */
public class EnchantingGui extends SimpleGui {
    private final ServerPlayer player;
    private ItemStack targetItem = ItemStack.EMPTY;
    private ItemStack enchantBook = ItemStack.EMPTY;
    
    public EnchantingGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        this.player = player;
        this.setTitle(Component.literal("Enchanting Station"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear all slots
        for (int i = 0; i < 27; i++) {
            this.clearSlot(i);
        }
        
        // Decorative borders
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(" ")));
            }
        }
        for (int i = 18; i < 27; i++) {
            if (i != 22) {
                setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(" ")));
            }
        }
        
        // Title
        setSlot(4, new GuiElementBuilder(Items.ENCHANTING_TABLE)
            .setName(Component.literal("§5§lEnchanting Station"))
            .addLoreLine(Component.literal("§7Place item and book below"))
            .addLoreLine(Component.literal("§7then click the anvil to enchant!"))
        );
        
        // Item slot (left)
        if (targetItem.isEmpty()) {
            setSlot(11, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("§e§lPlace Item Here"))
                .addLoreLine(Component.literal("§7Click with item to place"))
                .setCallback((index, type, action) -> {
                    ItemStack cursor = player.containerMenu.getCarried();
                    if (!cursor.isEmpty()) {
                        targetItem = cursor.copy();
                        player.containerMenu.setCarried(ItemStack.EMPTY);
                        updateDisplay();
                    }
                })
            );
        } else {
            setSlot(11, new GuiElementBuilder(targetItem)
                .setName(Component.literal("§aItem: " + targetItem.getHoverName().getString()))
                .addLoreLine(Component.literal("§7Click to remove"))
                .setCallback((index, type, action) -> {
                    player.addItem(targetItem);
                    targetItem = ItemStack.EMPTY;
                    updateDisplay();
                })
            );
        }
        
        // Book slot (right)
        if (enchantBook.isEmpty()) {
            setSlot(15, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("§d§lPlace Enchanted Book Here"))
                .addLoreLine(Component.literal("§7Click with book to place"))
                .setCallback((index, type, action) -> {
                    ItemStack cursor = player.containerMenu.getCarried();
                    if (!cursor.isEmpty() && cursor.is(Items.ENCHANTED_BOOK)) {
                        enchantBook = cursor.copy();
                        player.containerMenu.setCarried(ItemStack.EMPTY);
                        updateDisplay();
                    }
                })
            );
        } else {
            setSlot(15, new GuiElementBuilder(enchantBook)
                .setName(Component.literal("§dBook: " + enchantBook.getHoverName().getString()))
                .addLoreLine(Component.literal("§7Click to remove"))
                .setCallback((index, type, action) -> {
                    player.addItem(enchantBook);
                    enchantBook = ItemStack.EMPTY;
                    updateDisplay();
                })
            );
        }
        
        // Enchant button
        if (!targetItem.isEmpty() && !enchantBook.isEmpty()) {
            setSlot(13, new GuiElementBuilder(Items.ANVIL)
                .setName(Component.literal("§a§l⚒ ENCHANT!"))
                .addLoreLine(Component.literal("§7Click to apply enchantment"))
                .addLoreLine(Component.literal("§7to your item!"))
                .glow()
                .setCallback((index, type, action) -> enchantItem())
            );
        } else {
            setSlot(13, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§l✗ Not Ready"))
                .addLoreLine(Component.literal("§7Place both item and book first"))
            );
        }
        
        // Close button
        setSlot(22, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
            .setName(Component.literal("§cClose"))
            .setCallback((index, type, action) -> this.close())
        );
    }
    
    private void enchantItem() {
        if (targetItem.isEmpty() || enchantBook.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cPlace both item and book first!"));
            return;
        }
        
        // Try to apply enchantment
        boolean success = EnchantmentShop.applyEnchantmentToItem(targetItem, enchantBook);
        
        if (success) {
            // Give back enchanted item
            player.addItem(targetItem);
            
            // Consume book
            enchantBook = ItemStack.EMPTY;
            targetItem = ItemStack.EMPTY;
            
            player.sendSystemMessage(Component.literal("§a✓ Successfully enchanted item!"));
            updateDisplay();
        } else {
            player.sendSystemMessage(Component.literal("§cFailed to apply enchantment!"));
        }
    }
}
