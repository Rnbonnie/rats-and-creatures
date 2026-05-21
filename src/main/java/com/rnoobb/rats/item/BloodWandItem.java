package com.rnoobb.rats.item;

import com.rnoobb.rats.entity.custom.BatCompanionEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
public class BloodWandItem extends Item {
    private static final double SEARCH_RANGE = 48.0D;

    public BloodWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.success(stack);
        }

        LivingEntity target = this.findTarget(user);
        if (target == null) {
            target = user;
        }
        LivingEntity healTarget = target;

        List<BatCompanionEntity> bats = serverWorld.getNonSpectatingEntities(BatCompanionEntity.class, new Box(user.getBlockPos()).expand(SEARCH_RANGE)).stream()
                .filter(bat -> bat.isTamed() && bat.isOwner(user) && bat.isAlive())
                .sorted(Comparator.comparingDouble(bat -> bat.squaredDistanceTo(healTarget)))
                .toList();

        for (BatCompanionEntity bat : bats) {
            bat.getNavigation().startMovingTo(healTarget, 1.3D);
            if (bat.squaredDistanceTo(healTarget) <= 16.0D && bat.trySpendBloodFor(healTarget)) {
                user.getItemCooldownManager().set(this, 60);
                user.sendMessage(Text.translatable("message.rats_and_creatures.bat_heal_forced", healTarget.getDisplayName()), true);
                return TypedActionResult.success(stack);
            }
        }

        user.sendMessage(Text.translatable("message.rats_and_creatures.bat_heal_failed"), true);
        return TypedActionResult.fail(stack);
    }

    @Nullable
    private LivingEntity findTarget(PlayerEntity user) {
        EntityHitResult hitResult = ProjectileUtil.getEntityCollision(
                user.getWorld(),
                user,
                user.getCameraPosVec(1.0F),
                user.getCameraPosVec(1.0F).add(user.getRotationVec(1.0F).multiply(16.0D)),
                user.getBoundingBox().stretch(user.getRotationVec(1.0F).multiply(16.0D)).expand(1.0D),
                candidate -> candidate instanceof LivingEntity && candidate.isAlive()
        );

        Entity entity = hitResult != null ? hitResult.getEntity() : null;
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }
}
