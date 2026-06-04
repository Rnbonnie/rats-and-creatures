package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.block.TrapBlock;
import com.rnoobb.rats.entity.custom.RatEntity;
import com.rnoobb.rats.entity.custom.AbstractHelperEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class RatSeekTrapGoal extends Goal {
    private final RatEntity rat;
    private final double speed;
    private BlockPos targetPos;
    private int searchCooldown;

    public RatSeekTrapGoal(RatEntity rat, double speed) {
        this.rat = rat;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }

        if (this.rat.isTamed()) {
            return false;
        }

        this.targetPos = this.findNearestTrap();
        if (this.targetPos == null) {
            this.searchCooldown = 200 + this.rat.getRandom().nextInt(200);
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return this.targetPos != null
                && TrapBlock.isAttractingTrap(this.rat.getWorld(), this.targetPos, this.rat)
                && !this.rat.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.moveToTrap();
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.rat.getNavigation().stop();
        this.searchCooldown = 100 + this.rat.getRandom().nextInt(100);
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3d target = Vec3d.ofCenter(this.targetPos);
        this.rat.getLookControl().lookAt(target.x, target.y, target.z);
        if (this.rat.getNavigation().isIdle()) {
            this.moveToTrap();
        }
    }

    private void moveToTrap() {
        if (this.targetPos == null) {
            return;
        }

        Vec3d target = Vec3d.ofCenter(this.targetPos);
        this.rat.getNavigation().startMovingTo(target.x, target.y, target.z, this.speed);
    }

    private BlockPos findNearestTrap() {
        BlockPos center = this.rat.getBlockPos();
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        // Search up to 12 blocks (down from 16) for performance
        for (BlockPos pos : BlockPos.iterateOutwards(center, 12, 3, 12)) {
            int range = TrapBlock.getAttractionRange(this.rat.getWorld(), pos, this.rat);
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
