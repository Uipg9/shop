package com.shopmod.automation;

import com.shopmod.gui.HubGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.List;

/**
 * GUI for managing automation settings
 * 9x6 interface with toggles, settings, notifications, and statistics
 */
public class AutomationGui extends SimpleGui {
    private final ServerPlayer player;
    
    public AutomationGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§l⚙ Automation Hub ⚙"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        AutomationSettings settings = AutomationManager.getSettings(player.getUUID());
        AutomationManager.AutomationStats stats = AutomationManager.getStats(player.getUUID());
        
        // Title
        setSlot(4, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§6§lAutomation Hub"))
            .addLoreLine(Component.literal("§7Automate repetitive tasks"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(String.format("§e%d/%d §7automations enabled", 
                settings.countEnabled(), 5)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lFREE TO USE!"))
        );
        
        // === AUTOMATION TOGGLES ===
        
        // Auto-Pay Loans
        setSlot(10, new GuiElementBuilder(settings.autoPayLoans ? Items.LIME_WOOL : Items.RED_WOOL)
            .setName(Component.literal(settings.autoPayLoans ? "§a§l✓ Auto-Pay Loans" : "§c§l✗ Auto-Pay Loans"))
            .addLoreLine(Component.literal("§7Automatically pay loan installments"))
            .addLoreLine(Component.literal("§7from your wallet daily"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Status: " + (settings.autoPayLoans ? "§aENABLED" : "§cDISABLED")))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to toggle"))
            .setCallback((index, type, action) -> {
                settings.autoPayLoans = !settings.autoPayLoans;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Pay Loans: " + (settings.autoPayLoans ? "§aON" : "§cOFF")));
                setupDisplay();
            })
        );
        
        // Auto-Collect Farms
        setSlot(11, new GuiElementBuilder(settings.autoCollectFarms ? Items.LIME_WOOL : Items.RED_WOOL)
            .setName(Component.literal(settings.autoCollectFarms ? "§a§l✓ Auto-Collect Farms" : "§c§l✗ Auto-Collect Farms"))
            .addLoreLine(Component.literal("§7Automatically harvest all farms"))
            .addLoreLine(Component.literal("§7and collect resources"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Status: " + (settings.autoCollectFarms ? "§aENABLED" : "§cDISABLED")))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to toggle"))
            .setCallback((index, type, action) -> {
                settings.autoCollectFarms = !settings.autoCollectFarms;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Collect Farms: " + (settings.autoCollectFarms ? "§aON" : "§cOFF")));
                setupDisplay();
            })
        );
        
        // Auto-Deposit Wallet
        setSlot(12, new GuiElementBuilder(settings.autoDepositWallet ? Items.LIME_WOOL : Items.RED_WOOL)
            .setName(Component.literal(settings.autoDepositWallet ? "§a§l✓ Auto-Deposit Wallet" : "§c§l✗ Auto-Deposit Wallet"))
            .addLoreLine(Component.literal("§7Automatically move excess wallet"))
            .addLoreLine(Component.literal("§7money to bank at midnight"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(String.format("§7Threshold: §6$%,d", settings.depositThreshold)))
            .addLoreLine(Component.literal("§7Status: " + (settings.autoDepositWallet ? "§aENABLED" : "§cDISABLED")))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to toggle"))
            .setCallback((index, type, action) -> {
                settings.autoDepositWallet = !settings.autoDepositWallet;
                player.sendSystemMessage(Component.literal(
                    "§e§l[AUTOMATION] Auto-Deposit Wallet: " + (settings.autoDepositWallet ? "§aON" : "§cOFF")));
                setupDisplay();
            })
        );
        
        // Auto-Sell Harvests
        setSlot(13, new GuiElementBuilder(settings.autoSellHarvests ? Items.LIME_WOOL : Items.RED_WOOL)
            .setName(Component.literal(settings.autoSellHarvests ? "§a§l✓ Auto-Sell Harvests" : "§c§l✗ Auto-Sell Harvests"))
            .addLoreLine(Component.literal("§7Automatically sell collected"))
            .addLoreLine(Component.literal("§7farm items instantly"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCOMING SOON!"))
            .addLoreLine(Component.literal("§7Status: " + (settings.autoSellHarvests ? "§aENABLED" : "§cDISABLED")))
        );
        
        // Auto-Invest Dividends
        setSlot(14, new GuiElementBuilder(settings.autoInvestDividends ? Items.LIME_WOOL : Items.RED_WOOL)
            .setName(Component.literal(settings.autoInvestDividends ? "§a§l✓ Auto-Invest Dividends" : "§c§l✗ Auto-Invest Dividends"))
            .addLoreLine(Component.literal("§7Automatically reinvest"))
            .addLoreLine(Component.literal("§7stock market dividends"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCOMING SOON!"))
            .addLoreLine(Component.literal("§7Status: " + (settings.autoInvestDividends ? "§aENABLED" : "§cDISABLED")))
        );
        
        // === DEPOSIT SETTINGS ===
        setSlot(19, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
            .setName(Component.literal("§e§lDeposit Threshold Settings"))
            .addLoreLine(Component.literal("§7Configure auto-deposit threshold"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(String.format("§7Current: §6$%,d", settings.depositThreshold)))
        );
        
        // Decrease buttons
        setSlot(20, new GuiElementBuilder(Items.RED_CONCRETE)
            .setName(Component.literal("§c§l-$10,000"))
            .addLoreLine(Component.literal("§7Decrease threshold"))
            .setCallback((index, type, action) -> {
                settings.depositThreshold = Math.max(1000, settings.depositThreshold - 10_000);
                player.sendSystemMessage(Component.literal(
                    String.format("§e§l[AUTOMATION] Threshold set to §6$%,d", settings.depositThreshold)));
                setupDisplay();
            })
        );
        
        setSlot(21, new GuiElementBuilder(Items.ORANGE_CONCRETE)
            .setName(Component.literal("§6§l-$1,000"))
            .addLoreLine(Component.literal("§7Decrease threshold"))
            .setCallback((index, type, action) -> {
                settings.depositThreshold = Math.max(1000, settings.depositThreshold - 1_000);
                player.sendSystemMessage(Component.literal(
                    String.format("§e§l[AUTOMATION] Threshold set to §6$%,d", settings.depositThreshold)));
                setupDisplay();
            })
        );
        
        // Current threshold display
        setSlot(22, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lCurrent Threshold"))
            .addLoreLine(Component.literal(String.format("§e$%,d", settings.depositThreshold)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Auto-deposit triggers when"))
            .addLoreLine(Component.literal("§7wallet exceeds this amount"))
        );
        
        // Increase buttons
        setSlot(23, new GuiElementBuilder(Items.LIME_CONCRETE)
            .setName(Component.literal("§a§l+$1,000"))
            .addLoreLine(Component.literal("§7Increase threshold"))
            .setCallback((index, type, action) -> {
                settings.depositThreshold += 1_000;
                player.sendSystemMessage(Component.literal(
                    String.format("§e§l[AUTOMATION] Threshold set to §6$%,d", settings.depositThreshold)));
                setupDisplay();
            })
        );
        
        setSlot(24, new GuiElementBuilder(Items.GREEN_CONCRETE)
            .setName(Component.literal("§a§l+$10,000"))
            .addLoreLine(Component.literal("§7Increase threshold"))
            .setCallback((index, type, action) -> {
                settings.depositThreshold += 10_000;
                player.sendSystemMessage(Component.literal(
                    String.format("§e§l[AUTOMATION] Threshold set to §6$%,d", settings.depositThreshold)));
                setupDisplay();
            })
        );
        
        // === STATISTICS ===
        setSlot(28, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§b§lToday's Statistics"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(String.format("§7Money Auto-Deposited: §6$%,d", stats.moneyAutoDeposited)))
            .addLoreLine(Component.literal(String.format("§7Items Auto-Sold: §e%d", stats.itemsAutoSold)))
            .addLoreLine(Component.literal(String.format("§7Loans Auto-Paid: §6$%,d", stats.loansAutoPaid)))
            .addLoreLine(Component.literal(String.format("§7Farms Auto-Collected: §e%d", stats.farmsAutoCollected)))
            .addLoreLine(Component.literal(String.format("§7Dividends Auto-Invested: §6$%,d", stats.dividendsAutoInvested)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Statistics reset daily"))
        );
        
        // === QUICK ACTIONS ===
        setSlot(30, new GuiElementBuilder(Items.LIGHTNING_ROD)
            .setName(Component.literal("§e§l⚡ Run All Now"))
            .addLoreLine(Component.literal("§7Execute all enabled automations"))
            .addLoreLine(Component.literal("§7immediately (manual trigger)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(String.format("§7Will run: §e%d §7tasks", settings.countEnabled())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to execute"))
            .setCallback((index, type, action) -> {
                AutomationManager.processManualAutomation(player);
                setupDisplay();
            })
        );
        
        // === NOTIFICATION CENTER ===
        setSlot(32, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§d§lNotification Center"))
            .addLoreLine(Component.literal("§7Recent automation actions"))
            .addLoreLine(Component.literal("§7(Last 10 events)"))
        );
        
        List<AutomationNotification> notifications = AutomationManager.getNotifications(player.getUUID());
        
        // Display up to 10 recent notifications
        int[] notificationSlots = {33, 34, 35, 36, 37, 38, 39, 40, 41, 42};
        for (int i = 0; i < Math.min(notifications.size(), 10); i++) {
            AutomationNotification notif = notifications.get(i);
            
            setSlot(notificationSlots[i], new GuiElementBuilder(Items.MAP)
                .setName(Component.literal("§e" + notif.getAction()))
                .addLoreLine(Component.literal("§7" + notif.getDetails()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Time: §f" + notif.getFormattedTime()))
                .addLoreLine(Component.literal(notif.getAmount() > 0 ? 
                    String.format("§7Amount: §6$%,d", notif.getAmount()) : ""))
            );
        }
        
        // Back button
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack to Hub"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
}
