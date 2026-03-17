package com.rnoobb.rats.client.renderer;

import com.rnoobb.rats.client.model.RatModel;
import com.rnoobb.rats.client.renderer.layer.*;
import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RatRenderer extends GeoEntityRenderer<RatEntity> {
    public RatRenderer(EntityRendererFactory.Context context) {
        super(context, new RatModel());
        this.addRenderLayer(new RatItemLayer(this));
        this.addRenderLayer(new RatArmorLayer(this));
  }

  @Override
    public void render(RatEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight) {
        float baseScale = 0.33f;
        if (entity.isBaby()) {
            baseScale *= 0.6f;
        }
        poseStack.scale(baseScale, baseScale, baseScale);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    };

}
