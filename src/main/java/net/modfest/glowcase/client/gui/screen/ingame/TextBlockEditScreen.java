package net.modfest.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import net.modfest.glowcase.block.entity.TextBlockEntity;
import net.modfest.glowcase.networking.GlowcaseClientNetworking;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class TextBlockEditScreen extends GlowcaseScreen {

	/*private final TextBlockEntity textBlockEntity;

	private SelectionManager selectionManager;
	private int currentRow;
	private long ticksSinceOpened = 0;
	private ButtonWidget changeAlignment;
	private TextFieldWidget colorEntryWidget;
	private ButtonWidget zOffsetToggle;
	private ButtonWidget shadowToggle;

	public TextBlockEditScreen(TextBlockEntity textBlockEntity) {
		textBlockEntity = textBlockEntity;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		renderDarkening(context);
	}

	@Override
	public void init() {
		super.init();
		int innerPadding = width / 100;
		// if (client != null) {
		// client.keyboard.setRepeatEvents(true);
		// }
		selectionManager = new SelectionManager(
				() -> textBlockEntity.getRawLine(currentRow),
				(string) -> {
					textBlockEntity.setRawLine(currentRow, string);
					textBlockEntity.renderDirty = true;
				},
				SelectionManager.makeClipboardGetter(client),
				SelectionManager.makeClipboardSetter(client),
				(string) -> true);
		ButtonWidget decreaseSize = ButtonWidget.builder(Text.literal("-"), action -> {
			textBlockEntity.scale -= (float) Math.max(0, 0.125);
			textBlockEntity.renderDirty = true;
		}).dimensions(80, 0, 20, 20).build();
		ButtonWidget increaseSize = ButtonWidget.builder(Text.literal("+"), action -> {
			textBlockEntity.scale += 0.125F;
			textBlockEntity.renderDirty = true;
		}).dimensions(100, 0, 20, 20).build();
		changeAlignment = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.alignment", textBlockEntity.textAlignment), action -> {
			switch (textBlockEntity.textAlignment) {
				case LEFT -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.CENTER;
				case CENTER -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.RIGHT;
				case RIGHT -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.LEFT;
			}
			textBlockEntity.renderDirty = true;
			changeAlignment.setMessage(Text.stringifiedTranslatable("gui.glowcase.alignment", textBlockEntity.textAlignment));
		}).dimensions(120 + innerPadding, 0, 160, 20).build();
		shadowToggle = ButtonWidget.builder(Text.translatable("gui.glowcase.shadow_type", textBlockEntity.shadowType), action -> {
			switch (textBlockEntity.shadowType) {
				case DROP -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.PLATE;
				case PLATE -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.NONE;
				case NONE -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.DROP;
			}
			textBlockEntity.renderDirty = true;
			shadowToggle.setMessage(Text.translatable("gui.glowcase.shadow_type", textBlockEntity.shadowType));
		}).dimensions(120 + innerPadding, 20 + innerPadding, 160, 20).build();
		colorEntryWidget = new TextFieldWidget(client.textRenderer, 280 + innerPadding * 2, 0, 50, 20, Text.empty());
		colorEntryWidget.setText("#" + Integer.toHexString(textBlockEntity.color & 0x00FFFFFF));
		colorEntryWidget.setChangedListener(string -> {
			TextColor.parse(colorEntryWidget.getText()).ifSuccess(color -> {
				textBlockEntity.color = color == null ? 0xFFFFFFFF : color.getRgb() | 0xFF000000;
				textBlockEntity.renderDirty = true;
			});
		});
		zOffsetToggle = ButtonWidget.builder(Text.literal(textBlockEntity.zOffset.name()), action -> {
			switch (textBlockEntity.zOffset) {
				case FRONT -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;
				case CENTER -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;
				case BACK -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;
			}
			textBlockEntity.renderDirty = true;
			zOffsetToggle.setMessage(Text.literal(textBlockEntity.zOffset.name()));
		}).dimensions(330 + innerPadding * 3, 0, 80, 20).build();
		addDrawableChild(increaseSize);
		addDrawableChild(decreaseSize);
		addDrawableChild(changeAlignment);
		addDrawableChild(shadowToggle);
		addDrawableChild(zOffsetToggle);
		addDrawableChild(colorEntryWidget);
	}

	@Override
	public void tick() {
		++ticksSinceOpened;
	}

	@Override
	public void close() {
		GlowcaseClientNetworking.editTextBlock(textBlockEntity);
		super.close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (client != null) {
			super.render(context, mouseX, mouseY, delta);
			context.getMatrices().push();
			context.getMatrices().translate(0, 40 + 2 * width / 100F, 0);
			for (int i = 0; i < textBlockEntity.lines.size(); ++i) {
				var text = currentRow == i ? Text.literal(textBlockEntity.getRawLine(i)) : textBlockEntity.lines.get(i);
				int lineWidth = textRenderer.getWidth(text);
				switch (textBlockEntity.textAlignment) {
					case LEFT ->
							context.drawTextWithShadow(client.textRenderer, text, width / 10, i * 12, textBlockEntity.color);
					case CENTER ->
							context.drawTextWithShadow(client.textRenderer, text, width / 2 - lineWidth / 2, i * 12, textBlockEntity.color);
					case RIGHT ->
							context.drawTextWithShadow(client.textRenderer, text, width - width / 10 - lineWidth, i * 12, textBlockEntity.color);
				}
			}
			int caretStart = selectionManager.getSelectionStart();
			int caretEnd = selectionManager.getSelectionEnd();
			if (caretStart >= 0) {
				String line = textBlockEntity.getRawLine(currentRow);
				int selectionStart = MathHelper.clamp(Math.min(caretStart, caretEnd), 0, line.length());
				int selectionEnd = MathHelper.clamp(Math.max(caretStart, caretEnd), 0, line.length());
				String preSelection = line.substring(0, MathHelper.clamp(line.length(), 0, selectionStart));
				int startX = client.textRenderer.getWidth(preSelection);
				float push = switch (textBlockEntity.textAlignment) {
					case LEFT -> width / 10F;
					case CENTER -> width / 2F - textRenderer.getWidth(line) / 2F;
					case RIGHT -> width - width / 10F - textRenderer.getWidth(line);
				};
				startX += (int) push;
				int caretStartY = currentRow * 12;
				int caretEndY = currentRow * 12 + 9;
				if (ticksSinceOpened / 6 % 2 == 0 && !colorEntryWidget.isActive()) {
					if (selectionStart < line.length()) {
						context.fill(startX, caretStartY, startX + 1, caretEndY, 0xCCFFFFFF);
					}
					else {
						context.drawText(client.textRenderer, "_", startX, currentRow * 12, 0xFFFFFFFF, false);
					}
				}
				if (caretStart != caretEnd) {
					int endX = startX + client.textRenderer.getWidth(line.substring(selectionStart, selectionEnd));
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferBuilder = tessellator.getBuffer();
					RenderSystem.enableColorLogicOp();
					RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
					bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
					bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), startX, caretEndY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), endX, caretEndY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), endX, caretStartY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), startX, caretStartY, 0.0F).color(0, 0, 255, 255).next();
					BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
					RenderSystem.disableColorLogicOp();
				}
			}
			context.getMatrices().pop();
			context.drawTextWithShadow(client.textRenderer, Text.translatable("gui.glowcase.scale", textBlockEntity.scale), 7, 7, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean charTyped(char chr, int keyCode) {
		if (colorEntryWidget.isActive()) {
			return colorEntryWidget.charTyped(chr, keyCode);
		}
		else {
			selectionManager.insert(chr);
			return true;
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (colorEntryWidget.isActive()) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				close();
				return true;
			}
			else {
				return colorEntryWidget.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		else {
			setFocused(null);
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				textBlockEntity.addRawLine(currentRow + 1,
						textBlockEntity.getRawLine(currentRow).substring(
								MathHelper.clamp(selectionManager.getSelectionStart(), 0, textBlockEntity.getRawLine(currentRow).length())
						));
				textBlockEntity.setRawLine(currentRow,
						textBlockEntity.getRawLine(currentRow).substring(0, MathHelper.clamp(selectionManager.getSelectionStart(), 0, textBlockEntity.getRawLine(currentRow).length())
						));
				textBlockEntity.renderDirty = true;
				++currentRow;
				selectionManager.moveCursorToEnd(false);
				return true;
			}
			else if (keyCode == GLFW.GLFW_KEY_UP) {
				currentRow = Math.max(currentRow - 1, 0);
				selectionManager.moveCursorToEnd(false);
				return true;
			}
			else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				currentRow = Math.min(currentRow + 1, (textBlockEntity.lines.size() - 1));
				selectionManager.moveCursorToEnd(false);
				return true;
			}
			else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && currentRow > 0 && textBlockEntity.lines.size() > 1 && selectionManager.getSelectionStart() == 0 && selectionManager.getSelectionEnd() == selectionManager.getSelectionStart()) {
				--currentRow;
				selectionManager.moveCursorToEnd(false);
				deleteLine();
				return true;
			}
			else if (keyCode == GLFW.GLFW_KEY_DELETE && currentRow < textBlockEntity.lines.size() - 1 && selectionManager.getSelectionEnd() == textBlockEntity.getRawLine(currentRow).length()) {
				deleteLine();
				return true;
			}
			else {
				try {
					return selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
				}
				catch (StringIndexOutOfBoundsException e) {
					e.printStackTrace();
					MinecraftClient.getInstance().setScreen(null);
					return false;
				}
			}
		}
	}

	private void deleteLine() {
		textBlockEntity.setRawLine(currentRow,
				textBlockEntity.getRawLine(currentRow) + textBlockEntity.getRawLine(currentRow + 1)
		);
		textBlockEntity.lines.remove(currentRow + 1);
		textBlockEntity.renderDirty = true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int topOffset = (int) (40 + 2 * width / 100F);
		if (!colorEntryWidget.mouseClicked(mouseX, mouseY, button)) {
			colorEntryWidget.setFocused(false);
		}
		if (mouseY > topOffset) {
			currentRow = MathHelper.clamp((int) (mouseY - topOffset) / 12, 0, textBlockEntity.lines.size() - 1);
			setFocused(null);
			String baseContents = textBlockEntity.getRawLine(currentRow);
			int baseContentsWidth = textRenderer.getWidth(baseContents);
			int contentsStart;
			int contentsEnd;
			switch (textBlockEntity.textAlignment) {
				case LEFT -> {
					contentsStart = width / 10;
					contentsEnd = contentsStart + baseContentsWidth;
				}
				case CENTER -> {
					int midpoint = width / 2;
					int textMidpoint = baseContentsWidth / 2;
					contentsStart = midpoint - textMidpoint;
					contentsEnd = midpoint + textMidpoint;
				}
				case RIGHT -> {
					contentsEnd = width - width / 10;
					contentsStart = contentsEnd - baseContentsWidth;
				}
				//even though this is exhaustive, javac won't treat contentsStart and contentsEnd as initialized
				//why? who knows! just throw bc this should be impossible
				default -> throw new IllegalStateException(":HOW:");
			}
			if (mouseX <= contentsStart) {
				selectionManager.moveCursorToStart();
			}
			else if (mouseX >= contentsEnd) {
				selectionManager.moveCursorToEnd(false);
			}
			else {
				int lastWidth = 0;
				for (int i = 1; i < baseContents.length(); i++) {
					String testContents = baseContents.substring(0, i);
					int width = textRenderer.getWidth(testContents);
					int midpointWidth = (width + lastWidth) / 2;
					if (mouseX < contentsStart + midpointWidth) {
						selectionManager.moveCursorTo(i - 1, false);
						break;
					}
					else if (mouseX <= contentsStart + width) {
						selectionManager.moveCursorTo(i, false);
						break;
					}
					lastWidth = width;
				}
			}
			return true;
		}
		else {
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}*/
}
