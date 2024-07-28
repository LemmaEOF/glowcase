package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record C2SEditHyperlinkBlock(BlockPos pos, String url) implements C2SEditBlockEntity {
	public static final Id<C2SEditHyperlinkBlock> ID = new Id<>(Glowcase.id("channel.hyperlink.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditHyperlinkBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditHyperlinkBlock::pos,
		PacketCodecs.STRING, C2SEditHyperlinkBlock::url,
		C2SEditHyperlinkBlock::new
	);

	public static C2SEditHyperlinkBlock of(HyperlinkBlockEntity be) {
		return new C2SEditHyperlinkBlock(be.getPos(), be.getUrl());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof HyperlinkBlockEntity be)) return;
		if (this.url().length() <= HyperlinkBlockEntity.URL_MAX_LENGTH) {
			be.setUrl(this.url());
		}
	}
}
