package com.shopmod.gui;

import com.shopmod.auction.*;
import com.shopmod.currency.CurrencyManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Auction House GUI - Browse and bid on items
 */
public class AuctionGui extends SimpleGui {
    private final ServerPlayer player;
    private int page = 0;
    private static final int ITEMS_PER_PAGE = 27;
    
    public AuctionGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.setTitle(Component.literal("§6§lAuction House"));
        updateDisplay();
    }
    
    private void updateDisplay() {
        // Clear GUI
        for (int i = 0; i < 54; i++) {
            this.clearSlot(i);
        }
        
        long currentTime = player.level().getServer().overworld().getDayTime();
        List<AuctionItem> auctions = AuctionManager.getCurrentAuctions(currentTime);
        
        // Info bar
        setupInfoBar(currentTime);
        
        // Display auction items
        displayAuctions(auctions, currentTime);
        
        // Navigation
        setupNavigation(auctions.size());
    }
    
    private void setupInfoBar(long currentTime) {
        // Time until refresh
        long timeUntilMidnight = 24000 - (currentTime % 24000);
        int minutesRemaining = (int)(timeUntilMidnight / 1000);
        
        setSlot(4, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal("§e§lAuction Refresh"))
            .addLoreLine(Component.literal("§7Resets at midnight"))
            .addLoreLine(Component.literal("§7Time remaining: §e~" + minutesRemaining + " minutes"))
        );
        
        // Player balance
        setSlot(8, new GuiElementBuilder(Items.GOLD_INGOT)
            .setName(Component.literal("§6§lYour Balance"))
            .addLoreLine(Component.literal(CurrencyManager.format(CurrencyManager.getBalance(player))))
        );
    }
    
    private void displayAuctions(List<AuctionItem> auctions, long currentTime) {
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, auctions.size());
        
        int slot = 9;
        for (int i = startIndex; i < endIndex && slot < 36; i++) {
            AuctionItem auction = auctions.get(i);
            final int auctionIndex = i;
            
            // Calculate time remaining
            long timeRemaining = auction.getTimeRemaining(currentTime);
            int minutesLeft = (int)(timeRemaining / 1000);
            
            boolean isPlayerBidding = auction.getCurrentBidder().equals(player.getName().getString());
            
            GuiElementBuilder builder = new GuiElementBuilder(auction.getItemStack().getItem())
                .setName(Component.literal((isPlayerBidding ? "§a" : "§e") + 
                    "§l" + auction.getDisplayName()))
                .addLoreLine(Component.literal("§7" + (auction.isRare() ? "§6★ RARE ★" : "Common Item")))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Current Bid: §6" + 
                    CurrencyManager.format(auction.getCurrentBid())))
                .addLoreLine(Component.literal("§7Leading Bidder: §b" + auction.getCurrentBidder()))
                .addLoreLine(Component.literal("§7Instant Buy: §a" + 
                    CurrencyManager.format(auction.getInstantBuyPrice())))
                .addLoreLine(Component.literal("§7Time Left: §e~" + minutesLeft + " min"))
                .addLoreLine(Component.literal(""));
            
            if (auction.getTier() > 0) {
                builder.addLoreLine(Component.literal("§7Tier: §d" + auction.getTier()));
            }
            
            builder.addLoreLine(Component.literal("§a§lLEFT CLICK §7to bid (+5%)"))
                .addLoreLine(Component.literal("§e§lSHIFT+LEFT §7to bid (+20%)"))
                .addLoreLine(Component.literal("§b§lRIGHT CLICK §7instant buy"));
            
            if (isPlayerBidding) {
                builder.glow();
                builder.addLoreLine(Component.literal("§a§lYou're winning!"));
            }
            
            builder.setCallback((index, type, action) -> {
                if (type.isRight) {
                    // Instant buy
                    AuctionManager.instantBuy(player, auctionIndex);
                    updateDisplay();
                } else if (type.isLeft && type.shift) {
                    // Bid 20% more
                    long newBid = (long)(auction.getCurrentBid() * 1.20);
                    AuctionManager.placeBid(player, auctionIndex, newBid);
                    updateDisplay();
                } else if (type.isLeft) {
                    // Bid 5% more
                    long newBid = (long)(auction.getCurrentBid() * 1.05);
                    AuctionManager.placeBid(player, auctionIndex, newBid);
                    updateDisplay();
                }
            });
            
            setSlot(slot, builder);
            slot++;
        }
    }
    
    private void setupNavigation(int totalItems) {
        int totalPages = (int)Math.ceil(totalItems / (double)ITEMS_PER_PAGE);
        
        // Previous page
        if (page > 0) {
            setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§l← Previous Page"))
                .addLoreLine(Component.literal("§7Page " + page + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    page--;
                    updateDisplay();
                })
            );
        }
        
        // Page info
        setSlot(49, new GuiElementBuilder(Items.PAPER)
            .setName(Component.literal("§6§lPage " + (page + 1) + "/" + Math.max(1, totalPages)))
            .addLoreLine(Component.literal("§7Total auctions: " + totalItems))
        );
        
        // Next page
        if (page < totalPages - 1) {
            setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e§lNext Page →"))
                .addLoreLine(Component.literal("§7Page " + (page + 2) + "/" + totalPages))
                .setCallback((index, type, action) -> {
                    page++;
                    updateDisplay();
                })
            );
        }
        
        // Hub button
        setSlot(52, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§l✦ Shop Hub"))
            .addLoreLine(Component.literal("§7Return to main menu"))
            .setCallback((index, type, action) -> {
                new HubGui(player).open();
            })
        );
    }
}
