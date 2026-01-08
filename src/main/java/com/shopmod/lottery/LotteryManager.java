package com.shopmod.lottery;

import com.shopmod.currency.CurrencyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Weekly Lottery System
 * 6 numbers, range 1-50
 * Draws every 7 in-game days
 */
public class LotteryManager {
    private static final Map<UUID, PlayerLotteryData> playerData = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    
    // Lottery constants
    private static final long TICKET_COST = 10000; // $10,000
    private static final int MAX_TICKETS_PER_PLAYER = 10;
    private static final int NUMBERS_PER_TICKET = 6;
    private static final int MAX_NUMBER = 50;
    private static final long STARTING_JACKPOT = 500000; // $500K
    private static final long MIN_JACKPOT_INCREASE = 50000; // $50K per week
    private static final double JACKPOT_GROWTH_RATE = 0.70; // 70% of ticket sales
    
    // Prize tiers
    private static final long PRIZE_MATCH_5 = 100000;
    private static final long PRIZE_MATCH_4 = 25000;
    private static final long PRIZE_MATCH_3 = 5000;
    private static final long PRIZE_MATCH_2 = 1000;
    
    // Global lottery state
    private static long currentJackpot = STARTING_JACKPOT;
    private static long nextDrawDay = 7; // First draw on day 7
    private static List<Integer> lastWinningNumbers = new ArrayList<>();
    private static long lastDrawDay = 0;
    private static long totalTicketSalesThisWeek = 0;
    private static Map<Integer, Integer> lastDrawWinners = new HashMap<>(); // matches -> count
    
    public static class PlayerLotteryData {
        private final List<LotteryTicket> activeTickets = new ArrayList<>();
        private long totalSpent = 0;
        private long totalWon = 0;
        private int ticketsPlayed = 0;
        
        public List<LotteryTicket> getActiveTickets() { return activeTickets; }
        public long getTotalSpent() { return totalSpent; }
        public void addSpent(long amount) { this.totalSpent += amount; }
        public long getTotalWon() { return totalWon; }
        public void addWon(long amount) { this.totalWon += amount; }
        public int getTicketsPlayed() { return ticketsPlayed; }
        public void incrementTicketsPlayed() { this.ticketsPlayed++; }
        
        public void clearActiveTickets() {
            activeTickets.clear();
        }
    }
    
    public static class LotteryTicket {
        private final UUID ticketId;
        private final List<Integer> numbers; // 6 numbers (1-50)
        private final long purchaseDay;
        
        public LotteryTicket(List<Integer> numbers, long purchaseDay) {
            this.ticketId = UUID.randomUUID();
            this.numbers = new ArrayList<>(numbers);
            this.purchaseDay = purchaseDay;
        }
        
        public UUID getTicketId() { return ticketId; }
        public List<Integer> getNumbers() { return new ArrayList<>(numbers); }
        public long getPurchaseDay() { return purchaseDay; }
        
        public int countMatches(List<Integer> winningNumbers) {
            int matches = 0;
            for (Integer num : numbers) {
                if (winningNumbers.contains(num)) {
                    matches++;
                }
            }
            return matches;
        }
    }
    
