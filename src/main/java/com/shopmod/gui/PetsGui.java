package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.pets.PetsManager;
import com.shopmod.pets.PetsManager.PetType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

/**
 * Pet Collection GUI
 */
public class PetsGui extends SimpleGui {
    private final ServerPlayer player;
    private final PetsManager.PetsData data;
    
    public PetsGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.data = PetsManager.getPetsData(player.getUUID());
        this.setTitle(Component.literal("§d§l🐾 Pet Collection"));
        setupDisplay();
    }
    
    private void setupDisplay() {
        // Background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            }
        }
        
        // Player info
        setSlot(4, new GuiElementBuilder(Items.NAME_TAG)
            .setName(Component.literal("§d§lPet Collection"))
            .addLoreLine(Component.literal("§7Pets Owned: §e" + data.getPetCount() + "/10"))
            .addLoreLine(Component.literal("§7Active Pet: " + 
                (data.getActivePet() != null ? data.getActivePet().getDisplayName() : "§7None")))
        );
        
        // Add all pets
        addPetButton(10, PetType.DOG, Items.BONE);
        addPetButton(11, PetType.CAT, Items.TROPICAL_FISH);
        addPetButton(12, PetType.PARROT, Items.FEATHER);
        addPetButton(13, PetType.HORSE, Items.SADDLE);
        addPetButton(14, PetType.PANDA, Items.BAMBOO);
        addPetButton(19, PetType.FOX, Items.SWEET_BERRIES);
        addPetButton(20, PetType.AXOLOTL, Items.AXOLOTL_BUCKET);
        addPetButton(21, PetType.BEE, Items.HONEYCOMB);
        addPetButton(22, PetType.DOLPHIN, Items.COD);
        addPetButton(23, PetType.DRAGON, Items.DRAGON_EGG);
        
        // Hub button
        setSlot(53, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
    
    private void addPetButton(int slot, PetType petType, net.minecraft.world.item.Item icon) {
        boolean owned = data.hasPet(petType);
        boolean active = data.getActivePet() == petType;
        
        GuiElementBuilder builder = new GuiElementBuilder(icon)
            .setName(Component.literal(petType.getDisplayName()))
            .addLoreLine(Component.literal(petType.getDescription()))
            .addLoreLine(Component.literal(""));
        
        if (!owned) {
            builder.addLoreLine(Component.literal("§7Cost: §6$" + CurrencyManager.format(petType.getCost())))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(CurrencyManager.canAfford(player, petType.getCost()) ? 
                    "§a§lCLICK TO PURCHASE" : "§c§lCannot afford"))
                .setCallback((index, type, action) -> {
                    if (PetsManager.purchasePet(player, petType)) {
                        updateDisplay();
                    }
                });
        } else {
            builder.addLoreLine(Component.literal(active ? "§a§l✓ ACTIVE" : "§7Owned"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(active ? "§7Already equipped" : "§e§lCLICK TO EQUIP"))
                .setCallback((index, type, action) -> {
                    if (!active) {
                        PetsManager.equipPet(player, petType);
                        updateDisplay();
                    }
                });
            
            if (active) {
                builder.glow();
            }
        }
        
        setSlot(slot, builder);
    }
    
    private void updateDisplay() {
        setupDisplay();
    }
}
