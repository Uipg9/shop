package com.shopmod.gui;

import com.shopmod.currency.CurrencyManager;
import com.shopmod.games.GamesManager;
import com.shopmod.games.GamesManager.*;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

import java.util.Random;

/**
 * Interactive Games GUI v2.0
 * Now with REAL gameplay mechanics and animations!
 */
public class GamesGui extends SimpleGui {
    private final ServerPlayer player;
    private final GameData data;
    private static final Random RANDOM = new Random();
    
    private GameType currentView = null;  // null = lobby
    private int animationTick = 0;
    
    public GamesGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.data = GamesManager.getGameData(player.getUUID());
        this.setTitle(Component.literal("§e§l🎮 Game Center"));
        showLobby();
    }
    
    /**
     * Animation helper - cycles through items in a slot
     */
    private void animateSlot(int slot, Item[] sequence, int delayTicks) {
        if (animationTick % delayTicks == 0) {
            int index = (animationTick / delayTicks) % sequence.length;
            setSlot(slot, new GuiElementBuilder(sequence[index]).setName(Component.literal("")));
        }
        animationTick++;
    }
    
    /**
     * LOBBY - Main game selection screen
     */
    private void showLobby() {
        currentView = null;
        this.setTitle(Component.literal("§e§l🎮 Game Center - Lobby"));
        
        // Clear and setup background
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal("")));
            } else {
                setSlot(i, new GuiElementBuilder(Items.AIR));
            }
        }
        
        // Player stats
        setSlot(4, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§e§lYour Statistics"))
            .addLoreLine(Component.literal("§7Balance: §6$" + CurrencyManager.format(CurrencyManager.getBalance(player))))
            .addLoreLine(Component.literal("§7Games Played: §e" + data.getGamesPlayed()))
            .addLoreLine(Component.literal("§7Total Earned: §6$" + CurrencyManager.format(data.getTotalEarned())))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lAll games require entry fees"))
            .addLoreLine(Component.literal("§7Click any game to play!"))
        );
        
        // Game 1: Number Guess
        setSlot(10, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§b§l🔢 Number Guess"))
            .addLoreLine(Component.literal("§7Choose from 10 numbers!"))
            .addLoreLine(Component.literal("§7Interactive selection"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$200"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$5,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.NUMBER_GUESS, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .setCallback((index, type, action) -> showNumberGuess())
        );
        
        // Game 2: Coin Flip
        setSlot(11, new GuiElementBuilder(Items.SUNFLOWER)
            .setName(Component.literal("§6§l🪙 Coin Flip"))
            .addLoreLine(Component.literal("§7Choose Heads or Tails!"))
            .addLoreLine(Component.literal("§7Watch the flip animation"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$500"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$3,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.COIN_FLIP, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .setCallback((index, type, action) -> showCoinFlip())
        );
        
        // Game 3: Dice Roll
        setSlot(12, new GuiElementBuilder(Items.QUARTZ)
            .setName(Component.literal("§f§l🎲 Dice Roll"))
            .addLoreLine(Component.literal("§7Roll and watch the die!"))
            .addLoreLine(Component.literal("§7Visual die display"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$1,000"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$10,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.DICE_ROLL, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .setCallback((index, type, action) -> showDiceRoll())
        );
        
        // Game 4: High-Low
        setSlot(13, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§c§l📊 High-Low"))
            .addLoreLine(Component.literal("§7Multi-round strategy!"))
            .addLoreLine(Component.literal("§7Cash out or continue"))
            .addLoreLine(Component.literal("§7Multiplier bonuses"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$300"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$10,000+"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.HIGH_LOW, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .setCallback((index, type, action) -> showHighLow())
        );
        
        // Game 5: Lucky Slots
        setSlot(14, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§l🎰 Lucky Slots"))
            .addLoreLine(Component.literal("§7Spin the reels!"))
            .addLoreLine(Component.literal("§7Animated slot machine"))
            .addLoreLine(Component.literal("§7Massive jackpots"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$2,000"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$50,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.SLOTS, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .glow()
            .setCallback((index, type, action) -> showSlots())
        );
        
        // Game 6: Blackjack (NEW!)
        setSlot(28, new GuiElementBuilder(Items.PAINTING)
            .setName(Component.literal("§0§l🃏 Blackjack"))
            .addLoreLine(Component.literal("§7Classic card game!"))
            .addLoreLine(Component.literal("§7Hit, Stand, or Double"))
            .addLoreLine(Component.literal("§7Beat the dealer"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$1,000"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$2,500"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§d§l★ NEW GAME!"))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.BLACKJACK, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .glow()
            .setCallback((index, type, action) -> showBlackjack())
        );
        
        // Game 7: Roulette (NEW!)
        setSlot(29, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c§l🎡 Roulette"))
            .addLoreLine(Component.literal("§7Spin the wheel!"))
            .addLoreLine(Component.literal("§7Multiple bet types"))
            .addLoreLine(Component.literal("§7European style (0-36)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lEntry: §6$2,000 + Bet"))
            .addLoreLine(Component.literal("§a§lMax Win: §6$72,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§d§l★ NEW GAME!"))
            .addLoreLine(Component.literal("§8Played: " + data.getGamesPlayedByType().getOrDefault(GameType.ROULETTE, 0)))
            .addLoreLine(Component.literal("§e§lCLICK to play!"))
            .glow()
            .setCallback((index, type, action) -> showRoulette())
        );
        
        // Info
        setSlot(31, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§6§lℹ How to Play"))
            .addLoreLine(Component.literal("§7Each game is now interactive!"))
            .addLoreLine(Component.literal("§7No more instant results"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lNew Features:"))
            .addLoreLine(Component.literal("§7• Visual animations"))
            .addLoreLine(Component.literal("§7• Strategic choices"))
            .addLoreLine(Component.literal("§7• Multi-round gameplay"))
            .addLoreLine(Component.literal("§7• Real game mechanics"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§l2 brand new games added!"))
        );
        
        // Hub button
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§6§l✦ Back to Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> new HubGui(player).open())
        );
    }
    
    /**
     * NUMBER GUESS GAME - Interactive number selection
     */
    private void showNumberGuess() {
        currentView = GameType.NUMBER_GUESS;
        this.setTitle(Component.literal("§b§l🔢 Number Guess"));
        
        // Clear display
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        // Background
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        // Instructions
        setSlot(4, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§b§lNumber Guess"))
            .addLoreLine(Component.literal("§7Pick a number 1-10!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lPrizes:"))
            .addLoreLine(Component.literal("§7• Exact match: §6$5,000"))
            .addLoreLine(Component.literal("§7• Off by 1: §6$2,000"))
            .addLoreLine(Component.literal("§7• Wrong: §6$500"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCost: $200"))
        );
        
        // Number buttons 1-10
        Item[] numberItems = {
            Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL,
            Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL,
            Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL
        };
        
        for (int i = 0; i < 10; i++) {
            final int number = i + 1;
            int slot = 20 + i + (i / 5) * 4;  // Layout: slots 20-24, 29-33
            
            setSlot(slot, new GuiElementBuilder(numberItems[i])
                .setName(Component.literal("§e§l" + number))
                .addLoreLine(Component.literal("§7Click to guess " + number))
                .setCallback((index, type, action) -> {
                    int secret = GamesManager.startNumberGuess(player);
                    if (secret != -1) {
                        GamesManager.guessNumber(player, number);
                        showLobby();
                    }
                })
            );
        }
        
        // Back button
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§l← Back to Lobby"))
            .setCallback((index, type, action) -> showLobby())
        );
    }
    
    /**
     * COIN FLIP GAME - Choose heads or tails
     */
    private void showCoinFlip() {
        currentView = GameType.COIN_FLIP;
        this.setTitle(Component.literal("§6§l🪙 Coin Flip"));
        
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        setSlot(4, new GuiElementBuilder(Items.SUNFLOWER)
            .setName(Component.literal("§6§lCoin Flip"))
            .addLoreLine(Component.literal("§7Choose your side!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lWin: §6$3,000"))
            .addLoreLine(Component.literal("§7Lose: §6$500"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCost: $500"))
        );
        
        // HEADS button
        setSlot(21, new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(Component.literal("§e§l§nHEADS"))
            .addLoreLine(Component.literal("§7The gold side"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to choose HEADS"))
            .glow()
            .setCallback((index, type, action) -> {
                GamesManager.startCoinFlip(player);
                GamesManager.chooseCoinSide(player, true);
                showLobby();
            })
        );
        
        // TAILS button
        setSlot(23, new GuiElementBuilder(Items.IRON_BLOCK)
            .setName(Component.literal("§7§l§nTAILS"))
            .addLoreLine(Component.literal("§7The silver side"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§e§lCLICK to choose TAILS"))
            .glow()
            .setCallback((index, type, action) -> {
                GamesManager.startCoinFlip(player);
                GamesManager.chooseCoinSide(player, false);
                showLobby();
            })
        );
        
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§l← Back to Lobby"))
            .setCallback((index, type, action) -> showLobby())
        );
    }
    
    /**
     * DICE ROLL GAME - Roll and see result
     */
    private void showDiceRoll() {
        currentView = GameType.DICE_ROLL;
        this.setTitle(Component.literal("§f§l🎲 Dice Roll"));
        
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        setSlot(4, new GuiElementBuilder(Items.QUARTZ)
            .setName(Component.literal("§f§lDice Roll"))
            .addLoreLine(Component.literal("§7Roll the die!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lPrizes:"))
            .addLoreLine(Component.literal("§7• Roll 6: §6$10,000"))
            .addLoreLine(Component.literal("§7• Roll 4-5: §6$4,000"))
            .addLoreLine(Component.literal("§7• Roll 1-3: §6$1,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCost: $1,000"))
        );
        
        // Roll button
        setSlot(22, new GuiElementBuilder(Items.QUARTZ_BLOCK)
            .setName(Component.literal("§f§l§n🎲 ROLL DICE"))
            .addLoreLine(Component.literal("§7Click to roll!"))
            .glow()
            .setCallback((index, type, action) -> {
                int roll = GamesManager.rollDice(player);
                if (roll != -1) {
                    showLobby();
                }
            })
        );
        
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§l← Back to Lobby"))
            .setCallback((index, type, action) -> showLobby())
        );
    }



    /**
     * HIGH-LOW GAME - Multi-round with cash out
     */
    private void showHighLow() {
        GameSession session = GamesManager.getSession(player.getUUID());
        
        if (session == null || session.type != GameType.HIGH_LOW) {
            GamesManager.startHighLow(player);
            session = GamesManager.getSession(player.getUUID());
            if (session == null) return;
        }
        
        currentView = GameType.HIGH_LOW;
        HighLowState state = (HighLowState) session.gameState;
        this.setTitle(Component.literal("§c§l📊 High-Low - Round " + state.round));
        
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        setSlot(4, new GuiElementBuilder(Items.COMPARATOR)
            .setName(Component.literal("§c§lHigh-Low"))
            .addLoreLine(Component.literal("§7Current number: §e" + state.currentNumber))
            .addLoreLine(Component.literal("§7Round: §e" + state.round))
            .addLoreLine(Component.literal("§7Multiplier: §6x" + String.format("%.1f", state.multiplier)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Current winnings: §6$" + CurrencyManager.format(state.currentWinnings)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Will next be higher or lower?"))
        );
        
        setSlot(20, new GuiElementBuilder(Items.LIME_WOOL)
            .setName(Component.literal("§a§l§n⬆ HIGHER"))
            .addLoreLine(Component.literal("§7Guess the next number"))
            .addLoreLine(Component.literal("§7will be HIGHER than §e" + state.currentNumber))
            .glow()
            .setCallback((index, type, action) -> {
                GamesManager.playHighLowRound(player, true);
                GameSession check = GamesManager.getSession(player.getUUID());
                if (check != null && check.type == GameType.HIGH_LOW) {
                    showHighLow();
                } else {
                    showLobby();
                }
            })
        );
        
        setSlot(24, new GuiElementBuilder(Items.RED_WOOL)
            .setName(Component.literal("§c§l§n⬇ LOWER"))
            .addLoreLine(Component.literal("§7Guess the next number"))
            .addLoreLine(Component.literal("§7will be LOWER than §e" + state.currentNumber))
            .glow()
            .setCallback((index, type, action) -> {
                GamesManager.playHighLowRound(player, false);
                GameSession check = GamesManager.getSession(player.getUUID());
                if (check != null && check.type == GameType.HIGH_LOW) {
                    showHighLow();
                } else {
                    showLobby();
                }
            })
        );
        
        if (state.round > 1) {
            setSlot(40, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("§a§l💰 CASH OUT"))
                .addLoreLine(Component.literal("§7Take your winnings and leave"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6§l$" + CurrencyManager.format(state.currentWinnings)))
                .glow()
                .setCallback((index, type, action) -> {
                    GamesManager.cashOutHighLow(player);
                    showLobby();
                })
            );
        }
        
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§l✖ Forfeit"))
            .addLoreLine(Component.literal("§7Leave the game"))
            .addLoreLine(Component.literal("§c(No winnings)"))
            .setCallback((index, type, action) -> {
                GamesManager.endSession(player.getUUID());
                showLobby();
            })
        );
    }
    
    /**
     * SLOTS GAME - Spin the reels
     */
    private void showSlots() {
        currentView = GameType.SLOTS;
        this.setTitle(Component.literal("§d§l🎰 Lucky Slots"));
        
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§lLucky Slots"))
            .addLoreLine(Component.literal("§7Spin for big prizes!"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§a§lPrizes:"))
            .addLoreLine(Component.literal("§7• 7️⃣7️⃣7️⃣: §6$50,000"))
            .addLoreLine(Component.literal("§7• 💎💎💎: §6$25,000"))
            .addLoreLine(Component.literal("§7• Triple: §6$15,000"))
            .addLoreLine(Component.literal("§7• Pair: §6$5,000"))
            .addLoreLine(Component.literal("§7• Any: §6$1,000"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§c§lCost: $2,000"))
        );
        
        setSlot(20, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
            .setName(Component.literal("§8[ ? ]")));
        setSlot(22, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
            .setName(Component.literal("§8[ ? ]")));
        setSlot(24, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
            .setName(Component.literal("§8[ ? ]")));
        
        setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§d§l§n⭐ SPIN"))
            .addLoreLine(Component.literal("§7Click to spin the slots!"))
            .glow()
            .setCallback((index, type, action) -> {
                int[] result = GamesManager.spinSlots(player);
                if (result != null) {
                    showLobby();
                }
            })
        );
        
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§l← Back to Lobby"))
            .setCallback((index, type, action) -> showLobby())
        );
    }
    
    /**
     * BLACKJACK GAME - Hit, Stand, Double
     */
    private void showBlackjack() {
        GameSession session = GamesManager.getSession(player.getUUID());
        
        if (session == null || session.type != GameType.BLACKJACK) {
            GamesManager.startBlackjack(player);
            session = GamesManager.getSession(player.getUUID());
            if (session == null) return;
        }
        
        currentView = GameType.BLACKJACK;
        BlackjackState state = (BlackjackState) session.gameState;
        this.setTitle(Component.literal("§0§l🃏 Blackjack"));
        
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        StringBuilder playerHand = new StringBuilder();
        for (int card : state.playerHand) {
            playerHand.append(state.getCardName(card)).append(" ");
        }
        
        setSlot(11, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§e§lYour Hand"))
            .addLoreLine(Component.literal("§7Cards: §e" + playerHand.toString()))
            .addLoreLine(Component.literal("§7Value: §e" + state.getHandValue(state.playerHand)))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(state.betAmount)))
        );
        
        if (state.playerStanding) {
            StringBuilder dealerHand = new StringBuilder();
            for (int card : state.dealerHand) {
                dealerHand.append(state.getCardName(card)).append(" ");
            }
            setSlot(15, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§c§lDealer Hand"))
                .addLoreLine(Component.literal("§7Cards: §c" + dealerHand.toString()))
                .addLoreLine(Component.literal("§7Value: §c" + state.getHandValue(state.dealerHand)))
            );
        } else {
            setSlot(15, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§c§lDealer Hand"))
                .addLoreLine(Component.literal("§7Shows: §c" + state.getCardName(state.dealerHand.get(0)) + " §8??"))
                .addLoreLine(Component.literal("§7Hidden card"))
            );
        }
        
        if (!state.playerStanding) {
            setSlot(29, new GuiElementBuilder(Items.LIME_WOOL)
                .setName(Component.literal("§a§l§nHIT"))
                .addLoreLine(Component.literal("§7Draw another card"))
                .glow()
                .setCallback((index, type, action) -> {
                    GamesManager.blackjackHit(player);
                    GameSession check = GamesManager.getSession(player.getUUID());
                    if (check != null && check.type == GameType.BLACKJACK) {
                        showBlackjack();
                    } else {
                        showLobby();
                    }
                })
            );
            
            setSlot(31, new GuiElementBuilder(Items.RED_WOOL)
                .setName(Component.literal("§c§l§nSTAND"))
                .addLoreLine(Component.literal("§7Keep your hand"))
                .addLoreLine(Component.literal("§7Dealer plays"))
                .glow()
                .setCallback((index, type, action) -> {
                    GamesManager.blackjackStand(player);
                    showLobby();
                })
            );
            
            if (state.playerHand.size() == 2) {
                setSlot(33, new GuiElementBuilder(Items.GOLD_INGOT)
                    .setName(Component.literal("§6§l§nDOUBLE"))
                    .addLoreLine(Component.literal("§7Double bet, draw 1 card"))
                    .addLoreLine(Component.literal("§7Then stand automatically"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§c§lCost: $" + CurrencyManager.format(state.betAmount)))
                    .setCallback((index, type, action) -> {
                        if (CurrencyManager.canAfford(player, state.betAmount)) {
                            CurrencyManager.removeMoney(player, state.betAmount);
                            state.betAmount *= 2;
                            GamesManager.blackjackHit(player);
                            GameSession check = GamesManager.getSession(player.getUUID());
                            if (check != null) {
                                GamesManager.blackjackStand(player);
                            }
                            showLobby();
                        } else {
                            player.sendSystemMessage(Component.literal("§c§l[BLACKJACK] Not enough money to double!"));
                        }
                    })
                );
            }
        }
        
        setSlot(49, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§c§l✖ Forfeit"))
            .setCallback((index, type, action) -> {
                GamesManager.endSession(player.getUUID());
                showLobby();
            })
        );
    }
    
    /**
     * ROULETTE GAME - Betting options
     */
    private void showRoulette() {
        currentView = GameType.ROULETTE;
        this.setTitle(Component.literal("§c§l🎡 Roulette"));
        
        for (int i = 0; i < 54; i++) {
            setSlot(i, new GuiElementBuilder(Items.AIR));
        }
        
        for (int i = 0; i < 9; i++) {
            setSlot(i, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")));
            setSlot(i + 45, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).setName(Component.literal("")));
        }
        
        setSlot(4, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c§lRoulette"))
            .addLoreLine(Component.literal("§7European Wheel (0-36)"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Choose your bet type:"))
            .addLoreLine(Component.literal("§c§lEntry: $2,000 + bet"))
        );
        
        long defaultBet = 1000;
        
        setSlot(19, new GuiElementBuilder(Items.RED_WOOL)
            .setName(Component.literal("§c§lRED"))
            .addLoreLine(Component.literal("§7Bet on red numbers"))
            .addLoreLine(Component.literal("§a§lPayout: §62:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .glow()
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.RED, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(20, new GuiElementBuilder(Items.BLACK_WOOL)
            .setName(Component.literal("§0§lBLACK"))
            .addLoreLine(Component.literal("§7Bet on black numbers"))
            .addLoreLine(Component.literal("§a§lPayout: §62:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .glow()
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.BLACK, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(24, new GuiElementBuilder(Items.LIME_WOOL)
            .setName(Component.literal("§a§lODD"))
            .addLoreLine(Component.literal("§7Bet on odd numbers"))
            .addLoreLine(Component.literal("§a§lPayout: §62:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.ODD, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(25, new GuiElementBuilder(Items.ORANGE_WOOL)
            .setName(Component.literal("§6§lEVEN"))
            .addLoreLine(Component.literal("§7Bet on even numbers"))
            .addLoreLine(Component.literal("§a§lPayout: §62:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.EVEN, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(29, new GuiElementBuilder(Items.YELLOW_WOOL)
            .setName(Component.literal("§e§lLOW (1-18)"))
            .addLoreLine(Component.literal("§7Bet on low numbers"))
            .addLoreLine(Component.literal("§a§lPayout: §62:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.LOW, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(30, new GuiElementBuilder(Items.PURPLE_WOOL)
            .setName(Component.literal("§5§lHIGH (19-36)"))
            .addLoreLine(Component.literal("§7Bet on high numbers"))
            .addLoreLine(Component.literal("§a§lPayout: §62:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.HIGH, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(33, new GuiElementBuilder(Items.PINK_WOOL)
            .setName(Component.literal("§d§lDOZEN 1 (1-12)"))
            .addLoreLine(Component.literal("§7First dozen"))
            .addLoreLine(Component.literal("§a§lPayout: §63:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.DOZEN1, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(34, new GuiElementBuilder(Items.MAGENTA_WOOL)
            .setName(Component.literal("§d§lDOZEN 2 (13-24)"))
            .addLoreLine(Component.literal("§7Second dozen"))
            .addLoreLine(Component.literal("§a§lPayout: §63:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.DOZEN2, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(35, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
            .setName(Component.literal("§b§lDOZEN 3 (25-36)"))
            .addLoreLine(Component.literal("§7Third dozen"))
            .addLoreLine(Component.literal("§a§lPayout: §63:1"))
            .addLoreLine(Component.literal("§7Bet: §6$" + CurrencyManager.format(defaultBet)))
            .setCallback((index, type, action) -> {
                GamesManager.startRoulette(player, RouletteState.BetType.DOZEN3, 0, defaultBet);
                showLobby();
            })
        );
        
        setSlot(49, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e§l← Back to Lobby"))
            .setCallback((index, type, action) -> showLobby())
        );
    }
}
