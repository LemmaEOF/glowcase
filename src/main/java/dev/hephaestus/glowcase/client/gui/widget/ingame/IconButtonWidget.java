package dev.hephaestus.glowcase.client.gui.widget.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class IconButtonWidget extends ButtonWidget {
	public Identifier icon;
	@Nullable
	public Identifier hoverIcon;
	public int iconWidth;
	public int iconHeight;
	public int z;

	public static IconButtonWidget.Builder builder(Identifier icon, ButtonWidget.PressAction onPress) {
		return new IconButtonWidget.Builder(icon, onPress);
	}

	public IconButtonWidget(int x, int y, int width, int height, int iconWidth, int iconHeight, Identifier icon, @Nullable Identifier hoverIcon, PressAction onPress) {
		super(x, y, width, height, Text.of(""), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
		this.icon = icon;
		this.hoverIcon = hoverIcon;
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		Identifier drawnIcon = this.icon;
		if(this.hoverIcon != null && this.isMouseOver(mouseX, mouseY)) {
			drawnIcon = this.hoverIcon;
		}
		context.drawGuiTexture(drawnIcon, this.getX(), this.getY(), z, this.iconWidth, this.iconHeight);
	}

	public void setPosition(int x, int y, int z, int size, int iconSize) {
		this.setX(x);
		this.setY(y);
		this.setDimensions(size, size);
		this.iconWidth = iconSize;
		this.iconHeight = iconSize;
		this.z = z;
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final Identifier icon;
		@Nullable
		private Identifier hoverIcon = null;
		private final ButtonWidget.PressAction onPress;
		private int x;
		private int y;
		private int iconWidth = 16;
		private int iconHeight = 16;
		private int width = 150;
		private int height = 150;

		public Builder(Identifier icon, ButtonWidget.PressAction onPress) {
			this.icon = icon;
			this.onPress = onPress;
		}

		public IconButtonWidget.Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public IconButtonWidget.Builder size(int width, int height, int iconWidth, int iconHeight) {
			this.width = width;
			this.height = height;
			this.iconWidth = iconWidth;
			this.iconHeight = iconHeight;
			return this;
		}

		public IconButtonWidget.Builder dimensions(int x, int y, int width, int height, int iconWidth, int iconHeight) {
			return this.position(x, y).size(width, height, iconWidth, iconHeight);
		}

		public IconButtonWidget.Builder hoverIcon(Identifier hoverIcon) {
			this.hoverIcon = hoverIcon;
			return this;
		}

		public IconButtonWidget build() {
			return new IconButtonWidget(this.x, this.y, this.width, this.height, this.iconWidth, this.iconHeight, this.icon, this.hoverIcon, this.onPress);
		}

	}


}
