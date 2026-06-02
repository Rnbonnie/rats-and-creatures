package com.rnoobb.rats.world;

import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import com.rnoobb.rats.entity.custom.RatEntity;
import com.rnoobb.rats.entity.custom.AbstractHelperEntity;
import com.rnoobb.rats.entity.custom.RavenEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;

public class ModWorldGeneration {
    public static void registerEntitySpawns() {
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE,
                ModEntities.RAVEN, 6, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE,
                ModEntities.RAT, 8, 2, 4);
        SpawnRestriction.register(ModEntities.BAT, SpawnRestriction.Location.NO_RESTRICTIONS,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BatCompanionEntity::canSpawn);
        SpawnRestriction.register(ModEntities.RAVEN, SpawnRestriction.Location.NO_RESTRICTIONS,
                Heightmap.Type.MOTION_BLOCKING, RavenEntity::canSpawn);
        SpawnRestriction.register(ModEntities.RAT, SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, RatEntity::canSpawn);
    }
}
