package com.shopmod.achievements;

public enum AchievementCategory {
    WEALTH("Wealth", "💰"),
    PROPERTY("Property", "🏠"),
    BUSINESS("Business", "💼"),
    JOBS("Jobs", "⚒️"),
    STOCK_MARKET("Stock Market", "📈"),
    GAMING("Gaming", "🎮"),
    WORKER("Worker", "👷"),
    LOTTERY("Lottery", "🎰"),
    FARM_MINE("Farm & Mine", "⛏️"),
    MISC("Miscellaneous", "✨");
    
    private final String displayName;
    private final String icon;
    
    AchievementCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
}
