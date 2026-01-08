package com.shopmod.insurance;

import com.shopmod.gui.HubGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.List;

/**
 * GUI for managing insurance policies and filing claims
 */
public class InsuranceGui extends SimpleGui {
    private final ServerPlayer player;
    private GuiMode mode;
    
    private enum GuiMode {
        MAIN,           // Overview
        AVAILABLE,      // Available policies to purchase
        ACTIVE,         // Active policies
        FILE_CLAIM,     // File a new claim
        CLAIMS_HISTORY  // View past claims
    }
    
    public InsuranceGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.mode = GuiMode.MAIN;
        this.setTitle(Component.literal("§9§l🛡 Insurance Center 🛡"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        clearDisplay();
        
        switch (mode) {
            case MAIN -> setupMainView();
            case AVAILABLE -> setupAvailableView();
            case ACTIVE -> setupActiveView();
            case FILE_CLAIM -> setupFileClaimView();
            case CLAIMS_HISTORY -> setupClaimsHistoryView();
        }
    }
    
    private void clearDisplay() {
        for (int i = 0; i < 54; i++) {
            clearSlot(i);
        }
        
        // Background border
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
    }
    
    private void setupMainView() {
        // Title
        setSlot(4, new GuiElementBuilder(Items.SHIELD)
            .setName(Component.literal("§9§lInsurance Center"))
            .addLoreLine(Component.literal("§7Protect your investments!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(String.format("§7Active Policies: §e%d", 
                InsuranceManager.getPolicies(player.getUUID()).stream()
                    .filter(InsurancePolicy::isActive).count())))
            .addLoreLine(Component.literal(String.format("§7Monthly Cost: §6$%,d", 
                InsuranceManager.getTotalMonthlyPremium(player.getUUID()))))
        );
        
        // Available Policies
        setSlot(20, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§a§lBrowse Policies"))
            .addLoreLine(Component.literal("§7View and purchase"))
            .addLoreLine(Component.literal("§7insurance coverage"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to view"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.AVAILABLE;
                setupDisplay();
            })
        );
        
        // Active Policies
        setSlot(22, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§b§lMy Policies"))
            .addLoreLine(Component.literal("§7View active insurance"))
            .addLoreLine(Component.literal("§7and payment status"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to view"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.ACTIVE;
                setupDisplay();
            })
        );
        
        // File Claim
        setSlot(24, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§e§lFile Claim"))
            .addLoreLine(Component.literal("§7Submit insurance claim"))
            .addLoreLine(Component.literal("§7for covered damages"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to file"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.FILE_CLAIM;
                setupDisplay();
            })
        );
        
        // Claims History
        setSlot(30, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§d§lClaims History"))
            .addLoreLine(Component.literal("§7View past claims"))
            .addLoreLine(Component.literal("§7and their status"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to view"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.CLAIMS_HISTORY;
                setupDisplay();
            })
        );
        
        addNavigationButtons();
    }
    
    private void setupAvailableView() {
        setSlot(4, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§a§lAvailable Policies"))
            .addLoreLine(Component.literal("§7Click to purchase coverage"))
        );
        
        int slot = 10;
        for (InsuranceType type : InsuranceType.values()) {
            boolean hasInsurance = InsuranceManager.hasInsurance(player.getUUID(), type);
            InsurancePolicy existingPolicy = InsuranceManager.getPolicy(player.getUUID(), type);
            
            GuiElementBuilder builder = new GuiElementBuilder(
                hasInsurance ? Items.LIME_CONCRETE : Items.WHITE_CONCRETE)
                .setName(Component.literal((hasInsurance ? "§a§l✓ " : "§f§l") + type.getDisplayName()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7" + type.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(String.format("§7Monthly Premium: §6$%,d", type.getMonthlyPremium())))
                .addLoreLine(Component.literal(String.format("§7Coverage: §6$%,d", type.getCoverageAmount())))
                .addLoreLine(Component.literal(""));
            
            if (hasInsurance) {
                double multiplier = existingPolicy.getPremiumMultiplier();
                if (multiplier > 1.0) {
                    builder.addLoreLine(Component.literal(
                        String.format("§cCurrent Premium: §6$%,d §c(%.0f%%)", 
                            existingPolicy.getMonthlyPremium(), multiplier * 100)));
                    builder.addLoreLine(Component.literal(
                        String.format("§7Claims Filed: §e%d", existingPolicy.getClaimsCount())));
                    builder.addLoreLine(Component.literal(""));
                }
                builder.addLoreLine(Component.literal("§a§lALREADY OWNED"));
            } else {
                builder.addLoreLine(Component.literal("§e§lCLICK §7to purchase"));
                builder.setCallback((index, clickType, actionType) -> {
                    InsuranceManager.purchasePolicy(player, type);
                    setupDisplay();
                });
            }
            
            setSlot(slot, builder);
            slot += 2;
        }
        
        // Back button
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.MAIN;
                setupDisplay();
            })
        );
        
