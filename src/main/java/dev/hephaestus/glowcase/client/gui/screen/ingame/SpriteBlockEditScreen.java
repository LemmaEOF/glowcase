package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.packet.C2SEditSpriteBlock;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpriteBlockEditScreen extends GlowcaseScreen {
	private final SpriteBlockEntity spriteBlockEntity;

	private ButtonWidget zOffsetToggle;
	private ButtonWidget rotationWidget;
	private TextFieldWidget spriteWidget;

	public SpriteBlockEditScreen(SpriteBlockEntity spriteBlockEntity) {
		this.spriteBlockEntity = spriteBlockEntity;
	}

	@Override
	public void init() {
		super.init();

		if (this.client == null) return;

		this.zOffsetToggle = ButtonWidget.builder(Text.literal(this.spriteBlockEntity.zOffset.name()), action -> {
			switch (spriteBlockEntity.zOffset) {
				case FRONT -> spriteBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;
				case CENTER -> spriteBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;
				case BACK -> spriteBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;
			}

			this.zOffsetToggle.setMessage(Text.literal(this.spriteBlockEntity.zOffset.name()));
			//GlowcaseClientNetworking.editArrowBlock(spriteBlockEntity);
		}).dimensions(width / 2 - 75, height / 2 - 40, 150, 20).build();

		this.rotationWidget = ButtonWidget.builder(Text.literal("Rotate"), (action) -> {
			this.spriteBlockEntity.rotation += 45;
			if (this.spriteBlockEntity.rotation >= 360) {
				this.spriteBlockEntity.rotation = 0;
			}
			//GlowcaseClientNetworking.editArrowBlock(spriteBlockEntity);
		}).dimensions(width / 2 - 75, height / 2 - 10, 150, 20).build();

		this.spriteWidget = new TextFieldWidget(this.client.textRenderer, width / 2 - 75, height / 2 + 20, 150, 20, Text.empty());
		this.spriteWidget.setText(spriteBlockEntity.sprite);
		this.spriteWidget.setChangedListener(string -> {
			if (Identifier.isPathValid(this.spriteWidget.getText())) {
				this.spriteBlockEntity.sprite = this.spriteWidget.getText();
				//this.spriteBlockEntity.renderDirty = true;
			}
		});

		this.addDrawableChild(this.zOffsetToggle);
		this.addDrawableChild(this.rotationWidget);
		this.addDrawableChild(this.spriteWidget);
	}

	@Override
	public void close() {
		spriteBlockEntity.setSprite(spriteWidget.getText());
		C2SEditSpriteBlock.of(spriteBlockEntity).send();
		super.close();
	}
}
