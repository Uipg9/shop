package com.shopmod.events;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.upgrades.UpgradeManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.*; 
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import java.util.Random;

/**
 * Dynamic mob drops system
 * Mobs drop random amounts of money and bonus XP when killed
 */
public class MobEarningsHandler {
    private static final Random random = new Random();
    
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(MobEarningsHandler::onEntityDeath);
    }
    
    /**
     * Called when any entity dies
     */
    public static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
        // Check if killed by player
        if (!(damageSource.getEntity() instanceof ServerPlayer killer)) {
            return;
        }
        if (killer == null) return;
        
        long baseMoney = getMobValue(entity);
        int baseXP = getMobXP(entity);
        
        if (baseMoney <= 0 && baseXP <= 0) return;
        
        // Add randomness (-20% to +40%)
        double randomMultiplier = 0.8 + (random.nextDouble() * 0.6);
        
        // Apply upgrade multipliers
        double incomeMultiplier = UpgradeManager.getIncomeMultiplier(killer.getUUID());
        double xpMultiplier = UpgradeManager.getXPMultiplier(killer.getUUID());
        
        long finalMoney = (long)(baseMoney * randomMultiplier * incomeMultiplier);
        int finalXP = (int)(baseXP * randomMultiplier * xpMultiplier);
        
        // Award money and XP
        if (finalMoney > 0) {
            CurrencyManager.addMoney(killer, finalMoney);
        }
        if (finalXP > 0) {
            killer.giveExperiencePoints(finalXP);
        }
        
        // Show earnings in action bar
        if (finalMoney > 0 || finalXP > 0) {
            String message = "§c" + entity.getName().getString() + ": §6+$" + String.format("%,d", finalMoney);
            if (finalXP > 0) {
                message += " §a+" + finalXP + " XP";
            }
            killer.displayClientMessage(Component.literal(message), true);
        }
    }
    
    /**
     * Get base money value for a mob
     */
    private static long getMobValue(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        
        // Boss mobs
        if (type == EntityType.ENDER_DRAGON) return 50000;
        if (type == EntityType.WITHER) return 25000;
        if (type == EntityType.WARDEN) return 10000;
        if (type == EntityType.ELDER_GUARDIAN) return 5000;
        
        // Hostile mobs (high value)
        if (type == EntityType.BLAZE) return 150;
        if (type == EntityType.ENDERMAN) return 120;
        if (type == EntityType.CREEPER) return 80;
        if (type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.WITHER_SKELETON) return 70;
        if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED) return 60;
        if (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER) return 50;
        if (type == EntityType.WITCH) return 200;
        if (type == EntityType.PHANTOM) return 100;
        if (type == EntityType.PILLAGER || type == EntityType.VINDICATOR) return 150;
        if (type == EntityType.RAVAGER) return 500;
        if (type == EntityType.GHAST) return 200;
        if (type == EntityType.HOGLIN) return 120;
        if (type == EntityType.PIGLIN_BRUTE) return 150;
        if (type == EntityType.PIGLIN) return 80;
        if (type == EntityType.MAGMA_CUBE || type == EntityType.SLIME) {
            if (entity instanceof Slime slime) {
                int size = slime.getSize();
                return 20 * size; // Bigger = more money
            }
        }
        if (type == EntityType.GUARDIAN) return 100;
        if (type == EntityType.SHULKER) return 150;
        
        // Neutral/Passive mobs (low value)
        if (type == EntityType.COW || type == EntityType.MOOSHROOM) return 15;
        if (type == EntityType.PIG) return 12;
        if (type == EntityType.SHEEP) return 13;
        if (type == EntityType.CHICKEN) return 8;
        if (type == EntityType.RABBIT) return 10;
        if (type == EntityType.WOLF) return 25; // Wolves
        if (type == EntityType.IRON_GOLEM) return 0; // Protect villagers!
        if (type == EntityType.SNOW_GOLEM) return 0;
        if (type == EntityType.VILLAGER) return 0; // Never reward killing villagers
        
        // Other animals
        if (type == EntityType.HORSE || type == EntityType.DONKEY) return 30;
        if (type == EntityType.LLAMA) return 25;
        if (type == EntityType.FOX) return 20;
        if (type == EntityType.TURTLE) return 18;
        if (type == EntityType.DOLPHIN) return 0; // Protect dolphins!
        if (type == EntityType.SQUID || type == EntityType.GLOW_SQUID) return 12;
        if (type == EntityType.BAT) return 5;
        if (type == EntityType.PARROT) return 15;
        
        return 10; // Default for unknown mobs
    }
    
    /**
     * Get base XP value for a mob
     */
    private static int getMobXP(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        
        // Boss mobs
        if (type == EntityType.ENDER_DRAGON) return 1000;
        if (type == EntityType.WITHER) return 500;
        if (type == EntityType.WARDEN) return 300;
        if (type == EntityType.ELDER_GUARDIAN) return 150;
        
        // Hostile mobs
        if (type == EntityType.BLAZE) return 10;
        if (type == EntityType.ENDERMAN) return 8;
        if (type == EntityType.CREEPER) return 7;
        if (type == EntityType.SKELETON || type == EntityType.ZOMBIE) return 6;
        if (type == EntityType.SPIDER) return 5;
        if (type == EntityType.WITCH) return 15;
        if (type == EntityType.PHANTOM) return 8;
        if (type == EntityType.PILLAGER) return 12;
        if (type == EntityType.RAVAGER) return 40;
        if (entity instanceof Ghast) return 18;
        
        // Passive mobs give minimal XP
        if (entity instanceof Animal) return 2;
        
        return 3; // Default
    }
}
