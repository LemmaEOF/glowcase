package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.block.entity.WireframeBlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public record WireframeBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<WireframeBlockEntity> {
	public void render(WireframeBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
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
	}
}
