package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;

import java.util.EnumSet;

public class RatFollowOwnerGoal extends Goal {
    private final RatEntity rat;
    private final EntityNavigation navigation;
    private final double speed;
    private final float minDistance;
    private final float maxDistance;
    private final boolean leavesOwner;
    private LivingEntity owner;
    private int updateCountdownTicks;
    private float oldWaterPathfindingPenalty;

    public RatFollowOwnerGoal(RatEntity rat, double speed, float minDistance, float maxDistance, boolean leavesOwner) {
        this.rat = rat;
        this.navigation = rat.getNavigation();
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.leavesOwner = leavesOwner;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        if (!(this.navigation instanceof MobNavigation) && !(this.navigation instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.rat.getOwner();
        if (livingEntity == null || livingEntity.isSpectator()) {
            return false;
        }
        if (this.rat.isSitting() || this.rat.getBehavior() != RatEntity.Behavior.FOLLOW) {
            return false;
        }
        if (this.rat.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance)) {
            return false;
        }
        this.owner = livingEntity;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (this.owner == null) {
            return false;
        }
        if (this.navigation.isIdle() || this.rat.isSitting() || this.rat.getBehavior() != RatEntity.Behavior.FOLLOW) {
            return false;
        }
        return this.rat.squaredDistanceTo(this.owner) > (double) (this.maxDistance * this.maxDistance);
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.rat.getPathfindingPenalty(PathNodeType.WATER);
        this.rat.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.rat.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.rat.getLookControl().lookAt(this.owner, 10.0f, this.rat.getMaxLookPitchChange());
        if (this.rat.isSitting()) {
            return;
        }

        if (--this.updateCountdownTicks > 0) {
            return;
        }

        this.updateCountdownTicks = this.getTickCount(10);
        if (this.rat.isLeashed() || this.rat.hasVehicle()) {
            return;
        }

        double distanceSq = this.rat.squaredDistanceTo(this.owner);
        if (distanceSq <= (double) (this.maxDistance * this.maxDistance)) {
            this.navigation.stop();
            return;
        }

        this.navigation.startMovingTo(this.owner, this.speed);
    }
}
