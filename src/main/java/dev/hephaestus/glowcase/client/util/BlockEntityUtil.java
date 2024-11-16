package dev.hephaestus.glowcase.client.util;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
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
import org.joml.Vector3f;

public class BlockEntityUtil {
	private static final Vector3f[] placeholderVertices = new Vector3f[] {
		new Vector3f(0, 0, 0.5F),
		new Vector3f(1, 0, 0.5F),
		new Vector3f(1, 1, 0.5F),
		new Vector3f(0, 1, 0.5F)
	};

	public static void renderPlaceholder(BlockState state, Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		if (state.contains(Properties.ROTATION)) {
			float rotation = -(state.get(Properties.ROTATION) * 360) / 16.0F;
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
		}
		var entry = matrices.peek();
		var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));
		placeholderVertex(entry, vertexConsumer, placeholderVertices[0], 0, 1);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[1], 1, 1);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[2], 1, 0);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[3], 0, 0);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[3], 0, 0);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[2], 1, 0);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[1], 1, 1);
		placeholderVertex(entry, vertexConsumer, placeholderVertices[0], 0, 1);
	}

	public static boolean shouldRenderPlaceholder(BlockPos pos) {
		return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isHolding(stack -> stack.isIn(Glowcase.ITEM_TAG)) && !(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr && bhr.getBlockPos().equals(pos));
	}

	private static void placeholderVertex(
		MatrixStack.Entry matrix, VertexConsumer vertexConsumer, Vector3f vertex, float u, float v) {
		vertexConsumer.vertex(matrix, vertex.x(), vertex.y(), vertex.z())
			.color(0xFFFFFFFF)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(0, 1, 0);
	}
}
