package com.shopmod.pets;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pet Collection system - pets provide passive bonuses
 */
public class PetsManager {
    private static final Map<UUID, PetsData> playerPets = new ConcurrentHashMap<>();
    
    public enum PetType {
        DOG("§6Loyal Dog", 5000, "§7+5% Mining Speed", PetBonus.MINING_SPEED, 0.05),
        CAT("§dCute Cat", 5000, "§7+5% Mob Loot", PetBonus.MOB_LOOT, 0.05),
        PARROT("§aColorful Parrot", 10000, "§7+10% XP Gain", PetBonus.XP_BOOST, 0.10),
        HORSE("§7Swift Horse", 15000, "§7+15% Movement Speed", PetBonus.SPEED, 0.15),
        PANDA("§fCute Panda", 20000, "§7+20% Luck", PetBonus.LUCK, 0.20),
        FOX("§cSneaky Fox", 25000, "§7+10% Shop Discounts", PetBonus.SHOP_DISCOUNT, 0.10),
        AXOLOTL("§bRare Axolotl", 50000, "§7+25% Regeneration", PetBonus.REGEN, 0.25),
        BEE("§eHoney Bee", 30000, "§7+15% Farm Production", PetBonus.FARM_BOOST, 0.15),
        DOLPHIN("§bPlayful Dolphin", 40000, "§7+20% Swim Speed", PetBonus.SWIM_SPEED, 0.20),
        DRAGON("§5Legendary Dragon", 1000000, "§7+50% All Income", PetBonus.INCOME_BOOST, 0.50);
        
        private final String displayName;
        private final long cost;
        private final String description;
        private final PetBonus bonusType;
        private final double bonusAmount;
        
        PetType(String displayName, long cost, String description, PetBonus bonusType, double bonusAmount) {
            this.displayName = displayName;
            this.cost = cost;
            this.description = description;
            this.bonusType = bonusType;
            this.bonusAmount = bonusAmount;
        }
        
        public String getDisplayName() { return displayName; }
        public long getCost() { return cost; }
        public String getDescription() { return description; }
        public PetBonus getBonusType() { return bonusType; }
        public double getBonusAmount() { return bonusAmount; }
    }
    
    public enum PetBonus {
        MINING_SPEED, MOB_LOOT, XP_BOOST, SPEED, LUCK,
        SHOP_DISCOUNT, REGEN, FARM_BOOST, SWIM_SPEED, INCOME_BOOST
    }
    
    public static class PetsData {
        private final Set<PetType> ownedPets = new HashSet<>();
        private PetType activePet = null;
        
        public boolean hasPet(PetType type) {
            return ownedPets.contains(type);
        }
        
        public void addPet(PetType type) {
            ownedPets.add(type);
        }
        
        public Set<PetType> getOwnedPets() {
            return new HashSet<>(ownedPets);
        }
        
        public PetType getActivePet() { return activePet; }
        public void setActivePet(PetType pet) { this.activePet = pet; }
        
        public double getBonus(PetBonus bonusType) {
            if (activePet != null && activePet.getBonusType() == bonusType) {
                return activePet.getBonusAmount();
            }
            return 0.0;
        }
        
        public int getPetCount() {
            return ownedPets.size();
        }
    }
    
    public static PetsData getPetsData(UUID playerUUID) {
        return playerPets.computeIfAbsent(playerUUID, k -> new PetsData());
    }
    
    public static boolean purchasePet(ServerPlayer player, PetType type) {
        PetsData data = getPetsData(player.getUUID());
        
        if (data.hasPet(type)) {
            player.sendSystemMessage(Component.literal("§c§l[PETS] You already own this pet!"));
            return false;
        }
        
        if (!CurrencyManager.canAfford(player, type.getCost())) {
            player.sendSystemMessage(Component.literal(
                "§c§l[PETS] Insufficient funds! Need: §6" + CurrencyManager.format(type.getCost())));
            return false;
        }
        
        CurrencyManager.removeMoney(player, type.getCost());
        data.addPet(type);
        
        player.sendSystemMessage(Component.literal(
            "§a§l[PETS] Unlocked " + type.getDisplayName() + "!"));
        
        // Auto-equip first pet
        if (data.getActivePet() == null) {
            data.setActivePet(type);
            player.sendSystemMessage(Component.literal("§7Automatically equipped!"));
        }
        
        return true;
    }
    
    public static void equipPet(ServerPlayer player, PetType type) {
        PetsData data = getPetsData(player.getUUID());
        
        if (!data.hasPet(type)) {
            player.sendSystemMessage(Component.literal("§c§l[PETS] You don't own this pet!"));
            return;
        }
        
        data.setActivePet(type);
        player.sendSystemMessage(Component.literal(
            "§a§l[PETS] Equipped " + type.getDisplayName() + "!"));
        player.sendSystemMessage(Component.literal("§7" + type.getDescription()));
    }
}
