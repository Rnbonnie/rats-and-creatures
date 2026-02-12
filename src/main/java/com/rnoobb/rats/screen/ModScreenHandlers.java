package com.rnoobb.rats.screen;

import com.rnoobb.rats.RatsAndCreatures;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<RatScreenHandler> RAT_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(RatsAndCreatures.MOD_ID, "rat_screen_handler"),
                    new ExtendedScreenHandlerType<>(RatScreenHandler::new));

    public static void registerScreenHandlers() {
        RatsAndCreatures.LOGGER.info("Registering Screen Handlers for " + RatsAndCreatures.MOD_ID);
    }
}
