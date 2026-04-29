package com.rnoobb.rats.block;

import com.rnoobb.rats.ModBlocks;
import com.rnoobb.rats.ModBlockEntities;
import com.rnoobb.rats.block.entity.TrapBlockEntity;
import com.rnoobb.rats.entity.custom.RatEntity;
import com.rnoobb.rats.item.CageItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrapBlock extends BlockWithEntity {
    public static final BooleanProperty CLOSED = Properties.TRIGGERED;

    public TrapBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(CLOSED, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CLOSED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TrapBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TrapBlockEntity trapBlockEntity)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);

        if (state.get(CLOSED) && trapBlockEntity.hasCapturedEntity()) {
            if (stack.getItem() instanceof CageItem && !CageItem.hasStoredEntity(stack)) {
                if (!world.isClient) {
                    player.setStackInHand(hand, CageItem.createFilledCage(new ItemStack(ModBlocks.CAGE), trapBlockEntity.removeCapturedEntity()));
                    world.setBlockState(pos, state.with(CLOSED, false), Block.NOTIFY_ALL);
                }
                return ActionResult.success(world.isClient);
            }

            return ActionResult.success(world.isClient);
        }

        if (player.isSneaking() && trapBlockEntity.hasBait()) {
            if (!world.isClient) {
                ItemStack bait = trapBlockEntity.removeBait();
                if (!bait.isEmpty() && !player.getInventory().insertStack(bait)) {
                    Block.dropStack(world, pos, bait);
                }
            }
            return ActionResult.success(world.isClient);
        }

        if (!state.get(CLOSED) && !trapBlockEntity.hasCapturedEntity() && !trapBlockEntity.hasBait() && stack.isFood()) {
            if (!world.isClient) {
                ItemStack bait = stack.copy();
                bait.setCount(1);
                trapBlockEntity.setBait(bait);
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && canCapture(entity) && world.getBlockEntity(pos) instanceof TrapBlockEntity trapBlockEntity) {
            if (!state.get(CLOSED) && trapBlockEntity.canCapture()) {
                trapBlockEntity.captureEntity((LivingEntity) entity);
                world.setBlockState(pos, state.with(CLOSED, true), Block.NOTIFY_ALL);
                entity.discard();
            }
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, moved);
            return;
        }

        if (world.getBlockEntity(pos) instanceof TrapBlockEntity trapBlockEntity) {
            ItemScatterer.spawn(world, pos, trapBlockEntity.getDroppedStacks());
            if (trapBlockEntity.hasCapturedEntity()) {
                Block.dropStack(world, pos, CageItem.createFilledCage(new ItemStack(ModBlocks.CAGE), trapBlockEntity.removeCapturedEntity()));
            }
            world.removeBlockEntity(pos);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return null;
    }

    public static boolean isAttractingTrap(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(ModBlocks.TRAP) || state.get(CLOSED)) {
            return false;
        }

        if (!(world.getBlockEntity(pos) instanceof TrapBlockEntity trapBlockEntity)) {
            return false;
        }

        return trapBlockEntity.canCapture();
    }

    private static boolean canCapture(Entity entity) {
        return entity instanceof RatEntity rat && !rat.isTamed();
    }
}
