package com.shopmod.gui;

import com.shopmod.blackmarket.BlackMarketManager;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.research.ResearchManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Black Market GUI - Risky deals
 */
public class BlackMarketGui extends SimpleGui {
    private final ServerPlayer player;
    
    public BlackMarketGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);
        this.player = player;
        this.setTitle(Component.literal("§4§lBLACK MARKET §8§l[RISKY]"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 27; i++) {
            this.clearSlot(i);
        }
        
        // Warning
        setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
            .setName(Component.literal("§4§l⚠ WARNING ⚠"))
            .addLoreLine(Component.literal("§c15% chance to get SCAMMED!"))
            .addLoreLine(Component.literal("§7You could lose your money"))
            .addLoreLine(Component.literal("§7and get nothing in return."))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7But the prices are..."))
            .addLoreLine(Component.literal("§a§lvery tempting"))
        );
        
        // Balance
        setSlot(8, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
        
        // Get deals
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        List<BlackMarketManager.BlackMarketDeal> deals = BlackMarketManager.getCurrentDeals(currentDay);
        
        int slot = 10;
        for (BlackMarketManager.BlackMarketDeal deal : deals) {
            boolean canAfford = CurrencyManager.getBalance(player) >= deal.getCost();
            
            GuiElementBuilder builder = new GuiElementBuilder(deal.getReward().getItem())
                .setCount(deal.getReward().getCount())
                .setName(Component.literal(deal.getName()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Cost: " + 
                    (canAfford ? "§6" : "§c") + CurrencyManager.format(deal.getCost())))
                .addLoreLine(Component.literal("§7Normal Price: §e~" + 
                    CurrencyManager.format((long)(deal.getCost() / (1.0 - deal.getDiscountPercent() / 100.0)))))
                .addLoreLine(Component.literal("§a§lYou save " + (int)deal.getDiscountPercent() + "%!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c⚠ 15% SCAM CHANCE ⚠"))
                .addLoreLine(Component.literal(""));
            
            if (canAfford) {
                builder.addLoreLine(Component.literal("§e§lCLICK to buy (RISKY!)"))
                    .setCallback((index, type, action) -> {
                        BlackMarketManager.purchase(player, deal);
                        updateDisplay();
                    });
            } else {
                builder.addLoreLine(Component.literal("§c§lNOT ENOUGH MONEY"));
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
}
