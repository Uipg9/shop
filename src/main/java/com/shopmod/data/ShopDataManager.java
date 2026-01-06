package com.shopmod.data;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages persistent player currency data across server restarts.
 * Stores balances, playtime, and statistics for each player using JSON.
 */
public class ShopDataManager {
    private final ConcurrentHashMap<UUID, PlayerShopData> playerData = new ConcurrentHashMap<>();
    private final MinecraftServer server;
    private final File dataFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public ShopDataManager(MinecraftServer server) {
        this.server = server;
        // Get the world save directory (world folder)
        Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        this.dataFile = worldPath.resolve("shop_data.json").toFile();
        load();
    }
    
    /**
     * Gets or creates shop data for a player
     */
    public PlayerShopData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, id -> new PlayerShopData());
    }
    
    /**
     * Gets the player's current balance
     */
    public long getBalance(UUID playerId) {
        return getPlayerData(playerId).balance;
    }
    
    public long getBalance(ServerPlayer player) {
        return getBalance(player.getUUID());
    }
    
    /**
     * Adds money to a player's balance
     */
    public void addMoney(UUID playerId, long amount) {
        PlayerShopData data = getPlayerData(playerId);
        data.balance += amount;
        data.totalEarned += amount;
        save();
    }
    
    public void addMoney(ServerPlayer player, long amount) {
        addMoney(player.getUUID(), amount);
    }
    
    /**
     * Removes money from a player's balance
     * @return true if successful, false if insufficient funds
     */
    public boolean removeMoney(UUID playerId, long amount) {
        PlayerShopData data = getPlayerData(playerId);
        if (data.balance >= amount) {
            data.balance -= amount;
            data.totalSpent += amount;
            save();
            return true;
        }
        return false;
    }
    
    public boolean removeMoney(ServerPlayer player, long amount) {
        return removeMoney(player.getUUID(), amount);
    }
    
    /**
     * Sets a player's balance
     */
    public void setBalance(UUID playerId, long amount) {
        PlayerShopData data = getPlayerData(playerId);
        data.balance = amount;
        save();
    }
    
    public void setBalance(ServerPlayer player, long amount) {
        setBalance(player.getUUID(), amount);
    }
    
    /**
     * Checks if a player can afford an amount
     */
    public boolean canAfford(UUID playerId, long amount) {
        return getBalance(playerId) >= amount;
    }
    
    /**
     * Updates playtime tracking
     */
    public void updatePlaytime(UUID playerId, long currentTick) {
        PlayerShopData data = getPlayerData(playerId);
        data.totalPlaytimeTicks = currentTick;
        save();
    }
    
    /**
     * Records the last passive income time
     */
    public void recordPassiveIncome(UUID playerId, long worldTime) {
        PlayerShopData data = getPlayerData(playerId);
        data.lastPassiveIncomeTime = worldTime;
        save();
    }
    
    /**
     * Gets the last passive income time
     */
    public long getLastPassiveIncomeTime(UUID playerId) {
        return getPlayerData(playerId).lastPassiveIncomeTime;
    }
    
    /**
     * Sets the last passive income time
     */
    public void setLastPassiveIncomeTime(UUID playerId, long time) {
        PlayerShopData data = getPlayerData(playerId);
        data.lastPassiveIncomeTime = time;
        save();
    }
    
    /**
     * Increments items bought counter
     */
    public void recordItemBought(UUID playerId) {
        getPlayerData(playerId).itemsBought++;
        save();
    }
    
    /**
     * Increments items sold counter
     */
    public void recordItemSold(UUID playerId) {
        getPlayerData(playerId).itemsSold++;
        save();
    }
    
    /**
     * Saves all player data to disk using JSON
     */
    public boolean save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            JsonObject root = new JsonObject();
            JsonArray playersArray = new JsonArray();
            
            for (UUID playerId : playerData.keySet()) {
                JsonObject playerObj = new JsonObject();
                PlayerShopData data = playerData.get(playerId);
                
                playerObj.addProperty("uuid", playerId.toString());
                playerObj.addProperty("balance", data.balance);
                playerObj.addProperty("totalEarned", data.totalEarned);
                playerObj.addProperty("totalSpent", data.totalSpent);
                playerObj.addProperty("totalPlaytimeTicks", data.totalPlaytimeTicks);
                playerObj.addProperty("lastPassiveIncomeTime", data.lastPassiveIncomeTime);
                playerObj.addProperty("itemsBought", data.itemsBought);
                playerObj.addProperty("itemsSold", data.itemsSold);
                
                // Save unlocked tiers
                JsonArray tiersArray = new JsonArray();
                for (int tier : data.unlockedTiers) {
                    tiersArray.add(tier);
                }
                playerObj.add("unlockedTiers", tiersArray);
                
                playersArray.add(playerObj);
            }
            
            root.add("players", playersArray);
            GSON.toJson(root, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads all player data from disk using JSON
     */
    public boolean load() {
        if (!dataFile.exists()) {
            return false;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (root.has("players")) {
                JsonArray playersArray = root.getAsJsonArray("players");
                
                for (JsonElement element : playersArray) {
                    JsonObject playerObj = element.getAsJsonObject();
                    
                    UUID playerId = UUID.fromString(playerObj.get("uuid").getAsString());
                    
                    PlayerShopData data = new PlayerShopData();
                    data.balance = playerObj.has("balance") ? playerObj.get("balance").getAsLong() : 0;
                    data.totalEarned = playerObj.has("totalEarned") ? playerObj.get("totalEarned").getAsLong() : 0;
                    data.totalSpent = playerObj.has("totalSpent") ? playerObj.get("totalSpent").getAsLong() : 0;
                    data.totalPlaytimeTicks = playerObj.has("totalPlaytimeTicks") ? playerObj.get("totalPlaytimeTicks").getAsLong() : 0;
                    data.lastPassiveIncomeTime = playerObj.has("lastPassiveIncomeTime") ? playerObj.get("lastPassiveIncomeTime").getAsLong() : 0;
                    data.itemsBought = playerObj.has("itemsBought") ? playerObj.get("itemsBought").getAsInt() : 0;
                    data.itemsSold = playerObj.has("itemsSold") ? playerObj.get("itemsSold").getAsInt() : 0;
                    
                    // Load unlocked tiers
                    if (playerObj.has("unlockedTiers")) {
                        JsonArray tiersArray = playerObj.getAsJsonArray("unlockedTiers");
                        for (JsonElement tierElement : tiersArray) {
                            data.unlockedTiers.add(tierElement.getAsInt());
                        }
                    } else {
                        // Default: Tier 0 unlocked
                        data.unlockedTiers.add(0);
                    }
                    
                    playerData.put(playerId, data);
                }
                return true;
            }
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Checks if a player has unlocked a tier
     */
    public boolean hasTierUnlocked(UUID playerId, int tierId) {
        PlayerShopData data = getPlayerData(playerId);
        return data.unlockedTiers.contains(tierId);
    }
    
    /**
     * Unlocks a tier for a player
     */
    public void unlockTier(UUID playerId, int tierId) {
        PlayerShopData data = getPlayerData(playerId);
        data.unlockedTiers.add(tierId);
        save();
    }
    
    /**
     * Gets the highest unlocked tier
     */
    public int getHighestUnlockedTier(UUID playerId) {
        PlayerShopData data = getPlayerData(playerId);
        int highest = 0;
        for (int tier : data.unlockedTiers) {
            if (tier > highest) highest = tier;
        }
        return highest;
    }
    
    /**
     * Data class to hold player shop information
     */
    public static class PlayerShopData {
        public long balance = 1000; // Start with $1,000
        public long totalEarned = 0;
        public long totalSpent = 0;
        public long totalPlaytimeTicks = 0;
        public long lastPassiveIncomeTime = 0;
        public int itemsBought = 0;
        public int itemsSold = 0;
        public java.util.Set<Integer> unlockedTiers = new java.util.HashSet<>();
        
        public PlayerShopData() {
            // Tier 0 (Starter) is always unlocked
            unlockedTiers.add(0);
        }
    }
}
