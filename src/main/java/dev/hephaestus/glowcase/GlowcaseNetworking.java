package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.packet.C2SEditHyperlinkBlock;
import dev.hephaestus.glowcase.packet.C2SEditItemDisplayBlock;
import dev.hephaestus.glowcase.packet.C2SEditSpriteBlock;
import dev.hephaestus.glowcase.packet.C2SEditTextBlock;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class GlowcaseNetworking {
	public static void init() {
		PayloadTypeRegistry.playC2S().register(C2SEditHyperlinkBlock.ID, C2SEditHyperlinkBlock.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(C2SEditItemDisplayBlock.ID, C2SEditItemDisplayBlock.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(C2SEditTextBlock.ID, C2SEditTextBlock.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(C2SEditSpriteBlock.ID, C2SEditSpriteBlock.PACKET_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(C2SEditHyperlinkBlock.ID, C2SEditHyperlinkBlock::receive);
		ServerPlayNetworking.registerGlobalReceiver(C2SEditItemDisplayBlock.ID, C2SEditItemDisplayBlock::receive);
		ServerPlayNetworking.registerGlobalReceiver(C2SEditTextBlock.ID, C2SEditTextBlock::receive);
		ServerPlayNetworking.registerGlobalReceiver(C2SEditSpriteBlock.ID, C2SEditSpriteBlock::receive);
	}
}
