package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.EntityDisplayBlockEntity;
import dev.hephaestus.glowcase.packet.C2SEditEntityDisplayBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class EntityDisplayEditScreen extends GlowcaseScreen {
	private final EntityDisplayBlockEntity displayBlock;

	private ButtonWidget givesItemButton;
	private ButtonWidget rotationTypeButton;
	private ButtonWidget showNameButton;
	private ButtonWidget offsetButton;
	private ButtonWidget tickEntityButton;

	private int scaleTextX = 0;
	private int scaleTextY = 0;

	public EntityDisplayEditScreen(EntityDisplayBlockEntity displayBlock) {
		this.displayBlock = displayBlock;
	}

	@Override
	public void init() {
		super.init();

		if (this.client != null) {
			int padding = width / 100;
			int individualPadding = padding / 2;
			int centerW = width / 2;
			int centerH = height / 2;

			this.givesItemButton = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.gives_item", this.displayBlock.givesItem), (action) -> {
				this.displayBlock.cycleGiveType();
				this.givesItemButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.gives_item", this.displayBlock.givesItem));
				editItemDisplayBlock(true);
			}).dimensions(centerW - 75, centerH - 60 - individualPadding * 2, 150, 20).build();

			this.rotationTypeButton = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.rotation_type", this.displayBlock.rotationType), (action) -> {
				this.displayBlock.cycleRotationType(this.client.player);
				this.rotationTypeButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.rotation_type", this.displayBlock.rotationType));
				editItemDisplayBlock(true);
			}).dimensions(centerW - 75, centerH - 40 - individualPadding, 150, 20).build();

			this.showNameButton = ButtonWidget.builder(Text.translatable("gui.glowcase.show_name", this.displayBlock.showName), (action) -> {
				this.displayBlock.showName = !this.displayBlock.showName;
				this.showNameButton.setMessage(Text.translatable("gui.glowcase.show_name", this.displayBlock.showName));
				editItemDisplayBlock(false);
			}).dimensions(centerW - 75, centerH - 20, 150, 20).build();

			this.offsetButton = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.offset_value", this.displayBlock.offset), (action) -> {
				this.displayBlock.cycleOffset();
				this.offsetButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.offset_value", this.displayBlock.offset));
				editItemDisplayBlock(true);
			}).dimensions(centerW - 75, centerH + individualPadding, 150, 20).build();

			this.tickEntityButton = ButtonWidget.builder(Text.translatable("gui.glowcase.tick_entity", this.displayBlock.tickEntity), (action) -> {
				this.displayBlock.tickEntity = !this.displayBlock.tickEntity;
				this.tickEntityButton.setMessage(Text.translatable("gui.glowcase.tick_entity", this.displayBlock.tickEntity));
				editItemDisplayBlock(false);
			}).dimensions(centerW - 75, centerH + 20 + padding, 150, 20).build();

			this.scaleTextX = centerW - 75;
//			this.scaleTextY = centerH + 40 + padding * 2;
			this.scaleTextY = centerH + 20 + padding; //adjust to no tick entity button

			ButtonWidget decreaseSize = ButtonWidget.builder(Text.literal("-"), action -> {
				if(this.displayBlock.displayScale > -3) {
					this.displayBlock.displayScale -= (float) Math.max(0, 0.125);
					editItemDisplayBlock(true);
				}
			}).dimensions(scaleTextX + 110, scaleTextY, 20, 20).build();
			//150 (button length) - 40 (change size button length)

			ButtonWidget increaseSize = ButtonWidget.builder(Text.literal("+"), action -> {
				if(this.displayBlock.displayScale < 3) {
					this.displayBlock.displayScale += 0.125F;
					editItemDisplayBlock(true);
				}
			}).dimensions(scaleTextX + 132, scaleTextY, 20, 20).build();
			//150 (button length) - 20 (change size button length) + 2 (padding)

			this.addDrawableChild(this.givesItemButton);
			this.addDrawableChild(this.rotationTypeButton);
			this.addDrawableChild(this.showNameButton);
			this.addDrawableChild(this.offsetButton);
//			this.addDrawableChild(this.tickEntityButton);
			this.addDrawableChild(decreaseSize);
			this.addDrawableChild(increaseSize);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(client.textRenderer, Text.translatable("gui.glowcase.scale_value", this.displayBlock.displayScale), scaleTextX, scaleTextY + 6, 0xFFFFFFFF);
	}

	private void editItemDisplayBlock(boolean updatePitchAndYaw) {
		if (updatePitchAndYaw && MinecraftClient.getInstance().getCameraEntity() != null) {
			Vec2f pitchAndYaw = EntityDisplayBlockEntity.getPitchAndYaw(MinecraftClient.getInstance().getCameraEntity(), displayBlock, 0);
			displayBlock.pitch = pitchAndYaw.x;
			displayBlock.yaw = pitchAndYaw.y;
		}
		C2SEditEntityDisplayBlock.of(displayBlock).send();
	}
}
