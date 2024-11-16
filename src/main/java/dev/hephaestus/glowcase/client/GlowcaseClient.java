package dev.hephaestus.glowcase.client;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.HyperlinkBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.ItemAcceptorBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.ItemDisplayBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.OutlineBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.PopupBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.SpriteBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.TextBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class GlowcaseClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Glowcase.proxy = new GlowcaseClientProxy();

		BlockEntityRendererFactories.register(Glowcase.TEXT_BLOCK_ENTITY.get(), TextBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.HYPERLINK_BLOCK_ENTITY.get(), HyperlinkBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY.get(), ItemDisplayBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.POPUP_BLOCK_ENTITY.get(), PopupBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.SPRITE_BLOCK_ENTITY.get(), SpriteBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.OUTLINE_BLOCK_ENTITY.get(), OutlineBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.ITEM_ACCEPTOR_BLOCK_ENTITY.get(), ItemAcceptorBlockEntityRenderer::new);

		WorldRenderEvents.AFTER_TRANSLUCENT.register(BakedBlockEntityRenderer.Manager::render);
		InvalidateRenderStateCallback.EVENT.register(BakedBlockEntityRenderer.Manager::reset);

		BlockRenderLayerMap.INSTANCE.putBlock(Glowcase.ITEM_ACCEPTOR_BLOCK.get(), RenderLayer.getCutout());
	}
}
