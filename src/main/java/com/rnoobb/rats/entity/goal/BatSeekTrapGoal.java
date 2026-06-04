package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.block.TrapBlock;
import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class BatSeekTrapGoal extends Goal {
    private final BatCompanionEntity bat;
    private final double speed;
    private BlockPos targetPos;

    public BatSeekTrapGoal(BatCompanionEntity bat, double speed) {
        this.bat = bat;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.bat.isTamed()) {
            return false;
        }

        this.targetPos = this.findNearestTrap();
        return this.targetPos != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.targetPos != null
                && TrapBlock.isAttractingTrap(this.bat.getWorld(), this.targetPos, this.bat)
                && !this.bat.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.moveToTrap();
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.bat.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3d target = Vec3d.ofCenter(this.targetPos);
        this.bat.getLookControl().lookAt(target.x, target.y, target.z);
        if (this.bat.getNavigation().isIdle()) {
            this.moveToTrap();
        }
    }

    private void moveToTrap() {
        if (this.targetPos == null) {
            return;
        }

        Vec3d target = Vec3d.ofCenter(this.targetPos);
        this.bat.getNavigation().startMovingTo(target.x, target.y, target.z, this.speed);
    }

    private BlockPos findNearestTrap() {
        BlockPos center = this.bat.getBlockPos();
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterateOutwards(center, 16, 4, 16)) {
            int range = TrapBlock.getAttractionRange(this.bat.getWorld(), pos, this.bat);
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
