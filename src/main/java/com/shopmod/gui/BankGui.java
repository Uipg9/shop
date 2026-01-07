package com.shopmod.gui;

import com.shopmod.bank.BankManager;
import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Bank GUI for item storage and money investment
 */
public class BankGui extends SimpleGui {
    private final ServerPlayer player;
    private final BankManager.BankData bankData;
    private static final int STORAGE_START = 0;
    private int storageEnd;  // Dynamic based on storage level
    
    // Control slots - redesigned layout
    private static final int DEPOSIT_100 = 36;
    private static final int DEPOSIT_1K = 37;
    private static final int DEPOSIT_10K = 38;
    private static final int DEPOSIT_100K = 39;
    private static final int DEPOSIT_ALL = 40;
    
    private static final int WITHDRAW_100 = 42;
    private static final int WITHDRAW_1K = 43;
    private static final int WITHDRAW_10K = 44;
    private static final int WITHDRAW_100K = 45;
    private static final int WITHDRAW_ALL = 46;
    
    private static final int INFO_SLOT = 49;
    private static final int BALANCE_SLOT = 53;
    private static final int UPGRADE_SLOT = 41;
    
    public BankGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.bankData = BankManager.getBankData(player.getUUID());
        this.storageEnd = Math.min(bankData.getStorageSize() - 1, 26);  // Max 27 slots in first 3 rows
        
