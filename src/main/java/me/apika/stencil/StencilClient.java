package me.apika.stencil;

import me.apika.stencil.config.ConfigStorage;
import me.apika.stencil.gui.ConfigScreen;
import me.apika.stencil.spawnproof.SpawnProofManager;
import me.apika.stencil.spawnproof.SpawnProofRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StencilClient implements ClientModInitializer {
	public static final String MOD_ID = "stencil";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		ConfigStorage.load();
		ClientTickEvents.END_CLIENT_TICK.register(ConfigScreen::tickHotkey);
		ClientTickEvents.END_CLIENT_TICK.register(SpawnProofManager.getInstance()::onClientTick);
		LevelRenderEvents.COLLECT_SUBMITS.register(SpawnProofRenderer::collectSubmits);
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((mc, level) -> SpawnProofManager.getInstance().clear());
		LOGGER.info("Stencil initialised");
	}
}
