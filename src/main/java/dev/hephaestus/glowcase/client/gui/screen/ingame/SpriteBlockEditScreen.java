package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.packet.C2SEditSpriteBlock;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public class SpriteBlockEditScreen extends GlowcaseScreen {
	private final SpriteBlockEntity spriteBlockEntity;

	private TextFieldWidget spriteWidget;
	private ButtonWidget rotationWidget;
	private ButtonWidget zOffsetToggle;
	private TextFieldWidget colorEntryWidget;

	public SpriteBlockEditScreen(SpriteBlockEntity spriteBlockEntity) {
		this.spriteBlockEntity = spriteBlockEntity;
	}

	@Override
	public void init() {
		super.init();

		if (this.client == null) return;

		this.spriteWidget = new TextFieldWidget(this.client.textRenderer, width / 2 - 75, height / 2 - 55, 150, 20, Text.empty());
		this.spriteWidget.setText(spriteBlockEntity.sprite);
		this.spriteWidget.setChangedListener(string -> {
			if (Identifier.isPathValid(this.spriteWidget.getText())) {
				this.spriteBlockEntity.sprite = this.spriteWidget.getText();
			}
		});

		this.rotationWidget = ButtonWidget.builder(Text.translatable("gui.glowcase.rotate"), (action) -> {
			this.spriteBlockEntity.rotation += 45;
			if (this.spriteBlockEntity.rotation >= 360) {
				this.spriteBlockEntity.rotation = 0;
			}
		}).dimensions(width / 2 - 75, height / 2 - 25, 150, 20).build();

		this.zOffsetToggle = ButtonWidget.builder(Text.literal(this.spriteBlockEntity.zOffset.name()), action -> {
			switch (spriteBlockEntity.zOffset) {
				case FRONT -> spriteBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;
				case CENTER -> spriteBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;
				case BACK -> spriteBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;
			}

			this.zOffsetToggle.setMessage(Text.literal(this.spriteBlockEntity.zOffset.name()));
		}).dimensions(width / 2 - 75, height / 2 + 5, 150, 20).build();

		this.colorEntryWidget = new TextFieldWidget(this.client.textRenderer, width / 2 - 75, height / 2 + 35, 150, 20, Text.empty());
		this.colorEntryWidget.setText("#" + String.format("%1$06X", this.spriteBlockEntity.color & 0x00FFFFFF));
		this.colorEntryWidget.setChangedListener(string -> {
			TextColor.parse(this.colorEntryWidget.getText()).ifSuccess(color -> {
				this.spriteBlockEntity.color = color == null ? 0xFFFFFFFF : color.getRgb() | 0xFF000000;
			});
		});

		this.addDrawableChild(this.spriteWidget);
		this.addDrawableChild(this.rotationWidget);
		this.addDrawableChild(this.zOffsetToggle);
		this.addDrawableChild(this.colorEntryWidget);
	}

	@Override
	public void close() {
		spriteBlockEntity.setSprite(spriteWidget.getText());
		C2SEditSpriteBlock.of(spriteBlockEntity).send();
		super.close();
	}
}
