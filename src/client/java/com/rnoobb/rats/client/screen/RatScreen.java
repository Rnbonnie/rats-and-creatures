package com.rnoobb.rats.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rnoobb.rats.RatsAndCreatures;
import com.rnoobb.rats.screen.RatScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RatScreen extends HandledScreen<RatScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("rats_and_creatures", "textures/gui/creature.png");

    public RatScreen(RatScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // Высота от верхнего края рамки до нижнего края рамки в creature.png
        this.backgroundHeight = 180;
    }
  @Override
  protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      // Рисуем только название сверху (entity.rats...), не рисуем "Инвентарь" снизу
      context.drawText(this.textRenderer, this.title, this.titleX, this.titleY-3, 4210752, false);
    
      // Если хотите оставить "Инвентарь", но сдвинуть его, раскомментируйте и измените координаты:
      // context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY + 10, 4210752, false);
  }
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        
        // Draw the rat entity
        InventoryScreen.drawEntity(context, x + 35, y + 62, 51, (float)(x + 51) - mouseX, (float)(y + 75 - 50) - mouseY, this.handler.getEntity());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
