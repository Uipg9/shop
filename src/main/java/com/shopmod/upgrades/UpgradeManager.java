package com.shopmod.upgrades;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player upgrade levels and effects
 */
public class UpgradeManager {
    private static final File UPGRADE_DATA_FILE = new File("world/shopmod_upgrades.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // Player UUID -> Upgrade Type -> Level
    private static final Map<UUID, Map<UpgradeType, Integer>> playerUpgrades = new HashMap<>();
    
    // Player UUID -> whether night vision is toggled ON
    private static final Map<UUID, Boolean> nightVisionToggle = new HashMap<>();
    
    public static void initialize() {
        loadData();
    }
    
    /**
     * Get player's upgrade level
     */
    public static int getUpgradeLevel(UUID playerUUID, UpgradeType type) {
        return playerUpgrades
            .computeIfAbsent(playerUUID, k -> new HashMap<>())
            .getOrDefault(type, 0);
    }
    
    /**
     * Upgrade a specific upgrade type for a player
     */
    public static boolean upgrade(UUID playerUUID, UpgradeType type) {
        int currentLevel = getUpgradeLevel(playerUUID, type);
        
        if (currentLevel >= type.getMaxLevel()) {
            return false; // Already at max level
        }
        
        playerUpgrades
            .computeIfAbsent(playerUUID, k -> new HashMap<>())
            .put(type, currentLevel + 1);
        
        saveData();
        return true;
    }
    
    /**
     * Get income multiplier for player (1.0 = 100%, 1.5 = 150%)
     */
    public static double getIncomeMultiplier(UUID playerUUID) {
        int level = getUpgradeLevel(playerUUID, UpgradeType.INCOME_MULTIPLIER);
        return 1.0 + UpgradeType.INCOME_MULTIPLIER.getBenefitAtLevel(level);
    }
    
    /**
     * Get sell price multiplier for player
     */
    public static double getSellPriceMultiplier(UUID playerUUID) {
        int level = getUpgradeLevel(playerUUID, UpgradeType.SELL_PRICE_BOOST);
        return 1.0 + UpgradeType.SELL_PRICE_BOOST.getBenefitAtLevel(level);
    }
    
    /**
     * Get XP multiplier for player
     */
    public static double getXPMultiplier(UUID playerUUID) {
        int level = getUpgradeLevel(playerUUID, UpgradeType.XP_MULTIPLIER);
        return 1.0 + UpgradeType.XP_MULTIPLIER.getBenefitAtLevel(level);
    }
    
    /**
     * Apply mining speed effect to player
     */
    public static void applyMiningSpeed(ServerPlayer player) {
        int level = getUpgradeLevel(player.getUUID(), UpgradeType.MINING_SPEED);
        if (level > 0) {
            // Calculate haste level (max V = level 5)
            int hasteLevel = Math.min(4, (level - 1) / 10); // 0-4 for Haste I-V
            
            // Apply permanent-ish effect (20 seconds, reapply regularly)
            player.addEffect(new MobEffectInstance(
                MobEffects.HASTE,
                20 * 20, // 20 seconds
                hasteLevel,
                true,  // Ambient
                false  // Show particles
            ));
        }
    }
    
    public static void applyHealthBoost(ServerPlayer player) {
        int level = getUpgradeLevel(player.getUUID(), UpgradeType.HEALTH_BOOST);
        if (level > 0) {
            int healthLevel = Math.min(20, level); // Max +40 hearts
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 20, healthLevel, true, false));
        }
    }
    
    public static void applyLuck(ServerPlayer player) {
        int level = getUpgradeLevel(player.getUUID(), UpgradeType.LUCK_BOOST);
        if (level > 0) {
            int luckLevel = Math.min(9, level - 1);
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 20, luckLevel, true, false));
        }
    }
    
    public static void applyRegeneration(ServerPlayer player) {
        int level = getUpgradeLevel(player.getUUID(), UpgradeType.REGENERATION);
        if (level > 0) {
            // Low level regen only (max Regen III at level 3)
            int regenLevel = Math.min(2, level - 1); // 0-2 for Regen I-III
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, regenLevel, true, false));
        }
    }
    
    public static void applyFireResistance(ServerPlayer player) {
        if (getUpgradeLevel(player.getUUID(), UpgradeType.FIRE_RESISTANCE) > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 20, 0, true, false));
        }
    }
    
    public static void applyWaterBreathing(ServerPlayer player) {
        if (getUpgradeLevel(player.getUUID(), UpgradeType.WATER_BREATHING) > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 20, 0, true, false));
        }
    }
    
    public static void applyNightVision(ServerPlayer player) {
        if (getUpgradeLevel(player.getUUID(), UpgradeType.NIGHT_VISION) > 0) {
            // Check if night vision is toggled on (default true)
            boolean isEnabled = nightVisionToggle.getOrDefault(player.getUUID(), true);
            if (isEnabled) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 0, true, false));
            }
        }
    }
    
    /**
     * Toggle night vision on/off for a player
     */
    public static boolean toggleNightVision(UUID playerUUID) {
        boolean current = nightVisionToggle.getOrDefault(playerUUID, true);
        nightVisionToggle.put(playerUUID, !current);
        return !current; // Return new state
    }
    
    /**
     * Check if night vision is enabled for a player
     */
    public static boolean isNightVisionEnabled(UUID playerUUID) {
        return nightVisionToggle.getOrDefault(playerUUID, true);
    }
    
    private static void loadData() {
        if (!UPGRADE_DATA_FILE.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(UPGRADE_DATA_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            for (String uuidStr : root.keySet()) {
                UUID playerUUID = UUID.fromString(uuidStr);
                JsonObject playerData = root.getAsJsonObject(uuidStr);
                
                Map<UpgradeType, Integer> upgrades = new HashMap<>();
                for (UpgradeType type : UpgradeType.values()) {
                    String key = type.name();
                    if (playerData.has(key)) {
                        upgrades.put(type, playerData.get(key).getAsInt());
                    }
                }
                
                playerUpgrades.put(playerUUID, upgrades);
            }
        } catch (Exception e) {
            System.err.println("Failed to load upgrade data: " + e.getMessage());
        }
    }
    
    private static void saveData() {
        try {
            UPGRADE_DATA_FILE.getParentFile().mkdirs();
            
            JsonObject root = new JsonObject();
            
            for (Map.Entry<UUID, Map<UpgradeType, Integer>> entry : playerUpgrades.entrySet()) {
                JsonObject playerData = new JsonObject();
                
                for (Map.Entry<UpgradeType, Integer> upgrade : entry.getValue().entrySet()) {
                    playerData.addProperty(upgrade.getKey().name(), upgrade.getValue());
                }
                
                root.add(entry.getKey().toString(), playerData);
            }
            
            try (FileWriter writer = new FileWriter(UPGRADE_DATA_FILE)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            System.err.println("Failed to save upgrade data: " + e.getMessage());
        }
    }
}
