package com.shopmod.shop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import java.util.*;

/**
 * Simple enchantment shop system - buy enchanted books with money
 * Organized by tier for progression
 */
public class EnchantmentShop {
    
    public static class EnchantData {
        public final String name;
        public final int level;
        public final long price;
        public final ShopTier tier;
        public final String description;
        
        public EnchantData(String name, int level, long price, ShopTier tier, String description) {
            this.name = name;
            this.level = level;
            this.price = price;
            this.tier = tier;
            this.description = description;
        }
    }
    
    private static final List<EnchantData> ENCHANTMENTS = new ArrayList<>();
    
    static {
        // NOTE: MC 1.21.11 completely redesigned enchantment system
        // Enchantments are now ResourceKey<Enchantment> based, not direct Enchantment objects
        // This simple shop just tracks prices - actual enchantment application needs
        // new API with Holder<Enchantment>, RegistryAccess, etc.
        
        // TIER 0 - STARTER (Basic utility)
        addEnchant("Unbreaking I", 1, 200, ShopTier.STARTER, "Increases item durability");
        addEnchant("Efficiency I", 1, 250, ShopTier.STARTER, "Increases mining speed");
        
        // TIER 1 - FARMER (Basic enchantments)
        addEnchant("Unbreaking II", 2, 500, ShopTier.FARMER, "Better durability");
        addEnchant("Efficiency II", 2, 600, ShopTier.FARMER, "Faster mining");
        addEnchant("Fortune I", 1, 800, ShopTier.FARMER, "More block drops");
        addEnchant("Looting I", 1, 700, ShopTier.FARMER, "More mob drops");
        addEnchant("Feather Falling I", 1, 300, ShopTier.FARMER, "Reduces fall damage");
        
        // TIER 2 - ENGINEER (Intermediate enchantments)
        addEnchant("Unbreaking III", 3, 1200, ShopTier.ENGINEER, "Great durability");
        addEnchant("Efficiency III", 3, 1400, ShopTier.ENGINEER, "Fast mining");
        addEnchant("Fortune II", 2, 2000, ShopTier.ENGINEER, "Better block drops");
        addEnchant("Looting II", 2, 1800, ShopTier.ENGINEER, "Better mob drops");
        addEnchant("Sharpness I", 1, 500, ShopTier.ENGINEER, "Increases melee damage");
        addEnchant("Protection I", 1, 600, ShopTier.ENGINEER, "Reduces damage");
        addEnchant("Power I", 1, 500, ShopTier.ENGINEER, "Increases arrow damage");
        addEnchant("Silk Touch", 1, 2500, ShopTier.ENGINEER, "Mine blocks intact");
        
        // TIER 3 - MERCHANT (Advanced enchantments)
        addEnchant("Fortune III", 3, 5000, ShopTier.MERCHANT, "Maximum block drops");
        addEnchant("Looting III", 3, 4500, ShopTier.MERCHANT, "Maximum mob drops");
        addEnchant("Sharpness II", 2, 1200, ShopTier.MERCHANT, "More melee damage");
        addEnchant("Sharpness III", 3, 2400, ShopTier.MERCHANT, "Great melee damage");
        addEnchant("Protection II", 2, 1500, ShopTier.MERCHANT, "Better protection");
        addEnchant("Protection III", 3, 3000, ShopTier.MERCHANT, "Great protection");
        addEnchant("Power II", 2, 1200, ShopTier.MERCHANT, "More arrow damage");
        addEnchant("Power III", 3, 2400, ShopTier.MERCHANT, "Great arrow damage");
        addEnchant("Fire Aspect I", 1, 2000, ShopTier.MERCHANT, "Sets targets on fire");
        addEnchant("Flame", 1, 2000, ShopTier.MERCHANT, "Flaming arrows");
        addEnchant("Thorns I", 1, 1500, ShopTier.MERCHANT, "Reflects damage");
        
        // TIER 4 - NETHER MASTER (Powerful enchantments)
        addEnchant("Sharpness IV", 4, 4800, ShopTier.NETHER_MASTER, "Excellent melee damage");
        addEnchant("Protection IV", 4, 6000, ShopTier.NETHER_MASTER, "Excellent protection");
        addEnchant("Power IV", 4, 4800, ShopTier.NETHER_MASTER, "Excellent arrow damage");
        addEnchant("Fire Aspect II", 2, 4000, ShopTier.NETHER_MASTER, "More fire damage");
        addEnchant("Thorns II", 2, 3000, ShopTier.NETHER_MASTER, "Better damage reflection");
        addEnchant("Feather Falling IV", 4, 3000, ShopTier.NETHER_MASTER, "Maximum fall protection");
        addEnchant("Depth Strider III", 3, 3500, ShopTier.NETHER_MASTER, "Fast underwater movement");
        addEnchant("Respiration III", 3, 3000, ShopTier.NETHER_MASTER, "Breathe underwater longer");
        addEnchant("Channeling", 1, 5000, ShopTier.NETHER_MASTER, "Summon lightning with trident");
        addEnchant("Riptide I", 1, 3000, ShopTier.NETHER_MASTER, "Throw yourself with trident");
        
        // TIER 5 - ELITE (Top-tier enchantments)
        addEnchant("Sharpness V", 5, 10000, ShopTier.ELITE, "Maximum melee damage");
        addEnchant("Power V", 5, 10000, ShopTier.ELITE, "Maximum arrow damage");
        addEnchant("Thorns III", 3, 6000, ShopTier.ELITE, "Maximum damage reflection");
        addEnchant("Riptide III", 3, 8000, ShopTier.ELITE, "Maximum trident propulsion");
        addEnchant("Loyalty III", 3, 8000, ShopTier.ELITE, "Trident always returns");
        addEnchant("Mending", 1, 15000, ShopTier.ELITE, "Repair with XP - RARE!");
        addEnchant("Infinity", 1, 12000, ShopTier.ELITE, "Unlimited arrows - RARE!");
        addEnchant("Frost Walker II", 2, 10000, ShopTier.ELITE, "Walk on water");
    }
    
