package com.rnoobb.rats;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

public final class ModEntityDrops {
    private ModEntityDrops() {
    }

    public static void registerEntityDrops() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
                return;
            }

            float dropChance = getBloodClotDropChance(entity);
            if (dropChance <= 0.0F || serverWorld.random.nextFloat() >= dropChance) {
                return;
            }

            Block.dropStack(serverWorld, entity.getBlockPos(), new ItemStack(ModItems.BLOOD_CLOT));
        });
    }

    private static float getBloodClotDropChance(LivingEntity entity) {
        if (entity instanceof VillagerEntity) {
            return 0.10F;
        }
        if (entity instanceof ZombieEntity) {
            return 0.03F;
        }
        if (entity instanceof PassiveEntity) {
            return 0.05F;
        }
        return 0.0F;
    }
}
