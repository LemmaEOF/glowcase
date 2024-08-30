package dev.hephaestus.glowcase.client;

import dev.hephaestus.glowcase.GlowcaseCommonProxy;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.ItemDisplayBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.SpriteBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.util.math.BlockPos;

public class GlowcaseClientProxy extends GlowcaseCommonProxy {
	@Override
	public void openHyperlinkBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof HyperlinkBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new HyperlinkBlockEditScreen(be));
		}
	}

	@Override
	public void openUrlWithConfirmation(String url) {
		ConfirmLinkScreen.open(MinecraftClient.getInstance().currentScreen, url);
	}

	@Override
	public void openItemDisplayBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof ItemDisplayBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new ItemDisplayBlockEditScreen(be));
		}
	}

	@Override
	public void openTextBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof TextBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new TextBlockEditScreen(be));
		}
	}

	@Override
	public void openSpriteBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof SpriteBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new SpriteBlockEditScreen(be));
		}
	}
}
