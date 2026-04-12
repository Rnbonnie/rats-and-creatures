package com.rnoobb.rats.entity.goal;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class RatHarvestGoal extends Goal {
    private final RatEntity rat;
    private final double speed;
    private HarvestTarget target;
    private int cooldown;

    public RatHarvestGoal(RatEntity rat, double speed) {
        this.rat = rat;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (this.rat.isSitting() || !this.rat.canStoreMoreLoot()) {
            return false;
        }

        this.target = this.findBestTarget();
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        if (this.target == null || this.rat.isSitting() || !this.rat.canStoreMoreLoot()) {
            return false;
        }

        return this.target.isValid(this.rat);
    }

    @Override
    public void start() {
        this.moveToTarget();
    }

    @Override
    public void stop() {
        this.target = null;
        this.rat.getNavigation().stop();
        this.cooldown = 10;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        Vec3d targetPos = this.target.getPos();
        this.rat.getLookControl().lookAt(targetPos.x, targetPos.y, targetPos.z);

        if (this.rat.squaredDistanceTo(targetPos) <= 2.8D) {
            if (this.target.harvest(this.rat)) {
                this.target = null;
                this.cooldown = 6;
            }
            return;
        }

        if (this.rat.getNavigation().isIdle()) {
            this.moveToTarget();
        }
    }

    private void moveToTarget() {
        if (this.target == null) {
            return;
        }
        Vec3d pos = this.target.getPos();
        this.rat.getNavigation().startMovingTo(pos.x, pos.y, pos.z, this.speed);
    }

    private HarvestTarget findBestTarget() {
        List<HarvestTarget> candidates = new ArrayList<>();
        Box itemBox = this.rat.getBoundingBox().expand(3.0D, 1.5D, 3.0D);
        for (ItemEntity itemEntity : this.rat.getWorld().getNonSpectatingEntities(ItemEntity.class, itemBox)) {
            if (!itemEntity.getStack().isEmpty() && this.rat.canStore(itemEntity.getStack())) {
                candidates.add(HarvestTarget.forItem(itemEntity));
            }
        }

        BlockPos center = this.rat.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(center, 5, 2, 5)) {
            BlockState state = this.rat.getWorld().getBlockState(pos);
            if (!this.isHarvestable(state) || !this.rat.canStore(getHarvestPreview(state))) {
                continue;
            }
            candidates.add(HarvestTarget.forCrop(pos.toImmutable(), state));
        }

        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((HarvestTarget candidate) -> candidate.getPriority(this.rat))
                        .thenComparingDouble(candidate -> this.rat.squaredDistanceTo(candidate.getPos())))
                .findFirst()
                .orElse(null);
    }

    private boolean isHarvestable(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.get(NetherWartBlock.AGE) >= 3;
        }
        if (state.getBlock() instanceof SweetBerryBushBlock) {
            return state.get(SweetBerryBushBlock.AGE) >= 3;
        }
        if (state.getBlock() instanceof CocoaBlock) {
            return state.get(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
        }
        return false;
    }

    private ItemStack getHarvestPreview(BlockState state) {
        Block block = state.getBlock();
        ItemStack stack = block.asItem().getDefaultStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    private sealed interface HarvestTarget permits HarvestTarget.ItemTarget, HarvestTarget.CropTarget {
        Vec3d getPos();

        boolean isValid(RatEntity rat);

        boolean harvest(RatEntity rat);

        int getPriority(RatEntity rat);

        static HarvestTarget forItem(ItemEntity entity) {
            return new ItemTarget(entity);
        }

        static HarvestTarget forCrop(BlockPos pos, BlockState state) {
            return new CropTarget(pos, state);
        }

        record ItemTarget(ItemEntity entity) implements HarvestTarget {
            @Override
            public Vec3d getPos() {
                return this.entity.getPos();
            }

            @Override
            public boolean isValid(RatEntity rat) {
                return this.entity.isAlive() && !this.entity.getStack().isEmpty() && rat.canStore(this.entity.getStack());
            }

            @Override
            public boolean harvest(RatEntity rat) {
                ItemStack remaining = rat.storeStack(this.entity.getStack().copy());
                if (remaining.getCount() == this.entity.getStack().getCount()) {
                    return false;
                }

                if (remaining.isEmpty()) {
                    this.entity.discard();
                } else {
                    this.entity.setStack(remaining);
                }
                return true;
            }

            @Override
            public int getPriority(RatEntity rat) {
                return rat.getHarvestPriority(this.entity.getStack());
            }
        }

        record CropTarget(BlockPos pos, BlockState state) implements HarvestTarget {
            @Override
            public Vec3d getPos() {
                return Vec3d.ofCenter(this.pos);
            }

            @Override
            public boolean isValid(RatEntity rat) {
                return rat.getWorld().getBlockState(this.pos).equals(this.state) || rat.getWorld().getBlockState(this.pos).getBlock() == this.state.getBlock();
            }

            @Override
            public boolean harvest(RatEntity rat) {
                if (!(rat.getWorld() instanceof ServerWorld serverWorld)) {
                    return false;
                }

                BlockState currentState = serverWorld.getBlockState(this.pos);
                if (currentState.isAir()) {
                    return false;
                }

                BlockEntity blockEntity = serverWorld.getBlockEntity(this.pos);
                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(serverWorld)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos))
                        .add(LootContextParameters.TOOL, ItemStack.EMPTY);
                if (blockEntity != null) {
                    builder.add(LootContextParameters.BLOCK_ENTITY, blockEntity);
                }

                List<ItemStack> drops = currentState.getDroppedStacks(builder);
                for (ItemStack drop : drops) {
                    ItemStack remaining = rat.storeStack(drop.copy());
                    if (!remaining.isEmpty()) {
                        Block.dropStack(serverWorld, this.pos, remaining);
                    }
                }

                serverWorld.breakBlock(this.pos, false, rat);
                return true;
            }

            @Override
            public int getPriority(RatEntity rat) {
                return rat.getHarvestPriority(this.state.getBlock().asItem().getDefaultStack());
            }
        }
    }
}
