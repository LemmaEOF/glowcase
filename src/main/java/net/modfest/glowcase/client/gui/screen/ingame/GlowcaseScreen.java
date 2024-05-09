package net.modfest.glowcase.client.gui.screen.ingame;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class GlowcaseScreen extends Screen {
	protected GlowcaseScreen() {
		super(Text.empty());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0x88000000);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
