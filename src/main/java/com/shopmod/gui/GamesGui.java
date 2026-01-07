package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.games.GamesManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.Random;

/**
 * Games GUI - Play mini-games for starter income
 */
public class GamesGui extends SimpleGui {
    private final ServerPlayer player;
    private final GamesManager.GameData data;
    private static final Random RANDOM = new Random();
    
    public GamesGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.data = GamesManager.getGameData(player.getUUID());
        this.setTitle(Component.literal("§e§l🎮 Game Center"));
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
        
        // Player stats
        setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lGame Statistics"))
            .addLoreLine(Component.literal("§7Balance: §6$" + CurrencyManager.format(CurrencyManager.getBalance(player))))
            .addLoreLine(Component.literal("§7Games Played: §e" + data.getGamesPlayed()))
            .addLoreLine(Component.literal("§7Total Earned: §6$" + CurrencyManager.format(data.getTotalEarned())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lAll games are FREE to play!"))
            .addLoreLine(Component.literal("§7You ALWAYS win money!"))
        );
        
        // Number Guess Game
        setSlot(19, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§b§l🔢 Number Guess"))
            .addLoreLine(Component.literal("§7Guess a number 1-10!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lRewards:"))
            .addLoreLine(Component.literal("§7  Exact: §6$5,000"))
            .addLoreLine(Component.literal("§7  Close: §6$2,000"))
            .addLoreLine(Component.literal("§7  Wrong: §6$500"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .setCallback((index, type, action) -> {
                int guess = RANDOM.nextInt(10) + 1;
                GamesManager.playNumberGuess(player, guess);
                updateDisplay();
            })
        );
        
        // Coin Flip
        setSlot(20, new GuiElementBuilder(Items.SUNFLOWER)
            .setName(Component.literal("§6§l🪙 Coin Flip"))
            .addLoreLine(Component.literal("§7Flip a coin!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lRewards:"))
            .addLoreLine(Component.literal("§7  Win: §6$3,000"))
            .addLoreLine(Component.literal("§7  Lose: §6$500"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lLEFT §7= Heads"))
            .addLoreLine(Component.literal("§e§lRIGHT §7= Tails"))
            .setCallback((index, type, action) -> {
                GamesManager.playCoinFlip(player, type.isLeft);
                updateDisplay();
            })
        );
        
        // Dice Roll
        setSlot(21, new GuiElementBuilder(Items.QUARTZ)
            .setName(Component.literal("§f§l🎲 Dice Roll"))
            .addLoreLine(Component.literal("§7Roll the dice!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lRewards:"))
            .addLoreLine(Component.literal("§7  Roll 6: §6$10,000 §d★"))
            .addLoreLine(Component.literal("§7  Roll 4-5: §6$4,000"))
            .addLoreLine(Component.literal("§7  Roll 1-3: §6$1,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to roll!"))
            .setCallback((index, type, action) -> {
                GamesManager.playDiceRoll(player);
                updateDisplay();
            })
        );
        
        // High-Low
        setSlot(22, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§c§l📊 High-Low"))
            .addLoreLine(Component.literal("§7Guess higher or lower!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lRewards:"))
            .addLoreLine(Component.literal("§7  Same: §6$8,000 §d★"))
            .addLoreLine(Component.literal("§7  Correct: §6$3,500"))
            .addLoreLine(Component.literal("§7  Wrong: §6$750"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lLEFT §7= Higher"))
            .addLoreLine(Component.literal("§e§lRIGHT §7= Lower"))
            .setCallback((index, type, action) -> {
                int current = RANDOM.nextInt(10) + 1;
                GamesManager.playHighLow(player, current, type.isLeft);
                updateDisplay();
            })
        );
        
        // Lucky Slots
        setSlot(23, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§l🎰 Lucky Slots"))
            .addLoreLine(Component.literal("§7Spin the slots!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lRewards:"))
            .addLoreLine(Component.literal("§7  7️⃣7️⃣7️⃣: §6$50,000 §d§l★★★"))
            .addLoreLine(Component.literal("§7  💎💎💎: §6$25,000 §b§l★★"))
            .addLoreLine(Component.literal("§7  Triple: §6$15,000 §a★"))
            .addLoreLine(Component.literal("§7  Pair: §6$5,000"))
            .addLoreLine(Component.literal("§7  Nothing: §6$1,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to spin!"))
            .setCallback((index, type, action) -> {
                GamesManager.playLuckySlots(player);
                updateDisplay();
            })
            .glow()
        );
        
        // Daily Spin (Coming Soon)
        setSlot(25, new GuiElementBuilder(Items.EMERALD)
            .setName(Component.literal("§a§l🎁 Daily Bonus"))
            .addLoreLine(Component.literal("§7Get a free daily reward!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lComing Soon!"))
        );
        
        // Info
        setSlot(31, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§6§lℹ Game Info"))
            .addLoreLine(Component.literal("§7All games are §aCOMPLETELY FREE!"))
            .addLoreLine(Component.literal("§7You §aALWAYS§7 win money!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Perfect for:"))
            .addLoreLine(Component.literal("§7  • Getting started"))
            .addLoreLine(Component.literal("§7  • Quick cash boost"))
            .addLoreLine(Component.literal("§7  • Having fun!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lPlay as much as you want!"))
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
    
    private void updateDisplay() {
        setupDisplay();
    }
}
