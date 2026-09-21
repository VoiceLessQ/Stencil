package me.apika.stencil;

import me.apika.stencil.config.ConfigStorage;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StencilClient implements ClientModInitializer {
	public static final String MOD_ID = "stencil";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		ConfigStorage.load();
		LOGGER.info("Stencil initialised");
	}
}
