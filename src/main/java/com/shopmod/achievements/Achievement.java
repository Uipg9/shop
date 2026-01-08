package com.shopmod.achievements;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Represents a single achievement
 */
public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final AchievementCategory category;
    private final AchievementRequirement requirement;
    private final long cashReward;
    private final String bonusReward;
    private final ItemStack icon;
    
    public Achievement(String id, String name, String description, AchievementCategory category,
                      AchievementRequirement requirement, long cashReward, String bonusReward, ItemStack icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.requirement = requirement;
        this.cashReward = cashReward;
        this.bonusReward = bonusReward;
        this.icon = icon;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementCategory getCategory() { return category; }
    public AchievementRequirement getRequirement() { return requirement; }
    public long getCashReward() { return cashReward; }
    public String getBonusReward() { return bonusReward; }
    public ItemStack getIcon() { return icon; }
    
    /**
     * Check if this achievement is completed for a player
     */
    public boolean isCompleted(AchievementProgress progress) {
        return requirement.isMet(progress);
    }
    
    /**
     * Get progress percentage (0-100)
     */
    public int getProgress(AchievementProgress progress) {
        return requirement.getProgress(progress);
    }
}
