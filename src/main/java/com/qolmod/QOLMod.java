package com.qolmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QOLMod implements ModInitializer {
	public static final String MOD_ID = "qol-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("QOL Mod is initializing!");
		
		// Register items, blocks, and other content here
		// Example: Items.register();
		// Example: Blocks.register();
	}
}
