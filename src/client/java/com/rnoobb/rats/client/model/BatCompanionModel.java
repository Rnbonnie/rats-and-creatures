package com.rnoobb.rats.client.model;

import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class BatCompanionModel extends SinglePartEntityModel<BatCompanionEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public BatCompanionModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.rightWing = root.getChild("right_wing");
        this.leftWing = root.getChild("left_wing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F)
                        .uv(20, 0).cuboid(-4.0F, -5.0F, -1.0F, 2.0F, 3.0F, 1.0F)
                        .uv(20, 4).mirrored().cuboid(2.0F, -5.0F, -1.0F, 2.0F, 3.0F, 1.0F).mirrored(false),
                ModelTransform.pivot(0.0F, 16.0F, 0.0F));
        root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
                        .uv(0, 10).cuboid(-2.0F, -1.5F, -1.5F, 4.0F, 6.0F, 3.0F),
                ModelTransform.pivot(0.0F, 18.0F, 0.0F));
        root.addChild("right_wing", ModelPartBuilder.create()
                        .uv(14, 10).cuboid(-8.0F, 0.0F, -0.5F, 8.0F, 6.0F, 1.0F),
                ModelTransform.pivot(-2.0F, 17.0F, 0.0F));
        root.addChild("left_wing", ModelPartBuilder.create()
                        .uv(14, 10).mirrored().cuboid(0.0F, 0.0F, -0.5F, 8.0F, 6.0F, 1.0F).mirrored(false),
                ModelTransform.pivot(2.0F, 17.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(BatCompanionEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.pivotY = 16.0F;
        this.body.pivotY = 18.0F;
        this.rightWing.pivotY = 17.0F;
        this.leftWing.pivotY = 17.0F;
        this.head.yaw = headYaw * ((float) Math.PI / 180.0F);
        this.head.pitch = headPitch * ((float) Math.PI / 180.0F);
        this.body.pitch = 0.0F;
        this.rightWing.pitch = 0.0F;
        this.leftWing.pitch = 0.0F;
        this.rightWing.roll = 0.0F;
        this.leftWing.roll = 0.0F;

        if (entity.isSitting()) {
            this.head.pitch = -0.35F;
            this.body.pitch = 0.45F;
            this.head.pivotY = 18.0F;
            this.body.pivotY = 19.0F;
            this.rightWing.yaw = 0.2F;
            this.leftWing.yaw = -0.2F;
            this.rightWing.roll = 0.9F;
            this.leftWing.roll = -0.9F;
            this.rightWing.pivotY = 18.0F;
            this.leftWing.pivotY = 18.0F;
            return;
        }

        float wingFlap = MathHelper.cos(animationProgress * 1.25F) * 0.75F;
        this.rightWing.yaw = wingFlap;
        this.leftWing.yaw = -wingFlap;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
