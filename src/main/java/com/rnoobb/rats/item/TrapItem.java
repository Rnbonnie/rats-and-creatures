package com.rnoobb.rats.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TrapItem extends BlockItem {
    public static final String CAPTURED_ENTITY_KEY = "CapturedEntity";

    public TrapItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking() && hasCapturedEntity(stack)) {
            if (!world.isClient) {
                releaseEntity(world, user, stack);
            }
            return TypedActionResult.success(stack, world.isClient());
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSneaking() && hasCapturedEntity(context.getStack())) {
            return ActionResult.PASS; // Let 'use' handle it
        }
        return super.useOnBlock(context);
    }

    private void releaseEntity(World world, PlayerEntity player, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        NbtCompound entityData = getCapturedEntityData(stack);
        if (entityData == null || entityData.isEmpty()) return;

        BlockPos spawnPos = player.getBlockPos().offset(player.getHorizontalFacing());
        Entity entity = EntityType.loadEntityWithPassengers(entityData.copy(), serverWorld, loadedEntity -> {
            loadedEntity.refreshPositionAndAngles(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYaw(),
                    player.getPitch()
            );
            return loadedEntity;
        });

        if (entity != null) {
            if (player.getRandom().nextFloat() < 0.5f) {
                if (entity instanceof TameableEntity tameable) {
                    tameable.setOwner(player);
                    tameable.setTamed(true);
                    serverWorld.sendEntityStatus(tameable, (byte) 7); // Heart particles
                }
            } else {
                if (entity instanceof TameableEntity tameable) {
                    serverWorld.sendEntityStatus(tameable, (byte) 6); // Smoke particles
                }
            }

            serverWorld.spawnEntityAndPassengers(entity);
            
            // Remove the captured entity from the item
            stack.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY).remove(CAPTURED_ENTITY_KEY);
            if (stack.getSubNbt(BLOCK_ENTITY_TAG_KEY).isEmpty()) {
                stack.removeSubNbt(BLOCK_ENTITY_TAG_KEY);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (hasCapturedEntity(stack)) {
            tooltip.add(Text.translatable("tooltip.rats_and_creatures.trap_captured", getCapturedEntityName(stack)).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.rats_and_creatures.trap_release_hint").formatted(Formatting.YELLOW, Formatting.ITALIC));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static boolean hasCapturedEntity(ItemStack stack) {
        NbtCompound blockEntityTag = stack.getSubNbt(BLOCK_ENTITY_TAG_KEY);
        return blockEntityTag != null && blockEntityTag.contains(CAPTURED_ENTITY_KEY);
    }

    public static @Nullable NbtCompound getCapturedEntityData(ItemStack stack) {
        NbtCompound blockEntityTag = stack.getSubNbt(BLOCK_ENTITY_TAG_KEY);
        return blockEntityTag == null ? null : blockEntityTag.getCompound(CAPTURED_ENTITY_KEY);
    }

    public static Text getCapturedEntityName(ItemStack stack) {
        NbtCompound entityData = getCapturedEntityData(stack);
        if (entityData == null) {
            return Text.translatable("block.rats_and_creatures.trap");
        }

        Optional<EntityType<?>> entityType = EntityType.fromNbt(entityData);
        return entityType.map(EntityType::getName).orElse(Text.literal(entityData.getString("id")));
    }
}
