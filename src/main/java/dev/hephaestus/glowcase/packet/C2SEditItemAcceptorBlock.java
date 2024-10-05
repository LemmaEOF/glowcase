package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ItemAcceptorBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record C2SEditItemAcceptorBlock(BlockPos pos, Identifier item, int count, boolean isItemTag) implements C2SEditBlockEntity {
	public static final Id<C2SEditItemAcceptorBlock> ID = new Id<>(Glowcase.id("channel.item_acceptor.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditItemAcceptorBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditItemAcceptorBlock::pos,
		Identifier.PACKET_CODEC, C2SEditItemAcceptorBlock::item,
		PacketCodecs.INTEGER, C2SEditItemAcceptorBlock::count,
		PacketCodecs.BOOL, C2SEditItemAcceptorBlock::isItemTag,
		C2SEditItemAcceptorBlock::new
	);

	public static C2SEditItemAcceptorBlock of(ItemAcceptorBlockEntity be) {
		return new C2SEditItemAcceptorBlock(be.getPos(), be.getItem(), be.count, be.isItemTag);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof ItemAcceptorBlockEntity be)) return;

		be.setItem(this.item());
		be.count = this.count();
		be.isItemTag = this.isItemTag();

		be.markDirty();
		be.dispatch();
	}
}
