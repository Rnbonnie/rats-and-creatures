package com.rnoobb.rats.client.renderer;

import com.rnoobb.rats.client.ModModelLayers;
import com.rnoobb.rats.client.model.BatCompanionModel;
import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class BatCompanionRenderer extends MobEntityRenderer<BatCompanionEntity, BatCompanionModel> {
    private static final Identifier TEXTURE = new Identifier("rats_and_creatures", "textures/entity/bat.png");

    public BatCompanionRenderer(EntityRendererFactory.Context context) {
        super(context, new BatCompanionModel(context.getPart(ModModelLayers.BAT)), 0.35F);
    }

    @Override
    public Identifier getTexture(BatCompanionEntity entity) {
        return TEXTURE;
    }
}
