package com.rnoobb.rats.client.renderer;

import com.rnoobb.rats.client.model.RatModel;
import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RatRenderer extends GeoEntityRenderer<RatEntity> {
    public RatRenderer(EntityRendererFactory.Context context) {
        super(context, new RatModel());
    }

    @Override
    public void render(RatEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.scale(0.33f, 0.33f, 0.33f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}