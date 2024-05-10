package net.modfest.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SprucePositioned;
import dev.lambdaurora.spruceui.option.SpruceDoubleInputOption;
import dev.lambdaurora.spruceui.option.SpruceDoubleOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceIconButtonWidget;
import dev.lambdaurora.spruceui.widget.option.SpruceOptionSliderWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceNamedTextFieldWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.modfest.glowcase.Glowcase;
import net.modfest.glowcase.block.entity.TextBlockEntity;
import net.modfest.glowcase.mixin.client.SpruceSliderWidgetAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextBlockEditScreenV2 extends SpruceScreen {

	public static final Identifier HEADER_BACKGROUND = new Identifier("textures/gui/inworld_menu_list_background.png");
	public static final Identifier HEADER_SEPARATOR = new Identifier("textures/gui/inworld_header_separator.png");
	public static final Identifier ICONS = Glowcase.id("textures/gui/icons.png");

	public static final Text EMPTY_TEXT = Text.literal("");
	public static final Text TEXT_SCALE_TOOLTIP = Text.translatable("gui.glowcase.text_block_editor.text_scale");

	public record Anchor(int getX) implements SprucePositioned {}

	public final TextBlockEntity blockEntity;

	public TextBlockEditScreenV2(TextBlockEntity blockEntity) {
		super(Text.translatable("gui.glowcase.text_block_editor.title"));
		this.blockEntity = blockEntity;
	}

	public Pair<SpruceOptionSliderWidget, SpruceTextFieldWidget> addLinkedSlider(Position base, String label, double min, double max, int sliderWidth, int fieldWidth, Supplier<Double> supplier, Consumer<Double> setter) {
		Text tooltip = Text.translatable("gui.glowcase.text_block_editor." + label);
		var ref = new Object() {
			SpruceTextFieldWidget field = null;
		};
		var sliderOption = new SpruceDoubleOption(label + "_slider", min, max, 0.1f, supplier, value -> ref.field.setText(String.format("%.1f", value)), option -> EMPTY_TEXT, tooltip);
		var slider = (SpruceOptionSliderWidget) sliderOption.createWidget(base, sliderWidth);
		var inputOption = new SpruceDoubleInputOption(label + "_input", supplier, value -> {
			setter.accept(value);
			((SpruceSliderWidgetAccessor) slider).setValueRaw(Math.clamp((value - min) / (max - min), 0.0, 1.0));
		}, tooltip);
		ref.field = ((SpruceNamedTextFieldWidget) inputOption.createWidget(Position.of(slider, sliderWidth + 3, 0), fieldWidth)).getTextFieldWidget();
		ref.field.getPosition().setRelativeY(0);
		ref.field.setText(String.format("%.1f", supplier.get()));
		addDrawableChild(slider);
		addDrawableChild(ref.field);
		return new Pair<>(slider, ref.field);
	}

	public List<RadialButton> addRadialButtons(Position base, String label, int v, int active, Consumer<Integer> onPress) {
		List<RadialButton> buttons = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			var tooltip = Text.translatable("gui.glowcase.text_block_editor." + label + "." + i);
			final int index = i;
			var radialButton = new RadialButton(Position.of(base, 23 * i, 0), 20, 20, EMPTY_TEXT,
					button -> onPress.accept(index), 20 * i, v, tooltip, buttons);
			if (active == i) {
				radialButton.setActive(false);
			}
			else {
				radialButton.setTooltip(tooltip);
			}
			addDrawableChild(radialButton);
			buttons.add(radialButton);
		}
		return buttons;
	}

	@Override
	protected void init() {
		var center = new Anchor(width / 2);
		var topRight = new Anchor(width);
		addLinkedSlider(Position.of(this, 27, 6), "text_scale", 0.1, 5.0, 60, 30, () -> (double) blockEntity.scale, value -> blockEntity.scale = value.floatValue());
		addRadialButtons(Position.of(center, -75, 6), "alignment", 26, blockEntity.textAlignment.ordinal(), index -> blockEntity.textAlignment = TextBlockEntity.TextAlignment.values()[index]);
		addRadialButtons(Position.of(center, 9, 6), "shadow", 46, blockEntity.shadowType.ordinal(), index -> blockEntity.shadowType = TextBlockEntity.ShadowType.values()[index]);
		addLinkedSlider(Position.of(topRight, -98, 6), "z_offset", -4.0, 4.0, 60, 30, () -> (double) blockEntity.zOffset, value -> blockEntity.zOffset = value.floatValue());
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		renderDarkening(context);
		RenderSystem.enableBlend();
		context.drawTexture(HEADER_BACKGROUND, 0, 0, 0, 0, width, 32, 32, 32);
		context.drawTexture(HEADER_SEPARATOR, 0, 32, 0, 0, width, 2, 32, 2);
		RenderSystem.disableBlend();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawTexture(ICONS, 6, 10, 0, 0, 18, 13);
		context.drawTexture(ICONS, width - 116, 10, 0, 13, 13, 13);
	}

	public static class RadialButton extends SpruceIconButtonWidget {

		public int u, v;
		public Text tooltip;
		public List<RadialButton> buttons;

		public RadialButton(Position position, int width, int height, Text message, PressAction action, int u, int v, Text tooltip, List<RadialButton> buttons) {
			super(position, width, height, message, action);
			this.u = u;
			this.v = v;
			this.tooltip = tooltip;
			this.buttons = buttons;
		}

		@Override
		public void onPress() {
			super.onPress();
			setTooltip(null);
			for (var button : buttons) {
				button.setActive(button != this);
				if (button != this) {
					button.setTooltip(button.tooltip);
				}
			}
		}

		@Override
		protected int renderIcon(DrawContext graphics, int mouseX, int mouseY, float delta) {
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, getAlpha());
			RenderSystem.setShaderTexture(0, ICONS);
			RenderSystem.enableDepthTest();
			graphics.drawTexture(ICONS, getX(), getY(), u, v, getWidth(), getHeight(), 256, 256);
			return 0;
		}
	}
}
