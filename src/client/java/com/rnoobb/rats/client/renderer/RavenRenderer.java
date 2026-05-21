package com.rnoobb.rats.client.renderer;

import com.rnoobb.rats.client.model.RavenModel;
import com.rnoobb.rats.entity.custom.RavenEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RavenRenderer extends GeoEntityRenderer<RavenEntity> {
    public RavenRenderer(EntityRendererFactory.Context context) {
        super(context, new RavenModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public void render(RavenEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.scale(0.45F, 0.45F, 0.45F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
