package com.rnoobb.rats.entity.custom;

import com.rnoobb.rats.ModItems;
import com.rnoobb.rats.entity.ModEntities;
import com.rnoobb.rats.screen.RatScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.entity.player.PlayerInventory;
import java.util.UUID;

public class RatEntity extends TameableEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final SimpleInventory inventory = new SimpleInventory(3);

    public RatEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.inventory.addListener(sender -> updateArmor());
    }

    private void updateArmor() {
        if (this.getWorld().isClient) return;
        ItemStack stack = this.inventory.getStack(0);
        int armorValue = 0;
        if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
            armorValue = armorItem.getProtection();
        }
        if (this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR) != null) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(armorValue);
        }
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));
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
        if (itemStack.isOf(ModItems.CHEESE) && !this.isTamed()) {
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
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
            }
            return ActionResult.SUCCESS;
        } else if (this.isTamed() && this.isOwner(player)) {
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
                    this.setSitting(!this.isSitting());
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
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList list = new NbtList();
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound slotNbt = new NbtCompound();
                slotNbt.putByte("Slot", (byte) i);
                stack.writeNbt(slotNbt);
                list.add(slotNbt);
            }
        }
        nbt.put("Inventory", list);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtList list = nbt.getList("Inventory", 10);
        for (int i = 0; i < list.size(); ++i) {
            NbtCompound slotNbt = list.getCompound(i);
            int slot = slotNbt.getByte("Slot") & 255;
            if (slot < this.inventory.size()) {
                this.inventory.setStack(slot, ItemStack.fromNbt(slotNbt));
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
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
