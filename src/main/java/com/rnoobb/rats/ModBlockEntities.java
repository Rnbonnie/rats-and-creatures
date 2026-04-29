package com.rnoobb.rats;

import com.rnoobb.rats.block.entity.TrapBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static final BlockEntityType<TrapBlockEntity> TRAP = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(RatsAndCreatures.MOD_ID, "trap"),
            FabricBlockEntityTypeBuilder.create(TrapBlockEntity::new, ModBlocks.TRAP).build()
    );

    private ModBlockEntities() {
    }

    public static void registerModBlockEntities() {
        RatsAndCreatures.LOGGER.info("Registering Mod Block Entities for " + RatsAndCreatures.MOD_ID);
    }
}
