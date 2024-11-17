package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ParticleDisplayBlockEntity;
import dev.hephaestus.glowcase.client.util.BlockEntityUtil;
import dev.hephaestus.glowcase.math.DeviatedInteger;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public record ParticleDisplayBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<ParticleDisplayBlockEntity> {
	public static Identifier ITEM_TEXTURE = Glowcase.id("textures/item/particle_display.png");

	public void render(ParticleDisplayBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (entity.count.equals(DeviatedInteger.ZERO) || BlockEntityUtil.shouldRenderPlaceholder(entity.getPos())) BlockEntityUtil.renderPlaceholder(entity.getCachedState(), ITEM_TEXTURE, matrices, vertexConsumers);
	}
}
