package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.village.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * Village Management GUI - Hire workers, manage resources, build structures
 */
public class VillageGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode currentView = ViewMode.WORKERS;
    
    private enum ViewMode {
        WORKERS, RESOURCES, BUILDINGS, SETTINGS
    }
    
    public VillageGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§a§lVillage Management"));
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
        
        // Tab switchers
        setupTabs();
        
        // Main content
        switch (currentView) {
            case WORKERS -> displayWorkersView(village);
            case RESOURCES -> displayResourcesView(village);
            case BUILDINGS -> displayBuildingsView(village);
            case SETTINGS -> displaySettingsView(village);
        }
    }
    
    private void setupInfoBar(VillageManager.Village village) {
        // Village level
        GuiElementBuilder levelBuilder = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
            .setName(Component.literal("§e§lVillage Level " + village.getVillageLevel()))
            .addLoreLine(Component.literal("§71=Hamlet, 2=Village"))
            .addLoreLine(Component.literal("§73=Town, 4=City"));
        
        // Add upgrade cost if not max level
        if (village.getVillageLevel() < 4) {
            long[] costs = {100000L, 500000L, 2000000L};
            long upgradeCost = costs[village.getVillageLevel() - 1];
            boolean canAfford = CurrencyManager.canAfford(player, upgradeCost);
            
            levelBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Upgrade Cost: " + 
                    (canAfford ? "§a" : "§c") + "$" + CurrencyManager.format(upgradeCost)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(canAfford ? "§a§lCLICK §7to upgrade (Coming Soon)" : "§c§lInsufficient funds!"));
        } else {
            levelBuilder.addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§d§l⭐ MAX LEVEL ⭐"));
        }
        
        levelBuilder.setCallback((index, type, action) -> {
            player.sendSystemMessage(Component.literal("§c§l[VILLAGE] Upgrade feature coming soon!"));
            updateDisplay();
        });
        
        setSlot(2, levelBuilder);
        
        // Worker count
        int workerCount = village.getTotalWorkerCount();
        int workerSlots = village.getTotalWorkerSlots();
        setSlot(4, new GuiElementBuilder(Items.VILLAGER_SPAWN_EGG)
            .setName(Component.literal("§b§lWorkers: " + workerCount + "/" + workerSlots))
            .addLoreLine(Component.literal("§7Build houses for more slots"))
        );
        
        // Balance
        setSlot(6, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lBalance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    private void setupTabs() {
        // Workers tab
        setSlot(45, new GuiElementBuilder(currentView == ViewMode.WORKERS ? Items.EMERALD_BLOCK : Items.EMERALD)
            .setName(Component.literal("§a§lWorkers"))
            .addLoreLine(Component.literal("§7Hire and manage"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.WORKERS;
                updateDisplay();
            })
        );
        
        // Resources tab
        setSlot(46, new GuiElementBuilder(currentView == ViewMode.RESOURCES ? Items.CHEST : Items.BARREL)
            .setName(Component.literal("§e§lResources"))
            .addLoreLine(Component.literal("§7View storage"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.RESOURCES;
                updateDisplay();
            })
        );
        
        // Buildings tab
        setSlot(47, new GuiElementBuilder(currentView == ViewMode.BUILDINGS ? Items.BRICK : Items.BRICKS)
            .setName(Component.literal("§6§lBuildings"))
            .addLoreLine(Component.literal("§7Construct structures"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.BUILDINGS;
                updateDisplay();
            })
        );
        
        // Settings tab
        setSlot(48, new GuiElementBuilder(currentView == ViewMode.SETTINGS ? Items.COMPARATOR : Items.REPEATER)
            .setName(Component.literal("§7§lSettings"))
            .addLoreLine(Component.literal("§7Auto-manage mode"))
            .setCallback((index, type, action) -> {
                currentView = ViewMode.SETTINGS;
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
    
    private void displayWorkersView(VillageManager.Village village) {
        int slot = 18;
        
        for (VillagerWorker workerType : VillagerWorker.values()) {
            boolean canHire = workerType.getRequiredVillageLevel() <= village.getVillageLevel();
            VillageManager.WorkerData data = village.getWorkers().get(workerType);
            int count = data != null ? data.getCount() : 0;
            
            GuiElementBuilder builder = new GuiElementBuilder(workerType.getIcon())
                .setName(Component.literal((count > 0 ? "§a" : canHire ? "§7" : "§c") + 
                    "§l" + workerType.getDisplayName() + (count > 0 ? " x" + count : "")))
                .addLoreLine(Component.literal("§7" + workerType.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Hire Cost: §6$" + CurrencyManager.format(workerType.getHireCost())))
                .addLoreLine(Component.literal("§7Daily Salary: §c-$" + CurrencyManager.format(workerType.getDailySalary())));
            
            // Show inputs/outputs
            if (!workerType.getDailyInputs().isEmpty()) {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§7Consumes:"));
                for (var entry : workerType.getDailyInputs().entrySet()) {
                    builder.addLoreLine(Component.literal("§7  • " + entry.getValue() + " " + entry.getKey().getDisplayName()));
                }
            }
            
            if (!workerType.getDailyOutputs().isEmpty()) {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§7Produces:"));
                for (var entry : workerType.getDailyOutputs().entrySet()) {
                    builder.addLoreLine(Component.literal("§a  • " + entry.getValue() + " " + entry.getKey().getDisplayName()));
                }
            }
            
            if (canHire) {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§a§lLEFT CLICK §7hire one"));
                builder.addLoreLine(Component.literal("§e§lSHIFT+LEFT §7hire 5"));
                if (count > 0) {
                    builder.addLoreLine(Component.literal("§c§lRIGHT CLICK §7fire one"));
                }
                
                builder.setCallback((index, type, action) -> {
                    if (type.isLeft && type.shift) {
                        // Hire 5
                        for (int i = 0; i < 5; i++) {
                            VillageManager.hireWorker(player, workerType);
                        }
                        updateDisplay();
                    } else if (type.isLeft) {
                        // Hire 1
                        VillageManager.hireWorker(player, workerType);
                        updateDisplay();
                    } else if (type.isRight && count > 0) {
                        // Fire 1
                        VillageManager.fireWorker(player, workerType);
                        updateDisplay();
                    }
                });
            } else {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("§c§lLOCKED"));
                builder.addLoreLine(Component.literal("§7Requires Village Level " + workerType.getRequiredVillageLevel()));
            }
            
            setSlot(slot, builder);
            slot++;
            if (slot % 9 == 8 || slot >= 44) break;
        }
    }
    
    private void displayResourcesView(VillageManager.Village village) {
        int slot = 10;
        
        for (ResourceType type : ResourceType.values()) {
            long amount = village.getResources().getOrDefault(type, 0L);
            long capacity = village.getStorageCapacity().getOrDefault(type, 0L);
            
            GuiElementBuilder builder = new GuiElementBuilder(type.getRepresentativeItem())
                .setName(Component.literal("§e" + type.getIcon() + " " + type.getDisplayName()))
                .addLoreLine(Component.literal("§7Amount: §e" + amount + "/" + capacity))
                .addLoreLine(Component.literal("§7Value: §6$" + CurrencyManager.format(amount * type.getValuePerUnit())))
                .addLoreLine(Component.literal(""));
            
            if (amount > 0) {
                builder.addLoreLine(Component.literal("§a§lLEFT CLICK §7sell 10"));
                builder.addLoreLine(Component.literal("§e§lSHIFT+LEFT §7sell all"));
                
                builder.setCallback((index, clickType, action) -> {
                    player.sendSystemMessage(Component.literal("§c§l[VILLAGE] Sell feature coming soon!"));
                    updateDisplay();
                });
            }
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displayBuildingsView(VillageManager.Village village) {
        int slot = 18;
        
        for (VillageBuilding building : VillageBuilding.values()) {
            int count = village.getBuildings().getOrDefault(building, 0);
            boolean canBuild = true;
            
            GuiElementBuilder builder = new GuiElementBuilder(building.getIcon())
                .setName(Component.literal((count > 0 ? "§a" : canBuild ? "§7" : "§c") + 
                    "§l" + building.getDisplayName() + (count > 0 ? " x" + count : "")))
                .addLoreLine(Component.literal("§7" + building.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Worker Slots: §e+" + building.getWorkerSlots()));
            
            builder.addLoreLine(Component.literal(""));
            builder.addLoreLine(Component.literal("§a§lCLICK §7to build"));
            
            builder.setCallback((index, type, action) -> {
                if (VillageManager.buildBuilding(player, building)) {
                    player.sendSystemMessage(Component.literal("§a§l[VILLAGE] Built " + building.getDisplayName() + "!"));
                } else {
                    player.sendSystemMessage(Component.literal("§c§l[VILLAGE] Cannot afford building!"));
                }
                updateDisplay();
            });
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void displaySettingsView(VillageManager.Village village) {
        // Auto-manage toggle
        setSlot(22, new GuiElementBuilder(village.isAutoManage() ? Items.GREEN_WOOL : Items.RED_WOOL)
            .setName(Component.literal(village.isAutoManage() ? "§a§lAuto-Manage: ON" : "§c§lAuto-Manage: OFF"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7When enabled:"))
            .addLoreLine(Component.literal("§7  • Workers fed automatically"))
            .addLoreLine(Component.literal("§7  • -30% efficiency penalty"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7When disabled:"))
            .addLoreLine(Component.literal("§7  • Manual resource management"))
            .addLoreLine(Component.literal("§7  • Full efficiency"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to toggle"))
            .setCallback((index, type, action) -> {
                VillageManager.toggleAutoManage(player);
                updateDisplay();
            })
        );
        
        // Village stats
        setSlot(31, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§6§lVillage Statistics"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Workers: §e" + village.getTotalWorkerCount()))
            .addLoreLine(Component.literal("§7Worker Slots: §e" + village.getTotalWorkerSlots()))
            .addLoreLine(Component.literal("§7Buildings: §e" + village.getBuildings().size()))
            .addLoreLine(Component.literal("§7All-Time Earnings: §6$" + 
                CurrencyManager.format(village.getTotalEarningsAllTime())))
        );
    }
}
