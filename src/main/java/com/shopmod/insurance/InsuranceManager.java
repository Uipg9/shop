package com.shopmod.insurance;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.bank.BankManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages insurance policies and claims for players
 */
public class InsuranceManager {
    private static final Map<UUID, List<InsurancePolicy>> policiesMap = new ConcurrentHashMap<>();
    private static final Map<UUID, LinkedList<InsuranceClaim>> claimsMap = new ConcurrentHashMap<>();
    private static final int MAX_CLAIMS_HISTORY = 20;
    private static final int MAX_CLAIMS_PER_MONTH = 5;  // Fraud prevention
    
    /**
     * Get all policies for a player
     */
    public static List<InsurancePolicy> getPolicies(UUID playerUUID) {
        return policiesMap.computeIfAbsent(playerUUID, k -> new ArrayList<>());
    }
    
    /**
     * Get claims history for a player
     */
    public static List<InsuranceClaim> getClaims(UUID playerUUID) {
        return claimsMap.computeIfAbsent(playerUUID, k -> new LinkedList<>());
    }
    
    /**
     * Check if player has a specific insurance type
     */
    public static boolean hasInsurance(UUID playerUUID, InsuranceType type) {
        return getPolicies(playerUUID).stream()
            .anyMatch(p -> p.getType() == type && p.isActive());
    }
    
    /**
     * Get active policy of a specific type
     */
    public static InsurancePolicy getPolicy(UUID playerUUID, InsuranceType type) {
        return getPolicies(playerUUID).stream()
            .filter(p -> p.getType() == type && p.isActive())
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Purchase an insurance policy
     */
    public static boolean purchasePolicy(ServerPlayer player, InsuranceType type) {
        UUID playerUUID = player.getUUID();
        
        // Check if already has this insurance
        if (hasInsurance(playerUUID, type)) {
            player.sendSystemMessage(Component.literal(
                "§c§l[INSURANCE] You already have " + type.getDisplayName() + "!"));
            return false;
        }
        
        // Check if can afford first month
        long premium = type.getMonthlyPremium();
        if (!CurrencyManager.canAfford(player, premium)) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[INSURANCE] Cannot afford premium! Need §6$%,d", premium)));
            return false;
        }
        
        // Charge first month
        if (!CurrencyManager.removeMoney(player, premium)) {
            return false;
        }
        
        // Create and add policy
        InsurancePolicy policy = new InsurancePolicy(type, premium, type.getCoverageAmount());
        getPolicies(playerUUID).add(policy);
        
        player.sendSystemMessage(Component.literal(
            String.format("§a§l[INSURANCE] %s purchased! Monthly premium: §6$%,d", 
                type.getDisplayName(), premium)));
        
        return true;
    }
    
    /**
     * Cancel an insurance policy
     */
    public static boolean cancelPolicy(ServerPlayer player, InsuranceType type) {
        UUID playerUUID = player.getUUID();
        InsurancePolicy policy = getPolicy(playerUUID, type);
        
        if (policy == null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[INSURANCE] You don't have " + type.getDisplayName() + "!"));
            return false;
        }
        
        policy.setActive(false);
        player.sendSystemMessage(Component.literal(
            "§e§l[INSURANCE] " + type.getDisplayName() + " cancelled."));
        
        return true;
    }
    
    /**
     * File an insurance claim
     */
    public static boolean fileClaim(ServerPlayer player, InsuranceType policyType, 
                                   InsuranceClaim.ClaimType claimType, long amount, String reason) {
        UUID playerUUID = player.getUUID();
        
        // Check if has active policy
        InsurancePolicy policy = getPolicy(playerUUID, policyType);
        if (policy == null) {
            player.sendSystemMessage(Component.literal(
                "§c§l[INSURANCE] You don't have " + policyType.getDisplayName() + "!"));
            return false;
        }
        
        // Check claim amount vs coverage
        if (amount > policy.getCoverageAmount()) {
            player.sendSystemMessage(Component.literal(
                String.format("§c§l[INSURANCE] Claim amount exceeds coverage! Max: §6$%,d", 
                    policy.getCoverageAmount())));
            return false;
        }
        
        // Fraud detection - count claims this month
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        long recentClaims = getClaims(playerUUID).stream()
            .filter(c -> c.getFiledDate().isAfter(oneMonthAgo))
            .count();
        
        if (recentClaims >= MAX_CLAIMS_PER_MONTH) {
            player.sendSystemMessage(Component.literal(
                "§c§l[INSURANCE] Too many claims this month! Suspected fraud."));
            
            InsuranceClaim deniedClaim = new InsuranceClaim(policyType, claimType, amount, reason);
            deniedClaim.setStatus(InsuranceClaim.ClaimStatus.DENIED);
            deniedClaim.setDenialReason("Exceeded monthly claim limit");
            addClaim(playerUUID, deniedClaim);
            return false;
        }
        
        // Create and process claim
        InsuranceClaim claim = new InsuranceClaim(policyType, claimType, amount, reason);
        
        // Auto-approve if valid
        if (processClaim(player, claim, policy)) {
            player.sendSystemMessage(Component.literal(
                String.format("§a§l[INSURANCE] Claim approved! Payout: §6$%,d", amount)));
            return true;
        }
        
        return false;
    }
    
