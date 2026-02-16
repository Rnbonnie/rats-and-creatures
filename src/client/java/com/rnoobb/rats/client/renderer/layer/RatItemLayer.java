package com.rnoobb.rats.client.renderer.layer;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class RatItemLayer extends BlockAndItemGeoLayer<RatEntity> {
    public RatItemLayer(GeoRenderer<RatEntity> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getStackForBone(GeoBone bone, RatEntity animatable) {
        if ("item".equals(bone.getName())) {
            return animatable.getHeadItem();
        }
        return null;
    }

    @Override
    protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, RatEntity animatable) {
        return ModelTransformationMode.HEAD;
    }

    @Override
    protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, RatEntity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay) {
        poseStack.push();
        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        poseStack.pop();
    }
}
