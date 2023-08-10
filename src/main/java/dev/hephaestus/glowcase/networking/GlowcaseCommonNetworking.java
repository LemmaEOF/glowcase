package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.GlowcaseBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RotationPropertyHelper;

import java.util.ArrayList;
import java.util.List;

//TODO: Move other packet handling into this class.
public class GlowcaseCommonNetworking {
	public static final Identifier EDIT_HYPERLINK_BLOCK = Glowcase.id("channel.hyperlink.save");
	public static final Identifier EDIT_ITEM_DISPLAY_BLOCK_SETTINGS = Glowcase.id("channel.item_display");
	public static final Identifier EDIT_TEXT_BLOCK = Glowcase.id("channel.text_block");
	
	private static final int URL_MAX_LENGTH = 1024;
	
	public static void onInitialize() {
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, EDIT_HYPERLINK_BLOCK, GlowcaseCommonNetworking::onEditHyperlinkBlock);
			ServerPlayNetworking.registerReceiver(handler, EDIT_ITEM_DISPLAY_BLOCK_SETTINGS, GlowcaseCommonNetworking::onEditItemDisplayBlockSettings);
			ServerPlayNetworking.registerReceiver(handler, EDIT_TEXT_BLOCK, GlowcaseCommonNetworking::onEditTextBlock);
		});
	}
	
	private static void onEditHyperlinkBlock(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		BlockPos pos = buf.readBlockPos();
		String url = buf.readString(URL_MAX_LENGTH);
		
		server.submit(() -> {
			if(canEditGlowcase(player, pos, Glowcase.HYPERLINK_BLOCK) && player.getServerWorld().getBlockEntity(pos) instanceof HyperlinkBlockEntity link && url.length() <= URL_MAX_LENGTH) {
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
			if (canEditGlowcase(player, pos, Glowcase.ITEM_DISPLAY_BLOCK) && player.getServerWorld().getBlockEntity(pos) instanceof ItemDisplayBlockEntity be) {
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
	
	private static void onEditTextBlock(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		BlockPos pos = buf.readBlockPos();
		float scale = buf.readFloat();
		int lineCount = buf.readVarInt();
		TextBlockEntity.TextAlignment alignment = buf.readEnumConstant(TextBlockEntity.TextAlignment.class);
		int color = buf.readVarInt();
		TextBlockEntity.ZOffset zOffset = buf.readEnumConstant(TextBlockEntity.ZOffset.class);
		TextBlockEntity.ShadowType shadowType = buf.readEnumConstant(TextBlockEntity.ShadowType.class);

		List<MutableText> lines = new ArrayList<>();
		for (int i = 0; i < lineCount; ++i) {
			lines.add((MutableText) buf.readText());
		}

		server.execute(() -> {
			if(canEditGlowcase(player, pos, Glowcase.TEXT_BLOCK) && player.getServerWorld().getBlockEntity(pos) instanceof TextBlockEntity be) {
				be.scale = scale;
				be.lines = lines;
				be.textAlignment = alignment;
				be.color = color;
				be.zOffset = zOffset;
				be.shadowType = shadowType;
				
				be.markDirty();
				be.dispatch();
			}
		});
	}
	
	private static boolean canEditGlowcase(ServerPlayerEntity player, BlockPos pos, GlowcaseBlock glowcase) {
		return player.getServerWorld() != null &&
				player.getServerWorld().isChunkLoaded(ChunkPos.toLong(pos)) &&
				player.squaredDistanceTo(pos.toCenterPos()) <= 12 * 12 &&
				glowcase.canEditGlowcase(player, pos);
	}
}