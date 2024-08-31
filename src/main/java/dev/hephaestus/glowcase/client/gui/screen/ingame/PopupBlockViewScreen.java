package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.glowcase.block.entity.PopupBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

//TODO: multi-character selection at some point? it may be a bit complex but it'd be nice
public class PopupBlockViewScreen extends GlowcaseScreen {
	private final PopupBlockEntity popupBlockEntity;

	public PopupBlockViewScreen(PopupBlockEntity popupBlockEntity) {
		this.popupBlockEntity = popupBlockEntity;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		renderDarkening(context);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (this.client != null) {
			super.render(context, mouseX, mouseY, delta);

			context.getMatrices().push();
			context.getMatrices().translate(0, 40 + 2 * this.width / 100F, 0);
			for (int i = 0; i < this.popupBlockEntity.lines.size(); ++i) {
				var text = this.popupBlockEntity.lines.get(i);

				int lineWidth = this.textRenderer.getWidth(text);
				switch (this.popupBlockEntity.textAlignment) {
					case LEFT -> context.drawTextWithShadow(client.textRenderer, text, this.width / 10, i * 12, this.popupBlockEntity.color);
					case CENTER -> context.drawTextWithShadow(client.textRenderer, text, this.width / 2 - lineWidth / 2, i * 12, this.popupBlockEntity.color);
					case RIGHT -> context.drawTextWithShadow(client.textRenderer, text, this.width - this.width / 10 - lineWidth, i * 12, this.popupBlockEntity.color);
				}
			}

			context.getMatrices().pop();
		}
	}
}
