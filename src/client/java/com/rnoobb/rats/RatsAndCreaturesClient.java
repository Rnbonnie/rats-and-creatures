package com.rnoobb.rats;

import com.rnoobb.rats.client.ModModelLayers;
import com.rnoobb.rats.client.renderer.RatRenderer;
import com.rnoobb.rats.client.renderer.BatCompanionRenderer;
import com.rnoobb.rats.client.renderer.RavenRenderer;
import com.rnoobb.rats.client.screen.RatScreen;
import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.item.CageItem;
import com.rnoobb.rats.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import com.rnoobb.rats.client.renderer.armor.PlagueMaskRenderProvider;
import com.rnoobb.rats.item.PlagueMaskItem;
import net.minecraft.util.Identifier;

public class RatsAndCreaturesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.BAT, BatCompanionRenderer::new);
		EntityRendererRegistry.register(ModEntities.RAT, RatRenderer::new);
		EntityRendererRegistry.register(ModEntities.RAVEN, RavenRenderer::new);
        HandledScreens.register(ModScreenHandlers.RAT_SCREEN_HANDLER, RatScreen::new);

        ModelPredicateProviderRegistry.register(ModItems.CAGE, new Identifier(RatsAndCreatures.MOD_ID, "filled"),
                (stack, world, entity, seed) -> CageItem.hasStoredEntity(stack) ? 1f : 0f);

        PlagueMaskItem.RENDERER_PROVIDER = consumer -> consumer.accept(new PlagueMaskRenderProvider());
	}
}

