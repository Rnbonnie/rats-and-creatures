package com.rnoobb.rats.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;

public interface CompanionInventoryEntity {
    Inventory getCompanionInventory();

    RatEntity.Behavior getBehavior();

    void setBehavior(RatEntity.Behavior behavior);

    BlockPos getWanderAnchor();

    Entity asEntity();
}
