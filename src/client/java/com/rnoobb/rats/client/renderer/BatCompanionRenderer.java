package com.rnoobb.rats.client.renderer;

import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

class BatCompanionVanillaModel extends SinglePartEntityModel<BatCompanionEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWingTip;

    public BatCompanionVanillaModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightWing = this.body.getChild("right_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.leftWing = this.body.getChild("left_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(BatCompanionEntity batEntity, float f, float g, float h, float i, float j) {
        if (batEntity.isSitting()) {
            this.head.pitch = j * ((float)Math.PI / 180);
            this.head.yaw = (float)Math.PI - i * ((float)Math.PI / 180);
            this.head.roll = (float)Math.PI;
            this.head.setPivot(0.0f, -2.0f, 0.0f);
            this.rightWing.setPivot(-3.0f, 0.0f, 3.0f);
            this.leftWing.setPivot(3.0f, 0.0f, 3.0f);
            this.body.pitch = (float)Math.PI;
            this.rightWing.pitch = -0.15707964f;
            this.rightWing.yaw = -1.2566371f;
            this.rightWingTip.yaw = -1.7278761f;
            this.leftWing.pitch = this.rightWing.pitch;
            this.leftWing.yaw = -this.rightWing.yaw;
            this.leftWingTip.yaw = -this.rightWingTip.yaw;
        } else {
            this.head.pitch = j * ((float)Math.PI / 180);
            this.head.yaw = i * ((float)Math.PI / 180);
            this.head.roll = 0.0f;
            this.head.setPivot(0.0f, 0.0f, 0.0f);
            this.rightWing.setPivot(0.0f, 0.0f, 0.0f);
            this.leftWing.setPivot(0.0f, 0.0f, 0.0f);
            this.body.pitch = 0.7853982f + MathHelper.cos(h * 0.1f) * 0.15f;
            this.body.yaw = 0.0f;
            this.rightWing.yaw = MathHelper.cos(h * 74.48451f * ((float)Math.PI / 180)) * (float)Math.PI * 0.25f;
            this.leftWing.yaw = -this.rightWing.yaw;
            this.rightWingTip.yaw = this.rightWing.yaw * 0.5f;
            this.leftWingTip.yaw = -this.rightWing.yaw * 0.5f;
        }
    }
}

public class BatCompanionRenderer extends MobEntityRenderer<BatCompanionEntity, BatCompanionVanillaModel> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/bat.png");

    public BatCompanionRenderer(EntityRendererFactory.Context context) {
        super(context, new BatCompanionVanillaModel(context.getPart(EntityModelLayers.BAT)), 0.25F);
    }

    @Override
    public Identifier getTexture(BatCompanionEntity entity) {
        return TEXTURE;
    }
}
