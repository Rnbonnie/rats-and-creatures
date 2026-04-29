package com.rnoobb.rats.entity.custom;

import com.rnoobb.rats.ModItems;
import com.rnoobb.rats.ModStatusEffects;
import com.rnoobb.rats.entity.goal.RatFollowOwnerGoal;
import com.rnoobb.rats.entity.goal.RatHarvestGoal;
import com.rnoobb.rats.entity.goal.RatSeekTrapGoal;
import com.rnoobb.rats.entity.goal.RatWanderGoal;
import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.screen.RatScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.entity.player.PlayerInventory;
import java.util.Set;
import java.util.UUID;

public class RatEntity extends TameableEntity implements GeoEntity {
    private static final int BABY_GROWTH_TICKS = 24000;
    private static final int PLAGUE_DURATION = 100;
    private static final Set<net.minecraft.item.Item> RARE_LOOT = Set.of(
            Items.DIAMOND,
            Items.EMERALD,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.NETHERITE_INGOT,
            Items.NETHERITE_SCRAP,
            Items.REDSTONE,
            Items.LAPIS_LAZULI,
            Items.AMETHYST_SHARD,
            Items.ANCIENT_DEBRIS
    );

    public enum Behavior {
        FOLLOW,
        SIT,
        WANDER;

        public static Behavior fromName(String value) {
            for (Behavior behavior : values()) {
                if (behavior.name().equalsIgnoreCase(value)) {
                    return behavior;
                }
            }
            return FOLLOW;
        }

        public Text asText() {
            return Text.translatable("entity.rats_and_creatures.rat.behavior." + this.name().toLowerCase());
        }
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final SimpleInventory inventory = new SimpleInventory(3);
    private static final TrackedData<Integer> BEHAVIOR = DataTracker.registerData(RatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private BlockPos homePos;
    private LivingEntity fleeTarget;
    private boolean hasRetaliatedOnce;
    private boolean fleeingFromThreat;
    private int fleeTicks;

    public RatEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.homePos = this.getBlockPos();
        this.inventory.addListener(sender -> {
            // Слот 0 -> Надеваем на голову (шлем/шапка)
            this.equipStack(EquipmentSlot.HEAD, sender.getStack(0));
            // Слот 2 -> Даем в "руку" (чтобы было видно в зубах)
            this.equipStack(EquipmentSlot.MAINHAND, sender.getStack(2));
        });
    }


    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BEHAVIOR, Behavior.FOLLOW.ordinal());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.add(3, new RatHarvestGoal(this, 1.15D));
        this.goalSelector.add(4, new RatSeekTrapGoal(this, 1.1D));
        this.goalSelector.add(5, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(6, new RatFollowOwnerGoal(this, 1.0D, 12.0F, 2.0F, false));
        this.goalSelector.add(7, new TemptGoal(this, 1.1D, Ingredient.ofItems(ModItems.CHEESE), false));
        this.goalSelector.add(8, new RatWanderGoal(this, 1.0D, 10));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
    }

