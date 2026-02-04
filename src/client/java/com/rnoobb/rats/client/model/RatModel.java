package com.rnoobb.rats.client.model;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RatModel extends GeoModel<RatEntity> {

	@Override
	public Identifier getModelResource(RatEntity animatable) {
		return new Identifier("rats_and_creatures", "geo/rat.geo.json");
	}

	@Override
	public Identifier getTextureResource(RatEntity animatable) {
		return new Identifier("rats_and_creatures", "textures/rat.png");
	}

	@Override
	public Identifier getAnimationResource(RatEntity animatable) {
		return new Identifier("rats_and_creatures", "animations/rat.animation.json");
	}
}