package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

/**
 * Central Hub GUI - Access all shop features from one place
 */
public class HubGui extends SimpleGui {
    private final ServerPlayer player;
    
    public HubGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§l✦ Shop Hub ✦"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l" + player.getName().getString()))
            .addLoreLine(Component.literal("§7Balance: §6" + CurrencyManager.format(CurrencyManager.getBalance(player))))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Welcome to the Shop Hub!"))
            .addLoreLine(Component.literal("§7Click any icon to access that feature."))
        );
        
        // Row 1: Core Shop Features
        setSlot(10, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lMain Shop"))
            .addLoreLine(Component.literal("§7Buy and sell items"))
            .addLoreLine(Component.literal("§7Browse categories"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new ShopGui(player).open();
            })
        );
        
        setSlot(11, new GuiElementBuilder(Items.ANVIL)
            .setName(Component.literal("§7§lAnvil Shop"))
            .addLoreLine(Component.literal("§7Repair and rename items"))
            .addLoreLine(Component.literal("§7Combine enchantments"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                // Will open anvil GUI when available
                player.sendSystemMessage(Component.literal("§7Opening Anvil GUI..."));
                close();
            })
        );
        
        setSlot(12, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lBank"))
            .addLoreLine(Component.literal("§7Store items safely"))
            .addLoreLine(Component.literal("§7Deposit and withdraw"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new BankGui(player).open();
            })
        );
        
        setSlot(13, new GuiElementBuilder(Items.ENCHANTED_BOOK)
            .setName(Component.literal("§d§lEnchantments"))
            .addLoreLine(Component.literal("§7Buy enchantments"))
            .addLoreLine(Component.literal("§7Enchant your gear"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new EnchantingGui(player).open();
            })
        );
        
        // Row 2: Property & Investment
        setSlot(19, new GuiElementBuilder(Items.GRASS_BLOCK)
            .setName(Component.literal("§2§lReal Estate"))
            .addLoreLine(Component.literal("§7Buy properties"))
            .addLoreLine(Component.literal("§7Earn passive income"))
            .addLoreLine(Component.literal("§7Rent to villagers"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new PropertyGui(player).open();
            })
        );
        
        setSlot(20, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§b§lAuction House"))
            .addLoreLine(Component.literal("§740 daily items"))
            .addLoreLine(Component.literal("§7Bid against NPCs"))
            .addLoreLine(Component.literal("§7Find rare deals"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new AuctionGui(player).open();
            })
        );
        
        setSlot(21, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lStock Options"))
            .addLoreLine(Component.literal("§7Trade derivatives"))
            .addLoreLine(Component.literal("§7Call/Put positions"))
            .addLoreLine(Component.literal("§710x profit multiplier"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new StocksGui(player).open();
            })
        );
        
        setSlot(22, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§5§lBlack Market"))
            .addLoreLine(Component.literal("§7Risky deals"))
            .addLoreLine(Component.literal("§740-70% discounts"))
            .addLoreLine(Component.literal("§c15% scam chance"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new BlackMarketGui(player).open();
            })
        );
        
        // Row 3: Upgrades & Systems
        setSlot(28, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§6§lFarms"))
            .addLoreLine(Component.literal("§7Buy automated farms"))
            .addLoreLine(Component.literal("§7Produce resources"))
            .addLoreLine(Component.literal("§7Upgrade production"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new FarmGui(player).open();
            })
        );
        
        setSlot(29, new GuiElementBuilder(Items.OAK_SIGN)
            .setName(Component.literal("§3§lVillage"))
            .addLoreLine(Component.literal("§7Hire villagers"))
            .addLoreLine(Component.literal("§7Automate tasks"))
            .addLoreLine(Component.literal("§7Build operations"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lComing Soon!"))
        );
        
        setSlot(30, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("§a§lResearch"))
            .addLoreLine(Component.literal("§725 upgrades"))
            .addLoreLine(Component.literal("§7Unlock bonuses"))
            .addLoreLine(Component.literal("§7Technology tree"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new ResearchGui(player).open();
            })
        );
        
        setSlot(31, new GuiElementBuilder(Items.CHEST)
            .setName(Component.literal("§e§lLucky Crates"))
            .addLoreLine(Component.literal("§7Open daily crates"))
            .addLoreLine(Component.literal("§7Win random rewards"))
            .addLoreLine(Component.literal("§7Tier progression"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lComing Soon!"))
        );
        
        // Row 4: Utilities
        setSlot(37, new GuiElementBuilder(Items.ENDER_PEARL)
            .setName(Component.literal("§5§lTeleport"))
            .addLoreLine(Component.literal("§7Set waypoints"))
            .addLoreLine(Component.literal("§7Fast travel"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lComing Soon!"))
        );
        
        setSlot(38, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
            .setName(Component.literal("§b§lUpgrades"))
            .addLoreLine(Component.literal("§7Efficiency upgrades"))
            .addLoreLine(Component.literal("§7Fortune & Looting"))
            .addLoreLine(Component.literal("§7XP bonuses"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lComing Soon!"))
        );
        
        setSlot(39, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§6§lLoans"))
            .addLoreLine(Component.literal("§7Borrow money"))
            .addLoreLine(Component.literal("§715% interest"))
            .addLoreLine(Component.literal("§77-day terms"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lComing Soon!"))
        );
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
}
