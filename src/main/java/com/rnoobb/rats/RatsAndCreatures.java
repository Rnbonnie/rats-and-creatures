package com.rnoobb.rats;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.network.ModNetworking;


public class RatsAndCreatures implements ModInitializer {
	public static final String MOD_ID = "rats_and_creatures";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModEntities.registerModEntities();
		ModEntities.registerAttributes();
		ModNetworking.registerServerReceivers();

        LOGGER.info("Hello Fabric world!");
	}
}
