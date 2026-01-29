package com.rnoobb.rats;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.minecraft.item.FoodComponent;

public class RatsAndCreatures implements ModInitializer {
	public static final String MOD_ID = "rats_and_creatures";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
public static final FoodComponent SUSPICIOUS_FOOD_COMPONENT = new FoodComponent.Builder()
		.hunger(3)
        .saturationModifier(0.3f)
        .build();
	@Override
	public void onInitialize() {
		ModItems.registerModItems();

        LOGGER.info("Hello Fabric world!");
	}
}
