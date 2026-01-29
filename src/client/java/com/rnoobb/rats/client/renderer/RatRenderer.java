package com.rnoobb.rats.client.renderer;

import com.rnoobb.rats.client.ModModelLayers;
import com.rnoobb.rats.client.model.RatModel;
import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RatRenderer extends MobEntityRenderer<RatEntity, RatModel> {
    public RatRenderer(EntityRendererFactory.Context context) {
        super(context, new RatModel(context.getPart(ModModelLayers.RAT)), 0.17f);
    }

    @Override
    protected void scale(RatEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(0.33f, 0.33f, 0.33f);
        super.scale(entity, matrices, amount);
    }

    @Override
    public Identifier getTexture(RatEntity entity) {
        return new Identifier("rats_and_creatures", "textures/rat.png");
    }
}