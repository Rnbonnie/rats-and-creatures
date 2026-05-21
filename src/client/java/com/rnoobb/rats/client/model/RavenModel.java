package com.rnoobb.rats.client.model;

import com.rnoobb.rats.entity.custom.RavenEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class RavenModel extends GeoModel<RavenEntity> {
    @Override
    public Identifier getModelResource(RavenEntity animatable) {
        return new Identifier("rats_and_creatures", "geo/raven.geo.json");
    }

    @Override
    public Identifier getTextureResource(RavenEntity animatable) {
        return new Identifier("rats_and_creatures", "textures/entity/raven.png");
    }

    @Override
    public Identifier getAnimationResource(RavenEntity animatable) {
        return new Identifier("rats_and_creatures", "animations/raven.animation.json");
    }

    @Override
    public void setCustomAnimations(RavenEntity animatable, long instanceId, AnimationState<RavenEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
            head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}