    /**
     * Get player lottery data
     */
    public static PlayerLotteryData getPlayerData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> new PlayerLotteryData());
    }
    
    /**
     * Buy a lottery ticket with auto-generated numbers
     */
    public static boolean buyTicket(ServerPlayer player) {
        List<Integer> numbers = generateRandomNumbers();
        return buyTicket(player, numbers);
    }
    
    /**
     * Buy a lottery ticket with specific numbers
     */
    public static boolean buyTicket(ServerPlayer player, List<Integer> numbers) {
        PlayerLotteryData data = getPlayerData(player.getUUID());
        
        // Validate numbers
        if (numbers.size() != NUMBERS_PER_TICKET) {
            player.sendSystemMessage(Component.literal("§c§l[LOTTERY] Must select exactly " + NUMBERS_PER_TICKET + " numbers!"));
            return false;
        }
        
        for (Integer num : numbers) {
            if (num < 1 || num > MAX_NUMBER) {
                player.sendSystemMessage(Component.literal("§c§l[LOTTERY] Numbers must be between 1 and " + MAX_NUMBER + "!"));
                return false;
            }
        }
        
        // Check unique numbers
        if (new HashSet<>(numbers).size() != numbers.size()) {
            player.sendSystemMessage(Component.literal("§c§l[LOTTERY] All numbers must be unique!"));
            return false;
        }
        
        // Check max tickets
        if (data.getActiveTickets().size() >= MAX_TICKETS_PER_PLAYER) {
            player.sendSystemMessage(Component.literal("§c§l[LOTTERY] Maximum " + MAX_TICKETS_PER_PLAYER + " tickets per draw!"));
            return false;
        }
        
        // Check funds
        if (!CurrencyManager.canAfford(player, TICKET_COST)) {
            player.sendSystemMessage(Component.literal("§c§l[LOTTERY] Need " + CurrencyManager.format(TICKET_COST) + " to buy a ticket!"));
            return false;
        }
        
        // Purchase ticket
        CurrencyManager.removeMoney(player, TICKET_COST);
        long currentDay = player.level().getServer().overworld().getDayTime() / 24000;
        LotteryTicket ticket = new LotteryTicket(numbers, currentDay);
        data.getActiveTickets().add(ticket);
        data.addSpent(TICKET_COST);
        data.incrementTicketsPlayed();
        
        // Add to jackpot pool
        totalTicketSalesThisWeek += TICKET_COST;
        
        player.sendSystemMessage(Component.literal("§a§l[LOTTERY] Ticket purchased! Numbers: " + formatNumbers(numbers)));
        player.sendSystemMessage(Component.literal("§7Next draw in " + getDaysUntilDraw(currentDay) + " days"));
        
        return true;
    }
    
    /**
     * Process weekly lottery draw
     */
    public static void processWeeklyDraw(long currentDay, net.minecraft.server.MinecraftServer server) {
        if (currentDay < nextDrawDay) {
            return; // Not time for draw yet
        }
        
        // Generate winning numbers
        List<Integer> winningNumbers = generateRandomNumbers();
        lastWinningNumbers = winningNumbers;
        lastDrawDay = currentDay;
        lastDrawWinners.clear();
        
        // Calculate jackpot increase
        long jackpotIncrease = Math.max(MIN_JACKPOT_INCREASE, (long)(totalTicketSalesThisWeek * JACKPOT_GROWTH_RATE));
        long oldJackpot = currentJackpot;
        currentJackpot += jackpotIncrease;
        
        boolean jackpotWon = false;
        List<UUID> jackpotWinners = new ArrayList<>();
        
        // Check all players' tickets
        for (UUID playerUUID : playerData.keySet()) {
            PlayerLotteryData data = getPlayerData(playerUUID);
            
            if (data.getActiveTickets().isEmpty()) {
                continue;
            }
            
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            boolean hasWinnings = false;
            long totalWinnings = 0;
            List<String> prizes = new ArrayList<>();
            
            for (LotteryTicket ticket : data.getActiveTickets()) {
                int matches = ticket.countMatches(winningNumbers);
                long prize = 0;
                
                switch (matches) {
                    case 6: // Jackpot!
                        jackpotWon = true;
                        jackpotWinners.add(playerUUID);
                        break;
                    case 5:
                        prize = PRIZE_MATCH_5;
                        prizes.add("Match 5: " + CurrencyManager.format(prize));
                        break;
                    case 4:
                        prize = PRIZE_MATCH_4;
                        prizes.add("Match 4: " + CurrencyManager.format(prize));
                        break;
                    case 3:
                        prize = PRIZE_MATCH_3;
                        prizes.add("Match 3: " + CurrencyManager.format(prize));
                        break;
                    case 2:
                        prize = PRIZE_MATCH_2;
                        prizes.add("Match 2: " + CurrencyManager.format(prize));
                        break;
                    case 1:
                        // Free ticket - will be credited
                        prizes.add("Match 1: FREE TICKET next draw!");
                        break;
                }
                
                if (prize > 0) {
                    totalWinnings += prize;
                    hasWinnings = true;
                }
                
                // Track winners by match count
                if (matches > 0) {
                    lastDrawWinners.put(matches, lastDrawWinners.getOrDefault(matches, 0) + 1);
                }
            }
            
            // Award winnings
            if (player != null && hasWinnings) {
                CurrencyManager.addMoney(player, totalWinnings);
                data.addWon(totalWinnings);
                
                player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
                player.sendSystemMessage(Component.literal("§6§l✦ LOTTERY RESULTS ✦"));
                player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
                player.sendSystemMessage(Component.literal("§7Winning Numbers: §e" + formatNumbers(winningNumbers)));
                player.sendSystemMessage(Component.literal(""));
                
                for (String prizeMsg : prizes) {
                    player.sendSystemMessage(Component.literal("§a§l+ " + prizeMsg));
                }
                
                player.sendSystemMessage(Component.literal(""));
                player.sendSystemMessage(Component.literal("§6§lTotal Winnings: §a" + CurrencyManager.format(totalWinnings)));
                player.sendSystemMessage(Component.literal("§e§l═══════════════════════════════"));
            }
            
            // Clear tickets for next draw
            data.clearActiveTickets();
        }
        
        // Handle jackpot winners
        if (jackpotWon && !jackpotWinners.isEmpty()) {
            long jackpotPerWinner = oldJackpot / jackpotWinners.size();
            
            for (UUID winnerUUID : jackpotWinners) {
                ServerPlayer winner = server.getPlayerList().getPlayer(winnerUUID);
                if (winner != null) {
                    CurrencyManager.addMoney(winner, jackpotPerWinner);
                    getPlayerData(winnerUUID).addWon(jackpotPerWinner);
                    
                    winner.sendSystemMessage(Component.literal("§6§l═══════════════════════════════"));
                    winner.sendSystemMessage(Component.literal("§e§l✦✦✦ JACKPOT WINNER! ✦✦✦"));
                    winner.sendSystemMessage(Component.literal("§6§l═══════════════════════════════"));
                    winner.sendSystemMessage(Component.literal("§7Winning Numbers: §e" + formatNumbers(winningNumbers)));
                    winner.sendSystemMessage(Component.literal(""));
                    winner.sendSystemMessage(Component.literal("§a§lJACKPOT PRIZE: §6§l" + CurrencyManager.format(jackpotPerWinner)));
                    winner.sendSystemMessage(Component.literal("§6§l═══════════════════════════════"));
                    
                    // Broadcast to all players
                    server.getPlayerList().broadcastSystemMessage(
                        Component.literal("§6§l[LOTTERY] " + winner.getName().getString() + " WON THE JACKPOT: " + 
                            CurrencyManager.format(jackpotPerWinner) + "!"), false);
                }
            }
            
            // Reset jackpot
            currentJackpot = STARTING_JACKPOT;
            lastDrawWinners.put(6, jackpotWinners.size());
        }
        
        // Reset for next week
        totalTicketSalesThisWeek = 0;
        nextDrawDay = currentDay + 7;
        
        com.shopmod.ShopMod.LOGGER.info("Lottery draw complete! Winning numbers: " + winningNumbers + 
            ", Jackpot won: " + jackpotWon + ", Next draw: Day " + nextDrawDay);
    }
    
    /**
     * Generate 6 unique random numbers (1-50)
     */
    private static List<Integer> generateRandomNumbers() {
        List<Integer> numbers = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        
        while (numbers.size() < NUMBERS_PER_TICKET) {
            int num = RANDOM.nextInt(MAX_NUMBER) + 1;
            if (used.add(num)) {
                numbers.add(num);
            }
        }
        
        Collections.sort(numbers);
        return numbers;
    }
    
    /**
     * Format numbers for display
     */
    private static String formatNumbers(List<Integer> numbers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(numbers.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Get days until next draw
     */
    public static long getDaysUntilDraw(long currentDay) {
        return Math.max(0, nextDrawDay - currentDay);
    }
    
    /**
     * Get current jackpot
     */
    public static long getCurrentJackpot() {
        return currentJackpot;
    }
    
    /**
     * Get next draw day
     */
    public static long getNextDrawDay() {
        return nextDrawDay;
    }
    
    /**
     * Get last winning numbers
     */
    public static List<Integer> getLastWinningNumbers() {
        return new ArrayList<>(lastWinningNumbers);
    }
    
    /**
     * Get last draw day
     */
    public static long getLastDrawDay() {
        return lastDrawDay;
    }
    
    /**
     * Get last draw winners breakdown
     */
    public static Map<Integer, Integer> getLastDrawWinners() {
        return new HashMap<>(lastDrawWinners);
    }
    
    /**
     * Calculate odds for display
     */
    public static String getOdds(int matches) {
        switch (matches) {
            case 6:
                return "1 in 15,890,700";
            case 5:
                return "1 in 54,201";
            case 4:
                return "1 in 1,032";
            case 3:
                return "1 in 57";
            case 2:
                return "1 in 8";
            case 1:
                return "1 in 3";
            default:
                return "N/A";
        }
    }
}
