package com.rnoobb.rats.entity.custom;

import com.rnoobb.rats.ModItems;
import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.entity.goal.FlyingCompanionFollowOwnerGoal;
import com.rnoobb.rats.entity.goal.FlyingCompanionWanderGoal;
import com.rnoobb.rats.screen.RatScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class RavenEntity extends AbstractHelperEntity implements GeoEntity {
    private static final int SIGHT_RADIUS = 20;
    private static final Ingredient TAMING_ITEMS = Ingredient.ofItems(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private long lastGiftDay = -1L;

    public RavenEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 16, false);
    }


    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, new com.rnoobb.rats.entity.goal.RavenSeekTrapGoal(this, 1.1D));
        this.goalSelector.add(3, new FlyingCompanionFollowOwnerGoal(this, 1.15D, 5.0F, 16.0F));
        this.goalSelector.add(4, new TemptGoal(this, 1.05D, TAMING_ITEMS, false));
        this.goalSelector.add(5, new FlyingCompanionWanderGoal(this, 0.95D, 10, 4));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
    }

    public static DefaultAttributeContainer.Builder createRavenAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28D);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanPathThroughDoors(false);
        navigation.setCanEnterOpenDoors(false);
        navigation.setCanSwim(true);
        return navigation;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (TAMING_ITEMS.test(stack) && !this.isTamed()) {
            if (!this.getWorld().isClient) {
                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.setTamed(true);
                    this.setBehavior(Behavior.SIT);
                    this.getWorld().sendEntityStatus(this, (byte) 7);
                } else {
                    this.getWorld().sendEntityStatus(this, (byte) 6);
                }
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(ModItems.CAGE)) {
            return ActionResult.PASS;
        }

        ActionResult actionResult = this.handleCompanionInteraction(player, hand);
        if (actionResult.isAccepted()) {
            return actionResult;
        }

        return super.interactMob(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!this.getWorld().isClient && this.isTamed() && this.isAlive()) {
            if (this.homePos == null) {
                this.homePos = this.getBlockPos();
            }
            this.tickRavenSight();
            this.tickDailyGift();
        }
    }















    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        EntityDimensions dimensions = super.getDimensions(pose);
        if (this.getBehavior() == AbstractHelperEntity.Behavior.SIT) {
            return dimensions.scaled(0.85F);
        }
        return dimensions;
    }

    private void tickRavenSight() {
        if (this.age % 40 != 0 || this.isSitting()) {
            return;
        }
        List<HostileEntity> hostiles = this.getWorld().getNonSpectatingEntities(HostileEntity.class, this.getBoundingBox().expand(16.0D));
        for (HostileEntity hostile : hostiles) {
            hostile.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0, true, false, true));
        }
    }

    private void tickDailyGift() {
        if (this.age % 200 != 0 || this.isSitting()) {
            return;
        }
        LivingEntity owner = this.getOwner();
        if (!(owner instanceof PlayerEntity player) || !owner.isAlive() || this.squaredDistanceTo(owner) > 144.0D) {
            return;
        }

        long currentDay = this.getWorld().getTimeOfDay() / 24000L;
        if (currentDay <= this.lastGiftDay || this.random.nextFloat() > 0.35F) {
            return;
        }

        ItemStack gift = this.random.nextBoolean() ? new ItemStack(Items.GOLD_NUGGET, this.random.nextBetween(1, 3)) : new ItemStack(Items.EMERALD, 1);
        player.getInventory().offerOrDrop(gift);
        this.lastGiftDay = currentDay;
        player.sendMessage(Text.translatable("message.rats_and_creatures.raven_gift"), true);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return TAMING_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return ModEntities.RAVEN.create(world);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putLong("LastGiftDay", this.lastGiftDay);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.lastGiftDay = nbt.getLong("LastGiftDay");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.isSitting()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.raven.sit"));
            }
            if (!this.isOnGround()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.raven.fly"));
            }
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.raven.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.raven.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected void updateLimbs(float posDelta) {
        this.limbAnimator.updateLimbs(0.15F, 0.5F);
    }



    public static boolean canSpawn(EntityType<RavenEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if (spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.COMMAND) {
            return true;
        }

        // 25% chance to spawn
        if (random.nextFloat() > 0.25f) {
            return false;
        }

        BlockPos ground = pos.down();
        BlockState state = world.getBlockState(ground);
        // Can spawn on ground, leaves, or logs
        return (state.isSolidBlock(world, ground) || state.isIn(BlockTags.LEAVES) || state.isIn(BlockTags.LOGS)) && world.isSkyVisible(pos);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.lastGiftDay = (world.toServerWorld().getTimeOfDay() / 24000L) - 1L;
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("entity.rats_and_creatures.raven");
    }


}
