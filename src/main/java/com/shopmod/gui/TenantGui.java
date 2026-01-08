package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.tenant.TenantManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Tenant Management GUI - View and manage property tenants
 */
public class TenantGui extends SimpleGui {
    private final ServerPlayer player;
    
    public TenantGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§d§l🏠 Tenant Management"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        TenantManager.TenantData data = TenantManager.getTenantData(player.getUUID());
        
        // Header
        setupHeader(data);
        
        // Display tenants
        displayTenants(data);
        
        // Hub button
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void setupHeader(TenantManager.TenantData data) {
        // Total tenants
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§lTotal Tenants: " + data.getTenants().size()))
            .addLoreLine(Component.literal("§7Properties rented out"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Manage relationships"))
            .addLoreLine(Component.literal("§7Adjust rent amounts"))
            .addLoreLine(Component.literal("§7Evict problematic tenants"))
        );
        
        // Total daily rent income
        long totalRent = data.getTenants().values().stream()
            .mapToLong(TenantManager.TenantInfo::getRentAmount)
            .sum();
        
        setSlot(6, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lDaily Rent Income"))
            .addLoreLine(Component.literal(CurrencyManager.format(totalRent) + "/day"))
            .addLoreLine(Component.literal("§7From all rented properties"))
        );
        
        // Player balance
        setSlot(8, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lYour Balance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    private void displayTenants(TenantManager.TenantData data) {
        int slot = 18;
        
        for (TenantManager.TenantInfo tenant : data.getTenants().values()) {
            GuiElementBuilder builder = new GuiElementBuilder(tenant.getPropertyType().getIcon())
                .setName(Component.literal("§d§l" + tenant.getName()))
                .addLoreLine(Component.literal("§7Property: §e" + tenant.getPropertyType().getDisplayName()))
                .addLoreLine(Component.literal("§7Rent: §6" + CurrencyManager.format(tenant.getRentAmount()) + "/day"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(getRelationshipDisplay(tenant.getRelationshipScore())))
                .addLoreLine(Component.literal("§7Days Rented: §e" + tenant.getDaysRented()))
                .addLoreLine(Component.literal("§7Total Paid: §6" + CurrencyManager.format(tenant.getTotalPaidRent())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a§lLEFT CLICK §7- Increase rent (+10%)"))
                .addLoreLine(Component.literal("§e§lRIGHT CLICK §7- Decrease rent (-10%)"))
                .addLoreLine(Component.literal("§c§lMIDDLE CLICK §7- Evict tenant"))
                .setCallback((index, type, action) -> {
                    if (type.isLeft) {
                        TenantManager.adjustRent(player, tenant.getPropertyType(), true);
                        updateDisplay();
                    } else if (type.isRight) {
                        TenantManager.adjustRent(player, tenant.getPropertyType(), false);
                        updateDisplay();
                    } else if (type == eu.pb4.sgui.api.ClickType.MOUSE_MIDDLE) {
                        TenantManager.evictTenant(player, tenant.getPropertyType());
                        updateDisplay();
                    }
                });
            
            // Glow if high relationship
            if (tenant.getRelationshipScore() >= 70) {
                builder.glow();
            }
            
            setSlot(slot, builder);
            slot++;
            if (slot >= 44) break; // Prevent overflow
        }
        
        // If no tenants, show message
        if (data.getTenants().isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§7§lNo Tenants"))
                .addLoreLine(Component.literal("§7Visit the §e/property §7GUI"))
                .addLoreLine(Component.literal("§7and §dmiddle-click §7properties"))
                .addLoreLine(Component.literal("§7to rent them out!"))
            );
        }
    }
    
    private String getRelationshipDisplay(int score) {
        if (score >= 80) {
            return "§a§l❤❤❤❤❤ §7Excellent §a(" + score + "/100)";
        } else if (score >= 60) {
            return "§2§l❤❤❤❤ §7Good §2(" + score + "/100)";
        } else if (score >= 40) {
            return "§e§l❤❤❤ §7Neutral §e(" + score + "/100)";
        } else if (score >= 20) {
            return "§6§l❤❤ §7Poor §6(" + score + "/100)";
        } else {
            return "§c§l❤ §7Terrible §c(" + score + "/100)";
        }
    }
}
