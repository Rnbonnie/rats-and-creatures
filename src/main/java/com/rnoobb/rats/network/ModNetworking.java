package com.rnoobb.rats.network;

import com.rnoobb.rats.RatsAndCreatures;
import com.rnoobb.rats.entity.custom.RatEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public final class ModNetworking {
    public static final Identifier CHANGE_RAT_BEHAVIOR = new Identifier(RatsAndCreatures.MOD_ID, "change_rat_behavior");

    private ModNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_RAT_BEHAVIOR, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            RatEntity.Behavior behavior = buf.readEnumConstant(RatEntity.Behavior.class);

            server.execute(() -> {
                if (!(player.getWorld().getEntityById(entityId) instanceof RatEntity rat)) {
                    return;
                }

                if (!rat.isOwner(player)) {
                    return;
                }

                rat.setBehavior(behavior);
            });
        });
    }
}
