package com.shopmod.automation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an automated action notification
 */
public class AutomationNotification {
    private final String action;
    private final String details;
    private final long amount;
    private final LocalDateTime timestamp;
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public AutomationNotification(String action, String details, long amount) {
        this.action = action;
        this.details = details;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getAction() {
        return action;
    }
    
    public String getDetails() {
        return details;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public String getFormattedTime() {
        return timestamp.format(TIME_FORMAT);
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get a formatted display string for this notification
     */
    public String getDisplayString() {
        if (amount > 0) {
            return String.format("§7[%s] §e%s: §f%s (§6$%,d§f)", 
                getFormattedTime(), action, details, amount);
        } else {
            return String.format("§7[%s] §e%s: §f%s", 
                getFormattedTime(), action, details);
        }
    }
}
