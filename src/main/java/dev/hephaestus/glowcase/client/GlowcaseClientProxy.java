package dev.hephaestus.glowcase.client;

import dev.hephaestus.glowcase.GlowcaseCommonProxy;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
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
}
