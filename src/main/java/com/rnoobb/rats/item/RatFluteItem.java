package com.rnoobb.rats.item;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class RatFluteItem extends Item {
    private static final double RANGE = 80.0D; // 5 chunks

    public RatFluteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(world instanceof ServerWorld serverWorld) || !(user instanceof PlayerEntity player)) {
            return stack;
        }

        List<RatEntity> rats = serverWorld.getNonSpectatingEntities(RatEntity.class, new Box(user.getBlockPos()).expand(RANGE));
        List<RatEntity> ownedRats = rats.stream()
                .filter(rat -> rat.isTamed() && rat.isOwner(player))
                .toList();

        if (ownedRats.isEmpty()) {
            return stack;
        }

        RatEntity closestRat = ownedRats.stream()
                .min(Comparator.comparingDouble(rat -> rat.squaredDistanceTo(user)))
                .orElse(null);

        if (closestRat == null) {
            return stack;
        }

        RatEntity.Behavior currentBehavior = closestRat.getBehavior();
        RatEntity.Behavior nextBehavior = switch (currentBehavior) {
            case FOLLOW -> RatEntity.Behavior.SIT;
            case SIT -> RatEntity.Behavior.WANDER;
            case WANDER -> RatEntity.Behavior.FOLLOW;
        };

        for (RatEntity rat : ownedRats) {
            rat.setBehavior(nextBehavior);
        }

        player.sendMessage(Text.translatable("message.rats_and_creatures.rat_behavior_all", nextBehavior.asText()), true);
        player.getItemCooldownManager().set(this, 100); // 5 seconds cooldown
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 60; // 3 seconds usage time
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
