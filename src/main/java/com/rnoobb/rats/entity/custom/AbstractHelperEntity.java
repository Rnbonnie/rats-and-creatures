package com.rnoobb.rats.entity.custom;

import com.rnoobb.rats.screen.RatScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractHelperEntity extends TameableEntity implements CompanionInventoryEntity {

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

    protected static final TrackedData<Integer> BEHAVIOR = DataTracker.registerData(AbstractHelperEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public final SimpleInventory inventory = new SimpleInventory(11);
    protected BlockPos homePos;

    protected AbstractHelperEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.inventory.addListener(sender -> {
            this.equipStack(EquipmentSlot.HEAD, sender.getStack(0));
            this.equipStack(EquipmentSlot.MAINHAND, sender.getStack(2));
        });
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BEHAVIOR, Behavior.FOLLOW.ordinal());
    }

    @Override
    public SimpleInventory getCompanionInventory() {
        return this.inventory;
    }

    @Override
    public Behavior getBehavior() {
        int index = MathHelper.clamp(this.dataTracker.get(BEHAVIOR), 0, Behavior.values().length - 1);
        return Behavior.values()[index];
    }

    @Override
    public void setBehavior(Behavior behavior) {
        this.dataTracker.set(BEHAVIOR, behavior.ordinal());
        this.setSitting(behavior == Behavior.SIT);
        this.calculateDimensions();
        // Always stop navigation and clear target when behavior changes to ensure immediate re-evaluation of goals
        this.getNavigation().stop();
        this.setTarget(null);
        if (behavior == Behavior.SIT) {
            this.setVelocity(0.0D, this.getVelocity().y, 0.0D);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (target != null && this.isTamed() && this.isSitting()) {
            this.setBehavior(Behavior.FOLLOW);
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

    @Override
    public BlockPos getWanderAnchor() {
        LivingEntity owner = this.getOwner();
        return owner != null ? owner.getBlockPos() : this.getHomePos();
    }

    @Override
    public Entity asEntity() {
        return this;
    }

    protected ActionResult handleCompanionInteraction(PlayerEntity player, Hand hand) {
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
                            return AbstractHelperEntity.this.getDisplayName();
                        }

                        @Override
                        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                            return new RatScreenHandler(syncId, playerInventory, AbstractHelperEntity.this);
                        }
                    });
                }
                return ActionResult.SUCCESS;
            } else if (player.getStackInHand(hand).isEmpty() || (!player.getStackInHand(hand).isFood() && !player.getStackInHand(hand).isOf(com.rnoobb.rats.ModItems.CAGE))) {
                if (!this.getWorld().isClient) {
                    this.setBehavior(this.getBehavior() == Behavior.SIT ? Behavior.FOLLOW : Behavior.SIT);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
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
    public World method_48926() {
        return this.getWorld();
    }
}
