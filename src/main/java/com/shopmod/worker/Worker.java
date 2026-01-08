package com.shopmod.worker;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Individual worker data - NOT a physical entity, just system enhancement
 */
public class Worker {
    private final UUID workerId;
    private final String name;
    private final WorkerType type;
    private final long hireDate;
    
    private final Map<WorkerSkill, Integer> skills = new EnumMap<>(WorkerSkill.class);
    private int loyalty = 50; // 0-100
    private long experience = 0;
    private String assignedTo = null; // farmId/mineId/propertyId
    private long lastTrainingDay = -1;
    
    public Worker(String name, WorkerType type, long hireDate) {
        this.workerId = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.hireDate = hireDate;
        
        // Initialize skills based on worker type
        initializeSkills();
    }
    
    // Constructor for loading from data
    public Worker(UUID workerId, String name, WorkerType type, long hireDate, 
                  Map<WorkerSkill, Integer> skills, int loyalty, long experience,
                  String assignedTo, long lastTrainingDay) {
        this.workerId = workerId;
        this.name = name;
        this.type = type;
        this.hireDate = hireDate;
        this.skills.putAll(skills);
        this.loyalty = loyalty;
        this.experience = experience;
        this.assignedTo = assignedTo;
        this.lastTrainingDay = lastTrainingDay;
    }
    
    private void initializeSkills() {
        // All workers start with level 1 in applicable skills
        for (WorkerSkill skill : WorkerSkill.values()) {
            if (skill.isApplicableTo(type)) {
                skills.put(skill, 1);
            }
        }
    }
    
    public UUID getWorkerId() {
        return workerId;
    }
    
    public String getName() {
        return name;
    }
    
    public WorkerType getType() {
        return type;
    }
    
    public long getHireDate() {
        return hireDate;
    }
    
    public Map<WorkerSkill, Integer> getSkills() {
        return skills;
    }
    
    public int getSkillLevel(WorkerSkill skill) {
        return skills.getOrDefault(skill, 0);
    }
    
    public void setSkillLevel(WorkerSkill skill, int level) {
        if (level < 0) level = 0;
        if (level > 10) level = 10;
        skills.put(skill, level);
    }
    
    public int getLoyalty() {
        return loyalty;
    }
    
    public void setLoyalty(int loyalty) {
        if (loyalty < 0) loyalty = 0;
        if (loyalty > 100) loyalty = 100;
        this.loyalty = loyalty;
    }
    
    public void adjustLoyalty(int amount) {
        setLoyalty(loyalty + amount);
    }
    
    public long getExperience() {
        return experience;
    }
    
    public void addExperience(long exp) {
        this.experience += exp;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public boolean isAssigned() {
        return assignedTo != null && !assignedTo.isEmpty();
    }
    
    public long getLastTrainingDay() {
        return lastTrainingDay;
    }
    
    public void setLastTrainingDay(long day) {
        this.lastTrainingDay = day;
    }
    
    /**
     * Calculate daily salary based on total skill levels
     * Base $100 + $40 per total skill level
     */
    public long getDailySalary() {
        int totalSkillLevel = skills.values().stream().mapToInt(Integer::intValue).sum();
        return 100 + (totalSkillLevel * 40);
    }
    
    /**
     * Calculate total skill level
     */
    public int getTotalSkillLevel() {
        return skills.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Get efficiency bonus percentage (0.0 to 1.0+)
     * Based on primary skill level and efficiency skill
     */
    public double getEfficiencyBonus() {
        WorkerSkill primarySkill = getPrimarySkill();
        int primaryLevel = getSkillLevel(primarySkill);
        int efficiencyLevel = getSkillLevel(WorkerSkill.EFFICIENCY);
        
        // Primary skill contributes 5% per level
        // Efficiency skill contributes 2% per level
        return (primaryLevel * 0.05) + (efficiencyLevel * 0.02);
    }
    
    /**
     * Get primary skill for this worker type
     */
    public WorkerSkill getPrimarySkill() {
        return switch (type) {
            case FARM_HAND -> WorkerSkill.HARVESTING;
            case MINER -> WorkerSkill.MINING;
            case PROPERTY_MANAGER -> WorkerSkill.MAINTENANCE;
        };
    }
}
