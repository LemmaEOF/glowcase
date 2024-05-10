package net.modfest.glowcase.client.render.block.entity;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.modfest.glowcase.block.entity.TextBlockEntity;
import net.modfest.glowcase.client.GlowcaseRenderLayers;
import org.joml.Matrix4f;

public class TextBlockEntityRenderer extends BakedBlockEntityRenderer<TextBlockEntity> {

	public TextBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public boolean shouldBake(TextBlockEntity entity) {
		return !entity.lines.isEmpty();
	}

	@Override
	public void renderUnbaked(TextBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (entity.renderDirty) {
			entity.renderDirty = false;
			Manager.markForRebuild(entity.getPos());
		}
	}

	@Override
	public void renderBaked(TextBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		float rotation = -(blockEntity.getCachedState().get(Properties.ROTATION) * 360) / 16.0f;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
		matrices.translate(0.0, 0.0, blockEntity.zOffset);
		float scale = 0.010416667f * blockEntity.scale;
		matrices.scale(scale, -scale, scale);
		var textRenderer = context.getTextRenderer();
		double maxLength = 0;
		for (int i = 0; i < blockEntity.lines.size(); ++i) {
			maxLength = Math.max(maxLength, textRenderer.getWidth(blockEntity.lines.get(i)));
		}
		matrices.translate(0, -((blockEntity.lines.size() - 0.25) * 12) / 2.0, 0.0);
		for (int i = 0; i < blockEntity.lines.size(); ++i) {
			renderLine(blockEntity, matrices, vertexConsumers, textRenderer, i, maxLength);
		}
		matrices.pop();
	}

	public static void renderLine(TextBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, TextRenderer textRenderer, int index, double maxLength) {
		double width = textRenderer.getWidth(blockEntity.lines.get(index));
		double lineXOffset = switch (blockEntity.textAlignment) {
			case LEFT -> -maxLength / 2.0;
			case CENTER -> (maxLength - width) / 2.0 - maxLength / 2.0;
			case RIGHT -> maxLength - width - maxLength / 2.0;
		};
		matrices.push();
		matrices.translate(lineXOffset, 0, 0);
		if (blockEntity.shadowType == TextBlockEntity.ShadowType.PLATE && width > 0) {
			matrices.translate(0.0, 0.0, -0.025);
			renderPlate(matrices, vertexConsumers, (int) width + 5, (index + 1) * 12 - 2, -5, index * 12 - 2, 0x44000000);
			matrices.translate(0.0, 0.0, 0.025);
		}
		textRenderer.draw(blockEntity.lines.get(index), 0, index * 12, 0xFFFFFFFF, blockEntity.shadowType == TextBlockEntity.ShadowType.DROP, matrices.peek().getPositionMatrix(), vertexConsumers, TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		matrices.pop();
	}

	@SuppressWarnings("SameParameterValue")
	public static void renderPlate(MatrixStack matrices, VertexConsumerProvider vcp, int x1, int y1, int x2, int y2, int color) {
		float red = (float) (color >> 16 & 255) / 255.0f;
		float green = (float) (color >> 8 & 255) / 255.0f;
		float blue = (float) (color & 255) / 255.0f;
		float alpha = (float) (color >> 24 & 255) / 255.0f;
		var consumer = vcp.getBuffer(GlowcaseRenderLayers.TEXT_PLATE);
		var matrix = matrices.peek().getPositionMatrix();
		consumer.vertex(matrix, x1, y2, 0.0f).color(red, green, blue, alpha).next();
		consumer.vertex(matrix, x2, y2, 0.0f).color(red, green, blue, alpha).next();
		consumer.vertex(matrix, x2, y1, 0.0f).color(red, green, blue, alpha).next();
		consumer.vertex(matrix, x1, y1, 0.0f).color(red, green, blue, alpha).next();
	}
}
