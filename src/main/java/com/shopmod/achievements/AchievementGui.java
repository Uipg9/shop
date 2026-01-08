package com.shopmod.achievements;

import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.List;

/**
 * Achievement GUI - View and track achievements
 */
public class AchievementGui extends SimpleGui {
    private final ServerPlayer player;
    private AchievementCategory currentCategory = null; // null = ALL
    
    public AchievementGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§l✦ Achievements ✦"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            } else {
                setSlot(i, new GuiElementBuilder(Items.AIR));
            }
        }
        
        // Progress info
        AchievementProgress progress = AchievementManager.getProgress(player);
        int unlocked = AchievementManager.getUnlockedCount(player);
        int total = AchievementManager.getTotalCount();
        
        setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l" + player.getName().getString()))
            .addLoreLine(Component.literal("§7Achievements: §e" + unlocked + " §7/ §6" + total))
            .addLoreLine(Component.literal("§7Progress: §a" + (total > 0 ? (unlocked * 100 / total) : 0) + "%"))
        );
        
        // Category filters
        setupCategoryFilters();
        
        // Display achievements
        displayAchievements();
        
        // Back button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    private void setupCategoryFilters() {
        // ALL
        setSlot(10, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal(currentCategory == null ? "§a§l• ALL" : "§7ALL"))
            .addLoreLine(Component.literal("§7Show all achievements"))
            .setCallback((index, type, action) -> {
                currentCategory = null;
                setupDisplay();
            })
        );
        
        // Categories
        setSlot(11, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal(currentCategory == AchievementCategory.WEALTH ? "§a§l• WEALTH" : "§6WEALTH"))
            .setCallback((index, type, action) -> {
                currentCategory = AchievementCategory.WEALTH;
                setupDisplay();
            })
        );
        
        setSlot(12, new GuiElementBuilder(Items.GRASS_BLOCK)
            .setName(Component.literal(currentCategory == AchievementCategory.PROPERTY ? "§a§l• PROPERTY" : "§2PROPERTY"))
            .setCallback((index, type, action) -> {
                currentCategory = AchievementCategory.PROPERTY;
                setupDisplay();
            })
        );
        
        setSlot(13, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal(currentCategory == AchievementCategory.BUSINESS ? "§a§l• BUSINESS" : "§aBUSINESS"))
            .setCallback((index, type, action) -> {
                currentCategory = AchievementCategory.BUSINESS;
                setupDisplay();
            })
        );
        
        setSlot(14, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal(currentCategory == AchievementCategory.STOCK_MARKET ? "§a§l• STOCKS" : "§bSTOCKS"))
            .setCallback((index, type, action) -> {
                currentCategory = AchievementCategory.STOCK_MARKET;
                setupDisplay();
            })
        );
        
        setSlot(15, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal(currentCategory == AchievementCategory.GAMING ? "§a§l• GAMING" : "§eGAMING"))
            .setCallback((index, type, action) -> {
                currentCategory = AchievementCategory.GAMING;
                setupDisplay();
            })
        );
        
        setSlot(16, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal(currentCategory == AchievementCategory.FARM_MINE ? "§a§l• FARM/MINE" : "§6FARM/MINE"))
            .setCallback((index, type, action) -> {
                currentCategory = AchievementCategory.FARM_MINE;
                setupDisplay();
            })
        );
    }
    
    private void displayAchievements() {
        AchievementProgress progress = AchievementManager.getProgress(player);
        
        List<Achievement> achievementsToShow;
        if (currentCategory == null) {
            achievementsToShow = (List<Achievement>) AchievementManager.getAllAchievements();
        } else {
            achievementsToShow = AchievementManager.getAchievementsByCategory(currentCategory);
        }
        
        // Display slots: rows 2-4 (slots 19-25, 28-34, 37-43)
        int[] displaySlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        
        int slot = 0;
        for (Achievement achievement : achievementsToShow) {
            if (slot >= displaySlots.length) break;
            
            boolean unlocked = progress.isAchievementCompleted(achievement.getId());
            int progressPercent = achievement.getProgress(progress);
            
            ItemStack icon;
            if (unlocked) {
                icon = achievement.getIcon().copy();
            } else {
                icon = new ItemStack(Items.GRAY_DYE);
            }
            
            GuiElementBuilder builder = new GuiElementBuilder(icon)
                .setName(Component.literal((unlocked ? "§a✔ " : "§7") + achievement.getName()));
            
            builder.addLoreLine(Component.literal("§7" + achievement.getDescription()));
            builder.addLoreLine(Component.literal(""));
            
            if (unlocked) {
                builder.addLoreLine(Component.literal("§a§l✓ UNLOCKED"));
                builder.addLoreLine(Component.literal("§7Reward: §6" + CurrencyManager.format(achievement.getCashReward())));
                if (achievement.getBonusReward() != null && !achievement.getBonusReward().isEmpty()) {
                    builder.addLoreLine(Component.literal("§7Bonus: §d" + achievement.getBonusReward()));
                }
                builder.glow();
            } else {
                builder.addLoreLine(Component.literal("§e§lProgress: " + progressPercent + "%"));
                
                // Show target value
                AchievementRequirement req = achievement.getRequirement();
                long current = req.getCurrentValue(progress);
                long target = req.getTargetValue();
                builder.addLoreLine(Component.literal("§7" + formatNumber(current) + " / " + formatNumber(target)));
                
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§7Reward: §6" + CurrencyManager.format(achievement.getCashReward())));
                if (achievement.getBonusReward() != null && !achievement.getBonusReward().isEmpty()) {
                    builder.addLoreLine(Component.literal("§7Bonus: §d" + achievement.getBonusReward()));
                }
            }
            
            setSlot(displaySlots[slot], builder);
            slot++;
        }
    }
    
    private String formatNumber(long num) {
        if (num >= 1000000000) return String.format("%.1fB", num / 1000000000.0);
        if (num >= 1000000) return String.format("%.1fM", num / 1000000.0);
        if (num >= 1000) return String.format("%.1fK", num / 1000.0);
        return String.valueOf(num);
    }
}
