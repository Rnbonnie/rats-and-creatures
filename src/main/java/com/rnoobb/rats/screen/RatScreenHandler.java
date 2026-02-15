package com.rnoobb.rats.screen;

import com.rnoobb.rats.entity.custom.RatEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;


public class RatScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final RatEntity entity;

    public RatScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, (RatEntity) playerInventory.player.getWorld().getEntityById(buf.readInt()));
    }

    public RatScreenHandler(int syncId, PlayerInventory playerInventory, RatEntity entity) {
        super(ModScreenHandlers.RAT_SCREEN_HANDLER, syncId);
        this.inventory = entity.inventory;
        this.entity = entity;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 70, 25) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.HEAD;
            }
        });
        this.addSlot(new Slot(inventory, 1, 70, 46));
        this.addSlot(new Slot(inventory, 2, 70, 67));
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }
    
    public RatEntity getEntity() {
        return entity;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 98 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 156));
        }
    }
    
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }
}
