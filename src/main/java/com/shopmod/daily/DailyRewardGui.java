package com.shopmod.daily;

import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.time.LocalDate;

/**
 * Daily Rewards GUI
 */
public class DailyRewardGui extends SimpleGui {
    private final ServerPlayer player;
    
    public DailyRewardGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§l⭐ Daily Rewards"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            } else {
                setSlot(i, new GuiElementBuilder(Items.AIR));
            }
        }
        
        DailyRewardManager.DailyData data = DailyRewardManager.getData(player);
        boolean canClaim = DailyRewardManager.canClaimToday(player);
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l" + player.getName().getString()))
            .addLoreLine(Component.literal("§7Current Streak: §a" + data.getCurrentStreak() + " days"))
            .addLoreLine(Component.literal("§7Longest Streak: §6" + data.getLongestStreak() + " days"))
            .addLoreLine(Component.literal("§7Total Claims: §e" + data.getTotalClaims()))
        );
        
        // 7-day calendar
        int streak = data.getCurrentStreak();
        int currentDay = ((streak - 1) % 7) + 1;
        
        // Day 1
        setSlot(20, createDayButton(1, currentDay, canClaim,
            5000, null, false));
        
        // Day 2
        setSlot(21, createDayButton(2, currentDay, canClaim,
            10000, null, false));
        
        // Day 3
        setSlot(22, createDayButton(3, currentDay, canClaim,
            20000, DailyRewardManager.MysteryBox.COMMON, false));
        
        // Day 4
        setSlot(23, createDayButton(4, currentDay, canClaim,
            30000, null, false));
        
        // Day 5
        setSlot(24, createDayButton(5, currentDay, canClaim,
            50000, DailyRewardManager.MysteryBox.RARE, false));
        
        // Day 6
        setSlot(25, createDayButton(6, currentDay, canClaim,
            75000, null, false));
        
        // Day 7
        setSlot(26, createDayButton(7, currentDay, canClaim,
            100000, DailyRewardManager.MysteryBox.EPIC, true));
        
        // Claim button
        if (canClaim) {
            setSlot(31, new GuiElementBuilder(Items.EMERALD_BLOCK)
                .setName(Component.literal("§a§l✓ CLAIM TODAY'S REWARD"))
                .addLoreLine(Component.literal("§7Click to claim!"))
                .glow()
                .setCallback((index, type, action) -> {
                    DailyRewardManager.claimReward(player);
                    close();
                })
            );
        } else {
            LocalDate nextClaim = LocalDate.now().plusDays(1);
            setSlot(31, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Component.literal("§c§lAlready Claimed Today"))
                .addLoreLine(Component.literal("§7Come back tomorrow!"))
                .addLoreLine(Component.literal("§7Next reward: §e" + nextClaim))
            );
        }
        
        // Milestone info
        setSlot(38, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§lStreak Milestones"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal((data.getCurrentStreak() >= 30 ? "§a✓" : "§7○") + " §e30 Days: §a+5% Income Boost"))
            .addLoreLine(Component.literal((data.getCurrentStreak() >= 100 ? "§a✓" : "§7○") + " §e100 Days: §a+10% Income Boost + Legendary Box"))
        );
        
        // Stats
        setSlot(40, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§6§lYour Stats"))
            .addLoreLine(Component.literal("§7Current Streak: §a" + data.getCurrentStreak()))
            .addLoreLine(Component.literal("§7Longest Streak: §6" + data.getLongestStreak()))
            .addLoreLine(Component.literal("§7Total Claims: §e" + data.getTotalClaims()))
            .addLoreLine(Component.literal("§7Total Rewards: §6" + CurrencyManager.format(data.getTotalRewardsEarned())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Active Bonuses:"))
            .addLoreLine(Component.literal((data.hasPermanentBonus5Percent() ? "§a✓" : "§7○") + " §e+5% Income"))
            .addLoreLine(Component.literal((data.hasPermanentBonus10Percent() ? "§a✓" : "§7○") + " §e+10% Income"))
        );
        
        // Close button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    private GuiElementBuilder createDayButton(int day, int currentDay, boolean canClaim,
                                             long cashReward, DailyRewardManager.MysteryBox box, boolean insurance) {
        boolean isToday = (day == currentDay && canClaim);
        boolean isPast = (day < currentDay) || (!canClaim && day == currentDay);
        boolean isFuture = (day > currentDay);
        
        GuiElementBuilder builder;
        
        if (isToday) {
            builder = new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Component.literal("§a§l▶ DAY " + day + " (TODAY)"))
                .glow();
        } else if (isPast) {
            builder = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("§7§l✓ DAY " + day));
        } else {
            builder = new GuiElementBuilder(Items.ORANGE_STAINED_GLASS_PANE)
                .setName(Component.literal("§6§lDAY " + day));
        }
        
        builder.addLoreLine(Component.literal(""));
        builder.addLoreLine(Component.literal("§6" + CurrencyManager.format(cashReward)));
        
        if (box != null) {
            builder.addLoreLine(Component.literal("§d+ " + box.getName() + " Mystery Box"));
        }
        
        if (insurance) {
            builder.addLoreLine(Component.literal("§b+ Free Insurance Claim"));
        }
        
        return builder;
    }
}
