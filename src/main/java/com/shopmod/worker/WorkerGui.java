package com.shopmod.worker;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.farm.FarmManager;
import com.shopmod.farm.FarmType;
import com.shopmod.mining.MiningManager;
import com.shopmod.property.PropertyManager;
import com.shopmod.property.PropertyType;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * GUI for managing workers
 */
public class WorkerGui extends SimpleGui {
    private final ServerPlayer player;
    private ViewMode viewMode = ViewMode.OVERVIEW;
    private Worker selectedWorker = null;
    private WorkerSkill selectedSkill = null;
    
    private enum ViewMode {
        OVERVIEW,
        HIRE,
        MANAGE_WORKER,
        TRAINING,
        ASSIGNMENTS
    }
    
    public WorkerGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§l⚒ Worker Management"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        // Background border
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("")));
            setSlot(45 + i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Component.literal("")));
        }
        
        switch (viewMode) {
            case OVERVIEW -> showOverview();
            case HIRE -> showHiring();
            case MANAGE_WORKER -> showWorkerDetails();
            case TRAINING -> showTraining();
            case ASSIGNMENTS -> showAssignments();
        }
        
        // Navigation buttons
        setSlot(45, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§e§lOverview"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.OVERVIEW;
                selectedWorker = null;
                setupDisplay();
            })
        );
        
        setSlot(46, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lHire Worker"))
            .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(WorkerManager.getHiringFee())))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.HIRE;
                setupDisplay();
            })
        );
        
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lClose"))
            .setCallback((index, type, action) -> close())
        );
    }
    
    private void showOverview() {
        List<Worker> workers = WorkerManager.getPlayerWorkers(player.getUUID());
        
        // Info panel
        setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
            .setName(Component.literal("§6§lWorker Overview"))
            .addLoreLine(Component.literal("§7Total Workers: §e" + workers.size() + "/10"))
            .addLoreLine(Component.literal("§7Daily Salaries: §6" + CurrencyManager.format(WorkerManager.getDailySalaries(player.getUUID()))))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Workers boost:"))
            .addLoreLine(Component.literal("§7• Farm efficiency (+25%)"))
            .addLoreLine(Component.literal("§7• Mine downtime (-20%)"))
            .addLoreLine(Component.literal("§7• Repair costs (-30%)"))
        );
        
        if (workers.isEmpty()) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Workers"))
                .addLoreLine(Component.literal("§7Hire workers to boost"))
                .addLoreLine(Component.literal("§7your farms, mines, and properties!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lClick 'Hire Worker' below"))
            );
            return;
        }
        
        // Display workers (up to 28 slots)
        int[] workerSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 
                             28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        
        for (int i = 0; i < Math.min(workers.size(), workerSlots.length); i++) {
            Worker worker = workers.get(i);
            int slot = workerSlots[i];
            
            setSlot(slot, new GuiElementBuilder(getWorkerIcon(worker.getType()))
                .setName(Component.literal("§e§l" + worker.getName()))
                .addLoreLine(Component.literal("§7Type: §f" + worker.getType().getDisplayName()))
                .addLoreLine(Component.literal("§7Loyalty: " + getLoyaltyColor(worker.getLoyalty()) + worker.getLoyalty() + "%"))
                .addLoreLine(Component.literal("§7Salary: §6" + CurrencyManager.format(worker.getDailySalary())))
                .addLoreLine(Component.literal("§7Assignment: " + (worker.isAssigned() ? "§a" + worker.getAssignedTo() : "§c✗ None")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK §7to manage"))
                .setCallback((index, type, action) -> {
                    selectedWorker = worker;
                    viewMode = ViewMode.MANAGE_WORKER;
                    setupDisplay();
                })
            );
        }
    }
    
    private void showHiring() {
        setSlot(4, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§lHire Worker"))
            .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(WorkerManager.getHiringFee())))
            .addLoreLine(Component.literal("§7Workers: §e" + WorkerManager.getPlayerWorkers(player.getUUID()).size() + "/10"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Choose a worker type:"))
        );
        
        // Farm Hand
        setSlot(20, new GuiElementBuilder(Items.WHEAT)
            .setName(Component.literal("§2§lFarm Hand"))
            .addLoreLine(Component.literal("§7Specialty: Farming"))
            .addLoreLine(Component.literal("§7Starting Salary: §6$100/day"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Primary Skill: Harvesting"))
            .addLoreLine(Component.literal("§7Boosts farm yield by 25%"))
            .addLoreLine(Component.literal("§7when skill level 5+"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to hire"))
            .setCallback((index, type, action) -> {
                String name = WorkerManager.generateRandomName();
                Worker worker = WorkerManager.hireWorker(player, WorkerType.FARM_HAND, name);
                if (worker != null) {
                    viewMode = ViewMode.OVERVIEW;
                    setupDisplay();
                }
            })
        );
        
        // Miner
        setSlot(22, new GuiElementBuilder(Items.IRON_PICKAXE)
            .setName(Component.literal("§8§lMiner"))
            .addLoreLine(Component.literal("§7Specialty: Mining"))
            .addLoreLine(Component.literal("§7Starting Salary: §6$100/day"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Primary Skill: Mining"))
            .addLoreLine(Component.literal("§7Reduces mine downtime by 20%"))
            .addLoreLine(Component.literal("§7when skill level 5+"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to hire"))
            .setCallback((index, type, action) -> {
                String name = WorkerManager.generateRandomName();
                Worker worker = WorkerManager.hireWorker(player, WorkerType.MINER, name);
                if (worker != null) {
                    viewMode = ViewMode.OVERVIEW;
                    setupDisplay();
                }
            })
        );
        
        // Property Manager
        setSlot(24, new GuiElementBuilder(Items.OAK_DOOR)
            .setName(Component.literal("§6§lProperty Manager"))
            .addLoreLine(Component.literal("§7Specialty: Real Estate"))
            .addLoreLine(Component.literal("§7Starting Salary: §6$100/day"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Primary Skill: Maintenance"))
            .addLoreLine(Component.literal("§7Reduces repair costs by 30%"))
            .addLoreLine(Component.literal("§7when skill level 5+"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK §7to hire"))
            .setCallback((index, type, action) -> {
                String name = WorkerManager.generateRandomName();
                Worker worker = WorkerManager.hireWorker(player, WorkerType.PROPERTY_MANAGER, name);
                if (worker != null) {
                    viewMode = ViewMode.OVERVIEW;
                    setupDisplay();
                }
            })
        );
    }
    
    private void showWorkerDetails() {
        if (selectedWorker == null) {
            viewMode = ViewMode.OVERVIEW;
            setupDisplay();
            return;
        }
        
        // Worker info
        setSlot(4, new GuiElementBuilder(getWorkerIcon(selectedWorker.getType()))
            .setName(Component.literal("§e§l" + selectedWorker.getName()))
            .addLoreLine(Component.literal("§7Type: §f" + selectedWorker.getType().getDisplayName()))
            .addLoreLine(Component.literal("§7Loyalty: " + getLoyaltyColor(selectedWorker.getLoyalty()) + selectedWorker.getLoyalty() + "%"))
            .addLoreLine(Component.literal("§7Experience: §b" + selectedWorker.getExperience()))
            .addLoreLine(Component.literal("§7Daily Salary: §6" + CurrencyManager.format(selectedWorker.getDailySalary())))
            .addLoreLine(Component.literal("§7Assignment: " + (selectedWorker.isAssigned() ? "§a" + selectedWorker.getAssignedTo() : "§c✗ None")))
        );
        
        // Skills display
        int skillSlot = 19;
        for (WorkerSkill skill : WorkerSkill.values()) {
            if (!skill.isApplicableTo(selectedWorker.getType())) {
                continue;
            }
            
            int level = selectedWorker.getSkillLevel(skill);
            
            setSlot(skillSlot++, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                .setName(Component.literal("§a" + skill.getDisplayName()))
                .addLoreLine(Component.literal("§7Level: §e" + level + "/10"))
                .addLoreLine(Component.literal("§7" + skill.getDescription()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK §7to train"))
                .setCallback((index, type, action) -> {
                    selectedSkill = skill;
                    viewMode = ViewMode.TRAINING;
                    setupDisplay();
                })
            );
        }
        
        // Assign/Unassign button
        if (selectedWorker.isAssigned()) {
            setSlot(31, new GuiElementBuilder(Items.RED_WOOL)
                .setName(Component.literal("§c§lUnassign"))
                .addLoreLine(Component.literal("§7Remove from current assignment"))
                .setCallback((index, type, action) -> {
                    WorkerManager.unassignWorker(player, selectedWorker.getWorkerId());
                    setupDisplay();
                })
            );
        } else {
            setSlot(31, new GuiElementBuilder(Items.GREEN_WOOL)
                .setName(Component.literal("§a§lAssign"))
                .addLoreLine(Component.literal("§7Assign to farm/mine/property"))
                .setCallback((index, type, action) -> {
                    viewMode = ViewMode.ASSIGNMENTS;
                    setupDisplay();
                })
            );
        }
        
        // Fire button
        setSlot(40, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§lFire Worker"))
            .addLoreLine(Component.literal("§7Permanently remove this worker"))
            .addLoreLine(Component.literal("§cNo refund!"))
            .setCallback((index, type, action) -> {
                WorkerManager.fireWorker(player, selectedWorker.getWorkerId());
                selectedWorker = null;
                viewMode = ViewMode.OVERVIEW;
                setupDisplay();
            })
        );
    }
    
    private void showTraining() {
        if (selectedWorker == null || selectedSkill == null) {
            viewMode = ViewMode.OVERVIEW;
            setupDisplay();
            return;
        }
        
        int currentLevel = selectedWorker.getSkillLevel(selectedSkill);
        boolean canTrain = currentLevel < 10;
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        boolean onCooldown = selectedWorker.getLastTrainingDay() >= currentDay;
        
        setSlot(4, new GuiElementBuilder(Items.ENCHANTED_BOOK)
            .setName(Component.literal("§d§lTraining"))
            .addLoreLine(Component.literal("§7Worker: §e" + selectedWorker.getName()))
            .addLoreLine(Component.literal("§7Skill: §a" + selectedSkill.getDisplayName()))
            .addLoreLine(Component.literal("§7Current Level: §e" + currentLevel + "/10"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(WorkerManager.getTrainingCost())))
            .addLoreLine(Component.literal("§7Cooldown: §e1 day"))
        );
        
        if (canTrain && !onCooldown) {
            setSlot(22, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Component.literal("§a§lConfirm Training"))
                .addLoreLine(Component.literal("§7Train " + selectedSkill.getDisplayName()))
                .addLoreLine(Component.literal("§7Level " + currentLevel + " → " + (currentLevel + 1)))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Cost: §6" + CurrencyManager.format(WorkerManager.getTrainingCost())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§e§lCLICK §7to train"))
                .setCallback((index, type, action) -> {
                    if (WorkerManager.trainWorker(player, selectedWorker.getWorkerId(), selectedSkill)) {
                        viewMode = ViewMode.MANAGE_WORKER;
                        selectedSkill = null;
                        setupDisplay();
                    }
                })
            );
        } else if (onCooldown) {
            setSlot(22, new GuiElementBuilder(Items.GRAY_DYE)
                .setName(Component.literal("§7§lOn Cooldown"))
                .addLoreLine(Component.literal("§c" + selectedWorker.getName() + " is tired!"))
                .addLoreLine(Component.literal("§7Can train again tomorrow"))
            );
        } else {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lMax Level Reached"))
                .addLoreLine(Component.literal("§7" + selectedSkill.getDisplayName() + " is at maximum!"))
            );
        }
        
        setSlot(48, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.MANAGE_WORKER;
                selectedSkill = null;
                setupDisplay();
            })
        );
    }
    
    private void showAssignments() {
        if (selectedWorker == null) {
            viewMode = ViewMode.OVERVIEW;
            setupDisplay();
            return;
        }
        
        setSlot(4, new GuiElementBuilder(Items.COMPASS)
            .setName(Component.literal("§6§lAssignments"))
            .addLoreLine(Component.literal("§7Worker: §e" + selectedWorker.getName()))
            .addLoreLine(Component.literal("§7Type: §f" + selectedWorker.getType().getDisplayName()))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Select an assignment:"))
        );
        
        int slot = 19;
        
        // Show assignments based on worker type
        switch (selectedWorker.getType()) {
            case FARM_HAND -> {
                FarmManager.PlayerFarms farms = FarmManager.getPlayerFarms(player.getUUID());
                for (FarmType farmType : FarmType.values()) {
                    if (farms.getFarms().containsKey(farmType)) {
                        setSlot(slot++, new GuiElementBuilder(Items.WHEAT)
                            .setName(Component.literal("§2§l" + farmType.getDisplayName()))
                            .addLoreLine(Component.literal("§7Assign to this farm"))
                            .addLoreLine(Component.literal("§7+25% yield boost"))
                            .setCallback((index, type, action) -> {
                                WorkerManager.assignWorker(player, selectedWorker.getWorkerId(), "FARM_" + farmType.name());
                                viewMode = ViewMode.MANAGE_WORKER;
                                setupDisplay();
                            })
                        );
                    }
                }
            }
            case MINER -> {
                MiningManager.MiningData miningData = MiningManager.getMiningData(player.getUUID());
                for (MiningManager.MineType mineType : MiningManager.MineType.values()) {
                    if (miningData.hasMine(mineType)) {
                        setSlot(slot++, new GuiElementBuilder(Items.IRON_PICKAXE)
                            .setName(Component.literal("§8§l" + mineType.getDisplayName()))
                            .addLoreLine(Component.literal("§7Assign to this mine"))
                            .addLoreLine(Component.literal("§7-20% downtime"))
                            .setCallback((index, type, action) -> {
                                WorkerManager.assignWorker(player, selectedWorker.getWorkerId(), "MINE_" + mineType.name());
                                viewMode = ViewMode.MANAGE_WORKER;
                                setupDisplay();
                            })
                        );
                    }
                }
            }
            case PROPERTY_MANAGER -> {
                PropertyManager.PlayerProperties props = PropertyManager.getPlayerProperties(player.getUUID());
                for (PropertyType propType : PropertyType.values()) {
                    if (props.getProperties().containsKey(propType)) {
                        setSlot(slot++, new GuiElementBuilder(Items.OAK_DOOR)
                            .setName(Component.literal("§6§l" + propType.getDisplayName()))
                            .addLoreLine(Component.literal("§7Assign to this property"))
                            .addLoreLine(Component.literal("§7-30% repair costs"))
                            .setCallback((index, type, action) -> {
                                WorkerManager.assignWorker(player, selectedWorker.getWorkerId(), "PROPERTY_" + propType.name());
                                viewMode = ViewMode.MANAGE_WORKER;
                                setupDisplay();
                            })
                        );
                    }
                }
            }
        }
        
        if (slot == 19) {
            setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lNo Available Assignments"))
                .addLoreLine(Component.literal("§7You don't own any compatible"))
                .addLoreLine(Component.literal("§7farms, mines, or properties!"))
            );
        }
        
        setSlot(48, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§lBack"))
            .setCallback((index, type, action) -> {
                viewMode = ViewMode.MANAGE_WORKER;
                setupDisplay();
            })
        );
    }
    
    private net.minecraft.world.item.Item getWorkerIcon(WorkerType type) {
        return switch (type) {
            case FARM_HAND -> Items.WHEAT;
            case MINER -> Items.IRON_PICKAXE;
            case PROPERTY_MANAGER -> Items.OAK_DOOR;
        };
    }
    
    private String getLoyaltyColor(int loyalty) {
        if (loyalty >= 80) return "§a";
        if (loyalty >= 60) return "§e";
        if (loyalty >= 40) return "§6";
        if (loyalty >= 20) return "§c";
        return "§4";
    }
}
