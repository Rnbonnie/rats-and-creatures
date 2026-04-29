package com.rnoobb.rats.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CageItem extends BlockItem {
    private static final String STORED_ENTITY_KEY = "StoredEntity";
    private static final String STORED_HEALTH_KEY = "RAC_StoredHealth";
    private static final String STORED_MAX_HEALTH_KEY = "RAC_StoredMaxHealth";

    public CageItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (hasStoredEntity(stack) || !canCapture(user, entity)) {
            return ActionResult.PASS;
        }

        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        NbtCompound entityData = captureEntityData(entity);
        if (entityData == null || entityData.isEmpty()) {
            return ActionResult.FAIL;
        }

        user.setStackInHand(hand, createFilledCage(stack, entityData));
        entity.discard();
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        if (!hasStoredEntity(stack)) {
            return super.useOnBlock(context);
        }

        World world = context.getWorld();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.FAIL;
        }

        NbtCompound entityData = getStoredEntityData(stack);
        if (entityData == null || entityData.isEmpty()) {
            return ActionResult.FAIL;
        }

        BlockPos spawnPos = context.getBlockPos().offset(context.getSide());
        Entity entity = EntityType.loadEntityWithPassengers(entityData.copy(), serverWorld, loadedEntity -> {
            loadedEntity.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    loadedEntity.getYaw(),
                    loadedEntity.getPitch()
            );
            return loadedEntity;
        });

        if (entity == null) {
            return ActionResult.FAIL;
        }

        serverWorld.spawnEntityAndPassengers(entity);
        context.getPlayer().setStackInHand(context.getHand(), new ItemStack(this));
        return ActionResult.SUCCESS;
    }

    @Override
    public Text getName(ItemStack stack) {
        if (!hasStoredEntity(stack)) {
            return super.getName(stack);
        }

        return Text.translatable("item.rats_and_creatures.cage_filled", getStoredEntityName(stack));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasStoredEntity(stack) || super.hasGlint(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        if (hasStoredEntity(stack)) {
            tooltip.add(Text.translatable("tooltip.rats_and_creatures.cage_entity", getStoredEntityName(stack)));
            tooltip.add(Text.translatable(
                    "tooltip.rats_and_creatures.cage_health",
                    String.format("%.1f", getStoredHealth(stack)),
                    String.format("%.1f", getStoredMaxHealth(stack))
            ));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static boolean hasStoredEntity(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(STORED_ENTITY_KEY);
    }

    public static ItemStack createFilledCage(ItemStack cageStack, NbtCompound entityData) {
        ItemStack filledCage = cageStack.copy();
        filledCage.setCount(1);
        filledCage.getOrCreateNbt().put(STORED_ENTITY_KEY, entityData.copy());
        return filledCage;
    }

    public static NbtCompound captureEntityData(Entity entity) {
        NbtCompound entityData = new NbtCompound();
        if (!entity.saveNbt(entityData)) {
            return null;
        }

        if (entity instanceof LivingEntity livingEntity) {
            entityData.putFloat(STORED_HEALTH_KEY, livingEntity.getHealth());
            entityData.putFloat(STORED_MAX_HEALTH_KEY, livingEntity.getMaxHealth());
        }

        return entityData;
    }

    private static boolean canCapture(PlayerEntity player, LivingEntity entity) {
        return entity instanceof TameableEntity tameableEntity
                && tameableEntity.isTamed()
                && tameableEntity.isOwner(player);
    }

    private static @Nullable NbtCompound getStoredEntityData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt == null ? null : nbt.getCompound(STORED_ENTITY_KEY);
    }

    private static Text getStoredEntityName(ItemStack stack) {
        NbtCompound entityData = getStoredEntityData(stack);
        if (entityData == null) {
            return Text.translatable("item.rats_and_creatures.cage");
        }

        Optional<EntityType<?>> entityType = EntityType.fromNbt(entityData);
        return entityType.map(EntityType::getName).orElse(Text.literal(entityData.getString("id")));
    }

    private static float getStoredHealth(ItemStack stack) {
        NbtCompound entityData = getStoredEntityData(stack);
        return entityData == null ? 0.0F : entityData.getFloat(STORED_HEALTH_KEY);
    }

    private static float getStoredMaxHealth(ItemStack stack) {
        NbtCompound entityData = getStoredEntityData(stack);
        if (entityData == null) {
            return 0.0F;
        }

        float maxHealth = entityData.getFloat(STORED_MAX_HEALTH_KEY);
        return maxHealth > 0.0F ? maxHealth : getStoredHealth(stack);
    }
}
