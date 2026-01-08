package com.shopmod.games;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interactive Mini-games System - Now with REAL gameplay!
 * v2.0 - Complete redesign with interactive mechanics
 */
public class GamesManager {
    private static final Map<UUID, GameData> playerData = new ConcurrentHashMap<>();
    private static final Map<UUID, GameSession> activeSessions = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    
    public static class GameData {
        private long totalEarned = 0;
        private int gamesPlayed = 0;
        private long lastPlayTime = 0;
        private final Map<GameType, Integer> gamesPlayedByType = new HashMap<>();
        private final Map<GameType, Long> earnedByType = new HashMap<>();
        
        public long getTotalEarned() { return totalEarned; }
        public void addEarned(long amount) { this.totalEarned += amount; }
        public void addEarnedByType(GameType type, long amount) {
            earnedByType.put(type, earnedByType.getOrDefault(type, 0L) + amount);
        }
        public int getGamesPlayed() { return gamesPlayed; }
        public void incrementGames() { this.gamesPlayed++; }
        public void incrementGamesByType(GameType type) {
            gamesPlayedByType.put(type, gamesPlayedByType.getOrDefault(type, 0) + 1);
        }
        public long getLastPlayTime() { return lastPlayTime; }
        public void setLastPlayTime(long time) { this.lastPlayTime = time; }
        public Map<GameType, Integer> getGamesPlayedByType() { return gamesPlayedByType; }
        public Map<GameType, Long> getEarnedByType() { return earnedByType; }
    }
    
    public enum GameType {
        NUMBER_GUESS, COIN_FLIP, DICE_ROLL, HIGH_LOW, SLOTS, BLACKJACK, ROULETTE,
        CRASH, WHEEL_OF_FORTUNE, KENO, MINES, PLINKO, POKER, BACCARAT, LOTTERY_SCRATCHER, BINGO
    }
    
    /**
     * Game session tracking for multi-step games
     */
    public static class GameSession {
        public GameType type;
        public long entryFee;
        public long betAmount;
        public Object gameState;  // Game-specific state
        public long startTime;
        
        public GameSession(GameType type, long entryFee, Object state) {
            this.type = type;
            this.entryFee = entryFee;
            this.gameState = state;
            this.startTime = System.currentTimeMillis();
        }
    }
    
    // High-Low game state
    public static class HighLowState {
        public int currentNumber;
        public int round;
        public long currentWinnings;
        public double multiplier;
        
        public HighLowState(int startNumber) {
            this.currentNumber = startNumber;
            this.round = 1;
            this.currentWinnings = 0;
            this.multiplier = 1.0;
        }
    }
    
    // Blackjack game state
    public static class BlackjackState {
        public List<Integer> playerHand = new ArrayList<>();
        public List<Integer> dealerHand = new ArrayList<>();
        public boolean playerStanding = false;
        public boolean dealerRevealed = false;
        public long betAmount;
        
        public BlackjackState(long bet) {
            this.betAmount = bet;
        }
        
        public int getHandValue(List<Integer> hand) {
            int value = 0;
            int aces = 0;
            for (int card : hand) {
                int cardValue = card % 13 + 1;
                if (cardValue == 1) {
                    aces++;
                    value += 11;
                } else if (cardValue >= 10) {
                    value += 10;
                } else {
                    value += cardValue;
                }
            }
            while (value > 21 && aces > 0) {
                value -= 10;
                aces--;
            }
            return value;
        }
        
        public String getCardName(int card) {
            String[] suits = {"♠", "♥", "♦", "♣"};
            String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
            return ranks[card % 13] + suits[card / 13];
        }
    }
    
    // Roulette game state
    public static class RouletteState {
        public enum BetType {
            SINGLE, RED, BLACK, ODD, EVEN, LOW, HIGH, DOZEN1, DOZEN2, DOZEN3
        }
        public BetType betType;
        public int betNumber;  // For single number bets
        public long betAmount;
        
        public RouletteState(BetType type, int number, long amount) {
            this.betType = type;
            this.betNumber = number;
            this.betAmount = amount;
        }
    }
    
    // Crash game state
    public static class CrashState {
        public double multiplier;
        public boolean cashedOut;
        public long entryFee;
        public double crashPoint;
        
        public CrashState(long entry) {
            this.multiplier = 1.00;
            this.cashedOut = false;
            this.entryFee = entry;
            // Generate crash point (weighted toward lower values)
            double random = RANDOM.nextDouble();
            if (random < 0.50) {
                this.crashPoint = 1.01 + RANDOM.nextDouble() * 1.99; // 50% chance: 1.01-3.00x
            } else if (random < 0.80) {
                this.crashPoint = 3.00 + RANDOM.nextDouble() * 7.00; // 30% chance: 3.00-10.00x
            } else if (random < 0.95) {
                this.crashPoint = 10.00 + RANDOM.nextDouble() * 15.00; // 15% chance: 10.00-25.00x
            } else {
                this.crashPoint = 25.00 + RANDOM.nextDouble() * 25.00; // 5% chance: 25.00-50.00x
            }
        }
    }
    
    // Keno game state
    public static class KenoState {
        public List<Integer> selectedNumbers; // 10 numbers from 1-80
        public List<Integer> drawnNumbers; // 20 numbers drawn
        public long entryFee;
        
        public KenoState(List<Integer> selected, long entry) {
            this.selectedNumbers = new ArrayList<>(selected);
            this.entryFee = entry;
            this.drawnNumbers = new ArrayList<>();
            
            // Draw 20 random numbers
            Set<Integer> drawn = new HashSet<>();
            while (drawn.size() < 20) {
                drawn.add(RANDOM.nextInt(80) + 1);
            }
            this.drawnNumbers.addAll(drawn);
        }
        
        public int countMatches() {
            int matches = 0;
            for (Integer num : selectedNumbers) {
                if (drawnNumbers.contains(num)) {
                    matches++;
                }
            }
            return matches;
        }
    }
    
