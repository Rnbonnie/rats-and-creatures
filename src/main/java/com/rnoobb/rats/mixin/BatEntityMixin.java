package com.rnoobb.rats.mixin;

import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatEntity.class)
public class BatEntityMixin {
    @Unique
    private boolean rats$replaced;

    @Inject(method = "tick", at = @At("HEAD"))
    private void rats$replaceVanillaBat(CallbackInfo ci) {
        BatEntity self = (BatEntity) (Object) this;
        if (this.rats$replaced || self.getWorld().isClient || self.isRemoved()) {
            return;
        }

        if (!(self.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        BatCompanionEntity replacement = ModEntities.BAT.create(serverWorld);
        if (replacement == null) {
            return;
        }

        this.rats$replaced = true;
        replacement.refreshPositionAndAngles(self.getX(), self.getY(), self.getZ(), self.getYaw(), self.getPitch());
        replacement.setVelocity(self.getVelocity());
        replacement.setAiDisabled(self.isAiDisabled());
        replacement.setCustomName(self.getCustomName());
        replacement.setCustomNameVisible(self.isCustomNameVisible());
        replacement.initialize(serverWorld, serverWorld.getLocalDifficulty(self.getBlockPos()), SpawnReason.CONVERSION, (EntityData) null, null);
        serverWorld.spawnEntity(replacement);
        self.discard();
    }
}
