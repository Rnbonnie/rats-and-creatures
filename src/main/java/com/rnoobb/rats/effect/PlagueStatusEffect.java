package com.rnoobb.rats.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PlagueStatusEffect extends StatusEffect {
    public PlagueStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x6F8D5B);
        this.addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                "9f33a4f3-c4b9-4c1f-8a3e-c0ed645b5ef8",
                -0.1D,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        DamageSource source = entity.getDamageSources().magic();
        entity.damage(source, 1.0F + (float) amplifier * 0.5F);
    }
}