    private static void addEnchant(String name, int level, long price, ShopTier tier, String description) {
        ENCHANTMENTS.add(new EnchantData(name, level, price, tier, description));
    }
    
    public static List<EnchantData> getAllEnchantments() {
        return new ArrayList<>(ENCHANTMENTS);
    }
    
    public static List<EnchantData> getEnchantsForTier(ShopTier tier) {
        List<EnchantData> result = new ArrayList<>();
        for (EnchantData data : ENCHANTMENTS) {
            if (data.tier == tier) {
                result.add(data);
            }
        }
        return result;
    }
    
    public static long getPrice(String enchantName) {
        for (EnchantData data : ENCHANTMENTS) {
            if (data.name.equalsIgnoreCase(enchantName)) {
                return data.price;
            }
        }
        return -1; // Not found
    }
    
    public static long getSellPrice(String enchantName) {
        long buyPrice = getPrice(enchantName);
        return buyPrice > 0 ? (long)(buyPrice * 0.8) : 0; // 80% sell-back
    }
    
    /**
     * Creates an enchanted book with actual enchantment data (requires RegistryAccess)
     */
    public static ItemStack createEnchantedBook(String enchantName, HolderLookup.Provider registries) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        
        // Find the enchantment data
        EnchantData data = null;
        for (EnchantData e : ENCHANTMENTS) {
            if (e.name.equalsIgnoreCase(enchantName)) {
                data = e;
                break;
            }
        }
        
        if (data == null) {
            return book;
        }
        
