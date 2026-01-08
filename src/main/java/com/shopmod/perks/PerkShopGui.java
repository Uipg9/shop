package com.shopmod.perks;

import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.Map;

/**
 * Perk Shop GUI
 */
public class PerkShopGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode viewMode = ViewMode.BOOSTERS;
    
    private enum ViewMode {
        BOOSTERS, PERKS, ACTIVE
    }
    
    public PerkShopGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§d§l⭐ Perk Shop"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            } else {
                setSlot(i, new GuiElementBuilder(Items.AIR));
            }
        }
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§lPerk Shop"))
            .addLoreLine(Component.literal("§7Balance: §6" + CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
        
        // View mode buttons
        setupViewModeButtons();
        
        // Display content based on view mode
        switch (viewMode) {
            case BOOSTERS -> displayBoosters();
            case PERKS -> displayPerks();
            case ACTIVE -> displayActive();
        }
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    private void setupViewModeButtons() {
        setSlot(11, new GuiElementBuilder(Items.POTION)
            .setName(Component.literal(viewMode == ViewMode.BOOSTERS ? "§a§l• BOOSTERS" : "§eBOOSTERS"))
            .addLoreLine(Component.literal("§7Temporary power-ups"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.BOOSTERS;
                setupDisplay();
            })
        );
        
        setSlot(13, new GuiElementBuilder(Items.ENCHANTED_BOOK)
            .setName(Component.literal(viewMode == ViewMode.PERKS ? "§a§l• PERKS" : "§dPERKS"))
            .addLoreLine(Component.literal("§7Permanent upgrades"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.PERKS;
                setupDisplay();
            })
        );
        
        setSlot(15, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal(viewMode == ViewMode.ACTIVE ? "§a§l• ACTIVE" : "§bACTIVE"))
            .addLoreLine(Component.literal("§7Your active boosters"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.ACTIVE;
                setupDisplay();
            })
        );
    }
    
    private void displayBoosters() {
        PerkManager.PlayerPerks perks = PerkManager.getPerks(player);
        
        int slot = 19;
        for (PerkManager.TemporaryBooster booster : PerkManager.TemporaryBooster.values()) {
            if (slot > 34) break;
            
            boolean active = perks.hasActiveBooster(booster);
            
            GuiElementBuilder builder = new GuiElementBuilder(Items.POTION)
                .setName(Component.literal((active ? "§a" : "§e") + "§l" + booster.getName()));
            
            builder.addLoreLine(Component.literal("§7" + booster.getDescription()));
            builder.addLoreLine(Component.literal("§7Duration: §a" + booster.getDurationMinutes() + " minutes"));
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§7Price: §6" + CurrencyManager.format(booster.getPrice())));
            
            if (active) {
                long remaining = perks.getBoosterTimeRemaining(booster);
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§a§lACTIVE"));
                builder.addLoreLine(Component.literal("§7Time left: §e" + formatTime(remaining)));
                builder.glow();
            } else {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§e§lCLICK §7to purchase"));
                
                builder.setCallback((index, type, action) -> {
                    if (PerkManager.purchaseBooster(player, booster)) {
                        setupDisplay();
                    }
                });
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayPerks() {
        PerkManager.PlayerPerks perks = PerkManager.getPerks(player);
        
        int slot = 19;
        for (PerkManager.PermanentPerk perk : PerkManager.PermanentPerk.values()) {
            if (slot > 34) break;
            
            boolean owned = perks.hasPerk(perk);
            
            GuiElementBuilder builder = new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal((owned ? "§a✓ " : "§d") + "§l" + perk.getName()));
            
            builder.addLoreLine(Component.literal("§7" + perk.getDescription()));
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§7Price: §6" + CurrencyManager.format(perk.getPrice())));
            
            if (owned) {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§a§l✓ OWNED"));
                builder.glow();
            } else {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§e§lCLICK §7to purchase"));
                
                builder.setCallback((index, type, action) -> {
                    if (PerkManager.purchasePerk(player, perk)) {
                        setupDisplay();
                    }
                });
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayActive() {
        PerkManager.PlayerPerks perks = PerkManager.getPerks(player);
        Map<PerkManager.TemporaryBooster, Long> activeBoosters = perks.getActiveBoosters();
        
        if (activeBoosters.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Active Boosters"))
                .addLoreLine(Component.literal("§7Purchase boosters to see them here!"))
            );
        } else {
            int slot = 20;
            for (Map.Entry<PerkManager.TemporaryBooster, Long> entry : activeBoosters.entrySet()) {
                if (slot > 24) break;
                
                PerkManager.TemporaryBooster booster = entry.getKey();
                long remaining = perks.getBoosterTimeRemaining(booster);
                
                GuiElementBuilder builder = new GuiElementBuilder(Items.POTION)
                    .setName(Component.literal("§a§l" + booster.getName()))
                    .addLoreLine(Component.literal("§7" + booster.getDescription()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Time Remaining: §e" + formatTime(remaining)))
                    .glow();
                
                setSlot(slot, builder);
                slot++;
            }
        }
        
        // Display owned permanent perks
        setSlot(31, new GuiElementBuilder(Items.ENCHANTED_BOOK)
            .setName(Component.literal("§d§lPermanent Perks"))
            .addLoreLine(Component.literal("§7Your owned perks:"))
            .addLoreLine(Component.literal(""))
        );
        
        int perkSlot = 37;
        for (PerkManager.PermanentPerk perk : perks.getOwnedPerks()) {
            if (perkSlot > 43) break;
            
            GuiElementBuilder builder = new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("§d§l" + perk.getName()))
                .addLoreLine(Component.literal("§7" + perk.getDescription()))
                .glow();
            
            setSlot(perkSlot, builder);
            perkSlot++;
        }
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes < 60) return minutes + "m " + secs + "s";
        long hours = minutes / 60;
        long mins = minutes % 60;
        return hours + "h " + mins + "m";
    }
}
