package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.block.TrapBlock;
import com.rnoobb.rats.entity.custom.RavenEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RavenSeekTrapGoal extends Goal {
    private final RavenEntity raven;
    private final double speed;
    private BlockPos targetPos;
    private int searchCooldown;

    public RavenSeekTrapGoal(RavenEntity raven, double speed) {
        this.raven = raven;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }

        if (this.raven.isTamed()) {
            return false;
        }

        this.targetPos = this.findNearestTrap();
        if (this.targetPos == null) {
            this.searchCooldown = 200 + this.raven.getRandom().nextInt(200);
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return this.targetPos != null
                && TrapBlock.isAttractingTrap(this.raven.getWorld(), this.targetPos, this.raven)
                && !this.raven.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.moveToTrap();
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.raven.getNavigation().stop();
        this.searchCooldown = 100 + this.raven.getRandom().nextInt(100);
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3d target = Vec3d.ofCenter(this.targetPos);
        this.raven.getLookControl().lookAt(target.x, target.y, target.z);
        if (this.raven.getNavigation().isIdle()) {
            this.moveToTrap();
        }
    }

    private void moveToTrap() {
        if (this.targetPos == null) {
            return;
        }

        Vec3d target = Vec3d.ofCenter(this.targetPos);
        this.raven.getNavigation().startMovingTo(target.x, target.y, target.z, this.speed);
    }

    private BlockPos findNearestTrap() {
        BlockPos center = this.raven.getBlockPos();
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterateOutwards(center, 12, 3, 12)) {
            int range = TrapBlock.getAttractionRange(this.raven.getWorld(), pos, this.raven);
            if (range <= 0) {
                continue;
            }

            double distance = center.getSquaredDistance(pos);
            if (distance > range * range) {
                continue;
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestPos = pos.toImmutable();
            }
        }

        return bestPos;
    }
}
