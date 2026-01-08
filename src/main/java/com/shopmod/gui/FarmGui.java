package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.farm.*;
import com.shopmod.village.ResourceType;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.Map;

/**
 * Digital Farm GUI - Buy farms, manage production, collect resources
 */
public class FarmGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode currentView = ViewMode.FARMS;
    
    private enum ViewMode {
        FARMS, HARVEST, MANAGEMENT
    }
    
    public FarmGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§2§lDigital Farm System"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        FarmManager.PlayerFarms farms = FarmManager.getPlayerFarms(player.getUUID());
        
        // Top info bar
        setupInfoBar(farms);
        
        // Tab switchers (bottom row)
        setupTabs();
        
        // Main content based on current view
        switch (currentView) {
            case FARMS -> displayFarmsView(farms);
            case HARVEST -> displayHarvestView(farms);
            case MANAGEMENT -> displayManagementView(farms);
        }
    }
    
    private void setupInfoBar(FarmManager.PlayerFarms farms) {
        // Farm level
        GuiElementBuilder levelBuilder = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("§e§lFarm Level " + farms.getFarmLevel()))
            .addLoreLine(Component.literal("§7Technology level determines"))
            .addLoreLine(Component.literal("§7available farm types"));
        
        // Add upgrade cost if not max level
        if (farms.getFarmLevel() < 5) {
            long upgradeCost = farms.getFarmLevel() * 10000L;
            boolean canAfford = CurrencyManager.canAfford(player, upgradeCost);
            
            levelBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Upgrade Cost: " + 
                    (canAfford ? "§a" : "§c") + "$" + CurrencyManager.format(upgradeCost)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(canAfford ? "§a§lCLICK §7to upgrade!" : "§c§lInsufficient funds!"));
        } else {
            levelBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§d§l⭐ MAX LEVEL ⭐"));
        }
        
        levelBuilder.setCallback((index, type, action) -> {
            FarmManager.upgradeFarmLevel(player);
            updateDisplay();
        });
        
        setSlot(4, levelBuilder);
        
        // Total active farms
        int activeFarms = (int) farms.getFarms().values().stream()
            .mapToInt(farm -> farm.isActive() ? 1 : 0).sum();
        
        setSlot(6, new GuiElementBuilder(Items.GRASS_BLOCK)
            .setName(Component.literal("§a§lActive Farms: " + activeFarms))
            .addLoreLine(Component.literal("§7Total owned: " + farms.getFarms().size()))
        );
        
        // Player balance
        setSlot(8, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    private void setupTabs() {
        // Farm management tab
        setSlot(45, new GuiElementBuilder(currentView == ViewMode.FARMS ? Items.EMERALD_BLOCK : Items.EMERALD)
            .setName(Component.literal("§2§lFarms"))
            .addLoreLine(Component.literal("§7Buy and manage farms"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.FARMS;
                updateDisplay();
            })
        );
        
        // Harvest tab
        setSlot(46, new GuiElementBuilder(currentView == ViewMode.HARVEST ? Items.WHEAT : Items.WHEAT_SEEDS)
            .setName(Component.literal("§e§lHarvest"))
            .addLoreLine(Component.literal("§7Collect produced resources"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.HARVEST;
                updateDisplay();
            })
        );
        
        // Management tab
        setSlot(47, new GuiElementBuilder(currentView == ViewMode.MANAGEMENT ? Items.REDSTONE_BLOCK : Items.REDSTONE)
            .setName(Component.literal("§c§lManagement"))
            .addLoreLine(Component.literal("§7Toggle farms on/off"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.MANAGEMENT;
                updateDisplay();
            })
        );
        
        // Hub button
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void displayFarmsView(FarmManager.PlayerFarms farms) {
        int slot = 18;
        
        for (FarmType farmType : FarmType.values()) {
            boolean owned = farms.getFarms().containsKey(farmType);
            boolean canBuy = farmType.getRequiredLevel() <= farms.getFarmLevel();
            
            GuiElementBuilder builder = new GuiElementBuilder(farmType.getIcon())
                .setName(Component.literal((owned ? "§a" : canBuy ? "§7" : "§c") + 
                    "§l" + farmType.getDisplayName()))
                .addLoreLine(Component.literal(farmType.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Purchase Cost: §6" + 
                    CurrencyManager.format(farmType.getPurchaseCost())))
                .addLoreLine(Component.literal("§7Daily Salary: §c-" +
                    CurrencyManager.format(farmType.getDailySalary())))
                .addLoreLine(Component.literal("§7Production: §e+" + 
                    farmType.getDailyOutput() + " " + farmType.getOutputResource().getDisplayName()))
                .addLoreLine(Component.literal("§7Required Level: §b" + farmType.getRequiredLevel()));
                
            if (owned) {
                FarmManager.FarmData data = farms.getFarms().get(farmType);
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lOWNED"))
                    .addLoreLine(Component.literal("§7Status: " + 
                        (data.isActive() ? "§a✓ Active" : "§c✖ Inactive")))
                    .addLoreLine(Component.literal("§7Total Produced: " + data.getTotalProduced()));
            } else if (canBuy) {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§a§lCLICK §7to purchase!"));
                
                builder.setCallback((index, type, action) -> {
                    FarmManager.purchaseFarm(player, farmType);
                    updateDisplay();
                });
            } else {
                builder.addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lLOCKED"))
                    .addLoreLine(Component.literal("§7Upgrade farm level!"));
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayHarvestView(FarmManager.PlayerFarms farms) {
        // Harvest All button
        setSlot(10, new GuiElementBuilder(Items.GOLDEN_HOE)
            .setName(Component.literal("§6§l⚡ Harvest All"))
            .addLoreLine(Component.literal("§7Collect ALL resources"))
            .addLoreLine(Component.literal("§7and sell them instantly"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total value: §6" + CurrencyManager.format(getTotalHarvestValue(farms))))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lCLICK §7to harvest and sell all!"))
            .setCallback((index, type, action) -> {
                harvestAll(farms);
                updateDisplay();
            })
            .glow()
        );
        
        // Auto-Sell toggle
        boolean autoSell = FarmManager.getAutoSellEnabled(player.getUUID());
        setSlot(11, new GuiElementBuilder(autoSell ? Items.GREEN_WOOL : Items.RED_WOOL)
            .setName(Component.literal((autoSell ? "§a" : "§c") + "§lAuto-Sell: " + (autoSell ? "ON" : "OFF")))
            .addLoreLine(Component.literal("§7Automatically sell harvested"))
            .addLoreLine(Component.literal("§7resources as they're produced"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§b§lCLICK §7to toggle"))
            .setCallback((index, type, action) -> {
                FarmManager.toggleAutoSell(player);
                updateDisplay();
            })
        );
        
        // Total harvest value
        setSlot(13, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§6§lTotal Harvest Value"))
            .addLoreLine(Component.literal(CurrencyManager.format(getTotalHarvestValue(farms))))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Value of all harvested resources"))
        );
        
        // Farm statistics
        setSlot(15, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§e§l📊 Farm Statistics"))
            .addLoreLine(Component.literal("§7Total Harvested: §e" + farms.getTotalHarvested()))
            .addLoreLine(Component.literal("§7Total Earned: §6" + CurrencyManager.format(farms.getTotalEarned())))
            .addLoreLine(Component.literal("§7Active Farms: §a" + farms.getActiveFarmCount()))
        );
        
        int slot = 18;
        
        for (ResourceType type : ResourceType.values()) {
            long amount = farms.getHarvestedResources().getOrDefault(type, 0L);
            if (amount == 0) continue;
            
            GuiElementBuilder builder = new GuiElementBuilder(type.getRepresentativeItem())
                .setName(Component.literal("§e" + type.getIcon() + " " + type.getDisplayName()))
                .addLoreLine(Component.literal("§7Amount: §e" + amount))
                .addLoreLine(Component.literal("§7Value: §6" + 
                    CurrencyManager.format(amount * type.getValuePerUnit())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a§lLEFT CLICK §7collect 10"))
                .addLoreLine(Component.literal("§e§lSHIFT+LEFT §7collect all"));
            
            builder.setCallback((index, type1, action) -> {
                if (type1 == ClickType.MOUSE_LEFT) {
                    FarmManager.collectResources(player, type, Math.min(10, amount));
                    updateDisplay();
                } else if (type1 == ClickType.MOUSE_LEFT_SHIFT) {
                    FarmManager.collectResources(player, type, amount);
                    updateDisplay();
                }
            });
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayManagementView(FarmManager.PlayerFarms farms) {
        // Fertilizer purchase button
        long fertilizerCost = 5000L;
        setSlot(10, new GuiElementBuilder(Items.BONE_MEAL)
            .setName(Component.literal("§2§l🌱 Fertilizer System"))
            .addLoreLine(Component.literal("§7Boost next harvest by §e+50%"))
            .addLoreLine(Component.literal("§7Applies to all active farms"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(fertilizerCost)))
            .addLoreLine(Component.literal("§7Active: " + (farms.hasFertilizer() ? "§a✓ YES" : "§c✗ NO")))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lCLICK §7to purchase fertilizer"))
            .setCallback((index, type, action) -> {
                FarmManager.purchaseFertilizer(player, fertilizerCost);
                updateDisplay();
            })
        );
        
        int slot = 18;
        
        for (Map.Entry<FarmType, FarmManager.FarmData> entry : farms.getFarms().entrySet()) {
            FarmType farmType = entry.getKey();
            FarmManager.FarmData data = entry.getValue();
            
            GuiElementBuilder builder = new GuiElementBuilder(farmType.getIcon())
                .setName(Component.literal("§e§l" + farmType.getDisplayName()))
                .addLoreLine(Component.literal("§7Status: " + 
                    (data.isActive() ? "§a✓ Active" : "§c✖ Inactive")))
                .addLoreLine(Component.literal("§7Daily Salary: §c-" +
                    CurrencyManager.format(farmType.getDailySalary())))
                .addLoreLine(Component.literal("§7Production: §e+" + 
                    farmType.getDailyOutput() + " " + farmType.getOutputResource().getDisplayName()))
                .addLoreLine(Component.literal("§7Total Produced: " + data.getTotalProduced()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§b§lCLICK §7to toggle on/off"));
                
            if (data.isActive()) {
                builder.glow();
            }
            
            builder.setCallback((index, type, action) -> {
                FarmManager.toggleFarm(player, farmType);
                updateDisplay();
            });
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private long getTotalHarvestValue(FarmManager.PlayerFarms farms) {
        long total = 0;
        for (Map.Entry<ResourceType, Long> entry : farms.getHarvestedResources().entrySet()) {
            total += entry.getValue() * entry.getKey().getValuePerUnit();
        }
        return total;
    }
    
    private void harvestAll(FarmManager.PlayerFarms farms) {
        long totalValue = 0;
        int totalItems = 0;
        
        for (Map.Entry<ResourceType, Long> entry : farms.getHarvestedResources().entrySet()) {
            ResourceType type = entry.getKey();
            long amount = entry.getValue();
            
            if (amount > 0) {
                long value = amount * type.getValuePerUnit();
                totalValue += value;
                totalItems += amount;
                
                // Clear the harvested resources
                farms.getHarvestedResources().put(type, 0L);
            }
        }
        
        if (totalValue > 0) {
            CurrencyManager.addMoney(player, totalValue);
            player.sendSystemMessage(Component.literal(
                "§a§l[FARM] Harvested " + totalItems + " items and sold for §6" + 
                CurrencyManager.format(totalValue) + "§a!"));
        } else {
            player.sendSystemMessage(Component.literal("§c§l[FARM] No resources to harvest!"));
        }
    }
}