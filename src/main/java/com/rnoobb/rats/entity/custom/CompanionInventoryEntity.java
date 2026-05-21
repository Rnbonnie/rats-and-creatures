package com.rnoobb.rats.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

public interface CompanionInventoryEntity {
    Inventory getCompanionInventory();

    AbstractHelperEntity.Behavior getBehavior();

    void setBehavior(AbstractHelperEntity.Behavior behavior);

    BlockPos getWanderAnchor();

    Entity asEntity();
}
