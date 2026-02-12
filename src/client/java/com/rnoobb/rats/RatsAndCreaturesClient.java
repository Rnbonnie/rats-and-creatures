package com.rnoobb.rats;

import com.rnoobb.rats.client.renderer.RatRenderer;
import com.rnoobb.rats.client.screen.RatScreen;
import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class RatsAndCreaturesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.RAT, RatRenderer::new);
        HandledScreens.register(ModScreenHandlers.RAT_SCREEN_HANDLER, RatScreen::new);
	}
}
