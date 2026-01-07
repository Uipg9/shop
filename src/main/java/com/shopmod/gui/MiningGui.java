package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.mining.MiningManager;
import com.shopmod.mining.MiningManager.MineType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

/**
 * Mining Operations GUI
 */
public class MiningGui extends SimpleGui {
    private final ServerPlayer player;
    private final MiningManager.MiningData data;
    
    public MiningGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x4, player, false);
        this.player = player;
        this.data = MiningManager.getMiningData(player.getUUID());
        this.setTitle(Component.literal("§8§l⛏ Mining Operations"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
            .setName(Component.literal("§8§lMining Statistics"))
            .addLoreLine(Component.literal("§7Daily Income: §6$" + CurrencyManager.format(data.calculateDailyIncome())))
            .addLoreLine(Component.literal("§7Total Earned: §6$" + CurrencyManager.format(data.getTotalEarned())))
        );
        
        // Coal Mine
        addMineButton(10, MineType.COAL_MINE, Items.COAL);
        
        // Iron Mine
        addMineButton(11, MineType.IRON_MINE, Items.IRON_INGOT);
        
        // Gold Mine
        addMineButton(12, MineType.GOLD_MINE, Items.GOLD_INGOT);
        
        // Diamond Mine
        addMineButton(13, MineType.DIAMOND_MINE, Items.DIAMOND);
        
        // Netherite Mine
        addMineButton(14, MineType.NETHERITE_MINE, Items.NETHERITE_INGOT);
        
        // Hub button
        setSlot(35, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void addMineButton(int slot, MineType mineType, net.minecraft.world.item.Item icon) {
        int level = data.getMineLevel(mineType);
        boolean owned = data.hasMine(mineType);
        long cost = mineType.getCost(level);
        long income = mineType.getIncome(level + 1);
        
        GuiElementBuilder builder = new GuiElementBuilder(icon)
            .setName(Component.literal((owned ? "§a" : "§7") + "§l" + mineType.getDisplayName()))
            .addLoreLine(Component.literal("§7Level: §e" + level));
        
        if (owned) {
            builder.addLoreLine(Component.literal("§7Income: §6$" + CurrencyManager.format(mineType.getIncome(level)) + "/day"))
                .addLoreLine(Component.literal(""));
        }
        
        builder.addLoreLine(Component.literal("§7Upgrade Cost: §6$" + CurrencyManager.format(cost)))
            .addLoreLine(Component.literal("§7Next Income: §6$" + CurrencyManager.format(income) + "/day"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(CurrencyManager.canAfford(player, cost) ? 
                "§a§lCLICK TO " + (owned ? "UPGRADE" : "PURCHASE") : 
                "§c§lCannot afford"))
            .setCallback((index, type, action) -> {
                if (MiningManager.purchaseMine(player, mineType)) {
                    updateDisplay();
                }
            });
        
        if (owned) {
            builder.glow();
        }
        
        setSlot(slot, builder);
    }
    
    private void updateDisplay() {
        setupDisplay();
    }
}
