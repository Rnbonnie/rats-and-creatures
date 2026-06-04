package com.rnoobb.rats.client.model.armor;

import com.rnoobb.rats.RatsAndCreatures;
import com.rnoobb.rats.item.PlagueMaskItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PlagueMaskModel extends GeoModel<PlagueMaskItem> {
    @Override
    public Identifier getModelResource(PlagueMaskItem animatable) {
        return new Identifier(RatsAndCreatures.MOD_ID, "geo/plague_mask.geo.json");
    }

    @Override
    public Identifier getTextureResource(PlagueMaskItem animatable) {
        // Here we can return different textures based on the material
        String name = animatable.getMaterial().getName();
        // Since the user provided "diamond_plague_mask_model.png", I'll use that as a base
        // and assume others follow the pattern if they exist.
        // For now, I'll map them to the diamond one if not found, or just use the material name.
        return new Identifier(RatsAndCreatures.MOD_ID, "textures/entity/armor/" + name + "_plague_mask.png");
    }

    @Override
    public Identifier getAnimationResource(PlagueMaskItem animatable) {
        return null; // Armor doesn't usually have animations
    }
}