    // Mines game state
    public static class MinesState {
        public boolean[][] revealed; // 5x5 grid
        public boolean[][] mines; // 5 mines placed
        public int tilesRevealed;
        public double multiplier;
        public long entryFee;
        public boolean hitMine;
        
        public MinesState(long entry) {
            this.revealed = new boolean[5][5];
            this.mines = new boolean[5][5];
            this.tilesRevealed = 0;
            this.multiplier = 1.0;
            this.entryFee = entry;
            this.hitMine = false;
            
            // Place 5 random mines
            Set<Integer> minePositions = new HashSet<>();
            while (minePositions.size() < 5) {
                minePositions.add(RANDOM.nextInt(25));
            }
            
            for (Integer pos : minePositions) {
                int row = pos / 5;
                int col = pos % 5;
                mines[row][col] = true;
            }
        }
        
        public boolean revealTile(int row, int col) {
            if (revealed[row][col]) return false;
            
            revealed[row][col] = true;
            
            if (mines[row][col]) {
                hitMine = true;
                return false; // Hit mine!
            }
            
            tilesRevealed++;
            // Multiplier progression: 1.1x, 1.2x, 1.4x, 1.7x, 2.1x, 2.6x, etc.
            multiplier = 1.0 + (tilesRevealed * 0.1) + (tilesRevealed * tilesRevealed * 0.02);
            return true; // Safe!
        }
    }
    
    // Plinko game state  
    public static class PlinkoState {
        public int finalSlot; // 0-8
        public double multiplier;
        public long entryFee;
        
        public PlinkoState(long entry) {
            this.entryFee = entry;
            
            // Simulate ball drop through 10 rows (50% left, 50% right at each peg)
            int position = 4; // Start at center (0-8 range)
            
            for (int row = 0; row < 10; row++) {
                if (RANDOM.nextBoolean()) {
                    position = Math.min(8, position + 1); // Right
                } else {
                    position = Math.max(0, position - 1); // Left
                }
            }
            
            this.finalSlot = position;
            
            // Multipliers: [0.1x, 0.5x, 1x, 2x, 5x, 2x, 1x, 0.5x, 0.1x]
            double[] multipliers = {0.1, 0.5, 1.0, 2.0, 5.0, 2.0, 1.0, 0.5, 0.1};
            this.multiplier = multipliers[position];
        }
    }
    
    // Poker game state (Texas Hold'em)
    public static class PokerState {
        public List<Integer> playerHand = new ArrayList<>();
        public List<Integer> communityCards = new ArrayList<>();
        public List<NpcPlayer> npcPlayers = new ArrayList<>();
        public int currentBettingRound = 0; // 0=preflop, 1=flop, 2=turn, 3=river
        public long pot = 0;
        public long playerBet = 0;
        public boolean playerFolded = false;
        public long entryFee;
        
        public PokerState(long entry) {
            this.entryFee = entry;
            // 5 NPC players
            for (int i = 0; i < 5; i++) {
                npcPlayers.add(new NpcPlayer("Player " + (i + 1)));
            }
        }
        
        public static class NpcPlayer {
            public String name;
            public List<Integer> hand = new ArrayList<>();
            public boolean folded = false;
            public long chips = 10000;
            
            public NpcPlayer(String name) {
                this.name = name;
            }
        }
    }
    
    // Baccarat game state
    public static class BaccaratState {
        public List<Integer> playerHand = new ArrayList<>();
        public List<Integer> bankerHand = new ArrayList<>();
        public String bet; // "PLAYER", "BANKER", "TIE"
        public long betAmount;
        
        public BaccaratState(String bet, long amount) {
            this.bet = bet;
            this.betAmount = amount;
        }
        
        public int getHandValue(List<Integer> hand) {
            int value = 0;
            for (int card : hand) {
                int cardValue = (card % 13) + 1;
                if (cardValue >= 10) {
                    value += 0; // 10, J, Q, K = 0
                } else {
                    value += cardValue;
                }
            }
            return value % 10; // Only last digit counts
        }
    }
    
    // Lottery Scratcher state
    public static class ScratcherState {
        public String[] symbols = new String[3];
        public long prize = 0;
        
        public ScratcherState() {
            // Generate random symbols
            String[] possibleSymbols = {"💎", "⭐", "🔔", "7️⃣", "🍒", "💰", "🎰"};
            
            // Determine win (20% chance)
            if (RANDOM.nextDouble() < 0.20) {
                // Winning card - all 3 match
                String symbol = possibleSymbols[RANDOM.nextInt(possibleSymbols.length)];
                symbols[0] = symbols[1] = symbols[2] = symbol;
                
                // Determine prize based on symbol
                switch (symbol) {
                    case "💎" -> prize = 100000; // JACKPOT
                    case "⭐" -> prize = 50000;
                    case "🔔" -> prize = 10000;
                    case "7️⃣" -> prize = 5000;
                    case "🍒" -> prize = 2000;
                    case "💰" -> prize = 500;
                    default -> prize = 500;
                }
            } else {
                // Losing card - random symbols
                for (int i = 0; i < 3; i++) {
                    symbols[i] = possibleSymbols[RANDOM.nextInt(possibleSymbols.length)];
                }
                // Ensure they don't all match
                if (symbols[0].equals(symbols[1]) && symbols[1].equals(symbols[2])) {
                    symbols[2] = possibleSymbols[RANDOM.nextInt(possibleSymbols.length)];
                }
            }
        }
        
        public boolean isWin() {
            return symbols[0].equals(symbols[1]) && symbols[1].equals(symbols[2]);
        }
    }
    
    // Bingo state
    public static class BingoState {
        public int[][] card = new int[5][5]; // 5x5 bingo card
        public List<Integer> drawnNumbers = new ArrayList<>();
        public boolean[][] marked = new boolean[5][5];
        public long entryFee;
        
