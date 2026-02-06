package com.rnoobb.rats;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.rnoobb.rats.entity.ModEntities;

public class ModItems {

    public static final Item CHEESE = registerItem("cheese", new Item(new FabricItemSettings().food(new FoodComponent.Builder().hunger(4).saturationModifier(0.3f).build())));
    public static final Item FAKE_BLOOD_BOTTLE = registerItem("fake_blood_bottle", new Item(new FabricItemSettings()));
    public static final Item RAT_SPAWN_EGG = registerItem("rat_spawn_egg",new SpawnEggItem(ModEntities.RAT, 0xc4c4c4, 0xadadad, new FabricItemSettings()));
    
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RatsAndCreatures.MOD_ID, name), item);
    }
    public static void registerModItems() {
        RatsAndCreatures.LOGGER.info("Registering Mod Items for " + RatsAndCreatures.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(CHEESE);
            content.add(FAKE_BLOOD_BOTTLE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(RAT_SPAWN_EGG);
        });
    }
}
