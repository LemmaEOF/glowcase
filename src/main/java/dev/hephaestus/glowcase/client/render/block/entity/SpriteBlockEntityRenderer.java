package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.mixin.client.TextureManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.io.FileNotFoundException;

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
		Identifier identifier = Identifier.tryParse(Glowcase.MODID, "textures/sprite/" + entity.sprite + ".png");
		if (identifier == null) {
			identifier = Glowcase.id("textures/sprite/invalid.png");
		} else {
			TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
			ResourceManager resourceManager = ((TextureManagerAccessor) textureManager).glowcase$getResourceManager();
			try {
				resourceManager.getResourceOrThrow(identifier);
			} catch (FileNotFoundException ignored) {
				/* if the texture (file) does not exist, just replace it.
				this happens a lot when edit a sprite block, so I'm adding it to avoid log spam
				- SkyNotTheLimit
				 */
				identifier = Glowcase.id("textures/sprite/invalid.png");
			}
		}
		var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(identifier));

		vertex(entry, vertexConsumer, vertices[0], 0, 1, entity.color);
		vertex(entry, vertexConsumer, vertices[1], 1, 1, entity.color);
		vertex(entry, vertexConsumer, vertices[2], 1, 0, entity.color);
		vertex(entry, vertexConsumer, vertices[3], 0, 0, entity.color);

		matrices.pop();
	}

	private void vertex(
		MatrixStack.Entry matrix, VertexConsumer vertexConsumer, Vector3f vertex, float u, float v,
		int color) {
		vertexConsumer.vertex(matrix, vertex.x(), vertex.y(), vertex.z())
			.color(color)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(0, 1, 0);
	}
}