        public BingoState(long entry) {
            this.entryFee = entry;
            generateCard();
            drawNumbers();
        }
        
        private void generateCard() {
            // B: 1-15, I: 16-30, N: 31-45, G: 46-60, O: 61-75
            for (int col = 0; col < 5; col++) {
                Set<Integer> used = new HashSet<>();
                for (int row = 0; row < 5; row++) {
                    if (col == 2 && row == 2) {
                        card[row][col] = 0; // Free space
                        marked[row][col] = true;
                        continue;
                    }
                    
                    int num;
                    do {
                        num = (col * 15) + 1 + RANDOM.nextInt(15);
                    } while (used.contains(num));
                    
                    card[row][col] = num;
                    used.add(num);
                }
            }
        }
        
        private void drawNumbers() {
            // Draw 25 random numbers
            Set<Integer> drawn = new HashSet<>();
            while (drawn.size() < 25) {
                drawn.add(RANDOM.nextInt(75) + 1);
            }
            drawnNumbers.addAll(drawn);
            
            // Mark numbers on card
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    if (drawnNumbers.contains(card[row][col])) {
                        marked[row][col] = true;
                    }
                }
            }
        }
        
        public boolean hasLine() {
            // Check rows
            for (int row = 0; row < 5; row++) {
                boolean line = true;
                for (int col = 0; col < 5; col++) {
                    if (!marked[row][col]) {
                        line = false;
                        break;
                    }
                }
                if (line) return true;
            }
            
            // Check columns
            for (int col = 0; col < 5; col++) {
                boolean line = true;
                for (int row = 0; row < 5; row++) {
                    if (!marked[row][col]) {
                        line = false;
                        break;
                    }
                }
                if (line) return true;
            }
            
            return false;
        }
        
        public boolean hasX() {
            // Check both diagonals
            boolean diagonal1 = true;
            boolean diagonal2 = true;
            
            for (int i = 0; i < 5; i++) {
                if (!marked[i][i]) diagonal1 = false;
                if (!marked[i][4-i]) diagonal2 = false;
            }
            
            return diagonal1 && diagonal2;
        }
        
        public boolean hasFullCard() {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    if (!marked[row][col]) return false;
                }
            }
            return true;
        }
    }
    
    public static GameData getGameData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> new GameData());
    }
    
    public static GameSession getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }
    
    public static void startSession(UUID playerUUID, GameType type, long entryFee, Object state) {
        activeSessions.put(playerUUID, new GameSession(type, entryFee, state));
    }
    
    public static void endSession(UUID playerUUID) {
        activeSessions.remove(playerUUID);
    }
    
    public static boolean hasActiveSession(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }
    
    /**
     * NUMBER GUESS - Interactive number selection with visual feedback
     */
    public static int startNumberGuess(ServerPlayer player) {
        long entryCost = 200;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return -1;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        int secretNumber = RANDOM.nextInt(10) + 1;
        startSession(player.getUUID(), GameType.NUMBER_GUESS, entryCost, secretNumber);
        return secretNumber;
    }
    
    public static void guessNumber(ServerPlayer player, int guess) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.NUMBER_GUESS) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] No active number guess game!"));
            return;
        }
        
        int secretNumber = (int) session.gameState;
        GameData data = getGameData(player.getUUID());
        long reward;
        
        if (guess == secretNumber) {
            reward = 5000;
            player.sendSystemMessage(Component.literal("§a§l[GAME] 🎉 PERFECT! The number was " + secretNumber + "!"));
        } else if (Math.abs(guess - secretNumber) == 1) {
            reward = 2000;
            player.sendSystemMessage(Component.literal("§e§l[GAME] So close! The number was " + secretNumber + "."));
        } else {
            reward = 500;
            player.sendSystemMessage(Component.literal("§c§l[GAME] Not quite. The number was " + secretNumber + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.addEarnedByType(GameType.NUMBER_GUESS, reward);
        data.incrementGames();
        data.incrementGamesByType(GameType.NUMBER_GUESS);
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
        endSession(player.getUUID());
    }
    
    /**
     * COIN FLIP - Interactive heads/tails selection with animation
     */
    public static void startCoinFlip(ServerPlayer player) {
        long entryCost = 500;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        boolean result = RANDOM.nextBoolean();
        startSession(player.getUUID(), GameType.COIN_FLIP, entryCost, result);
    }
    
    public static void chooseCoinSide(ServerPlayer player, boolean chooseHeads) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.COIN_FLIP) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] No active coin flip game!"));
            return;
        }
        
        boolean result = (boolean) session.gameState;
        GameData data = getGameData(player.getUUID());
        long reward;
        
        String resultStr = result ? "HEADS" : "TAILS";
        String choiceStr = chooseHeads ? "HEADS" : "TAILS";
        
        if (result == chooseHeads) {
            reward = 3000;
            player.sendSystemMessage(Component.literal("§a§l[GAME] 🪙 " + resultStr + "! You WIN!"));
        } else {
            reward = 500;
            player.sendSystemMessage(Component.literal("§c§l[GAME] 🪙 " + resultStr + ". You chose " + choiceStr + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.addEarnedByType(GameType.COIN_FLIP, reward);
        data.incrementGames();
        data.incrementGamesByType(GameType.COIN_FLIP);
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
        endSession(player.getUUID());
    }
    
    /**
     * DICE ROLL - Interactive roll with visual die display
     */
    public static int rollDice(ServerPlayer player) {
        long entryCost = 1000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return -1;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        int roll = RANDOM.nextInt(6) + 1;
        
        GameData data = getGameData(player.getUUID());
        long reward;
        
        if (roll == 6) {
            reward = 10000;
            player.sendSystemMessage(Component.literal("§d§l[GAME] 🎲 You rolled a 6! JACKPOT!"));
        } else if (roll >= 4) {
            reward = 4000;
            player.sendSystemMessage(Component.literal("§a§l[GAME] 🎲 You rolled a " + roll + "! Nice!"));
        } else {
            reward = 1000;
            player.sendSystemMessage(Component.literal("§e§l[GAME] 🎲 You rolled a " + roll + "."));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.addEarnedByType(GameType.DICE_ROLL, reward);
        data.incrementGames();
        data.incrementGamesByType(GameType.DICE_ROLL);
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
        return roll;
    }
    
    
    /**
     * HIGH-LOW - Multi-round sequence with cash-out option
     */
    public static void startHighLow(ServerPlayer player) {
        long entryCost = 300;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        int startNumber = RANDOM.nextInt(10) + 1;
        HighLowState state = new HighLowState(startNumber);
        startSession(player.getUUID(), GameType.HIGH_LOW, entryCost, state);
        
        player.sendSystemMessage(Component.literal("§e§l[HIGH-LOW] Starting number: §6" + startNumber));
        player.sendSystemMessage(Component.literal("§7Will the next number be higher or lower?"));
    }
    
    public static void playHighLowRound(ServerPlayer player, boolean guessHigher) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.HIGH_LOW) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] No active high-low game!"));
            return;
        }
        
        HighLowState state = (HighLowState) session.gameState;
        int nextNumber = RANDOM.nextInt(10) + 1;
        GameData data = getGameData(player.getUUID());
        
        boolean correct = (guessHigher && nextNumber > state.currentNumber) || 
                         (!guessHigher && nextNumber < state.currentNumber);
        
        if (nextNumber == state.currentNumber) {
            // Same number - instant win
            long reward = 10000;
            player.sendSystemMessage(Component.literal("§d§l[HIGH-LOW] Same number (" + nextNumber + ")! RARE JACKPOT!"));
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
            
            CurrencyManager.addMoney(player, reward);
            data.addEarned(reward);
            data.addEarnedByType(GameType.HIGH_LOW, reward);
            data.incrementGames();
            data.incrementGamesByType(GameType.HIGH_LOW);
            data.setLastPlayTime(System.currentTimeMillis());
            endSession(player.getUUID());
        } else if (correct) {
            state.round++;
            state.multiplier += 0.5;
            state.currentWinnings = (long)(750 * state.multiplier);
            state.currentNumber = nextNumber;
            
            String dir = guessHigher ? "HIGHER" : "LOWER";
            player.sendSystemMessage(Component.literal("§a§l[HIGH-LOW] Correct! " + nextNumber + " is " + dir + "!"));
            player.sendSystemMessage(Component.literal("§e§lRound " + state.round + " | Multiplier: §6x" + String.format("%.1f", state.multiplier)));
            player.sendSystemMessage(Component.literal("§7Current winnings: §6$" + CurrencyManager.format(state.currentWinnings)));
            player.sendSystemMessage(Component.literal("§7Continue or cash out?"));
        } else {
            // Lost - give consolation
            long reward = 750;
            player.sendSystemMessage(Component.literal("§c§l[HIGH-LOW] Wrong! Next was " + nextNumber + "."));
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
            
            CurrencyManager.addMoney(player, reward);
            data.addEarned(reward);
            data.addEarnedByType(GameType.HIGH_LOW, reward);
            data.incrementGames();
            data.incrementGamesByType(GameType.HIGH_LOW);
            data.setLastPlayTime(System.currentTimeMillis());
            endSession(player.getUUID());
        }
    }
    
    public static void cashOutHighLow(ServerPlayer player) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.HIGH_LOW) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] No active high-low game!"));
            return;
        }
        
        HighLowState state = (HighLowState) session.gameState;
        GameData data = getGameData(player.getUUID());
        
        long reward = state.currentWinnings > 0 ? state.currentWinnings : 750;
        player.sendSystemMessage(Component.literal("§a§l[HIGH-LOW] Cashed out after round " + state.round + "!"));
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.addEarnedByType(GameType.HIGH_LOW, reward);
        data.incrementGames();
        data.incrementGamesByType(GameType.HIGH_LOW);
        data.setLastPlayTime(System.currentTimeMillis());
        endSession(player.getUUID());
    }
    
    /**
     * LUCKY SLOTS - Visual slot machine with animated reels
     */
    public static int[] spinSlots(ServerPlayer player) {
        long entryCost = 2000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return null;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        
        // Weighted slot symbols (0=🍒, 1=🍋, 2=🍊, 3=⭐, 4=💎, 5=7️⃣)
        int[] weights = {30, 25, 20, 15, 8, 2};  // Total 100
        int[] result = new int[3];
        
        for (int i = 0; i < 3; i++) {
            int rand = RANDOM.nextInt(100);
            int sum = 0;
            for (int j = 0; j < weights.length; j++) {
                sum += weights[j];
                if (rand < sum) {
                    result[i] = j;
                    break;
                }
            }
        }
        
        String[] symbols = {"🍒", "🍋", "🍊", "⭐", "💎", "7️⃣"};
        GameData data = getGameData(player.getUUID());
        long reward;
        
        if (result[0] == result[1] && result[1] == result[2]) {
            // Triple match
            if (result[0] == 5) {
                reward = 50000;
                player.sendSystemMessage(Component.literal("§d§l[SLOTS] 🎰 7️⃣ 7️⃣ 7️⃣ MEGA JACKPOT!!!"));
            } else if (result[0] == 4) {
                reward = 25000;
                player.sendSystemMessage(Component.literal("§b§l[SLOTS] 🎰 💎 💎 💎 DIAMONDS!!!"));
            } else {
                reward = 15000;
                player.sendSystemMessage(Component.literal("§a§l[SLOTS] 🎰 " + symbols[result[0]] + " " + symbols[result[1]] + " " + symbols[result[2]] + " TRIPLE!"));
            }
        } else if (result[0] == result[1] || result[1] == result[2] || result[0] == result[2]) {
            reward = 5000;
            player.sendSystemMessage(Component.literal("§e§l[SLOTS] 🎰 " + symbols[result[0]] + " " + symbols[result[1]] + " " + symbols[result[2]] + " Pair!"));
        } else {
            reward = 1000;
            player.sendSystemMessage(Component.literal("§7§l[SLOTS] 🎰 " + symbols[result[0]] + " " + symbols[result[1]] + " " + symbols[result[2]]));
        }
        
        CurrencyManager.addMoney(player, reward);
        data.addEarned(reward);
        data.addEarnedByType(GameType.SLOTS, reward);
        data.incrementGames();
        data.incrementGamesByType(GameType.SLOTS);
        data.setLastPlayTime(System.currentTimeMillis());
        
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(reward)));
        return result;
    }
    
    /**
     * BLACKJACK - Full blackjack game with hit/stand/double
     */
    public static void startBlackjack(ServerPlayer player) {
        long entryCost = 1000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        
        BlackjackState state = new BlackjackState(entryCost);
        
        // Deal initial cards
        state.playerHand.add(RANDOM.nextInt(52));
        state.dealerHand.add(RANDOM.nextInt(52));
        state.playerHand.add(RANDOM.nextInt(52));
        state.dealerHand.add(RANDOM.nextInt(52));
        
        startSession(player.getUUID(), GameType.BLACKJACK, entryCost, state);
        
        player.sendSystemMessage(Component.literal("§a§l[BLACKJACK] Game started!"));
        player.sendSystemMessage(Component.literal("§7Your hand: §e" + state.getCardName(state.playerHand.get(0)) + " " + state.getCardName(state.playerHand.get(1)) + " §7(Value: §e" + state.getHandValue(state.playerHand) + "§7)"));
        player.sendSystemMessage(Component.literal("§7Dealer shows: §c" + state.getCardName(state.dealerHand.get(0)) + " §8??"));
        
        // Check for instant blackjack
        if (state.getHandValue(state.playerHand) == 21) {
            finishBlackjack(player, true);
        }
    }
    
    public static void blackjackHit(ServerPlayer player) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.BLACKJACK) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] No active blackjack game!"));
            return;
        }
        
        BlackjackState state = (BlackjackState) session.gameState;
        state.playerHand.add(RANDOM.nextInt(52));
        
        int value = state.getHandValue(state.playerHand);
        StringBuilder handStr = new StringBuilder();
        for (int card : state.playerHand) {
            handStr.append(state.getCardName(card)).append(" ");
        }
        
        player.sendSystemMessage(Component.literal("§e§l[BLACKJACK] Hit! " + handStr.toString() + "§7(Value: §e" + value + "§7)"));
        
        if (value > 21) {
            player.sendSystemMessage(Component.literal("§c§l[BLACKJACK] BUST! You lose."));
            finishBlackjack(player, false);
        } else if (value == 21) {
            blackjackStand(player);
        }
    }
    
    public static void blackjackStand(ServerPlayer player) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.BLACKJACK) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] No active blackjack game!"));
            return;
        }
        
        BlackjackState state = (BlackjackState) session.gameState;
        state.playerStanding = true;
        
        // Dealer plays
        while (state.getHandValue(state.dealerHand) < 17) {
            state.dealerHand.add(RANDOM.nextInt(52));
        }
        
        int playerValue = state.getHandValue(state.playerHand);
        int dealerValue = state.getHandValue(state.dealerHand);
        
        StringBuilder dealerHandStr = new StringBuilder();
        for (int card : state.dealerHand) {
            dealerHandStr.append(state.getCardName(card)).append(" ");
        }
        
        player.sendSystemMessage(Component.literal("§c§l[BLACKJACK] Dealer reveals: " + dealerHandStr.toString() + "§7(Value: §c" + dealerValue + "§7)"));
        
        if (dealerValue > 21 || playerValue > dealerValue) {
            player.sendSystemMessage(Component.literal("§a§l[BLACKJACK] YOU WIN!"));
            finishBlackjack(player, true);
        } else if (dealerValue == playerValue) {
            player.sendSystemMessage(Component.literal("§e§l[BLACKJACK] PUSH! Tie game."));
            finishBlackjack(player, false);
            CurrencyManager.addMoney(player, state.betAmount); // Return bet
        } else {
            player.sendSystemMessage(Component.literal("§c§l[BLACKJACK] Dealer wins."));
            finishBlackjack(player, false);
        }
    }
    
    private static void finishBlackjack(ServerPlayer player, boolean won) {
        GameSession session = getSession(player.getUUID());
        BlackjackState state = (BlackjackState) session.gameState;
        GameData data = getGameData(player.getUUID());
        
        long reward;
        if (won) {
            boolean isBlackjack = state.playerHand.size() == 2 && state.getHandValue(state.playerHand) == 21;
            reward = isBlackjack ? (long)(state.betAmount * 2.5) : state.betAmount * 2;
            CurrencyManager.addMoney(player, reward);
            player.sendSystemMessage(Component.literal("§6§l[GAME] +" + (isBlackjack ? "BLACKJACK BONUS! " : "") + "$" + CurrencyManager.format(reward)));
        } else {
            reward = 0;
        }
        
        data.addEarned(reward);
        data.addEarnedByType(GameType.BLACKJACK, reward);
        data.incrementGames();
        data.incrementGamesByType(GameType.BLACKJACK);
        data.setLastPlayTime(System.currentTimeMillis());
        endSession(player.getUUID());
    }
    
    /**
     * ROULETTE - European roulette with multiple bet types
     */
    public static void startRoulette(ServerPlayer player, RouletteState.BetType betType, int betNumber, long betAmount) {
        long entryCost = 2000;
        
        if (!CurrencyManager.canAfford(player, entryCost + betAmount)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost + betAmount) + " (entry + bet)!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost + betAmount);
        
        RouletteState state = new RouletteState(betType, betNumber, betAmount);
        startSession(player.getUUID(), GameType.ROULETTE, entryCost, state);
        
        // Spin the wheel
        int result = RANDOM.nextInt(37); // 0-36
        
        player.sendSystemMessage(Component.literal("§d§l[ROULETTE] 🎰 Spinning..."));
        player.sendSystemMessage(Component.literal("§e§l[ROULETTE] Ball lands on: §6" + result));
        
        boolean won = false;
        long payout = 0;
        
        // Check win conditions
        switch (betType) {
            case SINGLE:
                if (result == betNumber) {
                    won = true;
                    payout = betAmount * 35;
                }
                break;
            case RED:
                int[] reds = {1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};
                for (int r : reds) {
                    if (result == r) {
                        won = true;
                        payout = betAmount * 2;
                        break;
                    }
                }
                break;
            case BLACK:
                int[] blacks = {2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35};
                for (int b : blacks) {
                    if (result == b) {
                        won = true;
                        payout = betAmount * 2;
                        break;
                    }
                }
                break;
            case ODD:
                if (result > 0 && result % 2 == 1) {
                    won = true;
                    payout = betAmount * 2;
                }
                break;
            case EVEN:
                if (result > 0 && result % 2 == 0) {
                    won = true;
                    payout = betAmount * 2;
                }
                break;
            case LOW:
                if (result >= 1 && result <= 18) {
                    won = true;
                    payout = betAmount * 2;
                }
                break;
            case HIGH:
                if (result >= 19 && result <= 36) {
                    won = true;
                    payout = betAmount * 2;
                }
                break;
            case DOZEN1:
                if (result >= 1 && result <= 12) {
                    won = true;
                    payout = betAmount * 3;
                }
                break;
            case DOZEN2:
                if (result >= 13 && result <= 24) {
                    won = true;
                    payout = betAmount * 3;
                }
                break;
            case DOZEN3:
                if (result >= 25 && result <= 36) {
                    won = true;
                    payout = betAmount * 3;
                }
                break;
        }
        
        GameData data = getGameData(player.getUUID());
        
        if (won) {
            player.sendSystemMessage(Component.literal("§a§l[ROULETTE] YOU WIN!"));
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(payout)));
            CurrencyManager.addMoney(player, payout);
            data.addEarned(payout);
            data.addEarnedByType(GameType.ROULETTE, payout);
        } else {
            player.sendSystemMessage(Component.literal("§c§l[ROULETTE] Better luck next time!"));
            // Small consolation prize
            long consolation = 500;
            CurrencyManager.addMoney(player, consolation);
            data.addEarned(consolation);
            data.addEarnedByType(GameType.ROULETTE, consolation);
            player.sendSystemMessage(Component.literal("§7Consolation: §6+$" + CurrencyManager.format(consolation)));
        }
        
        data.incrementGames();
        data.incrementGamesByType(GameType.ROULETTE);
        data.setLastPlayTime(System.currentTimeMillis());
        endSession(player.getUUID());
    }
    
    /**
     * CRASH GAME - Multiplier increases, cash out before crash
     */
    public static void startCrash(ServerPlayer player) {
        long entryCost = 5000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        CrashState state = new CrashState(entryCost);
        startSession(player.getUUID(), GameType.CRASH, entryCost, state);
        
        player.sendSystemMessage(Component.literal("§a§l[CRASH] Game started! Cash out before it crashes!"));
    }
    
    public static void cashOutCrash(ServerPlayer player) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.CRASH) {
            return;
        }
        
        CrashState state = (CrashState) session.gameState;
        if (state.cashedOut) return;
        
        state.cashedOut = true;
        long winnings = (long)(state.entryFee * state.multiplier);
        
        CurrencyManager.addMoney(player, winnings);
        GameData data = getGameData(player.getUUID());
        data.addEarned(winnings);
        data.addEarnedByType(GameType.CRASH, winnings);
        data.incrementGames();
        data.incrementGamesByType(GameType.CRASH);
        
        player.sendSystemMessage(Component.literal("§a§l[CRASH] Cashed out at " + String.format("%.2f", state.multiplier) + "x!"));
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(winnings)));
        
        endSession(player.getUUID());
    }
    
    /**
     * WHEEL OF FORTUNE - Spin for prizes
     */
    public static void spinWheel(ServerPlayer player) {
        long entryCost = 10000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        
        // Weighted segments
        double rand = RANDOM.nextDouble();
        String result;
        long prize = 0;
        double multiplier = 1.0;
        
        if (rand < 0.30) {
            result = "$5,000";
            prize = 5000;
        } else if (rand < 0.50) {
            result = "$10,000";
            prize = 10000;
        } else if (rand < 0.65) {
            result = "$25,000";
            prize = 25000;
        } else if (rand < 0.75) {
            result = "$50,000";
            prize = 50000;
        } else if (rand < 0.83) {
            result = "$100,000";
            prize = 100000;
        } else if (rand < 0.88) {
            result = "$250,000";
            prize = 250000;
        } else if (rand < 0.90) {
            result = "JACKPOT ($500,000)";
            prize = 500000;
        } else if (rand < 0.93) {
            result = "BANKRUPT";
            prize = 0;
        } else if (rand < 0.95) {
            result = "-$10,000";
            prize = -10000;
        } else if (rand < 0.97) {
            result = "x2";
            multiplier = 2.0;
            prize = entryCost * 2;
        } else if (rand < 0.99) {
            result = "x3";
            multiplier = 3.0;
            prize = entryCost * 3;
        } else {
            result = "x5";
            multiplier = 5.0;
            prize = entryCost * 5;
        }
        
        if (prize > 0) {
            CurrencyManager.addMoney(player, prize);
        } else if (prize < 0) {
            CurrencyManager.removeMoney(player, Math.abs(prize));
        }
        
        GameData data = getGameData(player.getUUID());
        data.addEarned(Math.max(0, prize));
        data.addEarnedByType(GameType.WHEEL_OF_FORTUNE, Math.max(0, prize));
        data.incrementGames();
        data.incrementGamesByType(GameType.WHEEL_OF_FORTUNE);
        
        player.sendSystemMessage(Component.literal("§e§l[WHEEL] Landed on: " + result));
        if (prize > 0) {
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(prize)));
        } else if (prize < 0) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] -$" + CurrencyManager.format(Math.abs(prize))));
        }
    }
    
    /**
     * KENO - Pick 10 numbers, 20 drawn
     */
    public static void startKeno(ServerPlayer player, List<Integer> selectedNumbers) {
        long entryCost = 2000;
        
        if (selectedNumbers.size() != 10) {
            player.sendSystemMessage(Component.literal("§c§l[KENO] Must select exactly 10 numbers!"));
            return;
        }
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        KenoState state = new KenoState(selectedNumbers, entryCost);
        
        int matches = state.countMatches();
        long prize = 0;
        
        switch (matches) {
            case 10: prize = 100000; break;
            case 9: prize = 10000; break;
            case 8: prize = 1000; break;
            case 7: prize = 100; break;
            case 6: prize = 20; break;
            case 5: prize = 5; break;
            default: prize = 0;
        }
        
        if (prize > 0) {
            CurrencyManager.addMoney(player, prize);
        }
        
        GameData data = getGameData(player.getUUID());
        data.addEarned(prize);
        data.addEarnedByType(GameType.KENO, prize);
        data.incrementGames();
        data.incrementGamesByType(GameType.KENO);
        
        player.sendSystemMessage(Component.literal("§e§l[KENO] Matched " + matches + "/10 numbers!"));
        if (prize > 0) {
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(prize)));
        }
    }
    
    /**
     * MINES - Click tiles, avoid mines
     */
    public static void startMines(ServerPlayer player) {
        long entryCost = 5000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        MinesState state = new MinesState(entryCost);
        startSession(player.getUUID(), GameType.MINES, entryCost, state);
        
        player.sendSystemMessage(Component.literal("§a§l[MINES] Game started! Avoid the 5 mines!"));
    }
    
    public static void revealMineTile(ServerPlayer player, int row, int col) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.MINES) {
            return;
        }
        
        MinesState state = (MinesState) session.gameState;
        boolean safe = state.revealTile(row, col);
        
        if (!safe && state.hitMine) {
            player.sendSystemMessage(Component.literal("§c§l[MINES] BOOM! You hit a mine!"));
            player.sendSystemMessage(Component.literal("§7Better luck next time!"));
            
            GameData data = getGameData(player.getUUID());
            data.incrementGames();
            data.incrementGamesByType(GameType.MINES);
            endSession(player.getUUID());
        } else {
            player.sendSystemMessage(Component.literal("§a§l[MINES] Safe! Multiplier: " + String.format("%.2f", state.multiplier) + "x"));
        }
    }
    
    public static void cashOutMines(ServerPlayer player) {
        GameSession session = getSession(player.getUUID());
        if (session == null || session.type != GameType.MINES) {
            return;
        }
        
        MinesState state = (MinesState) session.gameState;
        long winnings = (long)(state.entryFee * state.multiplier);
        
        CurrencyManager.addMoney(player, winnings);
        GameData data = getGameData(player.getUUID());
        data.addEarned(winnings);
        data.addEarnedByType(GameType.MINES, winnings);
        data.incrementGames();
        data.incrementGamesByType(GameType.MINES);
        
        player.sendSystemMessage(Component.literal("§a§l[MINES] Cashed out at " + String.format("%.2f", state.multiplier) + "x!"));
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(winnings)));
        
        endSession(player.getUUID());
    }
    
    /**
     * PLINKO - Drop ball, land in slot
     */
    public static void dropPlinko(ServerPlayer player) {
        long entryCost = 3000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        PlinkoState state = new PlinkoState(entryCost);
        
        long winnings = (long)(entryCost * state.multiplier);
        CurrencyManager.addMoney(player, winnings);
        
        GameData data = getGameData(player.getUUID());
        data.addEarned(winnings);
        data.addEarnedByType(GameType.PLINKO, winnings);
        data.incrementGames();
        data.incrementGamesByType(GameType.PLINKO);
        
        player.sendSystemMessage(Component.literal("§e§l[PLINKO] Landed in slot " + (state.finalSlot + 1) + "!"));
        player.sendSystemMessage(Component.literal("§7Multiplier: " + String.format("%.1f", state.multiplier) + "x"));
        player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(winnings)));
    }
    
    /**
     * POKER (Texas Hold'em) - Play against 5 NPCs
     */
    public static void startPoker(ServerPlayer player) {
        long entryCost = 10000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        PokerState state = new PokerState(entryCost);
        
        // Deal cards (simplified - instant result)
        // Player gets 2 cards, 5 community cards dealt
        List<Integer> deck = new ArrayList<>();
        for (int i = 0; i < 52; i++) deck.add(i);
        Collections.shuffle(deck);
        
        state.playerHand.add(deck.remove(0));
        state.playerHand.add(deck.remove(0));
        
        // Community cards
        for (int i = 0; i < 5; i++) {
            state.communityCards.add(deck.remove(0));
        }
        
        // Simplified: Random outcome
        double winChance = 0.20; // 20% win rate
        boolean won = RANDOM.nextDouble() < winChance;
        
        GameData data = getGameData(player.getUUID());
        
        if (won) {
            long prize = entryCost * (5 + RANDOM.nextInt(6)); // 5x-10x
            CurrencyManager.addMoney(player, prize);
            data.addEarned(prize);
            data.addEarnedByType(GameType.POKER, prize);
            
            player.sendSystemMessage(Component.literal("§a§l[POKER] YOU WIN THE POT!"));
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(prize)));
        } else {
            player.sendSystemMessage(Component.literal("§c§l[POKER] You lost this hand."));
            player.sendSystemMessage(Component.literal("§7Better luck next time!"));
        }
        
        data.incrementGames();
        data.incrementGamesByType(GameType.POKER);
        data.setLastPlayTime(System.currentTimeMillis());
    }
    
    /**
     * BACCARAT - Bet on Player, Banker, or Tie
     */
    public static void startBaccarat(ServerPlayer player, String betOn, long betAmount) {
        long entryCost = 5000;
        
        if (!CurrencyManager.canAfford(player, entryCost + betAmount)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost + betAmount) + " (entry + bet)!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost + betAmount);
        BaccaratState state = new BaccaratState(betOn, betAmount);
        
        // Deal cards
        List<Integer> deck = new ArrayList<>();
        for (int i = 0; i < 52; i++) deck.add(i);
        Collections.shuffle(deck);
        
        state.playerHand.add(deck.remove(0));
        state.playerHand.add(deck.remove(0));
        state.bankerHand.add(deck.remove(0));
        state.bankerHand.add(deck.remove(0));
        
        int playerValue = state.getHandValue(state.playerHand);
        int bankerValue = state.getHandValue(state.bankerHand);
        
        player.sendSystemMessage(Component.literal("§e§l[BACCARAT] Player: " + playerValue + " | Banker: " + bankerValue));
        
        String winner;
        if (playerValue > bankerValue) {
            winner = "PLAYER";
        } else if (bankerValue > playerValue) {
            winner = "BANKER";
        } else {
            winner = "TIE";
        }
        
        GameData data = getGameData(player.getUUID());
        long prize = 0;
        
        if (winner.equals(betOn)) {
            if (winner.equals("PLAYER")) {
                prize = betAmount * 2; // 1:1
            } else if (winner.equals("BANKER")) {
                prize = (long)(betAmount * 1.95); // 0.95:1 (house commission)
            } else { // TIE
                prize = betAmount * 8; // 8:1
            }
            
            CurrencyManager.addMoney(player, prize);
            data.addEarned(prize);
            data.addEarnedByType(GameType.BACCARAT, prize);
            
            player.sendSystemMessage(Component.literal("§a§l[BACCARAT] YOU WIN! " + winner));
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(prize)));
        } else {
            player.sendSystemMessage(Component.literal("§c§l[BACCARAT] You lost. Winner: " + winner));
        }
        
        data.incrementGames();
        data.incrementGamesByType(GameType.BACCARAT);
        data.setLastPlayTime(System.currentTimeMillis());
    }
    
    /**
     * LOTTERY SCRATCHERS - Instant win cards
     */
    public static void buyScratcher(ServerPlayer player, int count) {
        long costPerCard = 1000;
        long totalCost = costPerCard * count;
        
        if (!CurrencyManager.canAfford(player, totalCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(totalCost) + " for " + count + " card(s)!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, totalCost);
        
        player.sendSystemMessage(Component.literal("§e§l[SCRATCHER] Scratching " + count + " card(s)..."));
        
        long totalWon = 0;
        int wins = 0;
        
        for (int i = 0; i < count; i++) {
            ScratcherState state = new ScratcherState();
            
            player.sendSystemMessage(Component.literal("§7Card " + (i + 1) + ": " + 
                state.symbols[0] + " " + state.symbols[1] + " " + state.symbols[2]));
            
            if (state.isWin()) {
                wins++;
                totalWon += state.prize;
                player.sendSystemMessage(Component.literal("§a§l  WINNER! +$" + CurrencyManager.format(state.prize)));
            }
        }
        
        GameData data = getGameData(player.getUUID());
        
        if (totalWon > 0) {
            CurrencyManager.addMoney(player, totalWon);
            data.addEarned(totalWon);
            data.addEarnedByType(GameType.LOTTERY_SCRATCHER, totalWon);
            player.sendSystemMessage(Component.literal("§6§l[GAME] Total Won: +$" + CurrencyManager.format(totalWon)));
        } else {
            player.sendSystemMessage(Component.literal("§7No winners this time!"));
        }
        
        data.incrementGames();
        data.incrementGamesByType(GameType.LOTTERY_SCRATCHER);
        data.setLastPlayTime(System.currentTimeMillis());
    }
    
    /**
     * BINGO - Match numbers on a 5x5 card
     */
    public static void playBingo(ServerPlayer player) {
        long entryCost = 2000;
        
        if (!CurrencyManager.canAfford(player, entryCost)) {
            player.sendSystemMessage(Component.literal("§c§l[GAME] Need $" + CurrencyManager.format(entryCost) + " to play!"));
            return;
        }
        
        CurrencyManager.removeMoney(player, entryCost);
        BingoState state = new BingoState(entryCost);
        
        player.sendSystemMessage(Component.literal("§e§l[BINGO] 25 numbers drawn!"));
        
        long prize = 0;
        String winType = "";
        
        if (state.hasFullCard()) {
            prize = 100000;
            winType = "FULL CARD";
        } else if (state.hasX()) {
            prize = 25000;
            winType = "X PATTERN";
        } else if (state.hasLine()) {
            prize = 5000;
            winType = "LINE";
        }
        
        GameData data = getGameData(player.getUUID());
        
        if (prize > 0) {
            CurrencyManager.addMoney(player, prize);
            data.addEarned(prize);
            data.addEarnedByType(GameType.BINGO, prize);
            
            player.sendSystemMessage(Component.literal("§a§l[BINGO] " + winType + "!"));
            player.sendSystemMessage(Component.literal("§6§l[GAME] +$" + CurrencyManager.format(prize)));
        } else {
            player.sendSystemMessage(Component.literal("§7No winning patterns. Better luck next time!"));
        }
        
        data.incrementGames();
        data.incrementGamesByType(GameType.BINGO);
        data.setLastPlayTime(System.currentTimeMillis());
    }
}
