package dev.hephaestus.glowcase.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class GlowcaseClientNetworking {
	public static void onInitializeClient() {
		//...
	}
	
	public static void editHyperlinkBlock(BlockPos pos, String url) {
		ClientPlayNetworking.send(GlowcaseCommonNetworking.EDIT_HYPERLINK_BLOCK, new PacketByteBuf(Unpooled.buffer())
				.writeBlockPos(pos)
				.writeString(url));
	}
}
