package com.rnoobb.rats.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.block.entity.AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    private static final Identifier CHEESE_RECIPE_ID = new Identifier("rats_and_creatures", "cheese_craft");

    @Inject(method = "craftRecipe", at = @At("RETURN"))
    private static void rats$leaveEmptyBucketAfterCheese(
            DynamicRegistryManager registryManager,
            Recipe<?> recipe,
            DefaultedList<ItemStack> slots,
            int count,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ() || recipe == null || !CHEESE_RECIPE_ID.equals(recipe.getId())) {
            return;
        }

        ItemStack inputStack = slots.get(0);
        if (inputStack.isEmpty()) {
            slots.set(0, new ItemStack(Items.BUCKET));
        }
    }
}
