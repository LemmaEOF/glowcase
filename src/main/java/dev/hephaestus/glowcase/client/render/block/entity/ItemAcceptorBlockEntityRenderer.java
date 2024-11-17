package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.ItemAcceptorBlock;
import dev.hephaestus.glowcase.block.entity.ItemAcceptorBlockEntity;
import dev.hephaestus.glowcase.mixin.client.RenderSystemAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record ItemAcceptorBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<ItemAcceptorBlockEntity> {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final Quaternionf ITEM_LIGHT_ROTATION_3D = RotationAxis.POSITIVE_X.rotationDegrees(-15).mul(RotationAxis.POSITIVE_Y.rotationDegrees(15));
	private static final Quaternionf ITEM_LIGHT_ROTATION_FLAT = RotationAxis.POSITIVE_X.rotationDegrees(-45);

	public void render(ItemAcceptorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Entity camera = mc.getCameraEntity();

		if (camera == null) return;

		ItemRenderer itemRenderer = context.getItemRenderer();
		BakedModel itemModel = itemRenderer.getModel(entity.getDisplayItemStack(), entity.getWorld(), null, 0);

		// Render item
		float yaw = 0;
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		BlockState blockState = entity.getWorld().getBlockState(entity.getPos());
		if (blockState.isOf(Glowcase.ITEM_ACCEPTOR_BLOCK.get())) {
			yaw = getRotationYForSide2D(blockState.get(ItemAcceptorBlock.FACING));
		}
		matrices.peek().getPositionMatrix().mul(new Matrix4f().rotateY(yaw).translate(-0.125f, 0.125f, 0.51F).scale(1, 1, 0.01F));
		matrices.scale(0.5F, 0.5F, 0.5F);

		Vector3f[] lights = new Vector3f[2];
		System.arraycopy(RenderSystemAccessor.getShaderLightDirections(), 0, lights, 0, 2);

		if (itemModel.isSideLit()) {
			matrices.peek().getNormalMatrix().rotate(ITEM_LIGHT_ROTATION_3D);
			DiffuseLighting.enableGuiDepthLighting();
		} else {
			matrices.peek().getNormalMatrix().rotate(ITEM_LIGHT_ROTATION_FLAT);
			DiffuseLighting.disableGuiDepthLighting();
		}

		itemRenderer.renderItem(entity.getDisplayItemStack(), ModelTransformationMode.GUI, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);

		System.arraycopy(lights, 0, RenderSystemAccessor.getShaderLightDirections(), 0, 2);

		// Render count
		if (entity.count > 1) {
			float scale = 0.0625F;
			matrices.translate(0, 0, 1);
			matrices.scale(scale, -scale, scale);

			TextRenderer textRenderer = context.getTextRenderer();
			String string = String.valueOf(entity.count);
			textRenderer.draw(string, 9 - textRenderer.getWidth(string), 1, Colors.WHITE, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		}

		matrices.pop();
	}

	private static float getRotationYForSide2D(Direction side) {
		return -side.asRotation() * (float) Math.PI / 180f;
	}
}
