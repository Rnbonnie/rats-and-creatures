package com.rnoobb.rats;

import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.item.PlagueMaskItem;
import com.rnoobb.rats.item.BloodWandItem;
import com.rnoobb.rats.item.RatFluteItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item BLOOD_CLOT = registerItem("blood_clot", new Item(new FabricItemSettings()));
    public static final Item CHEESE = registerItem("cheese", new Item(new FabricItemSettings().food(new FoodComponent.Builder().hunger(4).saturationModifier(0.3f).build())));
    public static final Item FAKE_BLOOD_BOTTLE = registerItem("fake_blood_bottle", new Item(new FabricItemSettings()));
    public static final Item BLOOD_WAND = registerItem("blood_wand", new BloodWandItem(new FabricItemSettings().maxCount(1)));
    public static final Item RAT_FLUTE = registerItem("rat_flute", new RatFluteItem(new FabricItemSettings().maxCount(1)));
    public static final Item LEATHER_PLAGUE_MASK = registerItem("leather_plague_mask", new PlagueMaskItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new FabricItemSettings().maxCount(1)));
    public static final Item IRON_PLAGUE_MASK = registerItem("iron_plague_mask", new PlagueMaskItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new FabricItemSettings().maxCount(1)));
    public static final Item GOLDEN_PLAGUE_MASK = registerItem("golden_plague_mask", new PlagueMaskItem(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new FabricItemSettings().maxCount(1)));
    public static final Item DIAMOND_PLAGUE_MASK = registerItem("diamond_plague_mask", new PlagueMaskItem(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new FabricItemSettings().maxCount(1)));
    public static final Item NETHERITE_PLAGUE_MASK = registerItem("netherite_plague_mask", new PlagueMaskItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new FabricItemSettings().maxCount(1).fireproof()));
    public static final Item RAVEN_SPAWN_EGG = registerItem("raven_spawn_egg", new SpawnEggItem(ModEntities.RAVEN, 0x141414, 0xc9ccd1, new FabricItemSettings()));
    public static final Item RAT_SPAWN_EGG = registerItem("rat_spawn_egg",new SpawnEggItem(ModEntities.RAT, 0xc4c4c4, 0xadadad, new FabricItemSettings()));
    public static final Item CAGE = ModBlocks.CAGE.asItem();
    public static final Item TRAP = ModBlocks.TRAP.asItem();
    
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RatsAndCreatures.MOD_ID, name), item);
    }
    public static void registerModItems() {
        RatsAndCreatures.LOGGER.info("Registering Mod Items for " + RatsAndCreatures.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(CHEESE);
            content.add(FAKE_BLOOD_BOTTLE);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> content.add(BLOOD_CLOT));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(LEATHER_PLAGUE_MASK);
            content.add(IRON_PLAGUE_MASK);
            content.add(GOLDEN_PLAGUE_MASK);
            content.add(DIAMOND_PLAGUE_MASK);
            content.add(NETHERITE_PLAGUE_MASK);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(RAT_FLUTE);
            content.add(BLOOD_WAND);
            content.add(CAGE);
            content.add(TRAP);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(RAVEN_SPAWN_EGG);
            content.add(RAT_SPAWN_EGG);
        });
    }
}
