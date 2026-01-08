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
            .addLoreLine(Component.literal("§8Command: §f/shop"))
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
            .addLoreLine(Component.literal("§8Command: §f/anvil"))
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
            .addLoreLine(Component.literal("§7Multiple account types"))
            .addLoreLine(Component.literal("§7Credit cards & history"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/bank"))
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
            .addLoreLine(Component.literal("§8Command: §f/enchant"))
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
            .addLoreLine(Component.literal("§8Command: §f/property"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new PropertyGui(player).open();
            })
        );
        
        setSlot(20, new GuiElementBuilder(Items.VILLAGER_SPAWN_EGG)
            .setName(Component.literal("§d§lTenant Management"))
            .addLoreLine(Component.literal("§7Manage property tenants"))
            .addLoreLine(Component.literal("§7Adjust rent prices"))
            .addLoreLine(Component.literal("§7View relationships"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/tenant"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new TenantGui(player).open();
            })
        );
        
        setSlot(21, new GuiElementBuilder(Items.IRON_SHOVEL)
            .setName(Component.literal("§6§l⚒ Workers"))
            .addLoreLine(Component.literal("§7Hire workers for your business"))
            .addLoreLine(Component.literal("§7+25% farm efficiency"))
            .addLoreLine(Component.literal("§7-20% mine downtime"))
            .addLoreLine(Component.literal("§7-30% repair costs"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Train skills, manage loyalty"))
            .addLoreLine(Component.literal("§7Max 10 workers per player"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/workers"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new com.shopmod.worker.WorkerGui(player).open();
            })
        );
        
        setSlot(22, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§b§lAuction House"))
            .addLoreLine(Component.literal("§740 daily items"))
            .addLoreLine(Component.literal("§7Bid against NPCs"))
            .addLoreLine(Component.literal("§7Find rare deals"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/auction"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new AuctionGui(player).open();
            })
        );
        
        setSlot(23, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lStock Options"))
            .addLoreLine(Component.literal("§7Trade derivatives"))
            .addLoreLine(Component.literal("§7Call/Put positions"))
            .addLoreLine(Component.literal("§710x profit multiplier"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/stocks"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new StocksGui(player).open();
            })
        );
        
        setSlot(24, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§l⚡ Stock Market"))
            .addLoreLine(Component.literal("§717 companies"))
            .addLoreLine(Component.literal("§7Trade shares"))
            .addLoreLine(Component.literal("§7Earn dividends"))
            .addLoreLine(Component.literal("§7Build portfolio"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/stockmarket"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new StockMarketGui(player).open();
            })
        );
        
        setSlot(25, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§5§lBlack Market"))
            .addLoreLine(Component.literal("§7Risky deals"))
            .addLoreLine(Component.literal("§740-70% discounts"))
            .addLoreLine(Component.literal("§c15% scam chance"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/blackmarket"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new BlackMarketGui(player).open();
            })
        );
        
        // Row 3: Upgrades & Systems
        setSlot(27, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§6§lFarms"))
            .addLoreLine(Component.literal("§7Buy automated farms"))
            .addLoreLine(Component.literal("§7Produce resources"))
            .addLoreLine(Component.literal("§7Upgrade production"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/farms"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new FarmGui(player).open();
            })
        );
        
        setSlot(28, new GuiElementBuilder(Items.STICK)
            .setName(Component.literal("§6§l⚡ Sell Wand"))
            .addLoreLine(Component.literal("§7Right-click chests to sell"))
            .addLoreLine(Component.literal("§7Upgrade for bonuses"))
            .addLoreLine(Component.literal("§7Level up system"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/wand"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new SellWandGui(player).open();
            })
        );
        
        setSlot(29, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("§a§lResearch"))
            .addLoreLine(Component.literal("§725 upgrades"))
            .addLoreLine(Component.literal("§7Unlock bonuses"))
            .addLoreLine(Component.literal("§7Technology tree"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/research"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new ResearchGui(player).open();
            })
        );
        
        setSlot(30, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
            .setName(Component.literal("§8§l⛏ Mining"))
            .addLoreLine(Component.literal("§7Automated mines"))
            .addLoreLine(Component.literal("§7Passive income"))
            .addLoreLine(Component.literal("§75 mine types"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/mining"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new MiningGui(player).open();
            })
        );
        
        setSlot(31, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§6§l⚙ Automation Hub"))
            .addLoreLine(Component.literal("§7Automate repetitive tasks"))
            .addLoreLine(Component.literal("§7Auto-pay loans"))
            .addLoreLine(Component.literal("§7Auto-collect farms"))
            .addLoreLine(Component.literal("§7Auto-deposit wallet"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lFREE TO USE!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/automation"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new com.shopmod.automation.AutomationGui(player).open();
            })
        );
        
        setSlot(32, new GuiElementBuilder(Items.SHIELD)
            .setName(Component.literal("§9§l🛡 Insurance"))
            .addLoreLine(Component.literal("§7Protect your investments"))
            .addLoreLine(Component.literal("§7Property, Farm, Mine coverage"))
            .addLoreLine(Component.literal("§7File claims for damages"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Monthly premiums"))
            .addLoreLine(Component.literal("§7Up to $250K coverage"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/insurance"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new com.shopmod.insurance.InsuranceGui(player).open();
            })
        );
        
        // Row 4: Utilities
        setSlot(37, new GuiElementBuilder(Items.NAME_TAG)
            .setName(Component.literal("§d§l🐾 Pets"))
            .addLoreLine(Component.literal("§7Collect pets"))
            .addLoreLine(Component.literal("§7Passive bonuses"))
            .addLoreLine(Component.literal("§710 unique pets"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/pets"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new PetsGui(player).open();
            })
        );
        
        setSlot(38, new GuiElementBuilder(Items.ENDER_PEARL)
            .setName(Component.literal("§5§l🌟 Teleport"))
            .addLoreLine(Component.literal("§7Set waypoints"))
            .addLoreLine(Component.literal("§7Fast travel"))
            .addLoreLine(Component.literal("§7FREE teleportation!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/teleport"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new TeleportGui(player).open();
            })
        );
        
        setSlot(39, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§6§lLoans"))
            .addLoreLine(Component.literal("§7Borrow money"))
            .addLoreLine(Component.literal("§715% interest"))
            .addLoreLine(Component.literal("§77-day terms"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/loan"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new LoanGui(player).open();
            })
        );
        
        setSlot(40, new GuiElementBuilder(Items.BELL)
            .setName(Component.literal("§a§l🏘️ Village"))
            .addLoreLine(Component.literal("§7Hire workers"))
            .addLoreLine(Component.literal("§7Produce resources"))
            .addLoreLine(Component.literal("§7Build structures"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/village"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new VillageGui(player).open();
            })
        );
        
        setSlot(41, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§e§l🎮 Games"))
            .addLoreLine(Component.literal("§7Interactive mini-games!"))
            .addLoreLine(Component.literal("§7Real gameplay mechanics"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§l7 Games Available:"))
            .addLoreLine(Component.literal("§7• Number Guess"))
            .addLoreLine(Component.literal("§7• Coin Flip"))
            .addLoreLine(Component.literal("§7• Dice Roll"))
            .addLoreLine(Component.literal("§7• High-Low"))
            .addLoreLine(Component.literal("§7• Lucky Slots"))
            .addLoreLine(Component.literal("§d§l• Blackjack ★ NEW!"))
            .addLoreLine(Component.literal("§d§l• Roulette ★ NEW!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6Max Win: $50,000!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Command: §f/game"))
            .addLoreLine(Component.literal("§e§lCLICK §7to open"))
            .setCallback((index, type, action) -> {
                new GamesGui(player).open();
            })
        );
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
}
