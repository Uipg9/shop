package com.qolmod;

import net.fabricmc.api.ClientModInitializer;

public class QOLModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		QOLMod.LOGGER.info("QOL Mod client is initializing!");
		
		// Client-specific initialization
		// Register keybindings, client-side renderers, etc.
	}
}