    /**
     * Process a claim (auto-approve if valid)
     */
    private static boolean processClaim(ServerPlayer player, InsuranceClaim claim, InsurancePolicy policy) {
        UUID playerUUID = player.getUUID();
        
        // Simple validation - in real implementation, could have more complex rules
        claim.setStatus(InsuranceClaim.ClaimStatus.APPROVED);
        
        // Pay out to checking account (or wallet if no bank expansion implemented yet)
        CurrencyManager.addMoney(player, claim.getAmount());
        claim.setStatus(InsuranceClaim.ClaimStatus.PAID);
        
        // Increase policy premium multiplier
        policy.incrementClaimsCount();
        
        // Add to claims history
        addClaim(playerUUID, claim);
        
        // Notify about premium increase
        if (policy.getPremiumMultiplier() > 1.0) {
            player.sendSystemMessage(Component.literal(
                String.format("§e§l[INSURANCE] Your premium increased to §6$%,d §e(%.0f%% multiplier)", 
                    policy.getMonthlyPremium(), policy.getPremiumMultiplier() * 100)));
        }
        
        return true;
    }
    
    /**
     * Add claim to history (keep last 20)
     */
    private static void addClaim(UUID playerUUID, InsuranceClaim claim) {
        LinkedList<InsuranceClaim> claims = 
            (LinkedList<InsuranceClaim>) claimsMap.computeIfAbsent(playerUUID, k -> new LinkedList<>());
        
        claims.addFirst(claim);
        
        while (claims.size() > MAX_CLAIMS_HISTORY) {
            claims.removeLast();
        }
    }
    
    /**
     * Process monthly premium billing
     * Called from ShopMod daily processing on the 1st of each month
     */
    public static void processMonthlyBilling(ServerPlayer player, long currentDay) {
        UUID playerUUID = player.getUUID();
        List<InsurancePolicy> policies = getPolicies(playerUUID);
        
        for (InsurancePolicy policy : policies) {
            if (!policy.isActive()) continue;
            
            // Check if a month has passed since last payment
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastPayment = policy.getLastPaymentDate();
            long daysSince = ChronoUnit.DAYS.between(lastPayment, now);
            
            if (daysSince < 30) continue;  // Not time to pay yet
            
            long premium = policy.getMonthlyPremium();
            
            // Try to charge from wallet
            if (CurrencyManager.canAfford(player, premium)) {
                if (CurrencyManager.removeMoney(player, premium)) {
                    policy.setLastPaymentDate(now);
                    policy.resetMissedPayments();
                    
                    player.sendSystemMessage(Component.literal(
                        String.format("§e§l[INSURANCE] Paid §6$%,d §efor %s", 
                            premium, policy.getType().getDisplayName())));
                }
            } else {
                // Missed payment
                policy.incrementMissedPayments();
                
                if (policy.getMissedPayments() >= 2) {
                    // Cancel after 2 missed payments
                    policy.setActive(false);
                    player.sendSystemMessage(Component.literal(
                        String.format("§c§l[INSURANCE] %s cancelled due to non-payment!", 
                            policy.getType().getDisplayName())));
                } else {
                    // Warning
                    player.sendSystemMessage(Component.literal(
                        String.format("§c§l[INSURANCE] Missed payment for %s! Cancel after %d more missed payment(s).", 
                            policy.getType().getDisplayName(), 2 - policy.getMissedPayments())));
                }
            }
        }
    }
    
    /**
     * Get total monthly premium cost for all active policies
     */
    public static long getTotalMonthlyPremium(UUID playerUUID) {
        return getPolicies(playerUUID).stream()
            .filter(InsurancePolicy::isActive)
            .mapToLong(InsurancePolicy::getMonthlyPremium)
            .sum();
    }
}
