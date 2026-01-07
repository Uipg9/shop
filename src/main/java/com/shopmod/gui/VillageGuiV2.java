package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.village.*;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Enhanced Village GUI with tabs for workers, resources, buildings, and trade center
 */
public class VillageGuiV2 extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode currentView = ViewMode.WORKERS;
    
    private enum ViewMode {
        WORKERS, RESOURCES, BUILDINGS, TRADE_CENTER, GUIDE
    }
    
    public VillageGuiV2(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§lDigital Village"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        VillageManager.Village village = VillageManager.getVillage(player.getUUID());
        
        // Top info bar
        setupInfoBar(village);
        
        // Tab switchers (bottom row)
        setupTabs(village);
        
        // Main content based on current view
        switch (currentView) {
            case WORKERS -> displayWorkersView(village);
            case RESOURCES -> displayResourcesView(village);
            case BUILDINGS -> displayBuildingsView(village);
            case TRADE_CENTER -> displayTradeCenterView();
            case GUIDE -> displayGuideView(village);
        }
    }
    
    /**
     * Setup info bar (top row)
     */
    private void setupInfoBar(VillageManager.Village village) {
        String levelName = VillageManager.getVillageLevelName(village.getVillageLevel());
        
        // Village level
        setSlot(0, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§6§l" + levelName))
            .addLoreLine(Component.literal("§7Level: §e" + village.getVillageLevel()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§71→2: §e50 Food, 30 Wood"))
            .addLoreLine(Component.literal("§72→3: §e200 Food, 100 Wood, 50 Ore, 5 workers"))
            .addLoreLine(Component.literal("§73→4: §eAll T3 workers, 10 workers, 3 houses"))
        );
        
        // Worker info
        setSlot(2, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§lWorkers"))
            .addLoreLine(Component.literal("§7Active: §a" + village.getTotalWorkerCount() + 
                "/" + village.getTotalWorkerSlots()))
            .addLoreLine(Component.literal("§7Build houses to expand!"))
        );
        
        // Food status
        long food = village.getResources().getOrDefault(ResourceType.FOOD, 0L) +
                   village.getResources().getOrDefault(ResourceType.FISH, 0L);
        int foodNeeded = village.getTotalWorkerCount();
        boolean hasFood = food >= foodNeeded;
        
        setSlot(4, new GuiElementBuilder(hasFood ? Items.BREAD : Items.ROTTEN_FLESH)
            .setName(Component.literal(hasFood ? "§a§lWell Fed" : "§c§lFOOD SHORTAGE!"))
            .addLoreLine(Component.literal("§7Daily Need: §e" + foodNeeded))
            .addLoreLine(Component.literal("§7Available: " + (hasFood ? "§a" : "§c") + food))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Workers consume 1 food/day"))
        );
        
        // Auto-manage toggle
        setSlot(6, new GuiElementBuilder(village.isAutoManage() ? Items.REDSTONE_TORCH : Items.LEVER)
            .setName(Component.literal(village.isAutoManage() ? 
                "§e§lAuto-Manage: ON" : "§a§lManual Mode: ON"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal(village.isAutoManage() ?
                "§7- Resources managed automatically" :
                "§7- Full control over resources"))
            .addLoreLine(Component.literal(village.isAutoManage() ?
                "§7- 30% efficiency penalty" :
                "§7- 100% efficiency"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to toggle"))
            .setCallback((index, type, action) -> {
                VillageManager.toggleAutoManage(player);
                updateDisplay();
            })
        );
        
        // Balance
        setSlot(8, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    /**
     * Setup tab switchers (bottom row)
     */
    private void setupTabs(VillageManager.Village village) {
        // Workers tab
        setSlot(45, new GuiElementBuilder(currentView == ViewMode.WORKERS ? Items.DIAMOND_SWORD : Items.IRON_SWORD)
            .setName(Component.literal("§e§lWorkers"))
            .addLoreLine(Component.literal("§7Hire, upgrade, and manage workers"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.WORKERS;
                updateDisplay();
            })
        );
        
        // Resources tab
        setSlot(46, new GuiElementBuilder(currentView == ViewMode.RESOURCES ? Items.DIAMOND : Items.COAL)
            .setName(Component.literal("§a§lResources"))
            .addLoreLine(Component.literal("§7View village warehouse"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.RESOURCES;
                updateDisplay();
            })
        );
        
        // Buildings tab
        setSlot(47, new GuiElementBuilder(currentView == ViewMode.BUILDINGS ? Items.BRICKS : Items.BRICK)
            .setName(Component.literal("§6§lBuildings"))
            .addLoreLine(Component.literal("§7Construct and upgrade buildings"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.BUILDINGS;
                updateDisplay();
            })
        );
        
        // Trade Center tab
        setSlot(48, new GuiElementBuilder(currentView == ViewMode.TRADE_CENTER ? Items.EMERALD_BLOCK : Items.EMERALD)
            .setName(Component.literal("§2§lTrade Center"))
            .addLoreLine(Component.literal("§7Export and sell resources"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.TRADE_CENTER;
                updateDisplay();
            })
        );
        
        // Guide tab
        setSlot(49, new GuiElementBuilder(currentView == ViewMode.GUIDE ? Items.ENCHANTED_BOOK : Items.BOOK)
            .setName(Component.literal("§d§lGuide"))
            .addLoreLine(Component.literal("§7Learn how the village works"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.GUIDE;
                updateDisplay();
            })
        );
        
        // Close button
        setSlot(53, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> this.close())
        );
    }
    
    /**
     * Display workers view (hire, fire, upgrade)
     */
    private void displayWorkersView(VillageManager.Village village) {
        int slot = 18;
        
        for (VillagerWorker workerType : VillagerWorker.values()) {
            // Check if unlocked
            boolean unlocked = workerType.getRequiredVillageLevel() <= village.getVillageLevel();
            
            VillageManager.WorkerData data = village.getWorkers().get(workerType);
            int count = data != null ? data.getCount() : 0;
            int level = data != null ? data.getLevel() : 1;
            VillageManager.WorkerStatus status = data != null ? data.getStatus() : VillageManager.WorkerStatus.ACTIVE;
            
            GuiElementBuilder builder = new GuiElementBuilder(workerType.getIcon())
                .setName(Component.literal((unlocked ? "" : "§8") + workerType.getDisplayName()));
            
            if (!unlocked) {
                builder.addLoreLine(Component.literal("§c§lLOCKED"))
                    .addLoreLine(Component.literal("§7Requires Village Level " + workerType.getRequiredVillageLevel()));
            } else if (count > 0) {
                builder.addLoreLine(Component.literal(workerType.getDescription()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Count: §e" + count + "x §8(Level " + level + ")"))
                    .addLoreLine(Component.literal("§7Status: " + status.getDisplay()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Consumes:"));
                
                if (workerType.getDailyInputs().isEmpty()) {
                    builder.addLoreLine(Component.literal("  §8Nothing (Producer)"));
                } else {
                    for (var input : workerType.getDailyInputs().entrySet()) {
                        builder.addLoreLine(Component.literal(String.format("  §7%s %s x%d",
                            input.getKey().getIcon(), input.getKey().getDisplayName(), input.getValue())));
                    }
                }
                
                builder.addLoreLine(Component.literal("§7Produces:"));
                for (var output : workerType.getDailyOutputs().entrySet()) {
                    builder.addLoreLine(Component.literal(String.format("  §a%s %s x%d",
                        output.getKey().getIcon(), output.getKey().getDisplayName(), output.getValue())));
                }
                
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lLEFT CLICK §7hire more"))
                    .addLoreLine(Component.literal("§e§lSHIFT+LEFT §7upgrade"))
                    .addLoreLine(Component.literal("§c§lRIGHT CLICK §7fire one"));
                
                builder.glow();
            } else {
                builder.addLoreLine(Component.literal(workerType.getDescription()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Hire Cost: §6" + 
                        CurrencyManager.format(workerType.getHireCost())))
                    .addLoreLine(Component.literal("§7Daily Salary: §c-" +
                        CurrencyManager.format(workerType.getDailySalary())))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lLEFT CLICK §7to hire"));
            }
            
            builder.setCallback((index, type, action) -> {
                if (!unlocked) return;
                
                if (type == ClickType.MOUSE_LEFT) {
                    VillageManager.hireWorker(player, workerType);
                    updateDisplay();
                } else if (type == ClickType.MOUSE_LEFT_SHIFT) {
                    VillageManager.upgradeWorker(player, workerType);
                    updateDisplay();
                } else if (type == ClickType.MOUSE_RIGHT) {
                    VillageManager.fireWorker(player, workerType);
                    updateDisplay();
                }
            });
            
            setSlot(slot, builder);
            slot++;
            if (slot >= 35) break; // Limit display
        }
    }
    
    /**
     * Display resources in village warehouse
     */
    private void displayResourcesView(VillageManager.Village village) {
        int slot = 18;
        
        for (ResourceType type : ResourceType.values()) {
            long amount = village.getResources().getOrDefault(type, 0L);
            long capacity = village.getStorageCapacity().getOrDefault(type, 0L);
            
            setSlot(slot, new GuiElementBuilder(type.getRepresentativeItem())
                .setName(Component.literal("§e" + type.getIcon() + " " + type.getDisplayName()))
                .addLoreLine(Component.literal(type.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Amount: §e" + amount + "/" + capacity))
                .addLoreLine(Component.literal("§7Value: §6" + 
                    CurrencyManager.format(type.getValuePerUnit()) + " §7per unit"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Transfer to Trade Center"))
                .addLoreLine(Component.literal("§7to sell for money"))
            );
            
            slot++;
        }
    }
    
    /**
     * Display buildings (constructible structures)
     */
    private void displayBuildingsView(VillageManager.Village village) {
        int slot = 18;
        
        for (VillageBuilding building : VillageBuilding.values()) {
            int count = village.getBuildings().getOrDefault(building, 0);
            
            GuiElementBuilder builder = new GuiElementBuilder(building.getIcon())
                .setName(Component.literal("§6" + building.getDisplayName()))
                .addLoreLine(Component.literal(building.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Owned: §e" + count));
            
            if (building.getWorkerSlots() > 0) {
                builder.addLoreLine(Component.literal("§7+1 Worker Slot per building"));
            }
            
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6§lBuild Cost:"));
            
            for (var cost : building.getBuildCost().entrySet()) {
                long have = village.getResources().getOrDefault(cost.getKey(), 0L);
                boolean enough = have >= cost.getValue();
                builder.addLoreLine(Component.literal(String.format("  %s%s %s x%d",
                    enough ? "§a" : "§c",
                    cost.getKey().getIcon(),
                    cost.getKey().getDisplayName(),
                    cost.getValue())));
            }
            
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§c§lDaily Upkeep:"));
            
            for (var upkeep : building.getDailyUpkeep().entrySet()) {
                builder.addLoreLine(Component.literal(String.format("  §7%s %s x%d",
                    upkeep.getKey().getIcon(),
                    upkeep.getKey().getDisplayName(),
                    upkeep.getValue())));
            }
            
            builder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a§lCLICK to build"));
            
            builder.setCallback((index, type, action) -> {
                VillageManager.buildBuilding(player, building);
                updateDisplay();
            });
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    /**
     * Display trade center (export/sell resources)
     */
    private void displayTradeCenterView() {
        TradeCenterManager.TradeCenter tradeCenter = TradeCenterManager.getTradeCenter(player.getUUID());
        
        int slot = 18;
        
        setSlot(10, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§6§lTotal Value"))
            .addLoreLine(Component.literal(CurrencyManager.format(tradeCenter.getTotalValue())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Value of all stored resources"))
        );
        
        for (ResourceType type : ResourceType.values()) {
            long amount = tradeCenter.getStorage().getOrDefault(type, 0L);
            long capacity = tradeCenter.getStorageCapacity().getOrDefault(type, 0L);
            boolean autoSell = tradeCenter.getAutoSellTypes().contains(type);
            
            GuiElementBuilder builder = new GuiElementBuilder(type.getRepresentativeItem())
                .setName(Component.literal("§e" + type.getIcon() + " " + type.getDisplayName()))
                .addLoreLine(Component.literal("§7Amount: §e" + amount + "/" + capacity))
                .addLoreLine(Component.literal("§7Value: §6" + 
                    CurrencyManager.format(amount * type.getValuePerUnit())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(autoSell ? "§a§lAuto-Sell: ON" : "§7Auto-Sell: OFF"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a§lLEFT CLICK §7sell 1"))
                .addLoreLine(Component.literal("§e§lSHIFT+LEFT §7sell all"))
                .addLoreLine(Component.literal("§b§lRIGHT CLICK §7toggle auto-sell"));
            
            if (autoSell) builder.glow();
            
            builder.setCallback((index, type1, action) -> {
                if (type1 == ClickType.MOUSE_LEFT) {
                    TradeCenterManager.sellResource(player, type, 1);
                    updateDisplay();
                } else if (type1 == ClickType.MOUSE_LEFT_SHIFT) {
                    TradeCenterManager.sellAllResource(player, type);
                    updateDisplay();
                } else if (type1 == ClickType.MOUSE_RIGHT) {
                    TradeCenterManager.toggleAutoSell(player, type);
                    updateDisplay();
                }
            });
            
            setSlot(slot, builder);
            slot++;
        }
    }

    private void displayGuideView(VillageManager.Village village) {
        setSlot(13, new GuiElementBuilder(Items.ENCHANTED_BOOK)
            .setName(Component.literal("§d§l═══ Village Guide ═══"))
            .addLoreLine(Component.literal("§7Everything you need to know"))
        );
        
        setSlot(18, new GuiElementBuilder(Items.OAK_SIGN)
            .setName(Component.literal("§e§l1. Getting Started"))
            .addLoreLine(Component.literal("§7Welcome to your village!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Your village produces resources"))
            .addLoreLine(Component.literal("§7through workers and buildings."))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e→ §7Start by hiring a §aFarmer"))
            .addLoreLine(Component.literal("§e→ §7Then hire a §6Lumberjack"))
            .addLoreLine(Component.literal("§e→ §7Build a §eHouse §7for more workers"))
        );
        
        setSlot(19, new GuiElementBuilder(Items.DIAMOND)
            .setName(Component.literal("§e§l2. Resource Types"))
            .addLoreLine(Component.literal("§710 resource types in 4 tiers:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§aTier 1: §fFood ($10), Wood ($15), Fish ($12)"))
            .addLoreLine(Component.literal("§9Tier 2: §fOre ($50), Leather ($30), Wool ($25)"))
            .addLoreLine(Component.literal("§dTier 3: §fTools ($150), Armor ($200)"))
            .addLoreLine(Component.literal("§5Tier 4: §fEnchanted ($500), Rare ($1000)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Resources stored in §eWarehouse"))
            .addLoreLine(Component.literal("§7Base capacity: §e200 §7per type"))
        );
        
        setSlot(20, new GuiElementBuilder(Items.PLAYER_HEAD)
            .setName(Component.literal("§e§l3. Workers & Supply Chains"))
            .addLoreLine(Component.literal("§78 worker types with tiers:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§aTier 1 (Hamlet):"))
            .addLoreLine(Component.literal("  §f• Farmer: §7→ 3 Food/day"))
            .addLoreLine(Component.literal("  §f• Lumberjack: §7→ 3 Wood/day"))
            .addLoreLine(Component.literal("  §f• Fisherman: §7→ 2 Fish/day"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§9Tier 2 (Village):"))
            .addLoreLine(Component.literal("  §f• Miner: §72F+1W → 2 Ore"))
            .addLoreLine(Component.literal("  §f• Rancher: §73F → 2L+2W"))
        );
        
        setSlot(21, new GuiElementBuilder(Items.DIAMOND_SWORD)
            .setName(Component.literal("§e§l4. Advanced Workers"))
            .addLoreLine(Component.literal("§dTier 3 (Town):"))
            .addLoreLine(Component.literal("  §f• Blacksmith: §72O+1W → 1T+1A"))
            .addLoreLine(Component.literal("  §f• Merchant: §7Any 3 res → Money"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§5Tier 4 (City):"))
            .addLoreLine(Component.literal("  §f• Enchanter: §71W+2O → 1 Enchanted"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Each worker needs §c1 Food/day"))
            .addLoreLine(Component.literal("§7Without food: §eHungry §7→ §cStrike"))
            .addLoreLine(Component.literal("§c1 Farmer feeds 3 workers!"))
        );
        
        setSlot(22, new GuiElementBuilder(Items.BRICKS)
            .setName(Component.literal("§e§l5. Buildings"))
            .addLoreLine(Component.literal("§710 buildings with daily upkeep:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lHouse §7(50W, -2W/day)"))
            .addLoreLine(Component.literal("  §7+10 worker slots"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lWorkshop §7(250W+30O, -5W-1T/day)"))
            .addLoreLine(Component.literal("  §a+10% all production"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§6§lMarket §7(200W+100O, -3W-1T/day)"))
            .addLoreLine(Component.literal("  §a2x sell value"))
        );
        
        setSlot(23, new GuiElementBuilder(Items.BEACON)
            .setName(Component.literal("§e§l6. Village Progression"))
            .addLoreLine(Component.literal("§7Village levels unlock workers:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§f1. §7Hamlet §7(Start)"))
            .addLoreLine(Component.literal("  §710 worker slots, Tier 1 workers"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a2. §aVillage §7(50F+30W)"))
            .addLoreLine(Component.literal("  §715 slots, +Tier 2 workers"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§93. §9Town §7(200F+100W+50O+5 workers+1 house)"))
            .addLoreLine(Component.literal("  §725+ slots, +Tier 3 workers"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§d4. §dCity §7(hire blacksmith+merchant+10w+3h)"))
            .addLoreLine(Component.literal("  §740+ slots, +Tier 4 workers"))
        );
        
        setSlot(24, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§e§l7. Auto-Manage Mode"))
            .addLoreLine(Component.literal("§7Two management modes:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lManual Mode §7(100% efficiency)"))
            .addLoreLine(Component.literal("  §7• Must have input resources"))
            .addLoreLine(Component.literal("  §7• Full production output"))
            .addLoreLine(Component.literal("  §7• Pay building upkeep"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lAuto-Manage §7(70% efficiency)"))
            .addLoreLine(Component.literal("  §7• No inputs needed"))
            .addLoreLine(Component.literal("  §7• 70% production"))
            .addLoreLine(Component.literal("  §7• No upkeep costs"))
            .addLoreLine(Component.literal("  §7• §bZero micromanagement!"))
        );
        
        setSlot(25, new GuiElementBuilder(Items.EMERALD_BLOCK)
            .setName(Component.literal("§e§l8. Trade Center"))
            .addLoreLine(Component.literal("§7Separate storage for selling:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7• Transfer resources from village"))
            .addLoreLine(Component.literal("§7• Sell resources for money"))
            .addLoreLine(Component.literal("§7• Auto-sell on daily reset"))
            .addLoreLine(Component.literal("§7• Upgrade storage capacity"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§aTip: §7Enable auto-sell on"))
            .addLoreLine(Component.literal("§7surplus resources for passive income!"))
        );
        
        setSlot(26, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§e§l9. Strategy Tips"))
            .addLoreLine(Component.literal("§a§lEarly Game:"))
            .addLoreLine(Component.literal("  §7• Hire 1-2 Farmers FIRST"))
            .addLoreLine(Component.literal("  §7• Then Lumberjack for wood"))
            .addLoreLine(Component.literal("  §7• Build Houses to expand"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§9§lMid Game:"))
            .addLoreLine(Component.literal("  §7• Upgrade to Village level"))
            .addLoreLine(Component.literal("  §7• Hire Miners for ore"))
            .addLoreLine(Component.literal("  §7• Build Workshop (+10% all!)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§d§lLate Game:"))
            .addLoreLine(Component.literal("  §7• Blacksmith for high-value items"))
            .addLoreLine(Component.literal("  §7• Merchants for direct income"))
            .addLoreLine(Component.literal("  §7• Market for 2x sell value"))
        );
        
        setSlot(34, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lFinancial Overview"))
            .addLoreLine(Component.literal("§7Check §6/bal §7for detailed financials:"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7• Your wallet balance"))
            .addLoreLine(Component.literal("§7• Total daily salaries §c(expenses)"))
            .addLoreLine(Component.literal("§7• Estimated daily income §a(earnings)"))
            .addLoreLine(Component.literal("§7• Net daily profit"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Income varies based on efficiency,"))
            .addLoreLine(Component.literal("§8resources, and building bonuses."))
        );
    }
}