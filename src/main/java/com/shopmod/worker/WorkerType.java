package com.shopmod.worker;

/**
 * Types of workers that can be hired
 */
public enum WorkerType {
    FARM_HAND("Farm Hand", "Improves farm efficiency"),
    MINER("Miner", "Reduces mining downtime"),
    PROPERTY_MANAGER("Property Manager", "Reduces repair costs");
    
    private final String displayName;
    private final String description;
    
    WorkerType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
