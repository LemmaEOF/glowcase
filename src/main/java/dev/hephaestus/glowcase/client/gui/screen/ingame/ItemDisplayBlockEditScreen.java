package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

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

            this.givesItemButtom = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.gives_item", this.displayBlock.givesItem), (action) -> {
                this.displayBlock.cycleGiveType();
                this.givesItemButtom.setMessage(Text.stringifiedTranslatable("gui.glowcase.gives_item", this.displayBlock.givesItem));
                editItemDisplayBlock(displayBlock, true);
            }).dimensions(centerW - 75, centerH - 40 - individualPadding, 150, 20).build();

            this.rotationTypeButton = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.rotation_type", this.displayBlock.rotationType), (action) -> {
                this.displayBlock.cycleRotationType(this.client.player);
                this.rotationTypeButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.rotation_type", this.displayBlock.rotationType));
                editItemDisplayBlock(displayBlock, true);
            }).dimensions(centerW - 75, centerH - 20, 150, 20).build();

            this.showNameButton = ButtonWidget.builder(Text.translatable("gui.glowcase.show_name", this.displayBlock.showName), (action) -> {
                this.displayBlock.showName = !this.displayBlock.showName;
                this.showNameButton.setMessage(Text.translatable("gui.glowcase.show_name", this.displayBlock.showName));
                editItemDisplayBlock(displayBlock, false);
            }).dimensions(centerW - 75, centerH + individualPadding, 150, 20).build();

            this.offsetButton = ButtonWidget.builder(Text.stringifiedTranslatable("gui.glowcase.offset", this.displayBlock.offset), (action) -> {
                this.displayBlock.cycleOffset();
                this.offsetButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.offset", this.displayBlock.offset));
                editItemDisplayBlock(displayBlock, true);
            }).dimensions(centerW - 75, centerH + 20 + padding, 150, 20).build();

            this.addDrawableChild(this.givesItemButtom);
            this.addDrawableChild(this.rotationTypeButton);
            this.addDrawableChild(this.showNameButton);
            this.addDrawableChild(this.offsetButton);
        }
    }

    private void editItemDisplayBlock(ItemDisplayBlockEntity be, boolean updatePitchAndYaw) {
        if (updatePitchAndYaw && MinecraftClient.getInstance().getCameraEntity() != null) {
            Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(MinecraftClient.getInstance().getCameraEntity(), be.getPos());
            be.pitch = pitchAndYaw.x;
            be.yaw = pitchAndYaw.y;
        }
        be.createPacketData().send();
    }
}