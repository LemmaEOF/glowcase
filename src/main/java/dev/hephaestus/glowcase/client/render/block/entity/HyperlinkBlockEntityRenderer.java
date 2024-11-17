package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public record HyperlinkBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<HyperlinkBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/hyperlink_block.png");

	public void render(HyperlinkBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Camera camera = context.getRenderDispatcher().camera;
		Quaternionf rotation = RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()).mul(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		BlockEntityUtil.renderPlaceholder(entity.getCachedState(), ITEM_TEXTURE, rotation, matrices, vertexConsumers);

		matrices.push();
		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if (hitResult instanceof BlockHitResult && ((BlockHitResult) hitResult).getBlockPos().equals(entity.getPos())) {
			float scale = 0.025F;
			matrices.scale(scale, scale, scale);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
			matrices.translate(-context.getTextRenderer().getWidth(entity.getText()) / 2F, -4, -scale);
			// Fixes shadow being rendered in front of actual text
			matrices.scale(1, 1, -1);
			context.getTextRenderer().draw(entity.getText(), 0, 0, 0xFFFFFF, true, matrices.peek().getPositionMatrix(), vertexConsumers, TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		}
		matrices.pop();
	}
}
