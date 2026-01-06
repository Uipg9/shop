package com.shopmod.events;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Earnings for smelting items
 * Players earn money when taking items from furnaces
 */
public class SmeltEarningsHandler {
    
    public static void register() {
        // Note: Fabric doesn't have a direct furnace output event
        // We'll need to use a mixin or check when players interact with furnaces
        // For now, this is a placeholder structure
    }
    
    /**
     * Called when a player takes an item from a furnace
     * This would need to be hooked via mixin or similar
     */
    public static void onSmeltComplete(ServerPlayer player, int itemCount) {
        if (itemCount <= 0) return;
        
        // Base reward per item smelted
        long baseMoneyPerItem = 2; // $2 per smelted item
        int baseXPPerItem = 1;     // 1 XP per smelted item
        
        // Apply multipliers
        double incomeMultiplier = UpgradeManager.getIncomeMultiplier(player.getUUID());
        double xpMultiplier = UpgradeManager.getXPMultiplier(player.getUUID());
        
        long totalMoney = (long)(baseMoneyPerItem * itemCount * incomeMultiplier);
        int totalXP = (int)(baseXPPerItem * itemCount * xpMultiplier);
        
        // Award
        if (totalMoney > 0) {
            CurrencyManager.addMoney(player, totalMoney);
        }
        if (totalXP > 0) {
            player.giveExperiencePoints(totalXP);
        }
        
        // Notify
        if (totalMoney > 0 || totalXP > 0) {
            String message = "§e" + itemCount + " items smelted: §6+$" + String.format("%,d", totalMoney);
            if (totalXP > 0) {
                message += " §a+" + totalXP + " XP";
            }
            player.displayClientMessage(Component.literal(message), true);
        }
    }
}
