package com.shopmod.business;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.gui.HubGui;
import com.shopmod.business.BusinessManager.*;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.*;

/**
 * Business GUI - 9x6 interface
 * View modes: AVAILABLE, MY_BUSINESSES, MANAGE_BUSINESS
 */
public class BusinessGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode currentView = ViewMode.AVAILABLE;
    private Business selectedBusiness = null;
    
    private enum ViewMode {
        AVAILABLE, MY_BUSINESSES, MANAGE_BUSINESS
    }
    
    public BusinessGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§a§l✦ Business Empire ✦"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        // Background
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        // Tabs
        setupTabs();
        
        // Display based on current view
        switch (currentView) {
            case AVAILABLE:
                showAvailableBusinesses();
                break;
            case MY_BUSINESSES:
                showMyBusinesses();
                break;
            case MANAGE_BUSINESS:
                showManageBusiness();
                break;
        }
        
        // Navigation
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
        
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void setupTabs() {
        setSlot(45, new GuiElementBuilder(currentView == ViewMode.AVAILABLE ? Items.EMERALD_BLOCK : Items.EMERALD)
            .setName(Component.literal("§a§lAvailable Businesses"))
            .addLoreLine(Component.literal("§7View all business types"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.AVAILABLE;
                updateDisplay();
            })
        );
        
        setSlot(46, new GuiElementBuilder(currentView == ViewMode.MY_BUSINESSES ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
            .setName(Component.literal("§6§lMy Businesses"))
            .addLoreLine(Component.literal("§7View owned businesses"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.MY_BUSINESSES;
                selectedBusiness = null;
                updateDisplay();
            })
        );
    }
    
    private void showAvailableBusinesses() {
        this.setTitle(Component.literal("§a§l✦ Available Businesses ✦"));
        
        List<Business> owned = BusinessManager.getPlayerBusinesses(player.getUUID());
        Set<BusinessType> ownedTypes = new HashSet<>();
        for (Business b : owned) {
            ownedTypes.add(b.getType());
        }
        
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        
        for (BusinessType type : BusinessType.values()) {
            if (index >= slots.length) break;
            
            boolean isOwned = ownedTypes.contains(type);
            
            GuiElementBuilder builder = new GuiElementBuilder(isOwned ? Items.GRAY_DYE : Items.EMERALD)
                .setName(Component.literal((isOwned ? "§7" : "§a") + "§l" + type.getDisplayName()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(type.getPurchaseCost())))
                .addLoreLine(Component.literal("§7Daily Income: §a+" + CurrencyManager.format(type.getBaseDailyIncome())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Level 1 → 5: +50% income per level"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(isOwned ? "§c§lALREADY OWNED" : "§e§lCLICK to purchase"));
            
            if (!isOwned) {
                builder.setCallback((idx, tp, act) -> {
                    if (BusinessManager.buyBusiness(player, type)) {
                        updateDisplay();
                    }
                });
            }
            
            setSlot(slots[index], builder);
            index++;
        }
        
        // Synergy info
        int synergyBonus = BusinessManager.getSynergyBonusPercent(player.getUUID());
        setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§lSynergy Bonuses"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§73+ businesses: §a+20% income"))
            .addLoreLine(Component.literal("§75+ businesses: §a+50% income"))
            .addLoreLine(Component.literal("§77 businesses: §a+100% income"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current Bonus: §a+" + synergyBonus + "%"))
            .glow()
        );
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l" + player.getName().getString()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Balance: §6" + CurrencyManager.format(CurrencyManager.getBalance(player))))
            .addLoreLine(Component.literal("§7Businesses Owned: §e" + owned.size() + "/7"))
            .addLoreLine(Component.literal("§7Daily Income: §a+" + CurrencyManager.format(BusinessManager.getDailyIncome(player.getUUID()))))
        );
    }
    
    private void showMyBusinesses() {
        this.setTitle(Component.literal("§6§l✦ My Businesses ✦"));
        
        List<Business> businesses = BusinessManager.getPlayerBusinesses(player.getUUID());
        
        if (businesses.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§7§lNo Businesses"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Purchase businesses to earn"))
                .addLoreLine(Component.literal("§7passive daily income!"))
            );
        } else {
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
            long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
            
            for (int i = 0; i < Math.min(businesses.size(), slots.length); i++) {
                Business business = businesses.get(i);
                long uncollected = business.getUncollectedIncome(currentDay);
                
                GuiElementBuilder builder = new GuiElementBuilder(Items.EMERALD_BLOCK)
                    .setName(Component.literal("§a§l" + business.getType().getDisplayName()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Level: §e" + business.getLevel() + "/5"))
                    .addLoreLine(Component.literal("§7Daily Income: §a+" + CurrencyManager.format(business.getType().getDailyIncome(business.getLevel()))))
                    .addLoreLine(Component.literal("§7Total Earned: §6" + CurrencyManager.format(business.getTotalEarned())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Uncollected: §e" + CurrencyManager.format(uncollected)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§e§lCLICK to manage"))
                    .setCallback((index, type, action) -> {
                        selectedBusiness = business;
                        currentView = ViewMode.MANAGE_BUSINESS;
                        updateDisplay();
                    });
                
                if (business.getLevel() >= 5) {
                    builder.glow();
                }
                
                setSlot(slots[i], builder);
            }
            
            // Collect all button
            setSlot(40, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("§6§l§nCOLLECT ALL"))
                .addLoreLine(Component.literal("§7Collect income from all businesses"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK to collect"))
                .glow()
                .setCallback((index, type, action) -> {
                    long total = 0;
                    for (Business b : businesses) {
                        BusinessManager.collectIncome(player, b.getBusinessId());
                    }
                    updateDisplay();
                })
            );
        }
    }
    
    private void showManageBusiness() {
        if (selectedBusiness == null) {
            currentView = ViewMode.MY_BUSINESSES;
            updateDisplay();
            return;
        }
        
        this.setTitle(Component.literal("§6§l✦ " + selectedBusiness.getType().getDisplayName() + " ✦"));
        
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        long uncollected = selectedBusiness.getUncollectedIncome(currentDay);
        
        // Business info
        setSlot(13, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§a§l" + selectedBusiness.getType().getDisplayName()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Level: §e" + selectedBusiness.getLevel() + "/5"))
            .addLoreLine(Component.literal("§7Daily Income: §a+" + CurrencyManager.format(selectedBusiness.getType().getDailyIncome(selectedBusiness.getLevel()))))
            .addLoreLine(Component.literal("§7Total Earned: §6" + CurrencyManager.format(selectedBusiness.getTotalEarned())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Value: §6" + CurrencyManager.format(selectedBusiness.getCurrentValue())))
            .glow()
        );
        
        // Collect button
        setSlot(20, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§l§nCOLLECT INCOME"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Available: §e" + CurrencyManager.format(uncollected)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(uncollected > 0 ? "§e§lCLICK to collect" : "§7Nothing to collect yet"))
            .setCallback((index, type, action) -> {
                if (BusinessManager.collectIncome(player, selectedBusiness.getBusinessId())) {
                    updateDisplay();
                }
            })
        );
        
        // Upgrade button
        if (selectedBusiness.getLevel() < 5) {
            long upgradeCost = selectedBusiness.getType().getUpgradeCost(selectedBusiness.getLevel());
            long newIncome = selectedBusiness.getType().getDailyIncome(selectedBusiness.getLevel() + 1);
            
            setSlot(22, new GuiElementBuilder(Items.DIAMOND)
                .setName(Component.literal("§b§l§nUPGRADE"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Next Level: §e" + (selectedBusiness.getLevel() + 1) + "/5"))
                .addLoreLine(Component.literal("§7New Income: §a+" + CurrencyManager.format(newIncome)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(upgradeCost)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK to upgrade"))
                .glow()
                .setCallback((index, type, action) -> {
                    if (BusinessManager.upgradeBusiness(player, selectedBusiness.getBusinessId())) {
                        updateDisplay();
                    }
                })
            );
        } else {
            setSlot(22, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Component.literal("§b§l§nMAX LEVEL"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a§lFully Upgraded!"))
                .glow()
            );
        }
        
        // Sell button
        setSlot(24, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§l§nSELL BUSINESS"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Sale Value: §6" + CurrencyManager.format(selectedBusiness.getCurrentValue())))
            .addLoreLine(Component.literal("§7(60% of investment)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCLICK to sell"))
            .setCallback((index, type, action) -> {
                if (BusinessManager.sellBusiness(player, selectedBusiness.getBusinessId())) {
                    selectedBusiness = null;
                    currentView = ViewMode.MY_BUSINESSES;
                    updateDisplay();
                }
            })
        );
        
        // Back button
        setSlot(48, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§l← Back to My Businesses"))
            .setCallback((index, type, action) -> {
                selectedBusiness = null;
                currentView = ViewMode.MY_BUSINESSES;
                updateDisplay();
            })
        );
    }
}
