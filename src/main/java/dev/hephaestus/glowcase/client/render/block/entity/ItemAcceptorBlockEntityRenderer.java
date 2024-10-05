package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.block.entity.ItemAcceptorBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;

public record ItemAcceptorBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<ItemAcceptorBlockEntity>
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	public void render(ItemAcceptorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Entity camera = mc.getCameraEntity();

		if (camera == null) return;

		// Render item
		float yaw = (float) Math.toRadians(Util.getMeasuringTimeMs() / 1000f * 57.595f);
		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
		matrices.scale(0.5F, 0.5F, 0.5F);
		context.getItemRenderer().renderItem(entity.getDisplayItemStack(), ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);

		// Render count
		Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(camera, entity.getPos(), tickDelta);
		float scale = 0.0375F;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(pitchAndYaw.y - yaw));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
		matrices.translate(0, 0.75, 0);
		matrices.scale(scale, scale, scale);

		Text count = Text.literal(String.valueOf(entity.count));
		matrices.translate(-context.getTextRenderer().getWidth(count) / 2F, -4, 0);
		context.getTextRenderer().draw(count, 0, 0, Colors.WHITE, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);

		matrices.pop();
	}
}
