package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.networking.packet.EditHyperlinkBlock;
import dev.hephaestus.glowcase.networking.packet.EditItemDisplayBlockSettings;
import dev.hephaestus.glowcase.networking.packet.EditTextBlock;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

@SuppressWarnings("unused")
public class NetworkPayloads {
	static {
		registerC2S(EditHyperlinkBlock.PACKET_ID, EditHyperlinkBlock.PACKET_CODEC);
		registerC2S(EditItemDisplayBlockSettings.PACKET_ID, EditItemDisplayBlockSettings.PACKET_CODEC);
		registerC2S(EditTextBlock.PACKET_ID, EditTextBlock.PACKET_CODEC);
	}

	private static <T extends CustomPayload> void registerS2C(CustomPayload.Id<T> packetIdentifier, PacketCodec<RegistryByteBuf, T> codec) {
		PayloadTypeRegistry.playS2C().register(packetIdentifier, codec);
	}

	private static <T extends CustomPayload> void registerC2S(CustomPayload.Id<T> packetIdentifier, PacketCodec<RegistryByteBuf, T> codec) {
		PayloadTypeRegistry.playC2S().register(packetIdentifier, codec);
	}

	public static void initialize() {
		// static initialisation
	}
}
