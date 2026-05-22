package com.rnoobb.rats.entity.custom;

import com.rnoobb.rats.ModItems;
import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.entity.goal.FlyingCompanionFollowOwnerGoal;
import com.rnoobb.rats.entity.goal.FlyingCompanionWanderGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BatCompanionEntity extends AbstractHelperEntity {
    private static final int MAX_BLOOD = 100;
    private static final int HEAL_COST = 20;
    private static final int HEAL_DURATION = 80;
    private static final TrackedData<Integer> BLOOD_GAUGE = DataTracker.registerData(BatCompanionEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private int healCooldown;

    public BatCompanionEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BLOOD_GAUGE, 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.add(3, new FlyingCompanionFollowOwnerGoal(this, 1.2D, 4.0F, 14.0F));
        this.goalSelector.add(4, new TemptGoal(this, 1.0D, Ingredient.ofItems(ModItems.FAKE_BLOOD_BOTTLE), false));
        this.goalSelector.add(5, new FlyingCompanionWanderGoal(this, 1.0D, 8, 5));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
    }

    public static DefaultAttributeContainer.Builder createBatAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.45D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0D);
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
        if (stack.isOf(ModItems.FAKE_BLOOD_BOTTLE) && !this.isTamed()) {
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
        if (this.healCooldown > 0) {
            this.healCooldown--;
        }

        if (!this.getWorld().isClient && this.isTamed() && this.isAlive() && !this.isSitting()) {
            this.tickVampirismSupport();
        }
    }

    private void tickVampirismSupport() {
        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlive() && owner.getHealth() < owner.getMaxHealth() * 0.5F) {
            this.getNavigation().startMovingTo(owner, 1.25D);
            if (this.healCooldown == 0 && this.squaredDistanceTo(owner) <= 9.0D) {
                this.trySpendBloodFor(owner);
            }
            return;
        }

        if (this.getHealth() < this.getMaxHealth() * 0.3F && this.healCooldown == 0) {
            this.trySpendBloodFor(this);
        }
    }

    public boolean trySpendBloodFor(LivingEntity target) {
        if (this.getBloodGauge() < HEAL_COST) {
            return false;
        }
        this.setBloodGauge(this.getBloodGauge() - HEAL_COST);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, HEAL_DURATION, 0));
        this.healCooldown = 100;
        return true;
    }

    public int getBloodGauge() {
        return this.dataTracker.get(BLOOD_GAUGE);
    }

    private void setBloodGauge(int amount) {
        this.dataTracker.set(BLOOD_GAUGE, MathHelper.clamp(amount, 0, MAX_BLOOD));
    }

    @Override
    public boolean tryAttack(net.minecraft.entity.Entity target) {
        boolean attacked = super.tryAttack(target);
        if (attacked) {
            this.setBloodGauge(this.getBloodGauge() + 5);
        }
        return attacked;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isOf(ModItems.FAKE_BLOOD_BOTTLE);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return ModEntities.BAT.create(world);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("BloodGauge", this.getBloodGauge());
        nbt.putInt("HealCooldown", this.healCooldown);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setBloodGauge(nbt.getInt("BloodGauge"));
        this.healCooldown = nbt.getInt("HealCooldown");
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected void updateLimbs(float posDelta) {
        this.limbAnimator.updateLimbs(0.1F, 0.4F);
    }

    public static boolean canSpawn(EntityType<BatCompanionEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if (spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.COMMAND) {
            return true;
        }
        return pos.getY() < world.getSeaLevel() && world.getLightLevel(pos) <= 7;
    }

    @Override
    public net.minecraft.entity.EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable net.minecraft.entity.EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setBloodGauge(random.nextBetween(10, 40));
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("entity.rats_and_creatures.bat");
    }



    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isSitting()) {
            this.setSitting(false);
        }
        return super.damage(source, amount);
    }
}
