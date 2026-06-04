package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.entity.custom.CompanionInventoryEntity;
import com.rnoobb.rats.entity.custom.RatEntity;
import com.rnoobb.rats.entity.custom.AbstractHelperEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FlyingCompanionWanderGoal extends Goal {
    private final TameableEntity tameable;
    private final double speed;
    private final int horizontalRadius;
    private final int verticalRadius;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int cooldown;

    public FlyingCompanionWanderGoal(TameableEntity tameable, double speed, int horizontalRadius, int verticalRadius) {
        this.tameable = tameable;
        this.speed = speed;
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (this.tameable.isSitting() || this.tameable.getNavigation().isFollowingPath()) {
            return false;
        }
        if (this.tameable instanceof CompanionInventoryEntity companion
                && companion.getBehavior() != AbstractHelperEntity.Behavior.WANDER) {
            return false;
        }

        Vec3d target = this.findTarget();
        if (target == null) {
            this.cooldown = 20 + this.tameable.getRandom().nextInt(20);
            return false;
        }

        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return !this.tameable.isSitting()
                && !this.tameable.getNavigation().isIdle()
                && (!(this.tameable instanceof CompanionInventoryEntity companion)
                || companion.getBehavior() == AbstractHelperEntity.Behavior.WANDER);
    }

    @Override
    public void start() {
        this.tameable.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return false;
    }

    @Nullable
    private Vec3d findTarget() {
        BlockPos anchor = this.tameable instanceof CompanionInventoryEntity companion
                ? companion.getWanderAnchor()
                : this.tameable.getOwner() != null ? this.tameable.getOwner().getBlockPos() : this.tameable.getBlockPos();

        for (int i = 0; i < 15; i++) {
            BlockPos candidate = anchor.add(
                    this.tameable.getRandom().nextBetween(-this.horizontalRadius, this.horizontalRadius),
                    this.tameable.getRandom().nextBetween(-this.verticalRadius, this.verticalRadius),
                    this.tameable.getRandom().nextBetween(-this.horizontalRadius, this.horizontalRadius)
            );
            if (!this.tameable.getWorld().isAir(candidate)) {
                continue;
            }
            if (!this.tameable.getWorld().isAir(candidate.up())) {
                continue;
            }
            return Vec3d.ofCenter(candidate);
        }

        return null;
    }
}
