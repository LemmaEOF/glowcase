package dev.hephaestus.glowcase.client.util;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BlockEntityRenderUtil {
	private static final Vector3f[] placeholderVertices = new Vector3f[] {
		new Vector3f(-0.5F, -0.5F, 0.0F),
		new Vector3f(0.5F, -0.5F, 0.0F),
		new Vector3f(0.5F, 0.5F, 0.0F),
		new Vector3f(-0.5F, 0.5F, 0.0F)
	};

	public static void renderPlaceholder(BlockEntity entity, Identifier texture, float scale, Quaternionf rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		if (entity.getCachedState().contains(Properties.ROTATION)) {
			float blockRotation = -(entity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(blockRotation));
		}
		matrices.multiply(rotation);
		matrices.scale(scale, scale, scale);
		var entry = matrices.peek();
		var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
		var color = MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && bhr.getBlockPos().equals(entity.getPos()) ? 0x808080 : 0xFFFFFF;
		placeholderVertex(entry, vertexConsumer, placeholderVertices[0], 0, 1, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[1], 1, 1, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[2], 1, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[3], 0, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[3], 0, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[2], 1, 0, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[1], 1, 1, color);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[0], 0, 1, color);
		matrices.pop();
	}

	public static void renderPlaceholder(BlockEntity entity, Identifier texture, float scale, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		renderPlaceholder(entity, texture, scale, RotationAxis.POSITIVE_Y.rotationDegrees(0), matrices, vertexConsumers);
	}

	public static boolean shouldRenderPlaceholder(BlockPos pos) {
		return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isHolding(stack -> stack.isIn(Glowcase.ITEM_TAG)) && !(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && bhr.getBlockPos().equals(pos));
	}

	private static void placeholderVertex(
		MatrixStack.Entry matrix, VertexConsumer vertexConsumer, Vector3f vertex, float u, float v, int color) {
		vertexConsumer.vertex(matrix, vertex.x(), vertex.y(), vertex.z())
			.color(color)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(0, 1, 0);
	}
}
