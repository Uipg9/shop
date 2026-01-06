package com.shopmod;

import net.fabricmc.api.ClientModInitializer;

public class ShopModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ShopMod.LOGGER.info("Shop Mod client is initializing!");
		
		// Client-specific initialization
		// Register keybindings if needed
	}
}
