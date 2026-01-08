package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.property.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Property/Real Estate GUI - Buy properties for passive income
 */
public class PropertyGui extends SimpleGui {
    private final ServerPlayer player;
    private PropertyType.PropertyCategory currentCategory = PropertyType.PropertyCategory.LAND;
    
    public PropertyGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§lProperty Investment"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        PropertyManager.PlayerProperties props = PropertyManager.getPlayerProperties(player.getUUID());
        
        // Top info bar
        setupInfoBar(props);
        
        // Category tabs
        setupCategoryTabs();
        
        // Property listings
        displayProperties(props);
    }
    
    private void setupInfoBar(PropertyManager.PlayerProperties props) {
        // Property level
        GuiElementBuilder levelBuilder = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("§e§lProperty Level " + props.getPropertyLevel()))
            .addLoreLine(Component.literal("§7Unlocks higher tier properties"));
        
        // Add upgrade cost if not max level
        if (props.getPropertyLevel() < 5) {
            long[] costs = {50000L, 200000L, 500000L, 1000000L, 5000000L};
            long upgradeCost = costs[props.getPropertyLevel() - 1];
            boolean canAfford = CurrencyManager.canAfford(player, upgradeCost);
            
            levelBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Upgrade Cost: " + 
                    (canAfford ? "§a" : "§c") + "$" + CurrencyManager.format(upgradeCost)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(canAfford ? "§a§lCLICK §7to upgrade!" : "§c§lInsufficient funds!"));
        } else {
            levelBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§d§l⭐ MAX LEVEL ⭐"));
        }
        
        levelBuilder.setCallback((index, type, action) -> {
            PropertyManager.upgradePropertyLevel(player);
            updateDisplay();
        });
        
        setSlot(2, levelBuilder);
        
        // Total properties
        setSlot(4, new GuiElementBuilder(Items.MAP)
            .setName(Component.literal("§b§lTotal Properties: " + props.getTotalProperties()))
            .addLoreLine(Component.literal("§7Owned across all categories"))
        );
        
        // Daily income
        setSlot(6, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lDaily Income"))
            .addLoreLine(Component.literal(CurrencyManager.format(props.getTotalDailyIncome()) + "/day"))
            .addLoreLine(Component.literal("§7Passive income from properties"))
        );
        
        // Player balance
        setSlot(8, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lCurrent Balance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    private void setupCategoryTabs() {
        // Land tab
        setSlot(45, new GuiElementBuilder(
                currentCategory == PropertyType.PropertyCategory.LAND ? Items.GRASS_BLOCK : Items.DIRT)
            .setName(Component.literal("§2§lLand Plots"))
            .addLoreLine(Component.literal("§7Empty plots for development"))
            .setCallback((index, type, action) -> {
                currentCategory = PropertyType.PropertyCategory.LAND;
                updateDisplay();
            })
        );
        
        // Buildings tab
        setSlot(46, new GuiElementBuilder(
                currentCategory == PropertyType.PropertyCategory.BUILDING ? Items.BRICK : Items.BRICKS)
            .setName(Component.literal("§e§lBuildings"))
            .addLoreLine(Component.literal("§7Commercial properties"))
            .setCallback((index, type, action) -> {
                currentCategory = PropertyType.PropertyCategory.BUILDING;
                updateDisplay();
            })
        );
        
        // Settlements tab
        setSlot(47, new GuiElementBuilder(
                currentCategory == PropertyType.PropertyCategory.SETTLEMENT ? Items.BELL : Items.OAK_DOOR)
            .setName(Component.literal("§b§lSettlements"))
            .addLoreLine(Component.literal("§7Villages, towns, cities"))
            .setCallback((index, type, action) -> {
                currentCategory = PropertyType.PropertyCategory.SETTLEMENT;
                updateDisplay();
            })
        );
        
        // Hub button
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void displayProperties(PropertyManager.PlayerProperties props) {
        int slot = 18;
        
        for (PropertyType propertyType : PropertyType.values()) {
            if (propertyType.getCategory() != currentCategory) continue;
            
            PropertyManager.PropertyData data = props.getProperties().get(propertyType);
            int owned = data != null ? data.getQuantity() : 0;
            boolean canAfford = CurrencyManager.canAfford(player, propertyType.getPurchaseCost());
            boolean levelUnlocked = propertyType.getRequiredLevel() <= props.getPropertyLevel();
            
            GuiElementBuilder builder = new GuiElementBuilder(propertyType.getIcon())
                .setName(Component.literal((owned > 0 ? "§a" : canAfford && levelUnlocked ? "§e" : "§c") + 
                    "§l" + propertyType.getDisplayName()))
                .addLoreLine(Component.literal(propertyType.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Purchase: §6" + 
                    CurrencyManager.format(propertyType.getPurchaseCost())))
                .addLoreLine(Component.literal("§7Daily Income: §a+" +
                    CurrencyManager.format(propertyType.getDailyIncome()) + "/day"))
                .addLoreLine(Component.literal("§7Required Level: §b" + propertyType.getRequiredLevel()));
                
            if (owned > 0) {
                // Show rental status
                if (data.isRented()) {
                    builder.addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§d§lRENTED to: " + data.getRenterName()))
                        .addLoreLine(Component.literal("§7Rent income: §6+50% §7(§a" + 
                            CurrencyManager.format((long)(propertyType.getDailyIncome() * 1.5 * owned)) + "/day§7)"))
                        .addLoreLine(Component.literal("§7Repair in: §e" + data.getDaysUntilRepair() + " days"));
                }
                
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lOWNED: " + owned))
                    .addLoreLine(Component.literal("§7Total income: §6+" +
                        CurrencyManager.format(propertyType.getDailyIncome() * owned) + "/day"))
                    .addLoreLine(Component.literal("§7Total earned: §6" +
                        CurrencyManager.format(data.getTotalEarned())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lLEFT CLICK §7buy 1 more"))
                    .addLoreLine(Component.literal("§e§lSHIFT+LEFT §7buy 10 more"))
                    .addLoreLine(Component.literal("§c§lRIGHT CLICK §7sell 1"))
                    .addLoreLine(Component.literal(data.isRented() ? 
                        "§4§lMIDDLE CLICK §7evict renter" : 
                        "§d§lMIDDLE CLICK §7rent out (+50%)"))
                    .setCallback((index, type, action) -> {
                        if (type.isLeft && type.shift) {
                            PropertyManager.purchaseProperty(player, propertyType, 10);
                        } else if (type.isLeft) {
                            PropertyManager.purchaseProperty(player, propertyType, 1);
                        } else if (type.isRight) {
                            PropertyManager.sellProperty(player, propertyType, 1);
                        } else if (type == eu.pb4.sgui.api.ClickType.MOUSE_MIDDLE) {
                            if (data.isRented()) {
                                PropertyManager.evictRenter(player, propertyType);
                            } else {
                                PropertyManager.rentOutProperty(player, propertyType);
                            }
                            updateDisplay();
                            return; // Prevent propagation
                        }
                        updateDisplay();
                    });
            } else if (levelUnlocked && canAfford) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lLEFT CLICK §7buy 1"))
                    .addLoreLine(Component.literal("§e§lSHIFT+LEFT §7buy 10"))
                    .setCallback((index, type, action) -> {
                        if (type.shift) {
                            PropertyManager.purchaseProperty(player, propertyType, 10);
                        } else {
                            PropertyManager.purchaseProperty(player, propertyType, 1);
                        }
                        updateDisplay();
                    });
            } else if (!levelUnlocked) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lLOCKED"))
                    .addLoreLine(Component.literal("§7Upgrade property level!"));
            } else {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lCANNOT AFFORD"));
            }
            
            if (owned > 0) {
                builder.glow();
            }
            
            setSlot(slot, builder);
            slot++;
            if (slot >= 45) break; // Prevent overflow
        }
    }
}
