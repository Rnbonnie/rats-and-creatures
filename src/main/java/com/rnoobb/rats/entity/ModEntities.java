package com.rnoobb.rats.entity;

import com.rnoobb.rats.RatsAndCreatures;
import com.rnoobb.rats.entity.custom.RatEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<RatEntity> RAT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RatsAndCreatures.MOD_ID, "rat"), // ID мода и моба
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RatEntity::new)
                    .dimensions(EntityDimensions.fixed(1.0f, 1.0f)) // Размер хитбокса
                    .build()
    );

    public static void registerModEntities() {
        RatsAndCreatures.LOGGER.info("Registering Entities for " + RatsAndCreatures.MOD_ID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(RAT, RatEntity.createRatAttributes());
    }
}
