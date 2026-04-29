package com.rnoobb.rats;

import com.rnoobb.rats.block.TrapBlock;
import com.rnoobb.rats.item.CageItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModBlocks {
    public static final Block CAGE = registerBlock(
            "cage",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque()),
            block -> new CageItem(block, new FabricItemSettings().maxCount(1))
    );

    public static final Block TRAP = registerBlock(
            "trap",
            new TrapBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).strength(2.5F).nonOpaque()),
            block -> new BlockItem(block, new FabricItemSettings())
    );

    private ModBlocks() {
    }

    private static Block registerBlock(String name, Block block, Function<Block, Item> itemFactory) {
        Block registeredBlock = Registry.register(Registries.BLOCK, new Identifier(RatsAndCreatures.MOD_ID, name), block);
        Registry.register(Registries.ITEM, new Identifier(RatsAndCreatures.MOD_ID, name), itemFactory.apply(registeredBlock));
        return registeredBlock;
    }

    public static void registerModBlocks() {
        RatsAndCreatures.LOGGER.info("Registering Mod Blocks for " + RatsAndCreatures.MOD_ID);
    }
}
