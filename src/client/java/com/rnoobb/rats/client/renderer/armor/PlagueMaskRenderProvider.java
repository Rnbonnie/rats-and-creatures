package com.rnoobb.rats.client.renderer.armor;

import com.rnoobb.rats.item.PlagueMaskItem;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.animatable.client.RenderProvider;

public class PlagueMaskRenderProvider implements RenderProvider {
    private PlagueMaskRenderer renderer;

    @Override
    public BipedEntityModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, BipedEntityModel<LivingEntity> original) {
        if (this.renderer == null) {
            this.renderer = new PlagueMaskRenderer();
        }

        this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

        return this.renderer;
    }
}
