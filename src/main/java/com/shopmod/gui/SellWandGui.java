package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.wand.SellWandManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

/**
 * Sell Wand upgrade GUI
 */
public class SellWandGui extends SimpleGui {
    private final ServerPlayer player;
    private final SellWandManager.WandData data;
    
    public SellWandGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        this.player = player;
        this.data = SellWandManager.getWandData(player.getUUID());
        this.setTitle(Component.literal("§6§l⚡ Sell Wand"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        // Wand info
        setSlot(10, new GuiElementBuilder(Items.STICK)
            .setName(Component.literal("§6§l⚡ Sell Wand"))
            .addLoreLine(Component.literal("§7Right-click chests to auto-sell"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current Level: §e" + data.getLevel()))
            .addLoreLine(Component.literal("§7Sell Bonus: §a+" + (int)(data.getSellMultiplier() * 100 - 100) + "%"))
            .addLoreLine(Component.literal("§7Max Level: §e20"))
        );
        
        // Statistics
        setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lStatistics"))
            .addLoreLine(Component.literal("§7Total Sold: §6$" + CurrencyManager.format(data.getTotalSold())))
            .addLoreLine(Component.literal("§7Items Sold: §e" + data.getItemsSold()))
        );
        
        // Upgrade info
        if (data.getLevel() < 20) {
            boolean canUpgrade = CurrencyManager.canAfford(player, data.getUpgradeCost()) &&
                                data.getTotalSold() >= data.getRequiredSales();
            
            setSlot(14, new GuiElementBuilder(canUpgrade ? Items.LIME_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE)
                .setName(Component.literal(canUpgrade ? "§a§lUPGRADE AVAILABLE" : "§c§lUPGRADE LOCKED"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Next Level: §e" + (data.getLevel() + 1)))
                .addLoreLine(Component.literal("§7Bonus: §a+" + (int)((1.0 + ((data.getLevel() + 1) * 0.05)) * 100 - 100) + "%"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Requirements:"))
                .addLoreLine(Component.literal("  §7Cost: " + 
                    (CurrencyManager.canAfford(player, data.getUpgradeCost()) ? "§a✓ " : "§c✗ ") +
                    "§6" + CurrencyManager.format(data.getUpgradeCost())))
                .addLoreLine(Component.literal("  §7Sales: " +
                    (data.getTotalSold() >= data.getRequiredSales() ? "§a✓ " : "§c✗ ") +
                    "§6$" + CurrencyManager.format(data.getRequiredSales())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(canUpgrade ? "§a§lCLICK TO UPGRADE" : "§c§lCannot upgrade yet"))
                .setCallback((index, type, action) -> {
                    if (canUpgrade) {
                        if (SellWandManager.upgradeWand(player)) {
                            updateDisplay();
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§c§l[WAND] Requirements not met!"));
                    }
                })
            );
        } else {
            setSlot(14, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("§d§l⭐ MAX LEVEL ⭐"))
                .addLoreLine(Component.literal("§7Your wand is fully upgraded!"))
                .addLoreLine(Component.literal("§7Sell Bonus: §a+100%"))
            );
        }
        
        // Get wand button
        setSlot(16, new GuiElementBuilder(Items.STICK)
            .setName(Component.literal("§e§lGet Sell Wand"))
            .addLoreLine(Component.literal("§7Click to receive your wand"))
            .addLoreLine(Component.literal("§7(Replaces old wand)"))
            .setCallback((index, type, action) -> {
                net.minecraft.world.item.ItemStack wand = SellWandManager.createSellWand(player);
                if (!player.getInventory().add(wand)) {
                    player.drop(wand, false);
                }
                player.sendSystemMessage(Component.literal("§a§l[WAND] Received Sell Wand!"));
            })
        );
        
        // Hub button
        setSlot(26, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void updateDisplay() {
        setupDisplay();
    }
}
