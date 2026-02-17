package com.rnoobb.rats.client.renderer.layer;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RatArmorLayer extends GeoRenderLayer<RatEntity> {
    // Пути к ТВОИМ текстурам брони (их нужно нарисовать!)
    // Замени "modid" на свой ID мода
    private static final Identifier DIAMOND_LAYER = new Identifier("rats_and_creatures", "textures/entity/rat/armor/diamond_rat_layer.png");
    private static final Identifier GOLD_LAYER = new Identifier("rats_and_creatures", "textures/entity/rat/armor/gold_rat_layer.png");
    private static final Identifier IRON_LAYER = new Identifier("rats_and_creatures", "textures/entity/rat/armor/iron_rat_layer.png");

    public RatArmorLayer(GeoRenderer<RatEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack poseStack, RatEntity entity, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ItemStack helmet = entity.getEquippedStack(EquipmentSlot.HEAD);

        if (!helmet.isEmpty() && helmet.getItem() instanceof ArmorItem) {
            Identifier armorTexture = getArmorTexture(helmet);

            if (armorTexture != null) {
                poseStack.push();
                RenderLayer armorRenderType = RenderLayer.getArmorCutoutNoCull(armorTexture);
                
                getRenderer().reRender(getDefaultBakedModel(entity), poseStack, bufferSource, entity, armorRenderType,
                        bufferSource.getBuffer(armorRenderType), partialTick, packedLight, OverlayTexture.DEFAULT_UV,
                        1f, 1f, 1f, 1f);

                poseStack.pop();
            }
        }
    }

    private Identifier getArmorTexture(ItemStack stack) {
        String name = stack.getItem().toString();
        if (name.contains("diamond")) return DIAMOND_LAYER;
        if (name.contains("gold")) return GOLD_LAYER;
        if (name.contains("iron")) return IRON_LAYER;
        return null;
    }
}
