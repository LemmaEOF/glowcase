package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SoundPlayerBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public record SoundPlayerBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<SoundPlayerBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/sound_block.png");

	@Override
	public void render(SoundPlayerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		//if (BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos()))
			BlockEntityRenderUtil.renderPlaceholder(entity, ITEM_TEXTURE, 1.0F, matrices, vertexConsumers, context.getRenderDispatcher().camera);
	}
}
