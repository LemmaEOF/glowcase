package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.HyperlinkBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.regex.Pattern;

//TODO: Move other packet handling into this class.
public class GlowcaseCommonNetworking {
	public static final Identifier EDIT_HYPERLINK_BLOCK = Glowcase.id("channel.hyperlink.save");
	
	//TODO: Is this needed for safety? is a simple "max length" check not good enough?
	private static final Pattern ALLOWED_URL_REGEX = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	private static final int URL_MAX_LENGTH = 1024;
	
	public static void onInitialize() {
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, EDIT_HYPERLINK_BLOCK, GlowcaseCommonNetworking::onEditHyperlinkBlock);
		});
	}
	
	private static void onEditHyperlinkBlock(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		BlockPos pos = buf.readBlockPos();
		String url = buf.readString();
		
		server.submit(() -> {
			if(sensible(player, pos) &&
					url.length() <= URL_MAX_LENGTH &&
					ALLOWED_URL_REGEX.matcher(url).matches() &&
					HyperlinkBlock.canEdit(player, pos) &&
					player.getServerWorld().getBlockEntity(pos) instanceof HyperlinkBlockEntity link)
			{
				link.setUrl(url);
			}
		});
	}
	
	//"firewall" really terrible packets before they get any farther
	private static boolean sensible(ServerPlayerEntity player, BlockPos pos) {
		return player.getServerWorld() != null && player.getServerWorld().isChunkLoaded(ChunkPos.toLong(pos));
	}
}