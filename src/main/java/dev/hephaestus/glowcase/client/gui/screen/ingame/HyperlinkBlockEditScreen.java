package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.networking.GlowcaseClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class HyperlinkBlockEditScreen extends GlowcaseScreen {
	private final HyperlinkBlockEntity hyperlinkBlockEntity;

	private TextFieldWidget urlEntryWidget;

	public HyperlinkBlockEditScreen(HyperlinkBlockEntity hyperlinkBlockEntity) {
		this.hyperlinkBlockEntity = hyperlinkBlockEntity;
	}

	@Override
	public void init() {
		super.init();

		if (this.client == null) return;

		this.urlEntryWidget = new TextFieldWidget(this.client.textRenderer, width / 10, height / 2 - 10, 8 * width / 10, 20, Text.empty());
		this.urlEntryWidget.setText(this.hyperlinkBlockEntity.getUrl());
		this.urlEntryWidget.setMaxLength(Integer.MAX_VALUE);

		this.addDrawableChild(this.urlEntryWidget);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.close();
			return true;
		} else if (this.urlEntryWidget.isActive()) {
			return this.urlEntryWidget.keyPressed(keyCode, scanCode, modifiers);
		}else {
			return false;
		}
	}

	@Override
	public void close() {
		GlowcaseClientNetworking.editHyperlinkBlock(hyperlinkBlockEntity.getPos(), urlEntryWidget.getText());
		super.close();
	}
}
