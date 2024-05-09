package net.modfest.glowcase.client;

import net.modfest.glowcase.GlowcaseCommonProxy;
import net.modfest.glowcase.block.entity.HyperlinkBlockEntity;
import net.modfest.glowcase.block.entity.ItemDisplayBlockEntity;
import net.modfest.glowcase.block.entity.TextBlockEntity;
import net.modfest.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import net.modfest.glowcase.client.gui.screen.ingame.ItemDisplayBlockEditScreen;
import net.modfest.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import net.modfest.glowcase.mixin.client.MinecraftClientAccessor;
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
