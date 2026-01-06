package com.shopmod.gui;

import com.shopmod.ShopMod;
import com.shopmod.currency.CurrencyManager;
import com.shopmod.economy.PriceFluctuation;
import com.shopmod.crates.LuckyCrateManager;
import com.shopmod.bank.BankManager;
import com.shopmod.shop.*;
import com.shopmod.upgrades.UpgradeManager;
import com.shopmod.upgrades.UpgradeType;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * Comprehensive categorized shop GUI
 * Categories: Food, Ores, Building Blocks, Tools, Combat, Farming, Redstone, Decorative, Spawners, Enchants
 */
public class ShopGui extends SimpleGui {
    private final ServerPlayer player;
    private Category currentCategory = Category.FOOD;
    private int page = 0;
    private BuyQuantity buyQuantity = BuyQuantity.ONE;
    private SellQuantity sellQuantity = SellQuantity.ALL;
    private boolean viewingCategories = true; // Track if showing category selection - start with categories
    private static final int SELL_BOX_SLOT = 46; // Bottom-left corner (next to prev page button)
    private static final int BACK_BUTTON_SLOT = 0; // Top-left corner when viewing items
    
    private enum BuyQuantity {
        ONE(1, "1x"),
        SIXTEEN(16, "16x"),
        SIXTYFOUR(64, "64x"),
        ONEHUNDREDTWENTYEIGHT(128, "128x"),
        MAX(Integer.MAX_VALUE, "Max");
        
        final int amount;
        final String display;
        
        BuyQuantity(int amount, String display) {
            this.amount = amount;
            this.display = display;
        }
        