    public static DefaultAttributeContainer.Builder createRatAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0D);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        boolean usedCheese = false;
        if (itemStack.isOf(ModItems.CHEESE)) {
            if (this.getBreedingAge() == 0 && !this.isBaby() && !this.isInLove()) {
                if (!this.getWorld().isClient) {
                    this.lovePlayer(player);
                }
                usedCheese = true;
            }

            if (this.isBaby() && !this.getWorld().isClient) {
                int growthTicks = Math.max(1, (int)((float)(-this.getBreedingAge()) * 0.1F));
                this.growUp(growthTicks, true);
                this.getWorld().sendEntityStatus(this, (byte) 7);
                usedCheese = true;
            }

            if (!this.isTamed()) {
                if (!this.getWorld().isClient) {
                    if (this.random.nextInt(3) == 0) {
                        this.setOwner(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setTamed(true);
                        this.getWorld().sendEntityStatus(this, (byte) 7);
                    } else {
                        this.getWorld().sendEntityStatus(this, (byte) 6);
                    }
                }
                usedCheese = true;
            }

            if (usedCheese) {
                if (!this.getWorld().isClient && !player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }

        if (this.isTamed() && this.isOwner(player)) {
            if (player.isSneaking()) {
                if (!this.getWorld().isClient) {
                    player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                        @Override
                        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                            buf.writeInt(getId());
                        }

                        @Override
                        public Text getDisplayName() {
                            return RatEntity.this.getDisplayName();
                        }

                        @Override
                        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                            return new RatScreenHandler(syncId, playerInventory, RatEntity.this);
                        }
                    });
                }
                return ActionResult.SUCCESS;
            } else {
                if (!this.getWorld().isClient) {
                    this.setBehavior(this.getBehavior() == Behavior.SIT ? Behavior.FOLLOW : Behavior.SIT);
                }
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return ModEntities.RAT.create(world);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isOf(ModItems.CHEESE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isTamed()) {
            if (this.fleeingFromThreat || this.fleeTarget != null) {
                this.resetFleeState();
            }
            return;
        }

        if (this.fleeTarget != null && !this.fleeTarget.isAlive()) {
            this.resetFleeState();
            return;
        }

        if (this.fleeingFromThreat) {
            if (--this.fleeTicks <= 0) {
                this.resetFleeState();
            } else if (this.fleeTarget != null && this.getNavigation().isIdle()) {
                this.startFleeingFrom(this.fleeTarget);
            }
            return;
        }

        if (this.fleeTarget != null) {
            if (this.hasRetaliatedOnce || this.getTarget() == null) {
                this.startFleeingFrom(this.fleeTarget);
            } else if (this.fleeTicks > 0) {
                this.fleeTicks--;
                if (this.fleeTicks == 0) {
                    this.startFleeingFrom(this.fleeTarget);
                }
            }
        }
    }

    public Behavior getBehavior() {
        int index = MathHelper.clamp(this.dataTracker.get(BEHAVIOR), 0, Behavior.values().length - 1);
        return Behavior.values()[index];
    }

    public void setBehavior(Behavior behavior) {
        this.dataTracker.set(BEHAVIOR, behavior.ordinal());
        this.setSitting(behavior == Behavior.SIT);
        this.calculateDimensions();
        // Always stop navigation and clear target when behavior changes to ensure immediate re-evaluation of goals
        this.getNavigation().stop();
        this.setTarget(null);
        if (behavior == Behavior.SIT) {
            this.setVelocity(0, getVelocity().y, 0);
        }
    }

    public BlockPos getHomePos() {
        if (this.homePos == null) {
            this.homePos = this.getBlockPos();
        }
        return this.homePos;
    }

    public void setHomePos(BlockPos homePos) {
        this.homePos = homePos;
    }

    public BlockPos getWanderAnchor() {
        LivingEntity owner = this.getOwner();
        return owner != null ? owner.getBlockPos() : this.getHomePos();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null && this.isTamed() && this.isSitting()) {
            this.setBehavior(Behavior.FOLLOW);
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        EntityDimensions dimensions = super.getDimensions(pose);
        if (this.getBehavior() == Behavior.SIT) {
            return dimensions.scaled(0.7F);
        }
        return dimensions;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        if (this.random.nextFloat() < 0.2F) {
            this.setBreedingAge(-BABY_GROWTH_TICKS);
        }
        return data;
    }

    public static boolean canSpawn(EntityType<RatEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if (spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.COMMAND) {
            return true;
        }

        BlockPos groundPos = pos.down();
        if (!world.getBlockState(groundPos).isSolidBlock(world, groundPos)) {
            return false;
        }

        ServerWorld serverWorld = world.toServerWorld();
        boolean nearVillage = serverWorld.getPointOfInterestStorage()
                .getInCircle(point -> point.isIn(PointOfInterestTypeTags.VILLAGE), pos, 48, PointOfInterestStorage.OccupationStatus.ANY)
                .findAny()
                .isPresent();

        return nearVillage;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        if (damaged) {
            Entity attacker = source.getAttacker();
            if (attacker instanceof LivingEntity living && attacker != this) {
                if (this.isTamed()) {
                    this.setBehavior(Behavior.FOLLOW);
                    this.setTarget(living);
                } else {
                    this.fleeTarget = living;
                    this.hasRetaliatedOnce = false;
                    this.fleeingFromThreat = false;
                    this.fleeTicks = 60;
                    this.setTarget(living);
                }
            }
        }
        return damaged;
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean attacked = super.tryAttack(target);
        if (attacked && target instanceof LivingEntity livingTarget) {
            this.applyPlagueOnHit(livingTarget);
        }
        if (attacked && !this.isTamed() && target == this.fleeTarget) {
            this.hasRetaliatedOnce = true;
            this.startFleeingFrom(this.fleeTarget);
        }
        return attacked;
    }

    private void startFleeingFrom(@Nullable LivingEntity threat) {
        if (threat == null) {
            return;
        }
        this.fleeingFromThreat = true;
        this.setTarget(null);
        Vec3d direction = this.getPos().subtract(threat.getPos());
        if (direction.lengthSquared() < 0.01D) {
            direction = new Vec3d(this.random.nextDouble() - 0.5D, 0.0D, this.random.nextDouble() - 0.5D);
        }
        Vec3d targetPos = this.getPos().add(direction.normalize().multiply(12.0D));
        this.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, 1.4D);
        this.fleeTicks = 100;
    }

    private void resetFleeState() {
        this.fleeTarget = null;
        this.hasRetaliatedOnce = false;
        this.fleeingFromThreat = false;
        this.fleeTicks = 0;
    }

    private void applyPlagueOnHit(LivingEntity target) {
        float chance = 0.30F + Math.max(0, this.countNearbyRats() - 1) * 0.05F;
        chance = Math.min(chance, 0.80F);
        chance *= this.getPlagueResistanceMultiplier(target);

        if (this.random.nextFloat() < chance) {
            if (this.random.nextBoolean()) {
                target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.PLAGUE, PLAGUE_DURATION, 0));
            } else {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, PLAGUE_DURATION, 1));
            }
        }

        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlive() && owner.squaredDistanceTo(this) <= 36.0D && this.random.nextFloat() < 0.03F * this.getPlagueResistanceMultiplier(owner)) {
            owner.addStatusEffect(new StatusEffectInstance(ModStatusEffects.PLAGUE, PLAGUE_DURATION, 0));
        }
    }

    private int countNearbyRats() {
        return this.getWorld().getNonSpectatingEntities(RatEntity.class, this.getBoundingBox().expand(8.0D))
                .size();
    }

    private float getPlagueResistanceMultiplier(LivingEntity entity) {
        ItemStack headStack = entity.getEquippedStack(EquipmentSlot.HEAD);
        if (isPlagueMask(headStack)) {
            return entity == this.getOwner() ? 0.1F : 0.2F;
        }
        return 1.0F;
    }

    private static boolean isPlagueMask(ItemStack stack) {
        return stack.isOf(ModItems.LEATHER_PLAGUE_MASK)
                || stack.isOf(ModItems.IRON_PLAGUE_MASK)
                || stack.isOf(ModItems.GOLDEN_PLAGUE_MASK)
                || stack.isOf(ModItems.DIAMOND_PLAGUE_MASK)
                || stack.isOf(ModItems.NETHERITE_PLAGUE_MASK);
    }

    public boolean canStoreMoreLoot() {
        for (int slot = 1; slot < this.inventory.size(); slot++) {
            ItemStack stack = this.inventory.getStack(slot);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxCount()) {
                return true;
            }
        }
        return false;
    }

    public boolean canStore(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        for (int slot = 1; slot < this.inventory.size(); slot++) {
            ItemStack existing = this.inventory.getStack(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (ItemStack.canCombine(existing, stack) && existing.getCount() < existing.getMaxCount()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack storeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int slot = 1; slot < this.inventory.size(); slot++) {
            ItemStack existing = this.inventory.getStack(slot);
            if (!existing.isEmpty() && ItemStack.canCombine(existing, stack)) {
                int transferable = Math.min(stack.getCount(), existing.getMaxCount() - existing.getCount());
                if (transferable > 0) {
                    existing.increment(transferable);
                    stack.decrement(transferable);
                    this.inventory.markDirty();
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        for (int slot = 1; slot < this.inventory.size(); slot++) {
            ItemStack existing = this.inventory.getStack(slot);
            if (existing.isEmpty()) {
                this.inventory.setStack(slot, stack.copy());
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    public int getHarvestPriority(ItemStack stack) {
        if (stack.isEmpty()) {
            return 3;
        }
        if (this.isPreferredRatFood(stack)) {
            return 0;
        }
        if (RARE_LOOT.contains(stack.getItem())) {
            return 1;
        }
        return 2;
    }

    private boolean isPreferredRatFood(ItemStack stack) {
        return stack.isOf(ModItems.CHEESE)
                || stack.isFood();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList list = new NbtList();
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack itemStack = this.inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                itemStack.writeNbt(nbtCompound);
                list.add(nbtCompound);
            }
        }
        nbt.put("Inventory", list);
        nbt.putString("Behavior", this.getBehavior().name());
        nbt.put("HomePos", NbtHelper.fromBlockPos(this.getHomePos()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Inventory")) {
            NbtList list = nbt.getList("Inventory", 10);
            for (int i = 0; i < list.size(); ++i) {
                NbtCompound nbtCompound = list.getCompound(i);
                int j = nbtCompound.getByte("Slot") & 255;
                if (j < this.inventory.size()) {
                    this.inventory.setStack(j, ItemStack.fromNbt(nbtCompound));
                }
            }
        }
        this.equipStack(EquipmentSlot.HEAD, this.inventory.getStack(0));
        this.equipStack(EquipmentSlot.MAINHAND, this.inventory.getStack(2));
        if (nbt.contains("HomePos")) {
            this.homePos = NbtHelper.toBlockPos(nbt.getCompound("HomePos"));
        } else {
            this.homePos = this.getBlockPos();
        }
        if (nbt.contains("Behavior")) {
            this.setBehavior(Behavior.fromName(nbt.getString("Behavior")));
        } else {
            this.setBehavior(Behavior.FOLLOW);
        }
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.isSitting()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.Rat.sit"));
            }
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.Rat.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.Rat.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @Nullable UUID getOwnerUuid() {
        return super.getOwnerUuid();
    }

    // This method corresponds to the remapped method_48926() from the Tameable interface
    @Override
    public World method_48926() {
        return this.getWorld();
    }
}
