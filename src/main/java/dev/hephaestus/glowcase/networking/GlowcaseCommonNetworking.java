package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.GlowcaseBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RotationPropertyHelper;

import java.util.ArrayList;
import java.util.List;

public class GlowcaseCommonNetworking {

	public record EditHyperlinkBlock(BlockPos pos, String url) implements CustomPayload {
		public static final Id<EditHyperlinkBlock> PACKET_ID = new Id<>(Glowcase.id("channel.hyperlink.save"));
		public static final PacketCodec<RegistryByteBuf, EditHyperlinkBlock> PACKET_CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, EditHyperlinkBlock::pos, PacketCodecs.STRING, EditHyperlinkBlock::url, EditHyperlinkBlock::new);

		public static void receive(EditHyperlinkBlock payload, ServerPlayNetworking.Context context) {
			context.player().server.submit(() -> {
				if(canEditGlowcase(context.player(), payload.pos(), Glowcase.HYPERLINK_BLOCK.get()) && context.player().getServerWorld().getBlockEntity(payload.pos()) instanceof HyperlinkBlockEntity link && payload.url().length() <= URL_MAX_LENGTH) {
					link.setUrl(payload.url());
				}
			});
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return PACKET_ID;
		}
	}

	// separated for tuple call
	public record ItemDisplayBlockValues(int rotation, boolean showName, float pitch, float yaw) {
		public static final PacketCodec<RegistryByteBuf, ItemDisplayBlockValues> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, ItemDisplayBlockValues::rotation,
				PacketCodecs.BOOL, ItemDisplayBlockValues::showName,
				PacketCodecs.FLOAT, ItemDisplayBlockValues::pitch,
				PacketCodecs.FLOAT, ItemDisplayBlockValues::yaw,
				ItemDisplayBlockValues::new
		);
	}

	public record EditItemDisplayBlockSettings(BlockPos pos, ItemDisplayBlockEntity.RotationType rotationType, ItemDisplayBlockEntity.GivesItem givesItem, ItemDisplayBlockEntity.Offset offset, ItemDisplayBlockValues values) implements CustomPayload {
		public static final Id<EditItemDisplayBlockSettings> PACKET_ID = new Id<>(Glowcase.id("channel.item_display"));

		public static final PacketCodec<RegistryByteBuf, EditItemDisplayBlockSettings> PACKET_CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, EditItemDisplayBlockSettings::pos,
				PacketCodecs.INTEGER.xmap(index -> ItemDisplayBlockEntity.RotationType.values()[index], ItemDisplayBlockEntity.RotationType::ordinal), EditItemDisplayBlockSettings::rotationType,
				PacketCodecs.INTEGER.xmap(index -> ItemDisplayBlockEntity.GivesItem.values()[index], ItemDisplayBlockEntity.GivesItem::ordinal), EditItemDisplayBlockSettings::givesItem,
				PacketCodecs.INTEGER.xmap(index -> ItemDisplayBlockEntity.Offset.values()[index], ItemDisplayBlockEntity.Offset::ordinal), EditItemDisplayBlockSettings::offset,
				ItemDisplayBlockValues.PACKET_CODEC, EditItemDisplayBlockSettings::values,
				EditItemDisplayBlockSettings::new
		);

		public static void receive(EditItemDisplayBlockSettings payload, ServerPlayNetworking.Context context) {
			if(payload.values().rotation() < 0 || payload.values().rotation() >= RotationPropertyHelper.getMax()) return;

			context.player().server.execute(() -> {
				if (canEditGlowcase(context.player(), payload.pos(), Glowcase.ITEM_DISPLAY_BLOCK.get()) && context.player().getServerWorld().getBlockEntity(payload.pos()) instanceof ItemDisplayBlockEntity be) {
					be.givesItem = payload.givesItem();
					be.rotationType = payload.rotationType();
					be.offset = payload.offset();
					be.pitch = payload.values().pitch();
					be.yaw = payload.values().yaw();
					be.showName = payload.values().showName();

					context.player().getWorld().setBlockState(payload.pos(), context.player().getWorld().getBlockState(payload.pos()).with(Properties.ROTATION, payload.values().rotation()));

					be.markDirty();
					be.dispatch();
				}
			});
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return PACKET_ID;
		}
	}

	// separated for tuple call
	public record TextBlockValues(float scale, int color, List<Text> lines) {
		public static final PacketCodec<RegistryByteBuf, TextBlockValues> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.FLOAT, TextBlockValues::scale,
				PacketCodecs.INTEGER, TextBlockValues::color,
				PacketCodecs.collection(ArrayList::new, TextCodecs.REGISTRY_PACKET_CODEC), TextBlockValues::lines,
				TextBlockValues::new
		);
	}

	public record EditTextBlock(BlockPos pos, TextBlockEntity.TextAlignment alignment, TextBlockEntity.ZOffset offset, TextBlockEntity.ShadowType shadowType, TextBlockValues values) implements CustomPayload {
		public static final Id<EditTextBlock> PACKET_ID = new Id<>(Glowcase.id("channel.text_block"));

		public static final PacketCodec<RegistryByteBuf, EditTextBlock> PACKET_CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, EditTextBlock::pos,
				PacketCodecs.INTEGER.xmap(index -> TextBlockEntity.TextAlignment.values()[index], TextBlockEntity.TextAlignment::ordinal), EditTextBlock::alignment,
				PacketCodecs.INTEGER.xmap(index -> TextBlockEntity.ZOffset.values()[index], TextBlockEntity.ZOffset::ordinal), EditTextBlock::offset,
				PacketCodecs.INTEGER.xmap(index -> TextBlockEntity.ShadowType.values()[index], TextBlockEntity.ShadowType::ordinal), EditTextBlock::shadowType,
				TextBlockValues.PACKET_CODEC, EditTextBlock::values,
				EditTextBlock::new
		);

		public static void receive(EditTextBlock payload, ServerPlayNetworking.Context context) {
			context.player().server.execute(() -> {
				if(canEditGlowcase(context.player(), payload.pos(), Glowcase.TEXT_BLOCK.get()) && context.player().getServerWorld().getBlockEntity(payload.pos()) instanceof TextBlockEntity be) {
					be.scale = payload.values().scale();
					be.lines = payload.values().lines();
					be.textAlignment = payload.alignment();
					be.color = payload.values().color();
					be.zOffset = payload.offset();
					be.shadowType = payload.shadowType();

					be.markDirty();
					be.dispatch();
				}
			});
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return PACKET_ID;
		}
	}

	private static final int URL_MAX_LENGTH = 1024;

	public static void onInitialize() {
		PayloadTypeRegistry.playC2S().register(EditHyperlinkBlock.PACKET_ID, EditHyperlinkBlock.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(EditItemDisplayBlockSettings.PACKET_ID, EditItemDisplayBlockSettings.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(EditTextBlock.PACKET_ID, EditTextBlock.PACKET_CODEC);
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, EditHyperlinkBlock.PACKET_ID, EditHyperlinkBlock::receive);
			ServerPlayNetworking.registerReceiver(handler, EditItemDisplayBlockSettings.PACKET_ID, EditItemDisplayBlockSettings::receive);
			ServerPlayNetworking.registerReceiver(handler, EditTextBlock.PACKET_ID, EditTextBlock::receive);
		});
	}

	private static boolean canEditGlowcase(ServerPlayerEntity player, BlockPos pos, GlowcaseBlock glowcase) {
		return player.getServerWorld() != null &&
				player.getServerWorld().isChunkLoaded(ChunkPos.toLong(pos)) &&
				player.squaredDistanceTo(pos.toCenterPos()) <= 12 * 12 &&
				glowcase.canEditGlowcase(player, pos);
	}
}