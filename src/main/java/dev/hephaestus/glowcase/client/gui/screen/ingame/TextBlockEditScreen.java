package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
@Environment(EnvType.CLIENT)
public class TextBlockEditScreen extends GlowcaseScreen {
    private final TextBlockEntity textBlockEntity;

    private SelectionManager selectionManager;
    private int currentRow;
    private long ticksSinceOpened = 0;
    private ButtonWidget changeAlignment;
    private TextFieldWidget colorEntryWidget;
    private ButtonWidget zOffsetToggle;
    private ButtonWidget shadowToggle;

    public TextBlockEditScreen(TextBlockEntity textBlockEntity) {
        this.textBlockEntity = textBlockEntity;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderDarkening(context);
    }

    @Override
    public void init() {
        super.init();

        int innerPadding = width / 100;

        this.selectionManager = new SelectionManager(
                () -> this.textBlockEntity.getRawLine(this.currentRow),
                (string) -> {
                    textBlockEntity.setRawLine(this.currentRow, string);
                    this.textBlockEntity.renderDirty = true;
                },
                SelectionManager.makeClipboardGetter(this.client),
                SelectionManager.makeClipboardSetter(this.client),
                (string) -> true);

        ButtonWidget decreaseSize = ButtonWidget.builder(Text.literal("-"), action -> {
            this.textBlockEntity.scale -= (float) Math.max(0, 0.125);
            this.textBlockEntity.renderDirty = true;
        }).dimensions(80, 0, 20, 20).build();

        ButtonWidget increaseSize = ButtonWidget.builder(Text.literal("+"), action -> {
            this.textBlockEntity.scale += 0.125F;
            this.textBlockEntity.renderDirty = true;
        }).dimensions(100, 0, 20, 20).build();

        this.changeAlignment = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.alignment", this.textBlockEntity.textAlignment), action -> {
            switch (textBlockEntity.textAlignment) {
                case LEFT -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.CENTER;
                case CENTER -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.RIGHT;
                case RIGHT -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.LEFT;
            }
            this.textBlockEntity.renderDirty = true;

            this.changeAlignment.setMessage(Text.stringifiedTranslatable("gui.glowcase.alignment", this.textBlockEntity.textAlignment));
        }).dimensions(120 + innerPadding, 0, 160, 20).build();

        this.shadowToggle = ButtonWidget.builder(Text.translatable("gui.glowcase.shadow_type", this.textBlockEntity.shadowType), action -> {
            switch (textBlockEntity.shadowType) {
                case DROP -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.PLATE;
                case PLATE -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.NONE;
                case NONE -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.DROP;
            }
            this.textBlockEntity.renderDirty = true;

            this.shadowToggle.setMessage(Text.translatable("gui.glowcase.shadow_type", this.textBlockEntity.shadowType));
        }).dimensions(120 + innerPadding, 20 + innerPadding, 160, 20).build();

        this.colorEntryWidget = new TextFieldWidget(this.client.textRenderer, 280 + innerPadding * 2, 0, 50, 20, Text.empty());
        this.colorEntryWidget.setText("#" + Integer.toHexString(this.textBlockEntity.color & 0x00FFFFFF));
        this.colorEntryWidget.setChangedListener(string -> {
            TextColor.parse(this.colorEntryWidget.getText()).ifSuccess(color -> {
                this.textBlockEntity.color = color == null ? 0xFFFFFFFF : color.getRgb() | 0xFF000000;
                this.textBlockEntity.renderDirty = true;
            });
        });

        this.zOffsetToggle = ButtonWidget.builder(Text.literal(this.textBlockEntity.zOffset.name()), action -> {
            switch (textBlockEntity.zOffset) {
                case FRONT -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;
                case CENTER -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;
                case BACK -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;
            }
            this.textBlockEntity.renderDirty = true;

            this.zOffsetToggle.setMessage(Text.literal(this.textBlockEntity.zOffset.name()));
        }).dimensions(330 + innerPadding * 3, 0, 80, 20).build();

        this.addDrawableChild(increaseSize);
        this.addDrawableChild(decreaseSize);
        this.addDrawableChild(this.changeAlignment);
        this.addDrawableChild(this.shadowToggle);
        this.addDrawableChild(this.zOffsetToggle);
        this.addDrawableChild(this.colorEntryWidget);
    }

    @Override
    public void tick() {
        ++this.ticksSinceOpened;
    }

    @Override
    public void close() {
        textBlockEntity.createPacketData().send();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null) {
            super.render(context, mouseX, mouseY, delta);

            context.getMatrices().push();
            context.getMatrices().translate(0, 40 + 2 * this.width / 100F, 0);
            for (int i = 0; i < this.textBlockEntity.lines.size(); ++i) {
                var text = this.currentRow == i ? Text.literal(this.textBlockEntity.getRawLine(i)) : this.textBlockEntity.lines.get(i);

                int lineWidth = this.textRenderer.getWidth(text);
                switch (this.textBlockEntity.textAlignment) {
                    case LEFT ->
                            context.drawTextWithShadow(client.textRenderer, text, this.width / 10, i * 12, this.textBlockEntity.color);
                    case CENTER ->
                            context.drawTextWithShadow(client.textRenderer, text, this.width / 2 - lineWidth / 2, i * 12, this.textBlockEntity.color);
                    case RIGHT ->
                            context.drawTextWithShadow(client.textRenderer, text, this.width - this.width / 10 - lineWidth, i * 12, this.textBlockEntity.color);
                }
            }

            int caretStart = this.selectionManager.getSelectionStart();
            int caretEnd = this.selectionManager.getSelectionEnd();

            if (caretStart >= 0) {
                String line = this.textBlockEntity.getRawLine(this.currentRow);
                int selectionStart = MathHelper.clamp(Math.min(caretStart, caretEnd), 0, line.length());
                int selectionEnd = MathHelper.clamp(Math.max(caretStart, caretEnd), 0, line.length());

                String preSelection = line.substring(0, MathHelper.clamp(line.length(), 0, selectionStart));
                int startX = this.client.textRenderer.getWidth(preSelection);

                float push = switch (this.textBlockEntity.textAlignment) {
                    case LEFT -> this.width / 10F;
                    case CENTER -> this.width / 2F - this.textRenderer.getWidth(line) / 2F;
                    case RIGHT -> this.width - this.width / 10F - this.textRenderer.getWidth(line);
                };

                startX += (int) push;


                int caretStartY = this.currentRow * 12;
                int caretEndY = this.currentRow * 12 + 9;
                if (this.ticksSinceOpened / 6 % 2 == 0 && !this.colorEntryWidget.isActive()) {
                    if (selectionStart < line.length()) {
                        context.fill(startX, caretStartY, startX + 1, caretEndY, 0xCCFFFFFF);
                    } else {
                        context.drawText(client.textRenderer, "_", startX, this.currentRow * 12, 0xFFFFFFFF, false);
                    }
                }

                if (caretStart != caretEnd) {
                    int endX = startX + this.client.textRenderer.getWidth(line.substring(selectionStart, selectionEnd));
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), startX, caretEndY, 0.0F).color(0, 0, 255, 255);
                    bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), endX, caretEndY, 0.0F).color(0, 0, 255, 255);
                    bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), endX, caretStartY, 0.0F).color(0, 0, 255, 255);
                    bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), startX, caretStartY, 0.0F).color(0, 0, 255, 255);
                    BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                    RenderSystem.disableColorLogicOp();
                }
            }

            context.getMatrices().pop();
            context.drawTextWithShadow(client.textRenderer, Text.translatable("gui.glowcase.scale", this.textBlockEntity.scale), 7, 7, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (this.colorEntryWidget.isActive()) {
            return this.colorEntryWidget.charTyped(chr, keyCode);
        } else {
            this.selectionManager.insert(chr);
            return true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.colorEntryWidget.isActive()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.close();
                return true;
            } else {
                return this.colorEntryWidget.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            setFocused(null);
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.textBlockEntity.addRawLine(this.currentRow + 1,
                        this.textBlockEntity.getRawLine(this.currentRow).substring(
                                MathHelper.clamp(this.selectionManager.getSelectionStart(), 0, this.textBlockEntity.getRawLine(this.currentRow).length())
                        ));
                this.textBlockEntity.setRawLine(this.currentRow,
                        this.textBlockEntity.getRawLine(this.currentRow).substring(0, MathHelper.clamp(this.selectionManager.getSelectionStart(), 0, this.textBlockEntity.getRawLine(this.currentRow).length())
                        ));
                this.textBlockEntity.renderDirty = true;
                ++this.currentRow;
                this.selectionManager.moveCursorToEnd(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                this.currentRow = Math.max(this.currentRow - 1, 0);
                this.selectionManager.moveCursorToEnd(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.currentRow = Math.min(this.currentRow + 1, (this.textBlockEntity.lines.size() - 1));
                this.selectionManager.moveCursorToEnd(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && this.currentRow > 0 && this.textBlockEntity.lines.size() > 1 && this.selectionManager.getSelectionStart() == 0 && this.selectionManager.getSelectionEnd() == this.selectionManager.getSelectionStart()) {
                --this.currentRow;
                this.selectionManager.moveCursorToEnd(false);
                deleteLine();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DELETE && this.currentRow < this.textBlockEntity.lines.size() - 1 && this.selectionManager.getSelectionEnd() == this.textBlockEntity.getRawLine(this.currentRow).length()) {
                deleteLine();
                return true;
            } else {
                try {
                    return this.selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    MinecraftClient.getInstance().setScreen(null);
                    return false;
                }
            }
        }
    }

    private void deleteLine() {
        this.textBlockEntity.setRawLine(this.currentRow,
                this.textBlockEntity.getRawLine(this.currentRow) + this.textBlockEntity.getRawLine(this.currentRow + 1)
        );

        this.textBlockEntity.lines.remove(this.currentRow + 1);
        this.textBlockEntity.renderDirty = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int topOffset = (int) (40 + 2 * this.width / 100F);
        if (!this.colorEntryWidget.mouseClicked(mouseX, mouseY, button)) {
            this.colorEntryWidget.setFocused(false);
        }
        if (mouseY > topOffset) {
            this.currentRow = MathHelper.clamp((int) (mouseY - topOffset) / 12, 0, this.textBlockEntity.lines.size() - 1);
            this.setFocused(null);
            String baseContents = this.textBlockEntity.getRawLine(currentRow);
            int baseContentsWidth = this.textRenderer.getWidth(baseContents);
            int contentsStart;
            int contentsEnd;
            switch (this.textBlockEntity.textAlignment) {
                case LEFT -> {
                    contentsStart = this.width / 10;
                    contentsEnd = contentsStart + baseContentsWidth;
                }
                case CENTER -> {
                    int midpoint = this.width / 2;
                    int textMidpoint = baseContentsWidth / 2;
                    contentsStart = midpoint - textMidpoint;
                    contentsEnd = midpoint + textMidpoint;
                }
                case RIGHT -> {
                    contentsEnd = this.width - this.width / 10;
                    contentsStart = contentsEnd - baseContentsWidth;
                }
                //even though this is exhaustive, javac won't treat contentsStart and contentsEnd as initialized
                //why? who knows! just throw bc this should be impossible
                default -> throw new IllegalStateException(":HOW:");
            }

            if (mouseX <= contentsStart) {
                this.selectionManager.moveCursorToStart();
            } else if (mouseX >= contentsEnd) {
                this.selectionManager.moveCursorToEnd(false);
            } else {
                int lastWidth = 0;
                for (int i = 1; i < baseContents.length(); i++) {
                    String testContents = baseContents.substring(0, i);
                    int width = this.textRenderer.getWidth(testContents);
                    int midpointWidth = (width + lastWidth) / 2;
                    if (mouseX < contentsStart + midpointWidth) {
                        this.selectionManager.moveCursorTo(i - 1, false);
                        break;
                    } else if (mouseX <= contentsStart + width) {
                        this.selectionManager.moveCursorTo(i, false);
                        break;
                    }
                    lastWidth = width;
                }
            }
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
