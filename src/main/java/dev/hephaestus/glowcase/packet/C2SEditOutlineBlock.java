package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.OutlineBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record C2SEditOutlineBlock(BlockPos pos, Vec3i offset, Vec3i scale, int color) implements C2SEditBlockEntity {
	public static final PacketCodec<RegistryByteBuf, Vec3i> VEC3I = PacketCodec.tuple(
		PacketCodecs.INTEGER, Vec3i::getX,
		PacketCodecs.INTEGER, Vec3i::getY,
		PacketCodecs.INTEGER, Vec3i::getZ,
		Vec3i::new
	);

	public static final Id<C2SEditOutlineBlock> ID = new Id<>(Glowcase.id("channel.outline.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditOutlineBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditOutlineBlock::pos,
		VEC3I, C2SEditOutlineBlock::offset,
		VEC3I, C2SEditOutlineBlock::scale,
		PacketCodecs.INTEGER, C2SEditOutlineBlock::color,
		C2SEditOutlineBlock::new
	);

	public static C2SEditOutlineBlock of(OutlineBlockEntity be) {
		return new C2SEditOutlineBlock(be.getPos(), be.offset, be.scale, be.color);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof OutlineBlockEntity be)) return;

		be.offset = this.offset();
		be.scale = this.scale();
		be.color = this.color();

		be.markDirty();
		be.dispatch();
	}
}
