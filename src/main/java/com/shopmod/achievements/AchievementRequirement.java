package com.shopmod.achievements;

import java.util.function.Function;

/**
 * Requirement checker for achievements
 */
public class AchievementRequirement {
    private final RequirementType type;
    private final long targetValue;
    private final Function<AchievementProgress, Long> valueGetter;
    
    public AchievementRequirement(RequirementType type, long targetValue, 
                                 Function<AchievementProgress, Long> valueGetter) {
        this.type = type;
        this.targetValue = targetValue;
        this.valueGetter = valueGetter;
    }
    
    public boolean isMet(AchievementProgress progress) {
        long currentValue = valueGetter.apply(progress);
        return currentValue >= targetValue;
    }
    
    public int getProgress(AchievementProgress progress) {
        long currentValue = valueGetter.apply(progress);
        if (targetValue == 0) return 100;
        int percent = (int) ((currentValue * 100) / targetValue);
        return Math.min(100, percent);
    }
    
    public long getTargetValue() { return targetValue; }
    public long getCurrentValue(AchievementProgress progress) { return valueGetter.apply(progress); }
    
    public enum RequirementType {
        BALANCE_REACHED,
        PROPERTIES_OWNED,
        BUSINESSES_OWNED,
        STOCK_TRADES,
        GAMES_WON,
        WORKERS_HIRED,
        LOTTERY_WON,
        FARMS_OWNED,
        MINES_OWNED,
        MONEY_EARNED,
        CONSECUTIVE_WINS,
        CUSTOM
    }
}
