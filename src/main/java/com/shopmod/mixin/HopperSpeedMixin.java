package com.shopmod.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Makes hoppers transfer items 8x faster
 */
@Mixin(HopperBlockEntity.class)
public class HopperSpeedMixin {
    
    /**
     * Reduce hopper cooldown from 8 ticks to 1 tick (8x faster)
     */
    @ModifyConstant(method = "tryMoveItems", constant = @Constant(intValue = 8))
    private static int reduceCooldown(int original) {
        return 1; // Change from 8 ticks to 1 tick
    }
}
