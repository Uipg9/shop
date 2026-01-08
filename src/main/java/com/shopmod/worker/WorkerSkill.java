package com.shopmod.worker;

/**
 * Skills that workers can have and train
 */
public enum WorkerSkill {
    HARVESTING("Harvesting", "Increases farm yield", WorkerType.FARM_HAND),
    MINING("Mining", "Reduces mine downtime", WorkerType.MINER),
    MAINTENANCE("Maintenance", "Reduces property repair costs", WorkerType.PROPERTY_MANAGER),
    EFFICIENCY("Efficiency", "General productivity boost", null),
    SPEED("Speed", "Works faster", null);
    
    private final String displayName;
    private final String description;
    private final WorkerType primaryType; // null means all types can have this skill
    
    WorkerSkill(String displayName, String description, WorkerType primaryType) {
        this.displayName = displayName;
        this.description = description;
        this.primaryType = primaryType;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public WorkerType getPrimaryType() {
        return primaryType;
    }
    
    /**
     * Check if this skill is applicable to the worker type
     */
    public boolean isApplicableTo(WorkerType type) {
        return primaryType == null || primaryType == type;
    }
}
