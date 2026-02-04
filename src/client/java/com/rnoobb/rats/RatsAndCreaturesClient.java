package com.rnoobb.rats;

import com.rnoobb.rats.client.renderer.RatRenderer;
import com.rnoobb.rats.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RatsAndCreaturesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.RAT, RatRenderer::new);
	}
}
