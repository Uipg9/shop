package com.shopmod.insurance;

import java.time.LocalDateTime;

/**
 * Represents an insurance claim filed by a player
 */
public class InsuranceClaim {
    private final InsuranceType policyType;
    private final ClaimType claimType;
    private final long amount;
    private final String reason;
    private final LocalDateTime filedDate;
    private ClaimStatus status;
    private String denialReason;
    
    public enum ClaimStatus {
        PENDING("Pending Review"),
        APPROVED("Approved"),
        DENIED("Denied"),
        PAID("Paid Out");
        
        private final String displayName;
        
        ClaimStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ClaimType {
        PROPERTY_DAMAGE("Property Damage"),
        CROP_FAILURE("Crop Failure"),
        EQUIPMENT_FAILURE("Equipment Failure"),
        TENANT_LOSS("Tenant Loss"),
        OTHER("Other");
        
        private final String displayName;
        
        ClaimType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public InsuranceClaim(InsuranceType policyType, ClaimType claimType, long amount, String reason) {
        this.policyType = policyType;
        this.claimType = claimType;
        this.amount = amount;
        this.reason = reason;
        this.filedDate = LocalDateTime.now();
        this.status = ClaimStatus.PENDING;
    }
    
    public InsuranceType getPolicyType() {
        return policyType;
    }
    
    public ClaimType getClaimType() {
        return claimType;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public LocalDateTime getFiledDate() {
        return filedDate;
    }
    
    public ClaimStatus getStatus() {
        return status;
    }
    
    public void setStatus(ClaimStatus status) {
        this.status = status;
    }
    
    public String getDenialReason() {
        return denialReason;
    }
    
    public void setDenialReason(String reason) {
        this.denialReason = reason;
    }
}