        addNavigationButtons();
    }
    
    private void setupActiveView() {
        setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§b§lMy Active Policies"))
        );
        
        List<InsurancePolicy> activePolicies = InsuranceManager.getPolicies(player.getUUID()).stream()
            .filter(InsurancePolicy::isActive)
            .toList();
        
        if (activePolicies.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Active Policies"))
                .addLoreLine(Component.literal("§7You don't have any insurance"))
                .addLoreLine(Component.literal("§7Browse policies to get started!"))
            );
        } else {
            int slot = 10;
            for (InsurancePolicy policy : activePolicies) {
                setSlot(slot, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Component.literal("§a§l" + policy.getType().getDisplayName()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(String.format("§7Monthly Premium: §6$%,d", policy.getMonthlyPremium())))
                    .addLoreLine(Component.literal(String.format("§7Coverage: §6$%,d", policy.getCoverageAmount())))
                    .addLoreLine(Component.literal(String.format("§7Claims Filed: §e%d", policy.getClaimsCount())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(policy.getPremiumMultiplier() > 1.0 ?
                        String.format("§cPremium Multiplier: §e%.0f%%", policy.getPremiumMultiplier() * 100) :
                        "§aPremium Multiplier: §e100%"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lCLICK §7to cancel policy"))
                    .setCallback((index, type, action) -> {
                        InsuranceManager.cancelPolicy(player, policy.getType());
                        setupDisplay();
                    })
                );
                slot += 2;
            }
        }
        
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.MAIN;
                setupDisplay();
            })
        );
        
        addNavigationButtons();
    }
    
    private void setupFileClaimView() {
        setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§e§lFile Insurance Claim"))
            .addLoreLine(Component.literal("§7Select your policy type"))
        );
        
        List<InsurancePolicy> activePolicies = InsuranceManager.getPolicies(player.getUUID()).stream()
            .filter(InsurancePolicy::isActive)
            .toList();
        
        if (activePolicies.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Active Policies"))
                .addLoreLine(Component.literal("§7You need insurance to file a claim!"))
            );
        } else {
            setSlot(22, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§e§lQuick Claim: $10,000"))
                .addLoreLine(Component.literal("§7File a standard claim"))
                .addLoreLine(Component.literal("§7(Use commands for custom amounts)"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7You have " + activePolicies.size() + " active policy/policies"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§8Use: §f/insurance claim <type> <amount>"))
            );
        }
        
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.MAIN;
                setupDisplay();
            })
        );
        
        addNavigationButtons();
    }
    
    private void setupClaimsHistoryView() {
        setSlot(4, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§d§lClaims History"))
            .addLoreLine(Component.literal("§7Last 20 claims"))
        );
        
        List<InsuranceClaim> claims = InsuranceManager.getClaims(player.getUUID());
        
        if (claims.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§7No Claims Filed"))
                .addLoreLine(Component.literal("§7You haven't filed any claims yet"))
            );
        } else {
            int slot = 10;
            int count = 0;
            for (InsuranceClaim claim : claims) {
                if (count >= 20) break;  // Show max 20
                
                String statusColor = switch (claim.getStatus()) {
                    case PENDING -> "§e";
                    case APPROVED, PAID -> "§a";
                    case DENIED -> "§c";
                };
                
                GuiElementBuilder builder = new GuiElementBuilder(
                    claim.getStatus() == InsuranceClaim.ClaimStatus.PAID ? Items.LIME_CONCRETE :
                    claim.getStatus() == InsuranceClaim.ClaimStatus.DENIED ? Items.RED_CONCRETE :
                    Items.YELLOW_CONCRETE)
                    .setName(Component.literal(statusColor + "§l" + claim.getStatus().getDisplayName()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Policy: §f" + claim.getPolicyType().getDisplayName()))
                    .addLoreLine(Component.literal("§7Type: §f" + claim.getClaimType().getDisplayName()))
                    .addLoreLine(Component.literal(String.format("§7Amount: §6$%,d", claim.getAmount())))
                    .addLoreLine(Component.literal(""));
                
                if (claim.getStatus() == InsuranceClaim.ClaimStatus.DENIED && claim.getDenialReason() != null) {
                    builder.addLoreLine(Component.literal("§cReason: " + claim.getDenialReason()));
                }
                
                setSlot(slot, builder);
                slot++;
                if (slot % 9 == 8) slot += 2;  // Skip border
                if (slot >= 45) break;
                count++;
            }
        }
        
        setSlot(45, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack"))
            .setCallback((index, type, action) -> {
                mode = GuiMode.MAIN;
                setupDisplay();
            })
        );
        
        addNavigationButtons();
    }
    
    private void addNavigationButtons() {
        // Back to Hub
        setSlot(47, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§e§lBack to Hub"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
        
        // Close
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
}
