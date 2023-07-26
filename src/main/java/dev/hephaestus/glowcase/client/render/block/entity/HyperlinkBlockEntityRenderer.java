package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3f;

public record HyperlinkBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<HyperlinkBlockEntity> {
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	public static final ItemStack STACK = new ItemStack(Glowcase.HYPERLINK_BLOCK_ITEM);

	public void render(HyperlinkBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Camera camera = context.getRenderDispatcher().camera;
		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);
		matrices.scale(0.5F, 0.5F, 0.5F);
		float n = -camera.getYaw();
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(n));
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
		MinecraftClient.getInstance().getItemRenderer().renderItem(STACK, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

		HitResult hitResult = mc.crosshairTarget;
		if (hitResult instanceof BlockHitResult && ((BlockHitResult) hitResult).getBlockPos().equals(entity.getPos())) {
			float scale = 0.025F;
			matrices.scale(scale, scale, scale);
			matrices.translate(-context.getTextRenderer().getWidth(entity.url) / 2F, -4, 0);
			context.getTextRenderer().drawWithShadow(matrices, entity.url, 0, 0, 0xFFFFFF);
		}
		matrices.pop();
	}
}
