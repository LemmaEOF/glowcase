package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RotationPropertyHelper;

//TODO: Move other packet handling into this class.
public class GlowcaseCommonNetworking {
	public static final Identifier EDIT_HYPERLINK_BLOCK = Glowcase.id("channel.hyperlink.save");
	public static final Identifier EDIT_ITEM_DISPLAY_BLOCK_SETTINGS = Glowcase.id("channel.item_display");
	
	private static final int URL_MAX_LENGTH = 1024;
	
	public static void onInitialize() {
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, EDIT_HYPERLINK_BLOCK, GlowcaseCommonNetworking::onEditHyperlinkBlock);
			ServerPlayNetworking.registerReceiver(handler, EDIT_ITEM_DISPLAY_BLOCK_SETTINGS, GlowcaseCommonNetworking::onEditItemDisplayBlockSettings);
		});
	}
	
	private static void onEditHyperlinkBlock(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		BlockPos pos = buf.readBlockPos();
		String url = buf.readString();
		
		server.submit(() -> {
			if(sensible(player, pos) &&
					url.length() <= URL_MAX_LENGTH &&
					Glowcase.HYPERLINK_BLOCK.canEditGlowcase(player, pos) &&
					player.getServerWorld().getBlockEntity(pos) instanceof HyperlinkBlockEntity link)
			{
				link.setUrl(url);
			}
		});
	}
	
	private static void onEditItemDisplayBlockSettings(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		BlockPos pos = buf.readBlockPos();
		ItemDisplayBlockEntity.RotationType rotationType = buf.readEnumConstant(ItemDisplayBlockEntity.RotationType.class);
		ItemDisplayBlockEntity.GivesItem givesItem = buf.readEnumConstant(ItemDisplayBlockEntity.GivesItem.class);
		ItemDisplayBlockEntity.Offset offset = buf.readEnumConstant(ItemDisplayBlockEntity.Offset.class);
		int rotation = buf.readVarInt();
		boolean showName = buf.readBoolean();
		float pitch = buf.readFloat();
		float yaw = buf.readFloat();
		
		if(rotation < 0 || rotation >= RotationPropertyHelper.getMax()) return;

		server.execute(() -> {
			if (sensible(player, pos) &&
					Glowcase.ITEM_DISPLAY_BLOCK.canEditGlowcase(player, pos) &&
					player.getServerWorld().getBlockEntity(pos) instanceof ItemDisplayBlockEntity be
			) {
				be.givesItem = givesItem;
				be.rotationType = rotationType;
				be.offset = offset;
				be.pitch = pitch;
				be.yaw = yaw;
				be.showName = showName;

				player.getWorld().setBlockState(pos, player.getWorld().getBlockState(pos).with(Properties.ROTATION, rotation));

				be.markDirty();
				be.dispatch();
			}
		});
	}
	
	//"firewall" really terrible packets before they get any farther
	private static boolean sensible(ServerPlayerEntity player, BlockPos pos) {
		return player.getServerWorld() != null &&
				player.getServerWorld().isChunkLoaded(ChunkPos.toLong(pos)) &&
				player.squaredDistanceTo(pos.toCenterPos()) <= 12 * 12;
	}
}