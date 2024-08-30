package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Colors;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public record SpriteBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<SpriteBlockEntity> {
	private static final Vector3f[] vertices = new Vector3f[] {
		new Vector3f(-0.5F, -0.5F, 0.0F),
		new Vector3f(0.5F, -0.5F, 0.0F),
		new Vector3f(0.5F, 0.5F, 0.0F),
		new Vector3f(-0.5F, 0.5F, 0.0F)
	};

	public void render(SpriteBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);

		float rotation = -(entity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
		matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(entity.rotation));

		switch (entity.zOffset) {
			case FRONT -> matrices.translate(0D, 0D, 0.4D);
			case BACK -> matrices.translate(0D, 0D, -0.4D);
		}

		var entry = matrices.peek();
		var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(Glowcase.id("textures/sprite/" + entity.sprite + ".png")));

		vertex(entry, vertexConsumer, vertices[0], 0, 1);
		vertex(entry, vertexConsumer, vertices[1], 1, 1);
		vertex(entry, vertexConsumer, vertices[2], 1, 0);
		vertex(entry, vertexConsumer, vertices[3], 0, 0);

		matrices.pop();
	}

	private void vertex(
		MatrixStack.Entry matrix, VertexConsumer vertexConsumer, Vector3f vertex, float u, float v
	) {
		vertexConsumer.vertex(matrix, vertex.x(), vertex.y(), vertex.z())
			.color(Colors.WHITE)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(0, 1, 0);
	}
}
