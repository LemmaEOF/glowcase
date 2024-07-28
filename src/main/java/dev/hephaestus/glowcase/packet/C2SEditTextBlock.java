package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record C2SEditTextBlock(BlockPos pos, TextBlockEntity.TextAlignment alignment, TextBlockEntity.ZOffset offset, TextBlockEntity.ShadowType shadowType, TextBlockValues values) implements C2SEditBlockEntity {
	public static final Id<C2SEditTextBlock> ID = new Id<>(Glowcase.id("channel.text_block"));
	public static final PacketCodec<RegistryByteBuf, C2SEditTextBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditTextBlock::pos,
		PacketCodecs.BYTE.xmap(index -> TextBlockEntity.TextAlignment.values()[index], textAlignment -> (byte) textAlignment.ordinal()), C2SEditTextBlock::alignment,
		PacketCodecs.BYTE.xmap(index -> TextBlockEntity.ZOffset.values()[index], zOffset -> (byte) zOffset.ordinal()), C2SEditTextBlock::offset,
		PacketCodecs.BYTE.xmap(index -> TextBlockEntity.ShadowType.values()[index], shadow -> (byte) shadow.ordinal()), C2SEditTextBlock::shadowType,
		TextBlockValues.PACKET_CODEC, C2SEditTextBlock::values,
		C2SEditTextBlock::new
	);

	public static C2SEditTextBlock of(TextBlockEntity be) {
		return new C2SEditTextBlock(be.getPos(), be.textAlignment, be.zOffset, be.shadowType, new TextBlockValues(be.scale, be.color, be.lines));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof TextBlockEntity be)) return;

		be.scale = this.values().scale();
		be.lines = this.values().lines();
		be.textAlignment = this.alignment();
		be.color = this.values().color();
		be.zOffset = this.offset();
		be.shadowType = this.shadowType();

		be.markDirty();
		be.dispatch();
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
}
