package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.networking.ItemDisplayBlockChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ItemDisplayBlockEditScreen extends GlowcaseScreen {
	private final ItemDisplayBlockEntity displayBlock;

	private ButtonWidget givesItemButtom;
	private ButtonWidget rotationTypeButton;
	private ButtonWidget showNameButton;
	private ButtonWidget offsetButton;

	public ItemDisplayBlockEditScreen(ItemDisplayBlockEntity displayBlock) {
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

			this.givesItemButtom = new ButtonWidget(centerW - 75, centerH - 40 - individualPadding, 150, 20, Text.translatable("gui.glowcase.gives_item", this.displayBlock.givesItem), (action) -> {
				this.displayBlock.cycleGiveType();
				this.givesItemButtom.setMessage(Text.translatable("gui.glowcase.gives_item", this.displayBlock.givesItem));
				ItemDisplayBlockChannel.sync(this.displayBlock, true);
			});

			this.rotationTypeButton = new ButtonWidget(centerW - 75, centerH - 20, 150, 20, Text.translatable("gui.glowcase.rotation_type", this.displayBlock.rotationType), (action) -> {
				this.displayBlock.cycleRotationType(this.client.player);
				this.rotationTypeButton.setMessage(Text.translatable("gui.glowcase.rotation_type", this.displayBlock.rotationType));
				ItemDisplayBlockChannel.sync(this.displayBlock, true);
			});

			this.showNameButton = new ButtonWidget(centerW - 75, centerH + individualPadding, 150, 20, Text.translatable("gui.glowcase.show_name", this.displayBlock.showName), (action) -> {
				this.displayBlock.showName = !this.displayBlock.showName;
				this.showNameButton.setMessage(Text.translatable("gui.glowcase.show_name", this.displayBlock.showName));
				ItemDisplayBlockChannel.sync(this.displayBlock, false);
			});

			this.offsetButton = new ButtonWidget(centerW - 75, centerH + 20 + padding, 150, 20, Text.translatable("gui.glowcase.offset", this.displayBlock.offset), (action) -> {
				this.displayBlock.cycleOffset();
				this.offsetButton.setMessage(Text.translatable("gui.glowcase.offset", this.displayBlock.offset));
				ItemDisplayBlockChannel.sync(this.displayBlock, true);
			});

			this.addDrawableChild(this.givesItemButtom);
			this.addDrawableChild(this.rotationTypeButton);
			this.addDrawableChild(this.showNameButton);
			this.addDrawableChild(this.offsetButton);
		}
	}
}