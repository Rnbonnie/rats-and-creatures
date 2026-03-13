package com.rnoobb.rats.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rnoobb.rats.RatsAndCreatures;
import com.rnoobb.rats.entity.custom.RatEntity;
import com.rnoobb.rats.network.ModNetworking;
import com.rnoobb.rats.screen.RatScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RatScreen extends HandledScreen<RatScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(RatsAndCreatures.MOD_ID, "textures/gui/creature.png");

    private final RatEntity.Behavior[] behaviors = RatEntity.Behavior.values();
    private int currentBehaviorIndex = 0;
    private ButtonWidget behaviorButton;

    public RatScreen(RatScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        this.currentBehaviorIndex = this.handler.getEntity().getBehavior().ordinal();

        // Инициализируем кнопку
        this.behaviorButton = ButtonWidget.builder(Text.literal(behaviors[currentBehaviorIndex].name()), button -> {
            // 1. Меняем визуал кнопки циклично
            currentBehaviorIndex = (currentBehaviorIndex + 1) % behaviors.length;
            button.setMessage(Text.literal(behaviors[currentBehaviorIndex].name()));

            // 2. Отправляем пакет на сервер с ID сущности и новым состоянием
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(this.handler.getEntity().getId());
            buf.writeEnumConstant(behaviors[currentBehaviorIndex]);
            ClientPlayNetworking.send(ModNetworking.CHANGE_RAT_BEHAVIOR, buf);

        }).dimensions(x + 90, y + 20, 70, 20).build(); // Настрой координаты (x, y) под свой GUI

        this.addDrawableChild(this.behaviorButton);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY - 3, 4210752, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        
        InventoryScreen.drawEntity(context, x + 35, y + 62, 51, (float)(x + 51) - mouseX, (float)(y + 75 - 50) - mouseY, this.handler.getEntity());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