        try {
            // Get the ResourceKey for the enchantment
            ResourceKey<Enchantment> enchantKey = getEnchantmentKey(data.name);
            
            if (enchantKey != null && registries != null) {
                // Get the enchantment registry
                HolderLookup.RegistryLookup<Enchantment> enchantRegistry = registries.lookupOrThrow(Registries.ENCHANTMENT);
                
                // Get the Holder for this enchantment
                Optional<Holder.Reference<Enchantment>> enchantHolder = enchantRegistry.get(enchantKey);
                
                if (enchantHolder.isPresent()) {
                    // Create ItemEnchantments and add the enchantment
                    ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                    enchantments.set(enchantHolder.get(), data.level);
                    
                    // Apply to book using STORED_ENCHANTMENTS
                    book.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
                } else {
                    System.out.println("[Shop] Enchantment holder not present for: " + data.name);
                }
            } else {
                System.out.println("[Shop] Enchantment key null or no registries for: " + data.name);
            }
        } catch (Exception e) {
            System.out.println("[Shop] Failed to create enchantment book for " + data.name + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        // Set display name (name already includes level like "Unbreaking II")
        book.set(DataComponents.CUSTOM_NAME, 
            Component.literal("§d" + data.name));
        
        return book;
    }
    
    /**
     * Creates an enchanted book item with display name (fallback without RegistryAccess)
     */
    public static ItemStack createEnchantedBook(String enchantName) {
        return createEnchantedBook(enchantName, null);
    }
    
    /**
     * Map enchantment name to ResourceKey
     */
    private static ResourceKey<Enchantment> getEnchantmentKey(String name) {
        // Strip roman numerals from END of name only (not from middle like "Unbreaking")
        String cleanName = name.toLowerCase()
            .replaceAll(" (i|ii|iii|iv|v)$", "")  // Remove roman numerals at end
            .replace(" ", "_")
            .trim();
        
        return switch(cleanName) {
            case "unbreaking" -> Enchantments.UNBREAKING;
            case "efficiency" -> Enchantments.EFFICIENCY;
            case "fortune" -> Enchantments.FORTUNE;
            case "silk_touch" -> Enchantments.SILK_TOUCH;
            case "sharpness" -> Enchantments.SHARPNESS;
            case "looting" -> Enchantments.LOOTING;
            case "protection" -> Enchantments.PROTECTION;
            case "feather_falling" -> Enchantments.FEATHER_FALLING;
            case "power" -> Enchantments.POWER;
            case "fire_aspect" -> Enchantments.FIRE_ASPECT;
            case "flame" -> Enchantments.FLAME;
            case "thorns" -> Enchantments.THORNS;
            case "depth_strider" -> Enchantments.DEPTH_STRIDER;
            case "respiration" -> Enchantments.RESPIRATION;
            case "channeling" -> Enchantments.CHANNELING;
            case "riptide" -> Enchantments.RIPTIDE;
            case "loyalty" -> Enchantments.LOYALTY;
            case "mending" -> Enchantments.MENDING;
            case "infinity" -> Enchantments.INFINITY;
            case "frost_walker" -> Enchantments.FROST_WALKER;
            default -> null;
        };
    }
    
    /**
     * Get enchantment holder - DISABLED for MC 1.21.11 API research
     */
    private static Object getEnchantmentHolder(String name) {
        // Enchantment registry system changed in MC 1.21.11
        // Need to research: Holder<Enchantment>, ResourceKey, RegistryAccess
        return null;
    }
    
    /**
     * Convert level to roman numerals
     */
    private static String getRomanNumeral(int level) {
        return switch(level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
    
    /**
     * Apply enchantment from book to target item
     * Now properly transfers enchantments using MC 1.21.11 DataComponents API
     */
    public static boolean applyEnchantmentToItem(ItemStack target, ItemStack book) {
        if (target.isEmpty() || book.isEmpty()) {
            return false;
        }
        
        if (!book.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        
        try {
            // Get enchantments from book (STORED_ENCHANTMENTS for books)
            ItemEnchantments bookEnchants = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            
            if (!bookEnchants.isEmpty()) {
                // Get current enchantments on target (ENCHANTMENTS for items)
                ItemEnchantments targetEnchants = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(targetEnchants);
                
                // Transfer all enchantments from book to item
                bookEnchants.entrySet().forEach(entry -> {
                    mutable.set(entry.getKey(), entry.getIntValue());
                });
                
                // Apply enchantments to target
                target.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                return true;
            }
        } catch (Exception e) {
            // Failed to apply enchantments
        }
        
        return false;
    }
    
    /**
     * Get a formatted display string for the shop GUI
     */
    public static String getDisplayText(EnchantData data) {
        return String.format("%s%s §7- §6$%,d §7- %s", 
            data.tier.getColor(), 
            data.name, 
            data.price, 
            data.description
        );
    }
}
