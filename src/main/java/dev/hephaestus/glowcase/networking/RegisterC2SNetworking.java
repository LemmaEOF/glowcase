package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.networking.packet.EditHyperlinkBlock;
import dev.hephaestus.glowcase.networking.packet.EditItemDisplayBlockSettings;
import dev.hephaestus.glowcase.networking.packet.EditTextBlock;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class RegisterC2SNetworking {
	static {
		ServerPlayNetworking.registerGlobalReceiver(EditHyperlinkBlock.PACKET_ID, EditHyperlinkBlock::receive);
		ServerPlayNetworking.registerGlobalReceiver(EditItemDisplayBlockSettings.PACKET_ID, EditItemDisplayBlockSettings::receive);
		ServerPlayNetworking.registerGlobalReceiver(EditTextBlock.PACKET_ID, EditTextBlock::receive);
	}

	public static void initialize() {
		// static initialisation
	}
}
