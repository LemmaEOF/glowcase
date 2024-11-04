package dev.hephaestus.glowcase.client.gui.widget.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.glowcase.client.gui.screen.ingame.ColorPickerIncludedScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ColorPickerWidget extends PressableWidget {
	static final Identifier CONFIRM_TEXTURE = Identifier.ofVanilla("pending_invite/accept");
	static final Identifier CONFIRM_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("pending_invite/accept_highlighted");
	static final Identifier CANCEL_TEXTURE = Identifier.ofVanilla("pending_invite/reject");
	static final Identifier CANCEL_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("pending_invite/reject_highlighted");

	public final ColorPickerIncludedScreen screen;
	public Element targetElement;
	public Color color = Color.red;
	public boolean includePresets = true;
	public ArrayList<ColorPresetWidget> presetWidgets = Lists.newArrayList();
	public boolean confirmOrCancelButtonDown = false;
	public IconButtonWidget confirmButton;
	public IconButtonWidget cancelButton;
	private Consumer<Color> changeListener;
	private BiConsumer<Color, @Nullable Formatting> presetListener;
	private Consumer<ColorPickerWidget> onAccept;
	private Consumer<ColorPickerWidget> onCancel;

	private boolean mouseDown = false;
	private int presetY, presetSize, presetPadding, presetHeight;
	private boolean presetDown = false;
	private int previewX, previewY, previewWidth, previewHeight;
	private int hueX, hueY, hueWidth, hueHeight;
	private boolean hueDown = false;
	private int satLightX, satLightY, satLightWidth, satLightHeight;
	private boolean satLightDown = false;
	public int hueThumbX;
	public int satLightThumbX, satLightThumbY;

	private float[] HSL;
	private float hue;
	private float saturation;
	private float light;

	public static ColorPickerWidget.Builder builder(ColorPickerIncludedScreen screen, int x, int y) {
		return new ColorPickerWidget.Builder(screen, x, y);
	}

	public ColorPickerWidget(ColorPickerIncludedScreen screen, int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
		this.screen = screen;

		this.confirmButton = IconButtonWidget.builder(CONFIRM_TEXTURE, action -> this.confirmColor())
			.hoverIcon(CONFIRM_HIGHLIGHTED_TEXTURE).build();

		this.cancelButton = IconButtonWidget.builder(CANCEL_TEXTURE, action -> this.cancel())
			.hoverIcon(CANCEL_HIGHLIGHTED_TEXTURE).build();

		updatePositions();
		updateHSL();
		updateThumbPositions();
	}

	public void setTargetElement(Element element) {
		this.targetElement = element;
	}

	public void setIncludePresets(boolean shouldInclude) {
		this.includePresets = shouldInclude;
	}

	public void setPresets(boolean includeDefaultPresets, List<Color> addedPresets) {
		if(includeDefaultPresets) {
			addDefaultPresets();
		}
		if(!addedPresets.isEmpty()) {
			for (Color preset : addedPresets) {
				this.presetWidgets.add(ColorPresetWidget.fromColor(this, preset));
			}
		}
	}

	public void confirmColor() {
//		this.insertColor(this.color);
		if(this.onAccept != null) {
			this.onAccept.accept(this);
		} else {
			this.toggle(false);
		}
	}

	public void cancel() {
//		this.toggle(false);
		if(this.onCancel != null) {
			this.onCancel.accept(this);
		} else {
			this.toggle(false);
		}
	}

	public void toggle(boolean active) {
		this.active = active;
		this.visible = active;
		if(this.active) {
			this.updatePositions();
			this.updateHSL();
			this.updateThumbPositions();
		}
	}

	public void insertColor(Color color) {
		String hex = getHexCode(color);
		this.screen.insertHexTag(hex);
	}

	public void insertFormatting(Formatting formatting) {
		this.screen.insertFormattingTag(formatting);
	}

	public void setColor(Color color) {
		this.color = color;
		this.updateHSL();
		this.updateThumbPositions();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if(!visible) return;
		updateHSL();

		context.setShaderColor(1f, 1f, 1f, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();

		int x = this.getX();
		int y = this.getY();
		int z = 1; //prevent z-fighting with other widgets
		int width = this.getWidth();
		int height = this.getHeight();

		//background
		context.drawTexture(Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png"), x, y, z, 0, 0, width, height, 32, 32);
		if(this.isSelected()) {
			//outline
			drawOutline(context, x, y, width, height, z, Color.white);
		}

		//color picker stuff
		updatePositions();
		this.confirmButton.setPosition(x + width - presetSize - presetPadding, y + height - presetSize - 2, z + 1, presetSize, presetSize + 2);
		this.cancelButton.setPosition(x + width - presetSize * 2 - presetPadding * 2 - 1, y + height - presetSize - 2, z + 1, presetSize, presetSize + 2);

		drawColorPreview(context, previewX, previewY, previewWidth, previewHeight, z + 1);
		drawSatLight(context, satLightX, satLightY, satLightWidth, satLightHeight, z + 3);
		drawHueBar(context, hueX, hueY, hueWidth, hueHeight, z + 1);
		if(this.includePresets) {
			//sorta dynamic but also really specific to keep it all aligned
			//I'm not going to worry about it a lot though because I do not see the custom preset thing being used a lot if at all
			drawPresets(context, mouseX, mouseY, delta, previewX, presetY, y + height - presetY, z + 1, presetSize, width / (presetSize + presetPadding), presetPadding);
		}

		this.confirmButton.renderWidget(context, mouseX, mouseY, delta);
		this.cancelButton.renderWidget(context, mouseX, mouseY, delta);

		context.setShaderColor(1f, 1f, 1f, 1f);
	}

	public void updatePositions() {
		int x = this.getX();
		int y = this.getY();
		int width = this.getWidth();
		int height = this.getHeight();

		presetSize = (int) (height / 6.5);
		presetPadding = 2;
		presetHeight = presetSize * 2 + presetPadding * 2;

		previewX = x + 2;
		previewY = y + 2;
		previewWidth = width / 3;
		previewHeight = height - 16 - (includePresets ? presetHeight : 0);
		satLightX = previewX + previewWidth + 2;
		satLightY = y + 2;
		satLightWidth = width - previewWidth - 6;
		satLightHeight = previewHeight;
		hueX = x + 2;
		hueY = previewY	+ previewHeight + 2;
		hueWidth = width - 4;
		hueHeight = height - previewHeight - 6 - (includePresets ? presetHeight : 0);
		presetY = hueY + hueHeight + presetPadding;
	}

	private void drawColorPreview(DrawContext context, int x, int y, int width, int height, int z) {
		context.fill(x, y, x + width, y + height, z, this.color.getRGB());
	}

	private void drawHueBar(DrawContext context, int x, int y, int width, int height, int z) {
		//rainbow gradient
		int[] colors = new int[] {Color.red.getRGB(), Color.yellow.getRGB(), Color.green.getRGB(),
			Color.cyan.getRGB(), Color.blue.getRGB(), Color.magenta.getRGB(), Color.red.getRGB()};

		int maxColors = colors.length - 1;
		for (int color = 0; color < maxColors; color++) {
			sidewaysGradient(context,
				x + ((float) width / maxColors * (color)), y,
				(float) width / maxColors, height,
				z + 1, colors[color], colors[color + 1]);
		}

		//thumb
		drawOutline(context, hueThumbX - 3, y - 1, 6, height + 2, z + 2, Color.white);
		context.fill(hueThumbX - 3, y - 1, hueThumbX + 3, y + height + 1, z + 1, getRgbFromHueThumb());
	}

	private void drawSatLight(DrawContext context, int x, int y, int width, int height, int z) {
		//white to current color's hue, left to right
		sidewaysGradient(context, x, y, width, height, z, Color.white.getRGB(), getRgbFromHueThumb());

		//transparent to black, top to bottom
		context.fillGradient(x, y, x + width, y + height, z, 0x00000000, Color.black.getRGB());

		//thumb
		context.fill(satLightThumbX - 4, satLightThumbY - 4, satLightThumbX + 4, satLightThumbY + 4, z + 1, this.color.getRGB());
		drawOutline(context, satLightThumbX - 4, satLightThumbY - 4, 8, 8, z + 2, Color.white);
	}

	private void drawOutline(DrawContext context, int x, int y, int width, int height, int z, Color outlineColor) {
		int color = outlineColor.getRGB();
		context.fill(x, y, x + width, y + 1, z, color);
		context.fill(x, y, x + 1, y + height, z, color);
		context.fill(x + width, y, x + width - 1, y + height, z, color);
		context.fill(x, y + height, x + width, y + height - 1, z, color);
	}

	private void sidewaysGradient(DrawContext context, float x, float y, float width, float height, float z, int startColor, int endColor) {
		RenderLayer layer = RenderLayer.getGui();
		VertexConsumer vertexConsumer = context.getVertexConsumers().getBuffer(layer);

		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		vertexConsumer.vertex(matrix, x, y, z).color(startColor);
		vertexConsumer.vertex(matrix, x, y + height, z).color(startColor);
		vertexConsumer.vertex(matrix, x + width, y + height, z).color(endColor);
		vertexConsumer.vertex(matrix, x + width, y, z).color(endColor);
	}

	private void drawPresets(DrawContext context, int mouseX, int mouseY, float delta, int x, int y, int height, int z, int presetSize, int presetsPerLine, int presetPadding) {
		int presetX = x;
		int presetY = y;
		int renderedPresets = 0;
		for (ColorPresetWidget preset : this.presetWidgets) {
			preset.setPosition(presetX, presetY, z, presetSize);
			preset.renderWidget(context, mouseX, mouseY, delta);
			presetX += presetSize + presetPadding;
			renderedPresets++;
			if(renderedPresets % presetsPerLine == 0) {
				presetY += presetSize + presetPadding;
				if(presetY > y + height) { //prevent overflow
					return;
				}
				presetX = x;
			}
		}
	}

	//done manually to keep list order instead of looping through Formatting.values()
	public void addDefaultPresets() {
		ColorPresetWidget darkRed = ColorPresetWidget.fromFormatting(this, Formatting.DARK_RED);
		ColorPresetWidget red = ColorPresetWidget.fromFormatting(this, Formatting.RED);
		ColorPresetWidget gold = ColorPresetWidget.fromFormatting(this, Formatting.GOLD);
		ColorPresetWidget yellow = ColorPresetWidget.fromFormatting(this, Formatting.YELLOW);
		ColorPresetWidget green = ColorPresetWidget.fromFormatting(this, Formatting.GREEN);
		ColorPresetWidget darkGreen = ColorPresetWidget.fromFormatting(this, Formatting.DARK_GREEN);
		ColorPresetWidget aqua = ColorPresetWidget.fromFormatting(this, Formatting.AQUA);
		ColorPresetWidget darkAqua = ColorPresetWidget.fromFormatting(this, Formatting.DARK_AQUA);
		ColorPresetWidget blue = ColorPresetWidget.fromFormatting(this, Formatting.BLUE);
		ColorPresetWidget darkBlue = ColorPresetWidget.fromFormatting(this, Formatting.DARK_BLUE);
		ColorPresetWidget lightPurple = ColorPresetWidget.fromFormatting(this, Formatting.LIGHT_PURPLE);
		ColorPresetWidget darkPurple = ColorPresetWidget.fromFormatting(this, Formatting.DARK_PURPLE);
		ColorPresetWidget white = ColorPresetWidget.fromFormatting(this, Formatting.WHITE);
		ColorPresetWidget grey = ColorPresetWidget.fromFormatting(this, Formatting.GRAY);
		ColorPresetWidget darkGrey = ColorPresetWidget.fromFormatting(this, Formatting.DARK_GRAY);
		ColorPresetWidget black = ColorPresetWidget.fromFormatting(this, Formatting.BLACK);
		this.presetWidgets.addAll(List.of(darkRed, red, gold, yellow, green, darkGreen, aqua, darkAqua,
			blue, darkBlue, lightPurple, darkPurple, white, grey, darkGrey, black));
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		this.mouseDown = true;
		this.satLightDown = false;
		this.hueDown = false;
		this.presetDown = false;
		this.confirmOrCancelButtonDown = false;
		setColorFromMouse(mouseX, mouseY);
	}

	public void setColorFromMouse(double mouseX, double mouseY) {
		if(clickedSatLight(mouseX, mouseY)) {
			setSatLightFromMouse(mouseX, mouseY);
		} else if(clickedHue(mouseX, mouseY)) {
			setHueFromMouse(mouseX);
		} else if(this.confirmButton.isMouseOver(mouseX, mouseY)) {
			if(satLightDown || hueDown || presetDown || confirmOrCancelButtonDown) return;
			this.confirmButton.onClick(mouseX, mouseY);
			confirmOrCancelButtonDown = true;
		} else if(this.cancelButton.isMouseOver(mouseX, mouseY)) {
			if(satLightDown || hueDown || presetDown || confirmOrCancelButtonDown) return;
			this.cancelButton.onClick(mouseX, mouseY);
			confirmOrCancelButtonDown = true;
		} else {
			//clickedPreset also sets the preset to avoid an extra calculation
			checkAndSetPreset(mouseX, mouseY);
		}

		if(this.changeListener != null) {
			this.changeListener.accept(this.color);
		}
	}

	public boolean clickedSatLight(double mouseX, double mouseY) {
		if(hueDown || presetDown || confirmOrCancelButtonDown) return false;

		if(mouseX >= satLightX
		&& mouseX <= satLightX + satLightWidth
		&& mouseY >= satLightY
		&& mouseY <= satLightY + satLightHeight) {
			satLightDown = true;
		}

		if(satLightDown) {
			satLightThumbX = (int) Math.clamp(mouseX, satLightX, satLightX + satLightWidth);
			satLightThumbY = (int) Math.clamp(mouseY, satLightY, satLightY + satLightHeight);
		}
		return satLightDown;
	}

	public boolean clickedHue(double mouseX, double mouseY) {
		if(satLightDown || presetDown || confirmOrCancelButtonDown) return false;

		if(mouseY >= hueY && mouseY <= hueY + hueHeight
		&& mouseX >= hueX && mouseX <= hueX + hueWidth) {
			hueDown = true;
		}

		if(hueDown) {
			hueThumbX = (int) Math.clamp(mouseX, hueX, hueX + hueWidth);
		}
		return hueDown;
	}

	public boolean checkAndSetPreset(double mouseX, double mouseY) {
		if(satLightDown || hueDown || presetDown || confirmOrCancelButtonDown) return false;

		//just checks for each preset here, and also sets here so it doesn't have to check again
		for (ColorPresetWidget preset : this.presetWidgets) {
			if(preset.isMouseOver(mouseX, mouseY)) {
				preset.onClick(mouseX, mouseY);
				//even though the preset closes the color picker,
				//this is added to prevent spamming tags when holding down the mouse button
				presetDown = true;
			}
		}
		return presetDown;
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		if(mouseDown || isMouseOver(mouseX, mouseY)) {
			setColorFromMouse(mouseX, mouseY);
		}
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		this.mouseDown = false;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return super.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void onPress() {}

	public void setSatLightFromMouse(double mouseX, double mouseY) {
		if(mouseX < satLightX) {
			this.saturation = 0f;
		} else if(mouseX > satLightX + satLightWidth) {
			this.saturation = 1f;
		} else {
			float newSat = (float) (mouseX - satLightX) / satLightWidth;
			this.saturation = Math.clamp(newSat, 0f, 1f);
		}

		if(mouseY < satLightY) {
			this.light = 1f;
		} else if (mouseY > satLightY + satLightHeight) {
			this.light = 0f;
		} else {
			float newLight = (float) (mouseY - satLightY) / satLightHeight;
			this.light = Math.clamp(1f - newLight, 0f, 1f);
		}
		setColorFromHSL();
	}

	public void setHueFromMouse(double mouseX) {
		if(mouseX < hueX) {
			this.hue = 0f;
		} else if(mouseX > hueX + hueWidth) {
			this.hue = 1f;
		} else {
			float newHue = (float) (mouseX - hueX) / hueWidth;
			this.hue = Math.clamp(newHue, 0f, 1f);
		}
		setColorFromHSL();
	}

	public void updateThumbPositions() {
		this.satLightThumbX = getSatLightThumbX();
		this.satLightThumbY = getSatLightThumbY();
		this.hueThumbX = getHueThumbX();
	}

	private int getSatLightThumbX() {
		int min = satLightX;
		int max = satLightX + satLightWidth;
		int value = (int) (min + (satLightWidth * this.saturation));
		return Math.clamp(value, min, max);
	}

	private int getSatLightThumbY() {
		int min = satLightY;
		int max = satLightY + satLightHeight;
		int value = (int) (min + (satLightHeight * (1.0f - this.light)));
		return Math.clamp(value, min, max);
	}

	private int getHueThumbX() {
		int min = hueX;
		int max = hueX + hueWidth;
		int value = (int) (min + hueWidth * this.hue);
		return Math.clamp(value, min, max);
	}

	public Color getCurrentColor() {
		return this.color;
	}

	public void setColorFromHSL() {
		float trueHue = (float) (hueThumbX - hueX) / hueWidth;
		this.color = Color.getHSBColor(trueHue, this.saturation, this.light);
	}

	public int getRgbFromHueThumb() {
		float trueHue = (float) (hueThumbX - hueX) / hueWidth;
		return Color.HSBtoRGB(trueHue, 1, 1);
	}

	public void updateHSL() {
		this.HSL = getHSL();
		this.hue = HSL[0];
		this.saturation = HSL[1];
		this.light = HSL[2];
	}

	protected float[] getHSL() {
		return Color.RGBtoHSB(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), null);
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		this.appendDefaultNarrations(builder);
	}

	public void setChangeListener(Consumer<Color> changeListener) {
		this.changeListener = changeListener;
	}

	public void setPresetListener(BiConsumer<Color, @Nullable Formatting> presetListener) {
		this.presetListener = presetListener;
	}

	public void setOnAccept(Consumer<ColorPickerWidget> onAccept) {
		this.onAccept = onAccept;
	}

	public void setOnCancel(Consumer<ColorPickerWidget> onCancel) {
		this.onCancel = onCancel;
	}

	public BiConsumer<Color, @Nullable Formatting> getPresetListener() {
		return presetListener;
	}

	public static String getHexCode(Color color) {
		return "#" + String.format("%1$06X", color.getRGB() & 0x00FFFFFF);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private ColorPickerIncludedScreen screen;
		private int x;
		private int y;
		private int width = 150;
		private int height = 200;
		private boolean includePresets = true;
		private boolean includeDefaultPresets = true;
		private List<Color> presets = Lists.newArrayList();

		public Builder(ColorPickerIncludedScreen screen, int x, int y) {
			this.screen = screen;
			this.x = x;
			this.y = y;
		}

		public ColorPickerWidget.Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public ColorPickerWidget.Builder includePresets(boolean shouldInclude) {
			this.includePresets = shouldInclude;
			return this;
		}

		public ColorPickerWidget.Builder withPreset(boolean includeDefault, Color... presets) {
			this.includeDefaultPresets = includeDefault;
			this.presets.addAll(Arrays.asList(presets));
			return this;
		}

		public ColorPickerWidget build() {
			ColorPickerWidget colorPickerWidget = new ColorPickerWidget(this.screen, this.x, this.y, this.width, this.height, Text.of(""));
			colorPickerWidget.setIncludePresets(this.includePresets);
			if(this.includePresets) {
				colorPickerWidget.setPresets(this.includeDefaultPresets, this.presets);
			}
			return colorPickerWidget;
		}
	}
}
