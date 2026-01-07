package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.research.ResearchManager;
import com.shopmod.research.ResearchType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.Set;

/**
 * Research/Upgrade GUI
 */
public class ResearchGui extends SimpleGui {
    private final ServerPlayer player;
    private ResearchType.ResearchCategory currentCategory;
    
    public ResearchGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.currentCategory = ResearchType.ResearchCategory.ECONOMIC;
        this.setTitle(Component.literal("§5§lResearch & Upgrades"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        // Player info
        setupInfoBar();
        
        // Category tabs
        setupCategoryTabs();
        
        // Display research for current category
        displayResearch();
    }
    
    private void setupInfoBar() {
        // Balance
        setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
        
        // Research count
        Set<ResearchType> owned = ResearchManager.getPlayerResearch(player.getUUID());
        setSlot(13, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
            .setName(Component.literal("§b§lResearch Progress"))
            .addLoreLine(Component.literal("§7Unlocked: §e" + owned.size() + " §7/ §e" + ResearchType.values().length))
        );
        
        // Hub button
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .addLoreLine(Component.literal("§7"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void setupCategoryTabs() {
        int slot = 18;
        for (ResearchType.ResearchCategory category : ResearchType.ResearchCategory.values()) {
            boolean selected = category == currentCategory;
            
            GuiElementBuilder builder = new GuiElementBuilder(
                selected ? Items.ENCHANTED_BOOK : Items.BOOK
            )
                .setName(Component.literal(category.getDisplayName() + " §lResearch"))
                .addLoreLine(Component.literal(category.getDescription()))
                .setCallback((index, type, action) -> {
                    currentCategory = category;
                    updateDisplay();
                });
            
            if (selected) {
                builder.glow();
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayResearch() {
        Set<ResearchType> owned = ResearchManager.getPlayerResearch(player.getUUID());
        
        int slot = 27;
        for (ResearchType research : ResearchType.values()) {
            if (research.getCategory() != currentCategory) continue;
            
            boolean hasIt = owned.contains(research);
            boolean canAfford = CurrencyManager.getBalance(player) >= research.getCost();
            
            // Check prerequisites
            boolean hasPrereqs = true;
            if (research.getTier() > 0) {
                for (ResearchType other : ResearchType.values()) {
                    if (other.getCategory() == research.getCategory() && 
                        other.getTier() < research.getTier() && 
                        !owned.contains(other)) {
                        hasPrereqs = false;
                        break;
                    }
                }
            }
            
            GuiElementBuilder builder = new GuiElementBuilder(
                hasIt ? Items.WRITABLE_BOOK : research.getIcon()
            )
                .setName(Component.literal((hasIt ? "§a§l✔ " : "§e§l") + research.getDisplayName()))
                .addLoreLine(Component.literal(research.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Tier: §e" + (research.getTier() + 1)))
                .addLoreLine(Component.literal("§7Cost: " + 
                    (canAfford ? "§a" : "§c") + CurrencyManager.format(research.getCost())));
            
            if (hasIt) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lUNLOCKED!"))
                    .glow();
            } else if (!hasPrereqs) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lLOCKED"))
                    .addLoreLine(Component.literal("§cResearch lower tiers first"));
            } else if (canAfford) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lCLICK to research"))
                    .setCallback((index, type, action) -> {
                        ResearchManager.purchaseResearch(player, research);
                        updateDisplay();
                    });
            } else {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lNOT ENOUGH MONEY"));
            }
            
            setSlot(slot, builder);
            slot++;
            if (slot >= 54) break;
        }
    }
}