        this.setTitle(Component.literal("§6§lBank"));
        setupGui();
    }
    
    private void setupGui() {
        // Setup storage slots (0 to storageEnd)
        for (int i = STORAGE_START; i <= storageEnd; i++) {
            if (i < bankData.getStorage().size()) {
                ItemStack storedItem = bankData.getStorage().get(i);
                if (!storedItem.isEmpty()) {
                    setSlot(i, storedItem, (index, type, action) -> {
                        handleStorageClick(index, type);
                    });
                } else {
                    setSlot(i, ItemStack.EMPTY, (index, type, action) -> {
                        handleStorageClick(index, type);
                    });
                }
            }
        }
        
        // Grey out locked storage slots
        for (int i = storageEnd + 1; i <= 26; i++) {
            setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("§c§lLocked"))
                .addLoreLine(Component.literal("§7Upgrade storage to unlock")));
        }
        
        // Fill decorative slots (skip button areas)
        for (int i = 27; i < 54; i++) {
            if (i >= DEPOSIT_100 && i <= DEPOSIT_ALL) continue;
            if (i >= WITHDRAW_100 && i <= WITHDRAW_ALL) continue;
            if (i == INFO_SLOT || i == BALANCE_SLOT || i == UPGRADE_SLOT) continue;  // Skip control slots
            
            setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("")));
        }
        
        // Setup new button layout
        setupDepositButtons();
        setupWithdrawButtons();
        setupInfoDisplay();
        setupBalanceDisplay();
        setupUpgradeButton();
    }
    
    private void setupDepositButtons() {
        // $100 button
        setSlot(DEPOSIT_100, new GuiElementBuilder(Items.GOLD_NUGGET)
            .setName(Component.literal("§e§lDeposit $100"))
            .addLoreLine(Component.literal("§7Click to invest"))
            .setCallback((index, type, action) -> depositMoney(100))
        );
        
        // $1,000 button
        setSlot(DEPOSIT_1K, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lDeposit $1,000"))
            .addLoreLine(Component.literal("§7Click to invest"))
            .setCallback((index, type, action) -> depositMoney(1000))
        );
        
        // $10,000 button
        setSlot(DEPOSIT_10K, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§e§lDeposit $10,000"))
            .addLoreLine(Component.literal("§7Click to invest"))
            .setCallback((index, type, action) -> depositMoney(10000))
        );
        
        // $100,000 button
        setSlot(DEPOSIT_100K, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§e§lDeposit $100,000"))
            .addLoreLine(Component.literal("§7Click to invest"))
            .setCallback((index, type, action) -> depositMoney(100000))
        );
        
        // Deposit ALL button
        setSlot(DEPOSIT_ALL, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§a§l§lINVEST ALL"))
            .addLoreLine(Component.literal("§7Invest entire wallet"))
            .glow()
            .setCallback((index, type, action) -> {
                long balance = CurrencyManager.getBalance(player);
                depositMoney(balance);
            })
        );
    }
    
    private void setupWithdrawButtons() {
        // $100 button  
        setSlot(WITHDRAW_100, new GuiElementBuilder(Items.IRON_NUGGET)
            .setName(Component.literal("§6§lWithdraw $100"))
            .addLoreLine(Component.literal("§7Click to withdraw"))
            .setCallback((index, type, action) -> withdrawMoney(100))
        );
        
        // $1,000 button
        setSlot(WITHDRAW_1K, new GuiElementBuilder(Items.IRON_INGOT)
            .setName(Component.literal("§6§lWithdraw $1,000"))
            .addLoreLine(Component.literal("§7Click to withdraw"))
            .setCallback((index, type, action) -> withdrawMoney(1000))
        );
        
        // $10,000 button
        setSlot(WITHDRAW_10K, new GuiElementBuilder(Items.IRON_BLOCK)
            .setName(Component.literal("§6§lWithdraw $10,000"))
            .addLoreLine(Component.literal("§7Click to withdraw"))
            .setCallback((index, type, action) -> withdrawMoney(10000))
        );
        
        // $100,000 button
        setSlot(WITHDRAW_100K, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§6§lWithdraw $100,000"))
            .addLoreLine(Component.literal("§7Click to withdraw"))
            .setCallback((index, type, action) -> withdrawMoney(100000))
        );
        
        // Withdraw ALL button
        setSlot(WITHDRAW_ALL, new GuiElementBuilder(Items.DIAMOND_BLOCK)
            .setName(Component.literal("§c§l§lWITHDRAW ALL"))
            .addLoreLine(Component.literal("§7Withdraw all investments"))
            .glow()
            .setCallback((index, type, action) -> {
                long invested = bankData.getInvestedMoney();
                withdrawMoney(invested);
            })
        );
    }
    
    private void depositMoney(long amount) {
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal("§cInvalid amount!"));
            return;
        }
        
        long balance = CurrencyManager.getBalance(player);
        if (balance < amount) {
            player.sendSystemMessage(Component.literal("§c§lInsufficient funds!"));
            return;
        }
        
        if (BankManager.depositMoney(player, amount)) {
            player.sendSystemMessage(Component.literal("§a§lInvested " + CurrencyManager.format(amount) + "!"));
            setupInfoDisplay();
            setupBalanceDisplay();
        }
    }
    
    private void withdrawMoney(long amount) {
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal("§cInvalid amount!"));
            return;
        }
        
        long invested = bankData.getInvestedMoney();
        if (invested < amount) {
            player.sendSystemMessage(Component.literal("§c§lInsufficient investment!"));
            return;
        }
        
        if (BankManager.withdrawMoney(player, amount)) {
            player.sendSystemMessage(Component.literal("§6§lWithdrew " + CurrencyManager.format(amount) + "!"));
            setupInfoDisplay();
            setupBalanceDisplay();
        }
    }
    
    private void handleStorageClick(int slot, ClickType clickType) {
        // Check if slot is unlocked
        if (slot > storageEnd) {
            player.sendSystemMessage(Component.literal("§c§lThis slot is locked! Upgrade storage to unlock."));
            return;
        }
        
        ItemStack cursorStack = player.containerMenu.getCarried();
        ItemStack slotStack = bankData.getStorage().get(slot);
        
        if (clickType == ClickType.MOUSE_LEFT) {
            // Swap items
            player.containerMenu.setCarried(slotStack.copy());
            bankData.getStorage().set(slot, cursorStack.copy());
        } else if (clickType == ClickType.MOUSE_RIGHT) {
            if (cursorStack.isEmpty() && !slotStack.isEmpty()) {
                // Take half
                int takeAmount = (slotStack.getCount() + 1) / 2;
                ItemStack taken = slotStack.copy();
                taken.setCount(takeAmount);
                slotStack.shrink(takeAmount);
                player.containerMenu.setCarried(taken);
                bankData.getStorage().set(slot, slotStack);
            } else if (!cursorStack.isEmpty() && slotStack.isEmpty()) {
                // Place one
                ItemStack placed = cursorStack.copy();
                placed.setCount(1);
                cursorStack.shrink(1);
                player.containerMenu.setCarried(cursorStack);
                bankData.getStorage().set(slot, placed);
            } else if (!cursorStack.isEmpty() && !slotStack.isEmpty() && 
                      ItemStack.isSameItemSameComponents(cursorStack, slotStack)) {
                // Add one to stack
                if (slotStack.getCount() < slotStack.getMaxStackSize()) {
                    slotStack.grow(1);
                    cursorStack.shrink(1);
                    player.containerMenu.setCarried(cursorStack);
                    bankData.getStorage().set(slot, slotStack);
                }
            }
        }
        
        setupGui(); // Refresh
    }
    

    
    private void setupInfoDisplay() {
        long invested = bankData.getInvestedMoney();
        long currentDay = player.level().getDayTime() / 24000L;
        long lastDay = bankData.getLastProcessedDay();
        
        String status = lastDay < currentDay ? "§e⏳ Pending" : "§a✓ Processed";
        
        setSlot(INFO_SLOT, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lInvestment Info"))
            .addLoreLine(Component.literal("§7"))
            .addLoreLine(Component.literal("§7Invested: §6$" + String.format("%,d", invested)))
            .addLoreLine(Component.literal("§7Status: " + status))
            .addLoreLine(Component.literal("§7"))
            .addLoreLine(Component.literal("§7Daily returns process at dawn"))
            .addLoreLine(Component.literal("§7Returns are random each day!"))
            .addLoreLine(Component.literal("§7"))
            .addLoreLine(Component.literal("§aEasy: §2+10% §7/ §c-5%"))
            .addLoreLine(Component.literal("§eNormal: §2+25% §7/ §c-15%"))
            .addLoreLine(Component.literal("§cHard: §2+50% §7/ §c-40%"))
        );
    }
    
    private void setupBalanceDisplay() {
        long balance = CurrencyManager.getBalance(player);
        
        setSlot(BALANCE_SLOT, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lWallet Balance"))
            .addLoreLine(Component.literal("§7"))
            .addLoreLine(Component.literal("§6$" + String.format("%,d", balance)))
            .hideFlags()
        );
    }
    
    private void setupUpgradeButton() {
        int level = bankData.getStorageLevel();
        int slots = bankData.getStorageSize();
        
        GuiElementBuilder builder = new GuiElementBuilder(Items.CHEST);
        builder.setName(Component.literal("§b§lUpgrade Storage"));
        builder.addLoreLine(Component.literal("§7Current: §eLevel " + level + " §8(" + slots + " slots)"));
        
        if (level < 3) {
            long cost = BankManager.getUpgradeCost(level);
            int nextSlots = 27 + ((level + 1) * 9);
            builder.addLoreLine(Component.literal("§7Next: §eLevel " + (level + 1) + " §8(" + nextSlots + " slots)"));
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(cost)));
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§e§lCLICK TO UPGRADE"));
            builder.glow();
        } else {
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§a§lMaximum level reached!"));
        }
        
        setSlot(UPGRADE_SLOT, builder.setCallback((index, type, action) -> {
            if (BankManager.upgradeStorage(player)) {
                setupGui(); // Refresh
            }
        }));
    }
}