        BuyQuantity next() {
            BuyQuantity[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
    
    private enum SellQuantity {
        ONE(1, "1x"),
        SIXTEEN(16, "16x"),
        SIXTYFOUR(64, "64x"),
        ONEHUNDREDTWENTYEIGHT(128, "128x"),
        ALL(Integer.MAX_VALUE, "All");
        
        final int amount;
        final String display;
        
        SellQuantity(int amount, String display) {
            this.amount = amount;
            this.display = display;
        }
        
        SellQuantity next() {
            SellQuantity[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
    
    private enum Category {
        FOOD("Food & Consumables", Items.COOKED_BEEF),
        ORES("Ores & Minerals", Items.DIAMOND),
        BLOCKS("Building Blocks", Items.STONE_BRICKS),
        TOOLS("Tools & Armor", Items.DIAMOND_PICKAXE),
        COMBAT("Combat & Weapons", Items.DIAMOND_SWORD),
        FARMING("Farming & Seeds", Items.WHEAT_SEEDS),
        POTIONS("Potions & Brewing", Items.POTION),
        REDSTONE("Redstone & Tech", Items.REDSTONE),
        DECORATIVE("Decorative Items", Items.FLOWER_POT),
        NETHER("Nether Items", Items.NETHER_BRICK),
        END("End Items", Items.END_STONE),
        OCEAN("Ocean Items", Items.PRISMARINE),
        SPAWNERS("Mob Spawners", Items.SPAWNER),
        ENCHANTS("Enchantments", Items.ENCHANTED_BOOK),
        LUCKY_CRATES("Lucky Crates", Items.CHEST),
        UPGRADES("Upgrades", Items.EXPERIENCE_BOTTLE),
        TIERS("Tier System", Items.NETHER_STAR);
        
        final String displayName;
        final Item icon;
        
        Category(String displayName, Item icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
    }
    
    public ShopGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("Shop - " + currentCategory.displayName));
        
        // Enable inventory interaction for selling - inventory IS accessible for selling
        this.setAutoUpdate(true);
        this.setLockPlayerInventory(false);
        
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear all slots
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        if (viewingCategories) {
            // Show category selector (top 2 rows)
            setupCategoryButtons();
            // Show welcome message
            setSlot(22, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("§6§l✦ SHOP MENU ✦"))
                .addLoreLine(Component.literal("§7Select a category to browse"))
            );
        } else {
            // Show back button when viewing items
            setSlot(BACK_BUTTON_SLOT, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§l← BACK"))
                .addLoreLine(Component.literal("§7Return to categories"))
                .setCallback((index, type, action) -> {
                    viewingCategories = true;
                    page = 0;
                    this.setTitle(Component.literal("Shop - Categories"));
                    updateDisplay();
                })
            );
            
            // Display current category content
            displayCurrentCategory();
        }
        
        // Balance display (bottom right corner) - always show
        setSlot(53, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance: " + CurrencyManager.format(CurrencyManager.getBalance(player))))
            .addLoreLine(Component.literal("§7Your current money"))
        );
        
        // Sell box (bottom left) - show when viewing items
        if (!viewingCategories) {
            setupSellBox();
        }
        
        // Buy quantity selector (bottom middle) - show when viewing items
        if (!viewingCategories) {
            setupBuyQuantitySelector();
        }
    }
    
    private void displayCurrentCategory() {
        switch (currentCategory) {
            case FOOD -> setupFoodCategory();
            case ORES -> setupOresCategory();
            case BLOCKS -> setupBlocksCategory();
            case TOOLS -> setupToolsCategory();
            case COMBAT -> setupCombatCategory();
            case FARMING -> setupFarmingCategory();
            case POTIONS -> setupPotionsCategory();
            case REDSTONE -> setupRedstoneCategory();
            case DECORATIVE -> setupDecorativeCategory();
            case NETHER -> setupNetherCategory();
            case END -> setupEndCategory();
            case OCEAN -> setupOceanCategory();
            case SPAWNERS -> setupSpawnersCategory();
            case ENCHANTS -> setupEnchantsCategory();
            case LUCKY_CRATES -> setupLuckyCratesCategory();
            case UPGRADES -> setupUpgradesCategory();
            case TIERS -> setupTiersTab();
        }
    }
    
    private void setupSellBox() {
        setSlot(SELL_BOX_SLOT, new GuiElementBuilder(Items.GOLD_NUGGET)
            .setName(Component.literal("§6§lSell Quantity: §f" + sellQuantity.display))
            .addLoreLine(Component.literal("§7Click to cycle"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Modes:"))
            .addLoreLine(Component.literal("§f• 1x §7- Sell 1 item"))
            .addLoreLine(Component.literal("§f• 16x §7- Sell 16 items"))
            .addLoreLine(Component.literal("§f• 64x §7- Sell 64 items (stack)"))
            .addLoreLine(Component.literal("§f• 128x §7- Sell 128 items (2 stacks)"))
            .addLoreLine(Component.literal("§f• All §7- Sell everything you have"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§eRight-Click inventory items to sell!"))
            .setCallback((index, type, action) -> {
                sellQuantity = sellQuantity.next();
                updateDisplay();
            })
        );
    }
    
    private void setupBuyQuantitySelector() {
        setSlot(49, new GuiElementBuilder(Items.HOPPER)
            .setName(Component.literal("§b§lBuy Quantity: §f" + buyQuantity.display))
            .addLoreLine(Component.literal("§7Click to cycle"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Modes:"))
            .addLoreLine(Component.literal("§f• 1x §7- Buy 1 item"))
            .addLoreLine(Component.literal("§f• 16x §7- Buy 16 items"))
            .addLoreLine(Component.literal("§f• 64x §7- Buy 64 items (stack)"))
            .addLoreLine(Component.literal("§f• 128x §7- Buy 128 items (2 stacks)"))
            .addLoreLine(Component.literal("§f• Max §7- Buy as many as you can afford"))
            .setCallback((index, type, action) -> {
                buyQuantity = buyQuantity.next();
                updateDisplay();
            })
        );
    }
    
    private void setupCategoryButtons() {
        Category[] categories = Category.values();
        // Use first 2 rows for categories (18 slots total)
        for (int i = 0; i < Math.min(categories.length, 18); i++) {
            Category cat = categories[i];
            
            GuiElementBuilder builder = new GuiElementBuilder(cat.icon)
                .setName(Component.literal("§7" + cat.displayName))
                .addLoreLine(Component.literal("§7Click to view"));
            
            final Category targetCategory = cat;
            builder.setCallback((index, type, action) -> {
                currentCategory = targetCategory;
                viewingCategories = false;
                page = 0;
                this.setTitle(Component.literal("Shop - " + currentCategory.displayName));
                updateDisplay();
            });
            
            setSlot(i, builder);
        }
        
        // Add bottom navigation buttons (only on categories page)
        // Slot 45: Bank
        setSlot(45, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§6§lBank"))
            .addLoreLine(Component.literal("§7Click to open bank"))
            .addLoreLine(Component.literal("§7Store items and invest money!"))
            .setCallback((index, type, action) -> {
                this.close();
                new BankGui(player).open();
            })
        );
        
        // Slot 46: Anvil
        setSlot(46, new GuiElementBuilder(Items.ANVIL)
            .setName(Component.literal("§f§lAnvil"))
            .addLoreLine(Component.literal("§7Click to open anvil"))
            .addLoreLine(Component.literal("§7Repair and rename items!"))
            .setCallback((index, type, action) -> {
                this.close();
                // Open anvil menu (will implement command later)
                player.sendSystemMessage(Component.literal("§eAnvil menu coming soon! Use /anvil"));
            })
        );
        
        // Slot 49: Market Info
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§b§lMarket Info"))
            .addLoreLine(Component.literal("§7Stock Market: §a Active"))
            .addLoreLine(Component.literal("§7Prices update every hour"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Lucky Crates: §a Available"))
            .addLoreLine(Component.literal("§7New crates daily!"))
        );
        
        // Slot 52: Refresh Shop
        setSlot(52, new GuiElementBuilder(Items.COMPASS)
            .setName(Component.literal("§e§lRefresh Shop"))
            .addLoreLine(Component.literal("§7Click to refresh display"))
            .setCallback((index, type, action) -> {
                updateDisplay();
                player.sendSystemMessage(Component.literal("§aShop refreshed!"));
            })
        );
    }
    
    private void setupFoodCategory() {
        List<Item> items = new ArrayList<>();
        // Collect all food items from ItemPricing
        for (Map.Entry<Item, ItemPricing.PriceData> entry : ItemPricing.getAllPrices().entrySet()) {
            Item item = entry.getKey();
            if (isFoodItem(item)) {
                items.add(item);
            }
        }
        displayItemGrid(items, 1);
    }
    
    private void setupOresCategory() {
        List<Item> items = Arrays.asList(
            Items.COAL, Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE,
            Items.IRON_INGOT, Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON,
            Items.COPPER_INGOT, Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER,
            Items.GOLD_INGOT, Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD, Items.NETHER_GOLD_ORE,
            Items.REDSTONE, Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE,
            Items.LAPIS_LAZULI, Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE,
            Items.DIAMOND, Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE,
            Items.EMERALD, Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE,
            Items.QUARTZ, Items.NETHER_QUARTZ_ORE,
            Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP, Items.ANCIENT_DEBRIS,
            Items.AMETHYST_SHARD
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupBlocksCategory() {
        List<Item> items = Arrays.asList(
            Items.STONE, Items.COBBLESTONE, Items.STONE_BRICKS, Items.SMOOTH_STONE,
            Items.DIRT, Items.GRASS_BLOCK, Items.COARSE_DIRT, Items.PODZOL,
            Items.SAND, Items.RED_SAND, Items.SANDSTONE, Items.RED_SANDSTONE,
            Items.GRAVEL, Items.CLAY, Items.TERRACOTTA,
            Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
            Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS,
            Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG,
            Items.GLASS, Items.WHITE_STAINED_GLASS, Items.BLACK_STAINED_GLASS,
            Items.BRICKS, Items.NETHER_BRICKS, Items.END_STONE_BRICKS,
            Items.QUARTZ_BLOCK, Items.PURPUR_BLOCK, Items.PRISMARINE,
            Items.OBSIDIAN, Items.CRYING_OBSIDIAN, Items.NETHERRACK, Items.BASALT,
            Items.GLOWSTONE, Items.SEA_LANTERN, Items.REDSTONE_LAMP
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupToolsCategory() {
        List<Item> items = Arrays.asList(
            Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE, Items.WOODEN_SWORD,
            Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE, Items.STONE_SWORD,
            Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_SWORD,
            Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE, Items.GOLDEN_SWORD,
            Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_SWORD,
            Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.NETHERITE_SWORD,
            Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.SHIELD,
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
            Items.ELYTRA, Items.SHEARS, Items.FISHING_ROD, Items.FLINT_AND_STEEL
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupFarmingCategory() {
        List<Item> items = Arrays.asList(
            Items.WHEAT_SEEDS, Items.WHEAT, Items.HAY_BLOCK,
            Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS, Items.BEETROOT,
            Items.MELON_SEEDS, Items.MELON_SLICE, Items.MELON,
            Items.PUMPKIN_SEEDS, Items.PUMPKIN, Items.CARVED_PUMPKIN,
            Items.SUGAR_CANE, Items.SUGAR, Items.PAPER,
            Items.COCOA_BEANS, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM,
            Items.BONE_MEAL, Items.COMPOSTER, Items.FARMLAND,
            Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING,
            Items.MANGROVE_PROPAGULE, Items.CHERRY_SAPLING, Items.AZALEA,
            Items.EGG, Items.MILK_BUCKET, Items.HONEY_BOTTLE, Items.HONEYCOMB
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupRedstoneCategory() {
        List<Item> items = Arrays.asList(
            Items.REDSTONE, Items.REDSTONE_TORCH, Items.REDSTONE_BLOCK,
            Items.REPEATER, Items.COMPARATOR, Items.OBSERVER,
            Items.PISTON, Items.STICKY_PISTON, Items.DISPENSER, Items.DROPPER,
            Items.HOPPER, Items.CHEST, Items.BARREL, Items.FURNACE,
            Items.LEVER, Items.STONE_BUTTON, Items.OAK_BUTTON, Items.OAK_PRESSURE_PLATE,
            Items.TRIPWIRE_HOOK, Items.TNT, Items.NOTE_BLOCK,
            Items.DAYLIGHT_DETECTOR, Items.LIGHTNING_ROD, Items.TARGET,
            Items.RAIL, Items.POWERED_RAIL, Items.DETECTOR_RAIL, Items.ACTIVATOR_RAIL,
            Items.MINECART, Items.CHEST_MINECART, Items.HOPPER_MINECART, Items.TNT_MINECART
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupDecorativeCategory() {
        List<Item> items = Arrays.asList(
            Items.FLOWER_POT, Items.PAINTING, Items.ITEM_FRAME, Items.ARMOR_STAND,
            Items.TORCH, Items.SOUL_TORCH, Items.LANTERN, Items.SOUL_LANTERN,
            Items.CANDLE, Items.CAMPFIRE, Items.SOUL_CAMPFIRE,
            Items.WHITE_CARPET, Items.WHITE_BANNER, Items.WHITE_BED,
            Items.BOOKSHELF, Items.LECTERN, Items.ENCHANTING_TABLE,
            Items.CRAFTING_TABLE, Items.SMITHING_TABLE, Items.BREWING_STAND,
            Items.ANVIL, Items.GRINDSTONE, Items.LOOM, Items.STONECUTTER,
            Items.LADDER, Items.SCAFFOLDING,
            Items.BELL, Items.BEACON, Items.CONDUIT,
            Items.POPPY, Items.DANDELION, Items.BLUE_ORCHID, Items.ALLIUM,
            Items.SUNFLOWER, Items.ROSE_BUSH, Items.PEONY, Items.LILY_PAD,
            Items.VINE, Items.GLOW_LICHEN, Items.MOSS_BLOCK, Items.MOSS_CARPET
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupSpawnersCategory() {
        List<SpawnerPricing.SpawnEggData> spawnEggs = SpawnerPricing.getAllSpawnEggs();
        
        // Add base spawner at slot 1 (top left after back button at 0)
        addBaseSpawnerItem(1);
        
        // Pagination for spawn eggs - 44 slots total minus 1 for base spawner = 43 eggs per page
        int itemsPerPage = 43;
        int totalPages = Math.max(1, (spawnEggs.size() + itemsPerPage - 1) / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, spawnEggs.size());
        
        // Display spawn eggs starting from slot 2 (next to base spawner)
        int slot = 2;
        for (int i = startIndex; i < endIndex && slot < 45; i++) {
            SpawnerPricing.SpawnEggData egg = spawnEggs.get(i);
            addSpawnEggItem(slot++, egg);
        }
        
        // Page navigation
        if (page > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Previous Page"))
                .setCallback((index, type, action) -> {
                    page--;
                    updateDisplay();
                })
            );
        }
        
        if (page < totalPages - 1) {
            setSlot(52, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eNext Page →"))
                .setCallback((index, type, action) -> {
                    page++;
                    updateDisplay();
                })
            );
        }
    }
    
    private void addBaseSpawnerItem(int slot) {
        long price = SpawnerPricing.BASE_SPAWNER_PRICE;
        boolean canAfford = CurrencyManager.canAfford(player, price);
        boolean tierUnlocked = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), ShopTier.FARMER.getId());
        
        GuiElementBuilder builder = new GuiElementBuilder(Items.SPAWNER)
            .setName(Component.literal((tierUnlocked ? "§e" : "§8§o") + SpawnerPricing.BASE_SPAWNER_NAME))
            .addLoreLine(Component.literal((tierUnlocked ? "§6" : "§8") + "Price: " + CurrencyManager.format(price)))
            .addLoreLine(Component.literal((tierUnlocked ? "§7" : "§8") + "Tier: " + ShopTier.FARMER.getColor() + ShopTier.FARMER.getName()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal((tierUnlocked ? "" : "§8") + SpawnerPricing.BASE_SPAWNER_DESC))
            .addLoreLine(Component.literal((tierUnlocked ? "§8" : "§8§o") + "Buy spawn eggs to configure"));
        
        if (!tierUnlocked) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c§l✗ LOCKED"))
                .addLoreLine(Component.literal("§7Unlocked at " + ShopTier.FARMER.getColor() + ShopTier.FARMER.getName()))
                .glow();
        } else if (!canAfford) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cInsufficient funds!"));
        } else {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aClick to purchase!"))
                .setCallback((index, type, action) -> buyBaseSpawner());
        }
        
        setSlot(slot, builder);
    }
    
    private void addSpawnEggItem(int slot, SpawnerPricing.SpawnEggData egg) {
        boolean canAfford = CurrencyManager.canAfford(player, egg.price);
        boolean tierUnlocked = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), egg.tier.getId());
        
        // Get the spawn egg item icon
        net.minecraft.world.item.Item eggItem = net.minecraft.world.item.SpawnEggItem.byId(egg.entityType);
        if (eggItem == null) eggItem = Items.EGG;
        
        GuiElementBuilder builder = new GuiElementBuilder(eggItem)
            .setName(Component.literal((tierUnlocked ? "§a" : "§8§o") + egg.displayName))
            .addLoreLine(Component.literal((tierUnlocked ? "§6" : "§8") + "Price: " + CurrencyManager.format(egg.price)))
            .addLoreLine(Component.literal((tierUnlocked ? "§7" : "§8") + "Tier: " + egg.tier.getColor() + egg.tier.getName()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal((tierUnlocked ? "" : "§8") + egg.description));
        
        if (!tierUnlocked) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c§l✗ LOCKED"))
                .addLoreLine(Component.literal("§7Unlocked at " + egg.tier.getColor() + egg.tier.getName()))
                .glow();
        } else if (!canAfford) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cInsufficient funds!"));
        } else {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aClick to purchase!"))
                .setCallback((index, type, action) -> buySpawnEgg(egg));
        }
        
        setSlot(slot, builder);
    }
    
    private void buyBaseSpawner() {
        long price = SpawnerPricing.BASE_SPAWNER_PRICE;
        
        if (!ShopMod.dataManager.hasTierUnlocked(player.getUUID(), ShopTier.FARMER.getId())) {
            player.sendSystemMessage(Component.literal("§cYou need " + ShopTier.FARMER.getColor() + 
                ShopTier.FARMER.getName() + " §ctier to buy this!"));
            return;
        }
        
        if (!CurrencyManager.canAfford(player, price)) {
            CurrencyManager.sendInsufficientFundsMessage(player, price);
            return;
        }
        
        if (CurrencyManager.removeMoney(player, price)) {
            ItemStack spawnerItem = SpawnerPricing.createBaseSpawner();
            player.addItem(spawnerItem);
            CurrencyManager.sendMoneySpentMessage(player, price, "Purchased " + SpawnerPricing.BASE_SPAWNER_NAME);
            updateDisplay();
        }
    }
    
    private void buySpawnEgg(SpawnerPricing.SpawnEggData egg) {
        if (!ShopMod.dataManager.hasTierUnlocked(player.getUUID(), egg.tier.getId())) {
            player.sendSystemMessage(Component.literal("§cYou need " + egg.tier.getColor() + 
                egg.tier.getName() + " §ctier to buy this!"));
            return;
        }
        
        if (!CurrencyManager.canAfford(player, egg.price)) {
            CurrencyManager.sendInsufficientFundsMessage(player, egg.price);
            return;
        }
        
        if (CurrencyManager.removeMoney(player, egg.price)) {
            ItemStack eggItem = SpawnerPricing.createSpawnEggItem(egg.entityType);
            player.addItem(eggItem);
            CurrencyManager.sendMoneySpentMessage(player, egg.price, "Purchased " + egg.displayName);
            updateDisplay();
        }
    }
    
    private void setupEnchantsCategory() {
        List<EnchantmentShop.EnchantData> enchants = EnchantmentShop.getAllEnchantments();
        
        // Debug: Print enchantment count
        ShopMod.LOGGER.info("Enchantments loaded: " + enchants.size());
        
        // Pagination - use all 44 slots (1-44)
        int itemsPerPage = 44;
        int totalPages = Math.max(1, (enchants.size() + itemsPerPage - 1) / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, enchants.size());
        
        ShopMod.LOGGER.info("Page " + (page + 1) + " of " + totalPages + " (showing " + startIndex + "-" + endIndex + ")");
        
        // Display enchantments starting at slot 1
        int slot = 1;
        for (int i = startIndex; i < endIndex && slot < 45; i++) {
            EnchantmentShop.EnchantData enchant = enchants.get(i);
            addEnchantItem(slot++, enchant);
        }
        
        // Page navigation
        if (page > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Previous Page"))
                .addLoreLine(Component.literal("§7Page " + (page + 1) + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    page--;
                    updateDisplay();
                })
            );
        }
        
        if (page < totalPages - 1) {
            setSlot(52, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eNext Page →"))
                .addLoreLine(Component.literal("§7Page " + (page + 2) + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    page++;
                    updateDisplay();
                })
            );
        }
    }
    
    private void addEnchantItem(int slot, EnchantmentShop.EnchantData enchant) {
        boolean canAfford = CurrencyManager.canAfford(player, enchant.price);
        boolean tierUnlocked = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), enchant.tier.getId());
        
        GuiElementBuilder builder = new GuiElementBuilder(Items.ENCHANTED_BOOK)
            .setName(Component.literal((tierUnlocked ? "§d" : "§7") + enchant.name + " " + enchant.level))
            .addLoreLine(Component.literal("§6Price: " + CurrencyManager.format(enchant.price)))
            .addLoreLine(Component.literal("§7Tier: " + enchant.tier.getColor() + enchant.tier.getName()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7" + enchant.description));
        
        if (!tierUnlocked) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c✗ Unlocked at " + enchant.tier.getColor() + enchant.tier.getName()));
        } else if (!canAfford) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cInsufficient funds!"));
        } else {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aClick to purchase!"))
                .setCallback((index, type, action) -> buyEnchantment(enchant));
        }
        
        setSlot(slot, builder);
    }
    
    private void buyEnchantment(EnchantmentShop.EnchantData enchant) {
        if (!ShopMod.dataManager.hasTierUnlocked(player.getUUID(), enchant.tier.getId())) {
            player.sendSystemMessage(Component.literal("§cYou need " + enchant.tier.getColor() + 
                enchant.tier.getName() + " §ctier to buy this!"));
            return;
        }
        
        if (!CurrencyManager.canAfford(player, enchant.price)) {
            CurrencyManager.sendInsufficientFundsMessage(player, enchant.price);
            return;
        }
        
        if (CurrencyManager.removeMoney(player, enchant.price)) {
            ItemStack book = EnchantmentShop.createEnchantedBook(enchant.name, player.level().registryAccess());
            player.addItem(book);
            CurrencyManager.sendMoneySpentMessage(player, enchant.price, "Purchased " + enchant.name + " " + enchant.level);
            updateDisplay();
        }
    }
    
    private void setupLuckyCratesCategory() {
        // Get today's crates
        long dayNumber = player.level().getDayTime() / 24000;
        LuckyCrateManager.updateDailyCrates(dayNumber);
        List<LuckyCrateManager.CrateType> crates = LuckyCrateManager.getAvailableCrates();
        
        // Display info in center
        setSlot(13, new GuiElementBuilder(Items.CHEST)
            .setName(Component.literal("§6§l✦ LUCKY CRATES ✦"))
            .addLoreLine(Component.literal("§7Test your luck!"))
            .addLoreLine(Component.literal("§7New crates every day!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current Day: §e" + dayNumber))
            .addLoreLine(Component.literal("§7Available Crates: §a" + crates.size()))
        );
        
        // Display all 5 crates
        int[] slots = {19, 21, 23, 25, 31}; // Spread across GUI
        for (int i = 0; i < Math.min(crates.size(), slots.length); i++) {
            LuckyCrateManager.CrateType crate = crates.get(i);
            addCrateItem(slots[i], crate);
        }
        
        // Daily reset info
        long nextDayTicks = ((dayNumber + 1) * 24000) - player.level().getDayTime();
        long nextDayMinutes = (nextDayTicks / 20) / 60; // Convert ticks to minutes
        
        setSlot(40, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal("§e⏰ Next Update"))
            .addLoreLine(Component.literal("§7Time until new crates:"))
            .addLoreLine(Component.literal("§a" + nextDayMinutes + " minutes"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Crates refresh at dawn!"))
        );
    }
    
    private void addCrateItem(int slot, LuckyCrateManager.CrateType crate) {
        boolean canAfford = CurrencyManager.canAfford(player, crate.cost);
        
        // Choose item based on rarity
        Item displayItem = switch(crate.rarity) {
            case "§f" -> Items.WHITE_SHULKER_BOX;
            case "§a" -> Items.LIME_SHULKER_BOX;
            case "§9" -> Items.LIGHT_BLUE_SHULKER_BOX;
            case "§5" -> Items.PURPLE_SHULKER_BOX;
            case "§6" -> Items.ORANGE_SHULKER_BOX;
            default -> Items.CHEST;
        };
        
        GuiElementBuilder builder = new GuiElementBuilder(displayItem)
            .setName(Component.literal(crate.rarity + "§l" + crate.name + " CRATE"))
            .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(crate.cost)))
            .addLoreLine(Component.literal("§7Win Chance: §a" + String.format("%.1f", crate.winChance * 100) + "%"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Possible Reward:"))
            .addLoreLine(Component.literal("§6" + CurrencyManager.format(crate.minReward) + " §7- §6" + CurrencyManager.format(crate.maxReward)))
            .addLoreLine(Component.literal(""))
            .glow();
        
        if (!canAfford) {
            builder.addLoreLine(Component.literal("§c§lCAN'T AFFORD!"));
        } else {
            builder.addLoreLine(Component.literal("§a§lCLICK TO ROLL!"))
                .setCallback((index, type, action) -> rollCrate(crate));
        }
        
        setSlot(slot, builder);
    }
    
    private void rollCrate(LuckyCrateManager.CrateType crate) {
        if (!CurrencyManager.canAfford(player, crate.cost)) {
            CurrencyManager.sendInsufficientFundsMessage(player, crate.cost);
            return;
        }
        
        if (!CurrencyManager.removeMoney(player, crate.cost)) {
            return;
        }
        
        long winnings = crate.rollReward();
        
        if (winnings > 0) {
            CurrencyManager.addMoney(player, winnings);
            player.sendSystemMessage(Component.literal("§a§l✦ YOU WON! §r§a+" + CurrencyManager.format(winnings) + "!"));
            // TODO: Add sound effect
        } else {
            player.sendSystemMessage(Component.literal("§c§l✗ Better luck next time!"));
            // TODO: Add sound effect
        }
        
        updateDisplay();
    }
    
    private void setupTiersTab() {
        // Display all tiers with unlock status
        setSlot(22, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§e§lTier Progression System"))
            .addLoreLine(Component.literal("§7Unlock tiers to access more items"))
            .addLoreLine(Component.literal("§7Use /shop unlock <tier> to unlock"))
        );
        
        // Find next tier to unlock and show progress
        ShopTier nextTier = null;
        long currentBalance = CurrencyManager.getBalance(player);
        for (ShopTier tier : ShopTier.values()) {
            if (!ShopMod.dataManager.hasTierUnlocked(player.getUUID(), tier.getId()) && tier.getId() > 0) {
                // Check if previous tier is unlocked
                ShopTier prevTier = ShopTier.getById(tier.getId() - 1);
                if (ShopMod.dataManager.hasTierUnlocked(player.getUUID(), prevTier.getId())) {
                    nextTier = tier;
                    break;
                }
            }
        }
        
        // Show progress bar to next tier
        if (nextTier != null) {
            long cost = nextTier.getUnlockCost();
            double progress = Math.min(1.0, (double) currentBalance / cost);
            int progressPercent = (int) (progress * 100);
            
            // Progress bar with barrier blocks (red) and lime wool (green)
            int barStartSlot = 19;
            int barLength = 7;
            for (int i = 0; i < barLength; i++) {
                int filledBars = (int) (progress * barLength);
                boolean filled = i < filledBars;
                
                setSlot(barStartSlot + i, new GuiElementBuilder(filled ? Items.LIME_WOOL : Items.RED_WOOL)
                    .setName(Component.literal(filled ? "§a■" : "§c■"))
                    .addLoreLine(Component.literal("§7Progress to §f" + nextTier.getColor() + nextTier.getName()))
                    .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(cost)))
                    .addLoreLine(Component.literal("§7You have: §6" + CurrencyManager.format(currentBalance)))
                    .addLoreLine(Component.literal("§7Progress: §e" + progressPercent + "%"))
                );
            }
        }
        
        int slot = 28; // Start in middle
        for (ShopTier tier : ShopTier.values()) {
            boolean unlocked = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), tier.getId());
            
            GuiElementBuilder builder = new GuiElementBuilder(unlocked ? Items.LIME_DYE : Items.GRAY_DYE)
                .setName(Component.literal(tier.getColor() + tier.getName()))
                .addLoreLine(Component.literal(unlocked ? "§a✓ Unlocked" : "§c✗ Locked"))
                .addLoreLine(Component.literal("§7Cost: " + (tier.getUnlockCost() == 0 ? "FREE" : CurrencyManager.format(tier.getUnlockCost()))))
                .addLoreLine(Component.literal(""));
            
            if (!unlocked && tier.getId() > 0) {
                ShopTier prevTier = ShopTier.getById(tier.getId() - 1);
                boolean canUnlock = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), prevTier.getId());
                
                if (canUnlock) {
                    boolean canAfford = CurrencyManager.canAfford(player, tier.getUnlockCost());
                    builder.addLoreLine(Component.literal(canAfford ? "§a§lCLICK TO UNLOCK!" : "§cInsufficient funds"))
                        .setCallback((index, type, action) -> {
                            if (CurrencyManager.canAfford(player, tier.getUnlockCost())) {
                                if (CurrencyManager.removeMoney(player, tier.getUnlockCost())) {
                                    ShopMod.dataManager.unlockTier(player.getUUID(), tier.getId());
                                    player.sendSystemMessage(Component.literal("§a✓ Unlocked " + tier.getColor() + tier.getName() + " §atier!"));
                                    updateDisplay();
                                }
                            } else {
                                CurrencyManager.sendInsufficientFundsMessage(player, tier.getUnlockCost());
                            }
                        });
                } else {
                    builder.addLoreLine(Component.literal("§cMust unlock " + prevTier.getName() + " first"));
                }
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayItemGrid(List<Item> items, int startSlot) {
        // Pagination - now we have slots 1-44 available (44 slots per page)
        // Skip slots: 0 (back), 45-48 (navigation/sell), 49 (buy qty), 50-52 (empty/nav), 53 (balance)
        int itemsPerPage = 44;
        int totalPages = Math.max(1, (items.size() + itemsPerPage - 1) / itemsPerPage);
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());
        
        // Display items from slot 1-44 (avoiding slot 0 for back button and 45+ for bottom row)
        int slot = startSlot;
        for (int i = startIndex; i < endIndex; i++) {
            // Skip slot 0 (back button) and slots 45+ (bottom row)
            if (slot == 0) slot = 1;
            if (slot >= 45) break;
            
            Item item = items.get(i);
            addBuyItem(slot++, item);
        }
        
        // Page navigation
        if (page > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Previous Page"))
                .addLoreLine(Component.literal("§7Page " + (page + 1) + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    page--;
                    updateDisplay();
                })
            );
        }
        
        if (page < totalPages - 1) {
            setSlot(52, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eNext Page →"))
                .addLoreLine(Component.literal("§7Page " + (page + 2) + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    page++;
                    updateDisplay();
                })
            );
        }
        
        // Sell mode toggle (bottom left)
        setSlot(46, new GuiElementBuilder(Items.GOLD_NUGGET)
            .setName(Component.literal("§6Right-click to Sell!"))
            .addLoreLine(Component.literal("§7Hold Shift + Right-click items"))
            .addLoreLine(Component.literal("§7to quick-sell them!"))
        );
    }
    
    private void addBuyItem(int slot, Item item) {
        long baseBuyPrice = ItemPricing.getBuyPrice(item);
        long baseSellPrice = ItemPricing.getSellPrice(item);
        
        // Apply stock market price fluctuations
        long buyPrice = PriceFluctuation.getAdjustedPrice(item, baseBuyPrice);
        long adjustedSellPrice = PriceFluctuation.getAdjustedPrice(item, baseSellPrice);
        
        // Apply sell price upgrade multiplier
        double sellMultiplier = UpgradeManager.getSellPriceMultiplier(player.getUUID());
        long sellPrice = (long) (adjustedSellPrice * sellMultiplier);
        
        ShopTier tier = ItemPricing.getTier(item);
        boolean tierUnlocked = ShopMod.dataManager.hasTierUnlocked(player.getUUID(), tier.getId());
        boolean canAfford = CurrencyManager.canAfford(player, buyPrice);
        int playerHas = countItemInInventory(player, item);
        
        // Get price change display
        String priceChange = PriceFluctuation.getPriceChangeDisplay(item);
        
        // Create item stack with count to show if locked (darker appearance)
        ItemStack displayStack = new ItemStack(item, tierUnlocked ? 1 : 1);
        
        GuiElementBuilder builder = new GuiElementBuilder(displayStack)
            .setName(Component.literal((tierUnlocked ? "§f" : "§8§o") + item.getName(item.getDefaultInstance()).getString()))
            .addLoreLine(Component.literal((tierUnlocked ? "§a" : "§8") + "Buy: " + CurrencyManager.format(buyPrice) + " " + priceChange))
            .addLoreLine(Component.literal((tierUnlocked ? "§6" : "§8") + "Sell: " + CurrencyManager.format(sellPrice) + " " + priceChange))
            .addLoreLine(Component.literal("§7Tier: " + tier.getColor() + tier.getName()));
        
        if (playerHas > 0) {
            builder.addLoreLine(Component.literal("§7You have: §a" + playerHas));
        }
        
        if (!tierUnlocked) {
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c§l✗ LOCKED"))
                .addLoreLine(Component.literal("§7Unlocked at " + tier.getColor() + tier.getName()));
            // Set glow effect to make it more obvious
            builder.glow();
        } else {
            // Calculate actual buy amount and cost
            int buyAmount = buyQuantity.amount;
            if (buyAmount == Integer.MAX_VALUE) {
                // Max mode: calculate how many they can afford
                long balance = CurrencyManager.getBalance(player);
                buyAmount = (int) Math.min(balance / buyPrice, 2304); // Cap at 36 stacks
            }
            long totalCost = buyPrice * buyAmount;
            boolean canAffordBulk = CurrencyManager.canAfford(player, totalCost);
            
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(canAffordBulk ? "§a§lLEFT CLICK TO BUY" : "§c§lCAN'T AFFORD!"))
                .addLoreLine(Component.literal("§7Buy Mode: §b" + buyQuantity.display))
                .addLoreLine(Component.literal("§7Amount: §f" + buyAmount + " §7items"))
                .addLoreLine(Component.literal("§7Total Cost: §6" + CurrencyManager.format(totalCost)))
                .addLoreLine(Component.literal(""));
            
            if (playerHas > 0) {
                // Calculate sell amount and earnings
                int sellAmount = sellQuantity.amount;
                if (sellAmount == Integer.MAX_VALUE) {
                    sellAmount = playerHas; // All
                } else {
                    sellAmount = Math.min(sellAmount, playerHas);
                }
                long totalEarnings = sellPrice * sellAmount;
                
                builder.addLoreLine(Component.literal("§6§lRIGHT CLICK TO SELL"))
                    .addLoreLine(Component.literal("§7Sell Mode: §6" + sellQuantity.display))
                    .addLoreLine(Component.literal("§7Amount: §f" + sellAmount + " §7items"))
                    .addLoreLine(Component.literal("§7Total Earnings: §6" + CurrencyManager.format(totalEarnings)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§8Shift+Right: Sell ALL"));
            }
        }
        
        // Always set callback so selling works
        builder.setCallback((index, type, action) -> {
            // Sell ALL on shift+right-click
            if (type == ClickType.MOUSE_RIGHT_SHIFT && playerHas > 0) {
                sellAnyItem(item, playerHas);
                updateDisplay();
            } else if (type == ClickType.MOUSE_RIGHT && playerHas > 0) {
                // Regular right-click: sell based on current mode
                int sellAmount = sellQuantity.amount;
                if (sellAmount == Integer.MAX_VALUE) {
                    sellAmount = playerHas; // All
                } else {
                    sellAmount = Math.min(sellAmount, playerHas);
                }
                sellAnyItem(item, sellAmount);
                updateDisplay();
            } else if (tierUnlocked && type == ClickType.MOUSE_LEFT) {
                // Buying with current quantity mode
                int buyAmount = buyQuantity.amount;
                if (buyAmount == Integer.MAX_VALUE) {
                    // Max mode: calculate how many they can afford
                    long balance = CurrencyManager.getBalance(player);
                    buyAmount = (int) Math.min(balance / buyPrice, 2304); // Cap at 36 stacks
                }
                buyItem(item, buyPrice, buyAmount);
            } else if (!tierUnlocked) {
                // Locked item clicked
                player.sendSystemMessage(Component.literal("§c✗ Unlock " + tier.getColor() + tier.getName() + " §cfirst!"));
            }
        });
        
        setSlot(slot, builder);
    }
    
    private void buyItem(Item item, long priceEach, int amount) {
        long totalPrice = priceEach * amount;
        
        if (!CurrencyManager.canAfford(player, totalPrice)) {
            CurrencyManager.sendInsufficientFundsMessage(player, totalPrice);
            return;
        }
        
        if (CurrencyManager.removeMoney(player, totalPrice)) {
            player.addItem(new ItemStack(item, amount));
            CurrencyManager.sendMoneySpentMessage(player, totalPrice, "Purchased " + amount + "x " + item.getName(item.getDefaultInstance()).getString());
            updateDisplay();
        }
    }
    
    private void sellItem(Item item, long priceEach, int amount) {
        int available = countItemInInventory(player, item);
        int toSell = Math.min(amount, available);
        
        if (toSell == 0) {
            player.sendSystemMessage(Component.literal("§cYou don't have any to sell!"));
            return;
        }
        
        int removed = removeItemFromInventory(player, item, toSell);
        
        if (removed > 0) {
            long totalEarned = priceEach * removed;
            CurrencyManager.addMoney(player, totalEarned);
            CurrencyManager.sendMoneyReceivedMessage(player, totalEarned, "Sold " + removed + "x " + item.getName(item.getDefaultInstance()).getString());
            updateDisplay();
        }
    }
    
    private void sellAnyItem(Item item, int amount) {
        // First, verify player actually has the items and remove them
        int actualAmount = removeItemFromInventory(player, item, amount);
        
        if (actualAmount <= 0) {
            player.sendSystemMessage(Component.literal("§cYou don't have any of that item!"));
            return;
        }
        
        long sellPrice = ItemPricing.getSellPrice(item);
        
        // Apply sell price boost upgrade
        double sellMultiplier = UpgradeManager.getSellPriceMultiplier(player.getUUID());
        long finalSellPrice = (long) (sellPrice * sellMultiplier);
        
        // Allow selling ANY item, even if not in shop
        if (finalSellPrice <= 0) {
            // Default sell price: $1 per item minimum
            finalSellPrice = 1;
        }
        
        long totalEarned = finalSellPrice * actualAmount;
        
        // Give money
        CurrencyManager.addMoney(player, totalEarned);
        player.sendSystemMessage(Component.literal(
            "§a§l✓ Sold " + actualAmount + "x " + 
            item.getName(item.getDefaultInstance()).getString() + 
            " §afor §6" + CurrencyManager.format(totalEarned)
        ));
    }
    
    private List<Item> filterAvailable(List<Item> items) {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (ItemPricing.getBuyPrice(item) > 0) {
                result.add(item);
            }
        }
        return result;
    }
    
    private void setupCombatCategory() {
        List<Item> items = Arrays.asList(
            Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW,
            Items.BOW, Items.CROSSBOW, Items.TRIDENT,
            Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
            Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
            Items.SHIELD, Items.TOTEM_OF_UNDYING,
            Items.TNT, Items.TNT_MINECART, Items.FIRE_CHARGE, Items.FIREWORK_ROCKET,
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupPotionsCategory() {
        List<Item> items = Arrays.asList(
            Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION,
            Items.GLASS_BOTTLE, Items.BREWING_STAND, Items.CAULDRON,
            Items.NETHER_WART, Items.BLAZE_POWDER, Items.MAGMA_CREAM,
            Items.FERMENTED_SPIDER_EYE, Items.SPIDER_EYE, Items.GLISTERING_MELON_SLICE,
            Items.GOLDEN_CARROT, Items.PUFFERFISH, Items.RABBIT_FOOT,
            Items.GHAST_TEAR, Items.PHANTOM_MEMBRANE,
            Items.GLOWSTONE_DUST, Items.REDSTONE, Items.GUNPOWDER, Items.DRAGON_BREATH,
            Items.HONEY_BOTTLE, Items.SUGAR, Items.TURTLE_HELMET
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupNetherCategory() {
        List<Item> items = Arrays.asList(
            Items.NETHERRACK, Items.NETHER_BRICKS, Items.RED_NETHER_BRICKS, Items.CRACKED_NETHER_BRICKS,
            Items.SOUL_SAND, Items.SOUL_SOIL, Items.BASALT, Items.SMOOTH_BASALT, Items.BLACKSTONE,
            Items.CRIMSON_NYLIUM, Items.WARPED_NYLIUM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS,
            Items.CRIMSON_ROOTS, Items.WARPED_ROOTS, Items.NETHER_SPROUTS, Items.CRIMSON_STEM, Items.WARPED_STEM,
            Items.SHROOMLIGHT, Items.GLOWSTONE, Items.MAGMA_BLOCK,
            Items.NETHER_WART, Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK,
            Items.NETHER_QUARTZ_ORE, Items.QUARTZ, Items.QUARTZ_BLOCK,
            Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT,
            Items.BLAZE_ROD, Items.BLAZE_POWDER, Items.GHAST_TEAR, Items.MAGMA_CREAM,
            Items.NETHER_GOLD_ORE, Items.NETHER_STAR,
            Items.GILDED_BLACKSTONE, Items.RESPAWN_ANCHOR, Items.CRYING_OBSIDIAN
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupEndCategory() {
        List<Item> items = Arrays.asList(
            Items.END_STONE, Items.END_STONE_BRICKS, Items.PURPUR_BLOCK, Items.PURPUR_PILLAR,
            Items.END_ROD, Items.CHORUS_FRUIT, Items.POPPED_CHORUS_FRUIT, Items.CHORUS_FLOWER, Items.CHORUS_PLANT,
            Items.ENDER_PEARL, Items.ENDER_EYE, Items.ENDER_CHEST,
            Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX, Items.ORANGE_SHULKER_BOX, Items.MAGENTA_SHULKER_BOX,
            Items.LIGHT_BLUE_SHULKER_BOX, Items.YELLOW_SHULKER_BOX, Items.LIME_SHULKER_BOX,
            Items.PINK_SHULKER_BOX, Items.GRAY_SHULKER_BOX, Items.LIGHT_GRAY_SHULKER_BOX,
            Items.CYAN_SHULKER_BOX, Items.PURPLE_SHULKER_BOX, Items.BLUE_SHULKER_BOX,
            Items.BROWN_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.RED_SHULKER_BOX, Items.BLACK_SHULKER_BOX,
            Items.SHULKER_SHELL, Items.ELYTRA, Items.DRAGON_BREATH, Items.DRAGON_HEAD, Items.DRAGON_EGG
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupOceanCategory() {
        List<Item> items = Arrays.asList(
            Items.PRISMARINE, Items.PRISMARINE_BRICKS, Items.DARK_PRISMARINE, Items.PRISMARINE_SHARD,
            Items.SEA_LANTERN, Items.PRISMARINE_CRYSTALS,
            Items.SPONGE, Items.WET_SPONGE,
            Items.HEART_OF_THE_SEA, Items.CONDUIT, Items.NAUTILUS_SHELL,
            Items.KELP, Items.DRIED_KELP, Items.DRIED_KELP_BLOCK, Items.SEAGRASS, Items.SEA_PICKLE,
            Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK,
            Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK,
            Items.TUBE_CORAL, Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.HORN_CORAL,
            Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON,
            Items.TROPICAL_FISH, Items.PUFFERFISH,
            Items.TRIDENT, Items.TURTLE_HELMET, Items.TURTLE_SCUTE,
            Items.OAK_BOAT, Items.SPRUCE_BOAT, Items.BIRCH_BOAT, Items.JUNGLE_BOAT
        );
        displayItemGrid(filterAvailable(items), 1);
    }
    
    private void setupUpgradesCategory() {
        // Title
        setSlot(22, new GuiElementBuilder(Items.ENCHANTING_TABLE)
            .setName(Component.literal("§6§l✦ UPGRADE SHOP ✦"))
            .addLoreLine(Component.literal("§7Purchase permanent upgrades"))
            .addLoreLine(Component.literal("§7to boost your performance!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lAvailable Upgrades:"))
            .addLoreLine(Component.literal("§6• Income Multiplier"))
            .addLoreLine(Component.literal("§b• Mining Speed"))
            .addLoreLine(Component.literal("§a• XP Multiplier"))
            .addLoreLine(Component.literal("§e• Sell Price Boost"))
            .addLoreLine(Component.literal("§d• Daily Deals Discount"))
        );
        
        // Display all upgrades
        UpgradeType[] upgrades = UpgradeType.values();
        int[] slots = {28, 29, 30, 31, 32}; // Slots for the 5 upgrades
        
        for (int i = 0; i < upgrades.length && i < slots.length; i++) {
            UpgradeType upgrade = upgrades[i];
            int currentLevel = UpgradeManager.getUpgradeLevel(player.getUUID(), upgrade);
            int nextLevel = currentLevel + 1;
            boolean maxed = currentLevel >= upgrade.getMaxLevel();
            long upgradeCost = upgrade.getCostForLevel(nextLevel);
            boolean canAfford = CurrencyManager.canAfford(player, upgradeCost);
            
            GuiElementBuilder builder = new GuiElementBuilder(upgrade.getIcon())
                .setName(Component.literal("§b§l" + upgrade.getDisplayName()))
                .addLoreLine(Component.literal(upgrade.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Current Level: §f" + currentLevel + "§7/§f" + upgrade.getMaxLevel()))
                .addLoreLine(Component.literal("§7Current Bonus: " + upgrade.formatBenefit(currentLevel)));
            
            if (!maxed) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§e§lNEXT LEVEL:"))
                    .addLoreLine(Component.literal("§7Level " + nextLevel + " Bonus: " + upgrade.formatBenefit(nextLevel)))
                    .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(upgradeCost)))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal(canAfford ? "§a§l✦ CLICK TO UPGRADE ✦" : "§c§l✗ INSUFFICIENT FUNDS"));
                
                if (canAfford) {
                    builder.glow();
                }
                
                builder.setCallback((index, type, action) -> {
                    if (CurrencyManager.canAfford(player, upgradeCost)) {
                        if (CurrencyManager.removeMoney(player, upgradeCost)) {
                            UpgradeManager.upgrade(player.getUUID(), upgrade);
                            player.sendSystemMessage(Component.literal(
                                "§a§l✓ Upgraded " + upgrade.getDisplayName() + " §ato Level " + nextLevel + "!"
                            ));
                            updateDisplay();
                        }
                    } else {
                        CurrencyManager.sendInsufficientFundsMessage(player, upgradeCost);
                    }
                });
            } else {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§l✓ MAX LEVEL REACHED!"))
                    .glow();
            }
            
            setSlot(slots[i], builder);
        }
    }
    
    private boolean isFoodItem(Item item) {
        return item == Items.BREAD || item == Items.COOKED_BEEF || item == Items.COOKED_PORKCHOP ||
               item == Items.COOKED_CHICKEN || item == Items.COOKED_MUTTON || item == Items.COOKED_RABBIT ||
               item == Items.COOKED_COD || item == Items.COOKED_SALMON || item == Items.BAKED_POTATO ||
               item == Items.COOKIE || item == Items.CAKE || item == Items.PUMPKIN_PIE ||
               item == Items.APPLE || item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE ||
               item == Items.MELON_SLICE || item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES ||
               item == Items.CARROT || item == Items.GOLDEN_CARROT || item == Items.POTATO ||
               item == Items.POISONOUS_POTATO || item == Items.BEETROOT || item == Items.BEETROOT_SOUP ||
               item == Items.MUSHROOM_STEW || item == Items.RABBIT_STEW || item == Items.SUSPICIOUS_STEW;
    }
    
    private int countItemInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    private int removeItemFromInventory(ServerPlayer player, Item item, int amount) {
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
