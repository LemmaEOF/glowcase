package dev.hephaestus.glowcase.client.gui.screen.ingame;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class GlowcaseScreen extends Screen {
	protected GlowcaseScreen() {
		super(Text.empty());
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fill(matrices, 0, 0, this.width, this.height, 0x88000000);

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
