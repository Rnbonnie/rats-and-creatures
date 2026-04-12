package com.rnoobb.rats;

import com.rnoobb.rats.effect.PlagueStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModStatusEffects {
    public static final StatusEffect PLAGUE = Registry.register(
            Registries.STATUS_EFFECT,
            new Identifier(RatsAndCreatures.MOD_ID, "plague"),
            new PlagueStatusEffect()
    );

    private ModStatusEffects() {
    }

    public static void registerModEffects() {
        RatsAndCreatures.LOGGER.info("Registering status effects for {}", RatsAndCreatures.MOD_ID);
    }
}
