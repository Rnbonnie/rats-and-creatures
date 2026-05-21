package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.entity.custom.CompanionInventoryEntity;
import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

public class FlyingCompanionFollowOwnerGoal extends Goal {
    private final TameableEntity tameable;
    private final EntityNavigation navigation;
    private final double speed;
    private final float minDistance;
    private final float maxDistance;
    private LivingEntity owner;
    private int updateCountdownTicks;
    private float oldWaterPathfindingPenalty;

    public FlyingCompanionFollowOwnerGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance) {
        this.tameable = tameable;
        this.navigation = tameable.getNavigation();
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        if (!(this.navigation instanceof MobNavigation) && !(this.navigation instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for follow owner goal");
        }
    }

    @Override
    public boolean canStart() {
        LivingEntity owner = this.tameable.getOwner();
        if (owner == null || owner.isSpectator() || this.tameable.isSitting()) {
            return false;
        }
        if (this.tameable instanceof CompanionInventoryEntity companion
                && companion.getBehavior() != RatEntity.Behavior.FOLLOW) {
            return false;
        }
        if (this.tameable.squaredDistanceTo(owner) < this.minDistance * this.minDistance) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return this.owner != null
                && !this.navigation.isIdle()
                && !this.tameable.isSitting()
                && (!(this.tameable instanceof CompanionInventoryEntity companion)
                || companion.getBehavior() == RatEntity.Behavior.FOLLOW)
                && this.tameable.squaredDistanceTo(this.owner) > this.maxDistance * this.maxDistance;
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.tameable.getPathfindingPenalty(PathNodeType.WATER);
        this.tameable.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tameable.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }
        this.tameable.getLookControl().lookAt(this.owner, 10.0F, this.tameable.getMaxLookPitchChange());
        if (--this.updateCountdownTicks > 0) {
            return;
        }
        this.updateCountdownTicks = this.getTickCount(10);
        if (this.tameable.isLeashed() || this.tameable.hasVehicle()) {
            return;
        }

        double distanceSq = this.tameable.squaredDistanceTo(this.owner);
        if (distanceSq >= 196.0D) {
            this.tameable.refreshPositionAndAngles(this.owner.getX(), this.owner.getBodyY(0.8D), this.owner.getZ(), this.tameable.getYaw(), this.tameable.getPitch());
            this.navigation.stop();
            return;
        }

        this.navigation.startMovingTo(this.owner, this.speed);
    }
}
