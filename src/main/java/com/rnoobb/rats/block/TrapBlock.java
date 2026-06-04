package com.rnoobb.rats.block;

import com.rnoobb.rats.ModBlocks;
import com.rnoobb.rats.ModBlockEntities;
import com.rnoobb.rats.ModItems;
import com.rnoobb.rats.block.entity.TrapBlockEntity;
import com.rnoobb.rats.entity.custom.RatEntity;
import com.rnoobb.rats.entity.custom.RavenEntity;
import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import com.rnoobb.rats.entity.custom.AbstractHelperEntity;
import com.rnoobb.rats.item.CageItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrapBlock extends BlockWithEntity {
    public static final BooleanProperty CLOSED = Properties.TRIGGERED;
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

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

        if (!state.get(CLOSED) && !trapBlockEntity.hasCapturedEntity() && !trapBlockEntity.hasBait() && (stack.isFood() || isSpecificBaitType(stack))) {
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

    private boolean isSpecificBaitType(ItemStack stack) {
        return stack.isOf(ModItems.CHEESE)
                || stack.isOf(Items.MELON_SEEDS)
                || stack.isOf(Items.PUMPKIN_SEEDS)
                || stack.isOf(ModItems.FAKE_BLOOD_BOTTLE)
                || stack.isOf(ModItems.BLOOD_CLOT);
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
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean closed = false;
        if (ctx.getStack().getNbt() != null) {
            net.minecraft.nbt.NbtCompound blockEntityTag = ctx.getStack().getSubNbt("BlockEntityTag");
            if (blockEntityTag != null && blockEntityTag.contains("CapturedEntity")) {
                closed = true;
            }
        }
        return this.getDefaultState().with(CLOSED, closed);
    }

    @Override
    public @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public static boolean isAttractingTrap(World world, BlockPos pos, LivingEntity entity) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(ModBlocks.TRAP) || state.get(CLOSED)) {
            return false;
        }

        if (!(world.getBlockEntity(pos) instanceof TrapBlockEntity trapBlockEntity)) {
            return false;
        }

        return trapBlockEntity.isAttracted(entity);
    }

    public static int getAttractionRange(World world, BlockPos pos, LivingEntity entity) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(ModBlocks.TRAP) || state.get(CLOSED)) {
            return 0;
        }

        if (!(world.getBlockEntity(pos) instanceof TrapBlockEntity trapBlockEntity)) {
            return 0;
        }

        return trapBlockEntity.getAttractionRange(entity);
    }

    private static boolean canCapture(Entity entity) {
        if (entity instanceof net.minecraft.entity.passive.TameableEntity tameable && tameable.isTamed()) {
            return false;
        }
        return entity instanceof RatEntity || entity instanceof RavenEntity || entity instanceof BatCompanionEntity;
    }
}
