package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.OutlineBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityRenderUtil;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

public record OutlineBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<OutlineBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/outline_block.png");

	public void render(OutlineBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		double x = entity.offset.getX();
		double y = entity.offset.getY();
		double z = entity.offset.getZ();
		double width = entity.scale.getX();
		double height = entity.scale.getY();
		double depth = entity.scale.getZ();
		float red = ((entity.color >> 16) & 0xFF) / 255f;
		float green = ((entity.color >> 8) & 0xFF) / 255f;
		float blue = (entity.color & 0xFF) / 255f;

		WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), x, y, z, x + width, y + height, z + depth, red, green, blue, 1);

		if (entity.scale.equals(Vec3i.ZERO) || BlockEntityRenderUtil.shouldRenderPlaceholder(entity.getPos())) BlockEntityRenderUtil.renderPlaceholder(entity, ITEM_TEXTURE, 1.0F, matrices, vertexConsumers, context.getRenderDispatcher().camera);
	}
}
