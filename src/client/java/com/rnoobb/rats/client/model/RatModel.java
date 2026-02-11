package com.rnoobb.rats.client.model;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

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

	@Override
	public void setCustomAnimations(RatEntity animatable, long instanceId, AnimationState<RatEntity> animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("head");

		if (head != null) {
			EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
			head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
			head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
		}
	}
}