package com.rnoobb.rats.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

// Используем PathAwareEntity (для мобов, которые ходят) или HostileEntity (для врагов)
public class RatEntity extends PathAwareEntity {

    public RatEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // Инициализация целей (AI)
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this)); // Плавать
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 1.0D)); // Бродить
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F)); // Смотреть на игрока
        this.goalSelector.add(3, new LookAroundGoal(this)); // Оглядываться
    }

    // Атрибуты (здоровье, скорость)
    public static DefaultAttributeContainer.Builder createRatAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0D);
    }
}
