package com.shopmod.events;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.wand.SellWandManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Handles sell wand chest interactions
 */
public class WandUseHandler {
    
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            
            ItemStack heldItem = player.getItemInHand(hand);
            
            // Check if holding sell wand
            if (!SellWandManager.isSellWand(heldItem)) {
                return InteractionResult.PASS;
            }
            
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            
            // Check if clicked on a chest
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                return InteractionResult.PASS;
            }
            
            // Get wand data
            SellWandManager.WandData wandData = SellWandManager.getWandData(serverPlayer.getUUID());
            double multiplier = wandData.getSellMultiplier();
            
            // Calculate total value
            long totalValue = 0;
            int itemCount = 0;
            
            // Check all chest slots
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (!stack.isEmpty()) {
                    long value = SellWandManager.calculateSellValue(stack, multiplier);
                    totalValue += value;
                    itemCount += stack.getCount();
                    chest.setItem(i, ItemStack.EMPTY);
                }
            }
            
            if (totalValue > 0) {
                // Add money to player
                CurrencyManager.addMoney(serverPlayer, totalValue);
                
                // Update wand statistics
                wandData.addSold(totalValue);
                wandData.addItemsSold(itemCount);
                
                // Send success message
                serverPlayer.sendSystemMessage(Component.literal(
                    "§a§l[WAND] Sold §e" + itemCount + " items §afor §6$" + 
                    CurrencyManager.format(totalValue) + " §7(+" + 
                    (int)((multiplier - 1) * 100) + "% bonus)"));
                
                // Play sound
                serverPlayer.playSound(
                    net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                    1.0F, 1.0F + (wandData.getLevel() * 0.05F)
                );
                
                chest.setChanged();
            } else {
                serverPlayer.sendSystemMessage(Component.literal("§c§l[WAND] Chest is empty!"));
            }
            
            return InteractionResult.SUCCESS;
        });
    }
}
