package dev.hephaestus.glowcase.client;

import dev.hephaestus.glowcase.GlowcaseCommonProxy;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.ItemDisplayBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import dev.hephaestus.glowcase.mixin.client.MinecraftClientAccessor;
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
        ConfirmLinkScreen.open(url, MinecraftClient.getInstance().currentScreen, false);
    }

    @Override
    public void openItemDisplayBlockEditScreen(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.getBlockEntity(pos) instanceof ItemDisplayBlockEntity be) {
            MinecraftClient.getInstance().setScreen(new ItemDisplayBlockEditScreen(be));
        }
    }

    @Override
    public void prefillMailboxChat(BlockPos pos) {
        ((MinecraftClientAccessor) MinecraftClient.getInstance())
                .invokeOpenChatScreen(String.format("/mail %d %d %d ", pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public void openTextBlockEditScreen(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.getBlockEntity(pos) instanceof TextBlockEntity be) {
            MinecraftClient.getInstance().setScreen(new TextBlockEditScreen(be));
        }
    }
}
