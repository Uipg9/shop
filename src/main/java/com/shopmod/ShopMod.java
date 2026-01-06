package com.shopmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopMod implements ModInitializer {
	public static final String MOD_ID = "shop";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Shop Mod is initializing!");
		
		// TODO: Register commands
		// TODO: Register event handlers
		// TODO: Load configuration
		// TODO: Initialize shop system
	}
}
