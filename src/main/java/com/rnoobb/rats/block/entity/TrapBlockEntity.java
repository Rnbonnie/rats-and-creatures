package com.rnoobb.rats.block.entity;

import com.rnoobb.rats.ModBlockEntities;
import com.rnoobb.rats.item.CageItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class TrapBlockEntity extends BlockEntity {
    private ItemStack bait = ItemStack.EMPTY;
    private NbtCompound capturedEntity;

    public TrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAP, pos, state);
    }

    public boolean hasBait() {
        return !this.bait.isEmpty();
    }

    public boolean hasCapturedEntity() {
        return this.capturedEntity != null && !this.capturedEntity.isEmpty();
    }

    public boolean canCapture() {
        return this.hasBait() && !this.hasCapturedEntity();
    }

    public void setBait(ItemStack bait) {
        this.bait = bait;
        this.markDirty();
    }

    public ItemStack removeBait() {
        ItemStack removed = this.bait.copy();
        this.bait = ItemStack.EMPTY;
        this.markDirty();
        return removed;
    }

    public void captureEntity(LivingEntity entity) {
        this.capturedEntity = CageItem.captureEntityData(entity);
        this.bait = ItemStack.EMPTY;
        this.markDirty();
    }

    public NbtCompound removeCapturedEntity() {
        NbtCompound removed = this.capturedEntity == null ? new NbtCompound() : this.capturedEntity.copy();
        this.capturedEntity = null;
        this.markDirty();
        return removed;
    }

    public DefaultedList<ItemStack> getDroppedStacks() {
        DefaultedList<ItemStack> drops = DefaultedList.of();
        if (!this.bait.isEmpty()) {
            drops.add(this.bait.copy());
        }
        return drops;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.bait = nbt.contains("Bait") ? ItemStack.fromNbt(nbt.getCompound("Bait")) : ItemStack.EMPTY;
        this.capturedEntity = nbt.contains("CapturedEntity") ? nbt.getCompound("CapturedEntity") : null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.bait.isEmpty()) {
            nbt.put("Bait", this.bait.writeNbt(new NbtCompound()));
        }
        if (this.capturedEntity != null && !this.capturedEntity.isEmpty()) {
            nbt.put("CapturedEntity", this.capturedEntity.copy());
        }
    }
}
