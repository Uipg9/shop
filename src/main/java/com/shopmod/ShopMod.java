package com.shopmod;

import com.shopmod.command.BankCommand;
import com.shopmod.command.ShopCommands;
import com.shopmod.commands.FarmCommand;
import com.shopmod.commands.PropertyCommand;
import com.shopmod.commands.AuctionCommand;
import com.shopmod.commands.StocksCommand;
import com.shopmod.commands.ResearchCommand;
import com.shopmod.commands.BlackMarketCommand;
import com.shopmod.commands.HubCommand;
import com.shopmod.commands.WandCommand;
import com.shopmod.commands.MiningCommand;
import com.shopmod.commands.PetsCommand;
import com.shopmod.data.ShopDataManager;
import com.shopmod.farm.FarmManager;
import com.shopmod.property.PropertyManager;
import com.shopmod.auction.AuctionManager;
import com.shopmod.auction.AuctionItem;
import com.shopmod.stocks.StockOptionsManager;
import com.shopmod.research.ResearchManager;
import com.shopmod.blackmarket.BlackMarketManager;
import com.shopmod.mining.MiningManager;
import com.shopmod.income.IncomeManager;
import com.shopmod.spawner.SpawnerPickupHandler;
import com.shopmod.upgrades.UpgradeManager;
import com.shopmod.upgrades.UpgradeEffectApplier;
import com.shopmod.economy.PriceFluctuation;
import com.shopmod.crates.LuckyCrateManager;
import com.shopmod.events.BlockEarningsHandler;
import com.shopmod.events.MobEarningsHandler;
import com.shopmod.events.PlayerJoinHandler;
import com.shopmod.events.WandUseHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopMod implements ModInitializer {
	public static final String MOD_ID = "shop";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static ShopDataManager dataManager = null;
	
	private static long lastPriceUpdate = 0;
	private static long lastDailyUpdate = -1;
	private static int lastPriceUpdatePeriod = -1; // Track which period (0=dawn, 1=noon, 2=dusk, 3=midnight)

	@Override
	public void onInitialize() {
		LOGGER.info("Shop Mod is initializing!");
		
		// Initialize income system (block breaking rewards)
		IncomeManager.initialize();
		LOGGER.info("Income system initialized!");
		
		// Initialize universal earnings systems
		BlockEarningsHandler.register();
		LOGGER.info("Block earnings system initialized!");
		
		MobEarningsHandler.register();
		LOGGER.info("Mob earnings system initialized!");
		
		// Initialize player join handler (welcome message)
		PlayerJoinHandler.register();
		LOGGER.info("Player join handler initialized!");
		
		// Initialize sell wand system
		WandUseHandler.register();
		LOGGER.info("Sell wand system initialized!");
		

		// Initialize spawner pickup with Silk Touch
		SpawnerPickupHandler.initialize();
		LOGGER.info("Spawner pickup system initialized!");
		
		// Initialize upgrade system
		UpgradeManager.initialize();
		LOGGER.info("Upgrade system initialized!");
		
		// Initialize upgrade effect applier (applies haste, etc.)
		UpgradeEffectApplier.initialize();
		LOGGER.info("Upgrade effect applier initialized!");
		
		// Initialize stock market
		PriceFluctuation.updatePrices();
		LOGGER.info("Stock market initialized!");
		
		// Initialize data manager on server start
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			dataManager = new ShopDataManager(server);
			LOGGER.info("Shop data manager initialized!");
			
			// Initialize daily crates
			long currentDay = server.overworld().getDayTime() / 24000;
			LuckyCrateManager.updateDailyCrates(currentDay);
			LOGGER.info("Lucky crates initialized for day " + currentDay);
		});
		
		// Save data on server stop
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if (dataManager != null) {
				dataManager.save();
				LOGGER.info("Shop data saved!");
			}
		});
		
		// Register server tick event for price updates (based on Minecraft time) and daily resets
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long dayTime = server.overworld().getDayTime();
			
			// Process auction endings
			AuctionManager.processAuctionEndings(dayTime);
			
			// Process expired stock options
			StockOptionsManager.processExpiredOptions(dayTime, server);
			
			long timeOfDay = dayTime % 24000; // Time within current day (0-23999)
			long currentDay = dayTime / 24000;
			
			// Update prices 4 times per Minecraft day:
			// Dawn (0-5999), Noon (6000-11999), Dusk (12000-17999), Midnight (18000-23999)
			// Determine which period we're in
			int currentPeriod = -1;
			String timeLabel = "";
			
			if (timeOfDay < 6000) {
				currentPeriod = 0;
				timeLabel = "Dawn";
			} else if (timeOfDay < 12000) {
				currentPeriod = 1;
				timeLabel = "Noon";
			} else if (timeOfDay < 18000) {
				currentPeriod = 2;
				timeLabel = "Dusk";
			} else {
				currentPeriod = 3;
				timeLabel = "Midnight";
			}
			
			// Only update if we've entered a new period
			if (currentPeriod != lastPriceUpdatePeriod) {
				PriceFluctuation.updatePrices();
				lastPriceUpdatePeriod = currentPeriod;
				LOGGER.info("Stock market prices updated at " + timeLabel + "!");
			}
			
			// Update crates and process investments at dawn (new day)
			if (currentDay > lastDailyUpdate) {
				LuckyCrateManager.updateDailyCrates(currentDay);
				
				// Generate new daily auctions and black market deals
				AuctionItem.generateDailyAuctions(dayTime);
				BlackMarketManager.generateDailyDeals();
				
				LOGGER.info("Lucky crates updated for day " + currentDay);
				
				// Process daily returns for all online players
				server.getPlayerList().getPlayers().forEach(player -> {
					// Process wallet interest (10% guaranteed)
					com.shopmod.currency.CurrencyManager.processDailyInterest(player);
					
					// Process bank investments (risky returns)
					com.shopmod.bank.BankManager.processDailyReturns(player);
					
					// Process loan payments
					com.shopmod.loan.LoanManager.processDailyPayments(player, currentDay);
					
					// Process village resource economy (V2)
					com.shopmod.village.VillageManager.processDailyVillage(player, currentDay);
					
					// Process trade center auto-selling
					com.shopmod.village.TradeCenterManager.processAutoSell(player);
				});
				
				// Process digital farm production (for all players)
				FarmManager.processDailyProduction(currentDay);
				
				// Process property passive income
				PropertyManager.processDailyIncome(currentDay, server);
				
				// Process research bonuses (Money Printer, etc.)
				ResearchManager.applyDailyIncome(server);
				
				// Process mining operations income
				MiningManager.processDailyIncome(currentDay, server);
				
				LOGGER.info("Daily processing complete: interest, investments, loans, village, farms, properties, research, and mining!");
				
				lastDailyUpdate = currentDay;
			}
		});
		
		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ShopCommands.register(dispatcher);
			BankCommand.register(dispatcher);
			com.shopmod.command.LoanCommand.register(dispatcher);
			FarmCommand.register(dispatcher);
			PropertyCommand.register(dispatcher);
			AuctionCommand.register(dispatcher);
			StocksCommand.register(dispatcher);
			ResearchCommand.register(dispatcher);
			MiningCommand.register(dispatcher);
			PetsCommand.register(dispatcher);
			BlackMarketCommand.register(dispatcher);
			HubCommand.register(dispatcher);
			WandCommand.register(dispatcher);
		});
		
		LOGGER.info("Shop Mod initialized! Use /shop to open the shop.");
	}
}
