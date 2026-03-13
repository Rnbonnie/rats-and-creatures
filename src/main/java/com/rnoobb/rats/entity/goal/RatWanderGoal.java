package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.EnumSet;

public class RatWanderGoal extends Goal {
    private final RatEntity rat;
    private final double speed;
    private final int radius;
    private double targetX;
    private double targetY;
    private double targetZ;

    public RatWanderGoal(RatEntity rat, double speed, int radius) {
        this.rat = rat;
        this.speed = speed;
        this.radius = radius;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.rat.getBehavior() != RatEntity.Behavior.WANDER || this.rat.isSitting() || this.rat.getNavigation().isFollowingPath()) {
            return false;
        }

        Vec3d target = this.chooseTarget();
        if (target == null) {
            return false;
        }

        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;
        return true;
    }

    @Override
    public void start() {
        this.rat.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public boolean shouldContinue() {
        return this.rat.getBehavior() == RatEntity.Behavior.WANDER && !this.rat.getNavigation().isIdle();
    }

    private Vec3d chooseTarget() {
        BlockPos anchor = this.rat.getWanderAnchor();
        Random random = this.rat.getRandom();

        for (int i = 0; i < 12; i++) {
            BlockPos candidate = anchor.add(
                    random.nextInt(this.radius * 2 + 1) - this.radius,
                    random.nextInt(5) - 2,
                    random.nextInt(this.radius * 2 + 1) - this.radius
            );

            if (anchor.getSquaredDistance(candidate) > (double) (this.radius * this.radius)) {
                continue;
            }

            BlockPos ground = candidate.down();
            if (!this.rat.getWorld().getBlockState(ground).isAir() && this.rat.getWorld().isAir(candidate)) {
                return Vec3d.ofCenter(candidate);
            }
        }

        return null;
    }
}
