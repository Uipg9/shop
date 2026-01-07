package com.shopmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.shopmod.ShopMod;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.gui.ShopGui;
import com.shopmod.gui.EnchantingGui;
import com.shopmod.shop.ItemPricing;
import com.shopmod.shop.ShopTier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

/**
 * Comprehensive shop commands - buy, sell, tiers, unlock, balance
 * All commands use player.getUUID() for data manager calls
 */
public class ShopCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Main /shop command - opens GUI
        dispatcher.register(Commands.literal("shop")
            .executes(ShopCommands::openGui)
            .then(Commands.literal("balance")
                .executes(ShopCommands::showBalance))
            .then(Commands.literal("buy")
                .then(Commands.argument("item", StringArgumentType.string())
                    .executes(ctx -> buyItem(ctx, 1))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                        .executes(ctx -> buyItem(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))))
            .then(Commands.literal("sell")
                .then(Commands.argument("item", StringArgumentType.string())
                    .executes(ctx -> sellItem(ctx, 1))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                        .executes(ctx -> sellItem(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))))
            .then(Commands.literal("tiers")
                .executes(ShopCommands::showTiers))
            .then(Commands.literal("unlock")
                .then(Commands.argument("tier", StringArgumentType.string())
                    .executes(ShopCommands::unlockTier)))
        );

        // Quick balance commands
        dispatcher.register(Commands.literal("balance").executes(ShopCommands::showBalance));
        dispatcher.register(Commands.literal("bal").executes(ShopCommands::showBalance));

        // Cleaner /buy command (user requested!)
        dispatcher.register(Commands.literal("buy")
            .then(Commands.argument("item", StringArgumentType.string())
                .executes(ctx -> buyItem(ctx, 1))
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                    .executes(ctx -> buyItem(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))));

        // Admin commands (TODO: Add permission check with proper MC 1.21.11 API)
        dispatcher.register(Commands.literal("shopadmin")
            .then(Commands.literal("setmoney")
                .then(Commands.argument("player", StringArgumentType.string())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ShopCommands::setMoney))))
            .then(Commands.literal("addmoney")
                .then(Commands.argument("player", StringArgumentType.string())
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(ShopCommands::addMoney))))
        );

        // Cleaner /sell command - defaults to ALL in hand (user requested!)
        dispatcher.register(Commands.literal("sell")
            .executes(ctx -> sellInHand(ctx, -1)) // -1 means ALL
            .then(Commands.literal("all")
                .executes(ctx -> sellAllItems(ctx)))
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(ctx -> sellInHand(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))
        );
        
        // /anvil command - opens anvil GUI for enchanting
        dispatcher.register(Commands.literal("anvil")
            .executes(ShopCommands::openAnvil)
        );
        
        // /nightvision toggle command
        dispatcher.register(Commands.literal("nightvision")
            .executes(ShopCommands::toggleNightVision)
        );
        dispatcher.register(Commands.literal("nv")
            .executes(ShopCommands::toggleNightVision)
        );
        
        // Teleportation commands
        dispatcher.register(Commands.literal("sethome")
            .executes(ShopCommands::setHome)
        );
        dispatcher.register(Commands.literal("home")
            .executes(ShopCommands::teleportHome)
        );
        
        // Village command
        dispatcher.register(Commands.literal("village")
            .executes(ShopCommands::openVillage)
        );
    }

    private static int openGui(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            new ShopGui(player).open();
        }
        return 1;
    }
    
    private static int openAnvil(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            new EnchantingGui(player).open();
        }
        return 1;
    }

    private static int showBalance(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            // Get wallet balance
            long balance = CurrencyManager.getBalance(player);
            
            // Get village financial info
            com.shopmod.village.VillageManager.Village village = 
                com.shopmod.village.VillageManager.getVillage(player.getUUID());
            
            long totalDailySalaries = 0;
            long estimatedDailyIncome = 0;
            
            for (var entry : village.getWorkers().entrySet()) {
                com.shopmod.village.VillagerWorker workerType = entry.getKey();
                com.shopmod.village.VillageManager.WorkerData data = entry.getValue();
                
                // Calculate daily salaries
                totalDailySalaries += workerType.getDailySalary() * data.getCount();
                
                // Estimate daily income from resource production
                estimatedDailyIncome += workerType.getEstimatedDailyValue() * data.getCount();
            }
            
            long netDailyProfit = estimatedDailyIncome - totalDailySalaries;
            
            // Send enhanced balance message
            player.sendSystemMessage(Component.literal("§6§l========== FINANCES =========="));
            player.sendSystemMessage(Component.literal("§7Wallet Balance: §6" + 
                CurrencyManager.format(balance)));
            
            if (village.getTotalWorkerCount() > 0) {
                player.sendSystemMessage(Component.literal(""));
                player.sendSystemMessage(Component.literal("§e§lVillage Economics:"));
                player.sendSystemMessage(Component.literal("§7Daily Salaries: §c-" + 
                    CurrencyManager.format(totalDailySalaries)));
                player.sendSystemMessage(Component.literal("§7Estimated Income: §a+" + 
                    CurrencyManager.format(estimatedDailyIncome)));
                player.sendSystemMessage(Component.literal("§7Net Daily Profit: " + 
                    (netDailyProfit >= 0 ? "§a+" : "§c") + 
                    CurrencyManager.format(netDailyProfit)));
                player.sendSystemMessage(Component.literal("§8(Income varies by efficiency & resources)"));
            }
            
            player.sendSystemMessage(Component.literal("§6§l=============================="));
        }
        return 1;
    }

    private static int buyItem(CommandContext<CommandSourceStack> ctx, int amount) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String itemName = StringArgumentType.getString(ctx, "item");
        
        // Parse item name - get from registry by string key
        Item item = null;
        for (Item candidate : BuiltInRegistries.ITEM) {
            String key = BuiltInRegistries.ITEM.getKey(candidate).toString();
            if (key.equals(itemName) || key.equals("minecraft:" + itemName)) {
                item = candidate;
                break;
            }
        }
        if (item == null) item = Items.AIR;

        if (item == Items.AIR || item == null) {
            player.sendSystemMessage(Component.literal("§cUnknown item: " + itemName));
            return 0;
        }

        long buyPrice = ItemPricing.getBuyPrice(item);
        if (buyPrice == 0) {
            player.sendSystemMessage(Component.literal("§cThat item is not available for purchase!"));
            return 0;
        }

        // Check tier requirement
        ShopTier itemTier = ItemPricing.getTier(item);
        int highestTier = ShopMod.dataManager.getHighestUnlockedTier(player.getUUID());
        if (itemTier.getId() > highestTier) {
            player.sendSystemMessage(Component.literal("§cYou need " + itemTier.getColor() + itemTier.getName() 
                + " §ctier to buy this! Use /shop unlock"));
            return 0;
        }

        long totalCost = buyPrice * amount;
        
        if (!CurrencyManager.canAfford(player, totalCost)) {
            CurrencyManager.sendInsufficientFundsMessage(player, totalCost);
            return 0;
        }

        if (CurrencyManager.removeMoney(player, totalCost)) {
            player.addItem(new ItemStack(item, amount));
            CurrencyManager.sendMoneySpentMessage(player, totalCost, 
                "Purchased " + amount + "x " + item.getName(item.getDefaultInstance()).getString());
            return 1;
        }

        return 0;
    }

    private static int sellItem(CommandContext<CommandSourceStack> ctx, int amount) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String itemName = StringArgumentType.getString(ctx, "item");
        
        // Parse item name - get from registry by string key
        Item item = null;
        for (Item candidate : BuiltInRegistries.ITEM) {
            String key = BuiltInRegistries.ITEM.getKey(candidate).toString();
            if (key.equals(itemName) || key.equals("minecraft:" + itemName)) {
                item = candidate;
                break;
            }
        }
        if (item == null) item = Items.AIR;
        
        if (item == Items.AIR || item == null) {
            player.sendSystemMessage(Component.literal("§cUnknown item: " + itemName));
            return 0;
        }

        if (!ItemPricing.canSell(item)) {
            player.sendSystemMessage(Component.literal("§cThat item cannot be sold!"));
            return 0;
        }

        long sellPrice = ItemPricing.getSellPrice(item);
        
        // Count how many they have
        int available = countItemInInventory(player, item);
        int toSell = Math.min(amount, available);
        
        if (toSell == 0) {
            player.sendSystemMessage(Component.literal("§cYou don't have any " + 
                item.getName(item.getDefaultInstance()).getString() + " to sell!"));
            return 0;
        }

        // Remove items from inventory
        int removed = removeItemFromInventory(player, item, toSell);
        
        if (removed > 0) {
            long totalEarned = sellPrice * removed;
            CurrencyManager.addMoney(player, totalEarned);
            CurrencyManager.sendMoneyReceivedMessage(player, totalEarned, 
                "Sold " + removed + "x " + item.getName(item.getDefaultInstance()).getString());
            return 1;
        }

        return 0;
    }

    /**
     * NEW: Sell item in hand command (like popular servers)
     * User requested this feature! Defaults to entire stack.
     */
    private static int sellInHand(CommandContext<CommandSourceStack> ctx, int amount) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cYou're not holding anything!"));
            return 0;
        }

        Item item = heldItem.getItem();
        
        if (!ItemPricing.canSell(item)) {
            player.sendSystemMessage(Component.literal("§cThat item cannot be sold!"));
            return 0;
        }

        long sellPrice = ItemPricing.getSellPrice(item);
        
        // Count how many they have in hand
        int available = heldItem.getCount();
        
        // If amount is -1, sell entire stack
        int toSell = (amount == -1) ? available : Math.min(amount, available);
        
        if (toSell == 0) {
            player.sendSystemMessage(Component.literal("§cYou don't have any to sell!"));
            return 0;
        }

        // Remove from hand
        heldItem.shrink(toSell);
        
        long totalEarned = sellPrice * toSell;
        CurrencyManager.addMoney(player, totalEarned);
        CurrencyManager.sendMoneyReceivedMessage(player, totalEarned, 
            "Sold " + toSell + "x " + item.getName(item.getDefaultInstance()).getString());
        return 1;
    }
    
    /**
     * Sell ALL sellable items in inventory
     */
    private static int sellAllItems(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        long totalEarned = 0;
        int totalItemsSold = 0;
        
        // Scan entire inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            if (!ItemPricing.canSell(item)) continue;
            
            long sellPrice = ItemPricing.getSellPrice(item);
            int count = stack.getCount();
            
            totalEarned += sellPrice * count;
            totalItemsSold += count;
            
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        
        if (totalItemsSold == 0) {
            player.sendSystemMessage(Component.literal("§cYou don't have any sellable items!"));
            return 0;
        }
        
        CurrencyManager.addMoney(player, totalEarned);
        player.sendSystemMessage(Component.literal("§a§lSOLD ALL ITEMS!"));
        CurrencyManager.sendMoneyReceivedMessage(player, totalEarned, 
            "Sold " + totalItemsSold + " items from inventory");
        return 1;
    }

    private static int showTiers(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.sendSystemMessage(Component.literal("§e§l=== Shop Tiers ==="));
        
        int highestTier = ShopMod.dataManager.getHighestUnlockedTier(player.getUUID());
        
        for (ShopTier tier : ShopTier.values()) {
            boolean unlocked = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), tier.getId());
            String status = unlocked ? "§a✓ UNLOCKED" : "§c✗ LOCKED";
            String current = (tier.getId() == highestTier) ? " §e§l← CURRENT" : "";
            
            player.sendSystemMessage(Component.literal(
                status + " " + tier.getColor() + tier.getName() + 
                " §7- " + (tier.getUnlockCost() == 0 ? "FREE" : CurrencyManager.format(tier.getUnlockCost())) + 
                current
            ));
        }
        
        player.sendSystemMessage(Component.literal("§7Use /shop unlock <tier> to unlock a new tier"));
        return 1;
    }

    private static int unlockTier(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String tierName = StringArgumentType.getString(ctx, "tier");
        
        // Find tier by name
        ShopTier targetTier = null;
        for (ShopTier tier : ShopTier.values()) {
            if (tier.getName().equalsIgnoreCase(tierName) || tier.name().equalsIgnoreCase(tierName)) {
                targetTier = tier;
                break;
            }
        }
        
        if (targetTier == null) {
            player.sendSystemMessage(Component.literal("§cUnknown tier: " + tierName));
            player.sendSystemMessage(Component.literal("§7Available: starter, farmer, engineer, merchant, nether_master, elite"));
            return 0;
        }

        // Check if already unlocked
        if (ShopMod.dataManager.hasTierUnlocked(player.getUUID(), targetTier.getId())) {
            player.sendSystemMessage(Component.literal("§cYou already have " + targetTier.getColor() + 
                targetTier.getName() + " §ctier unlocked!"));
            return 0;
        }

        // Check if previous tier is unlocked
        if (targetTier.getId() > 0) {
            ShopTier prevTier = ShopTier.getById(targetTier.getId() - 1);
            if (!ShopMod.dataManager.hasTierUnlocked(player.getUUID(), prevTier.getId())) {
                player.sendSystemMessage(Component.literal("§cYou must unlock " + prevTier.getColor() + 
                    prevTier.getName() + " §cfirst!"));
                return 0;
            }
        }

        // Check if can afford
        long cost = targetTier.getUnlockCost();
        if (!CurrencyManager.canAfford(player, cost)) {
            CurrencyManager.sendInsufficientFundsMessage(player, cost);
            return 0;
        }

        // Unlock the tier
        if (CurrencyManager.removeMoney(player, cost)) {
            ShopMod.dataManager.unlockTier(player.getUUID(), targetTier.getId());
            player.sendSystemMessage(Component.literal("§a✓ Unlocked " + targetTier.getColor() + 
                targetTier.getName() + " §atier!"));
            player.sendSystemMessage(Component.literal("§7You can now buy items from this tier!"));
            return 1;
        }

        return 0;
    }

    private static int setMoney(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        
        ServerPlayer targetPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            ctx.getSource().sendFailure(Component.literal("§cPlayer not found: " + playerName));
            return 0;
        }

        // Set money directly
        long currentBalance = ShopMod.dataManager.getBalance(targetPlayer.getUUID());
        if (amount > currentBalance) {
            ShopMod.dataManager.addMoney(targetPlayer.getUUID(), amount - currentBalance);
        } else {
            ShopMod.dataManager.removeMoney(targetPlayer.getUUID(), currentBalance - amount);
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal("§aSet " + playerName + "'s balance to " + 
            CurrencyManager.format(amount)), true);
        targetPlayer.sendSystemMessage(Component.literal("§aYour balance was set to " + 
            CurrencyManager.format(amount)));
        
        return 1;
    }

    private static int addMoney(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        
        ServerPlayer targetPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            ctx.getSource().sendFailure(Component.literal("§cPlayer not found: " + playerName));
            return 0;
        }

        ShopMod.dataManager.addMoney(targetPlayer.getUUID(), amount);
        
        ctx.getSource().sendSuccess(() -> Component.literal("§aAdded " + CurrencyManager.format(amount) + 
            " to " + playerName + "'s balance"), true);
        targetPlayer.sendSystemMessage(Component.literal("§aReceived " + CurrencyManager.format(amount)));
        
        return 1;
    }
    
    private static int toggleNightVision(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        // Check if player has night vision upgrade
        if (com.shopmod.upgrades.UpgradeManager.getUpgradeLevel(player.getUUID(), 
                com.shopmod.upgrades.UpgradeType.NIGHT_VISION) == 0) {
            player.sendSystemMessage(Component.literal("§cYou haven't purchased the Night Vision upgrade yet!"));
            return 0;
        }
        
        boolean newState = com.shopmod.upgrades.UpgradeManager.toggleNightVision(player.getUUID());
        
        if (newState) {
            player.sendSystemMessage(Component.literal("§a§lNight Vision: §aENABLED"));
        } else {
            player.sendSystemMessage(Component.literal("§c§lNight Vision: §cDISABLED"));
            // Remove the effect immediately
            player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
        }
        
        return 1;
    }
    
    private static int setHome(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        com.shopmod.teleport.TeleportManager.setHome(player);
        return 1;
    }
    
    private static int teleportHome(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        com.shopmod.teleport.TeleportManager.teleportHome(player);
        return 1;
    }
    
    private static int openVillage(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        com.shopmod.gui.VillageGuiV2 gui = new com.shopmod.gui.VillageGuiV2(player);
        gui.open();
        return 1;
    }

    // Helper methods
    private static int countItemInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int removeItemFromInventory(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (remaining <= 0) break;
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        
        return amount - remaining;
    }
}
