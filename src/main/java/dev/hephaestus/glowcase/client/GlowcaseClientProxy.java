package dev.hephaestus.glowcase.client;

import dev.hephaestus.glowcase.GlowcaseCommonProxy;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.OutlineBlockEntity;
import dev.hephaestus.glowcase.block.entity.PopupBlockEntity;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.ItemDisplayBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.OutlineBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.PopupBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.PopupBlockViewScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.SpriteBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import dev.hephaestus.glowcase.block.entity.*;
import dev.hephaestus.glowcase.client.gui.screen.ingame.*;
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
	public void openPopupBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof PopupBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new PopupBlockEditScreen(be));
		}
	}

	@Override
	public void openPopupBlockViewScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof PopupBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new PopupBlockViewScreen(be));
		}
	}

	@Override
	public void openSpriteBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof SpriteBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new SpriteBlockEditScreen(be));
		}
	}

	@Override
	public void openOutlineBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof OutlineBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new OutlineBlockEditScreen(be));
		}
	}

	@Override
	public void openParticleDisplayBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof ParticleDisplayBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new ParticleDisplayEditScreen(be));
		}
	}

	@Override
	public void openSoundBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof SoundPlayerBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new SoundPlayerBlockEditScreen(be));
		}
	}

	@Override
	public void openItemAcceptorBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof ItemAcceptorBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new ItemAcceptorBlockEditScreen(be));
		}
	}

	@Override
	public void openEntityDisplayBlockEditScreen(BlockPos pos) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getBlockEntity(pos) instanceof EntityDisplayBlockEntity be) {
			MinecraftClient.getInstance().setScreen(new EntityDisplayEditScreen(be));
		}
	}
}
