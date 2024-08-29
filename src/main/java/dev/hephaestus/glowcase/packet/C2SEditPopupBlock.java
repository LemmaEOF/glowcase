package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.PopupBlockEntity;
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

public record C2SEditPopupBlock(BlockPos pos, List<Text> lines, TextBlockEntity.TextAlignment alignment, int color) implements C2SEditBlockEntity {
	public static final Id<C2SEditPopupBlock> ID = new Id<>(Glowcase.id("channel.popup_block"));
	public static final PacketCodec<RegistryByteBuf, C2SEditPopupBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditPopupBlock::pos,
		PacketCodecs.collection(ArrayList::new, TextCodecs.REGISTRY_PACKET_CODEC), C2SEditPopupBlock::lines,
		PacketCodecs.BYTE.xmap(index -> TextBlockEntity.TextAlignment.values()[index], textAlignment -> (byte) textAlignment.ordinal()), C2SEditPopupBlock::alignment,
		PacketCodecs.INTEGER, C2SEditPopupBlock::color,
		C2SEditPopupBlock::new
	);

	public static C2SEditPopupBlock of(PopupBlockEntity be) {
		return new C2SEditPopupBlock(be.getPos(), be.lines, be.textAlignment, be.color);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof PopupBlockEntity be)) return;

		be.lines = this.lines();
		be.textAlignment = this.alignment();
		be.color = this.color();

		be.markDirty();
		be.dispatch();
	}
}
