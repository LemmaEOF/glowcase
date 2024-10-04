package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.WireframeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record C2SEditWireframeBlock(BlockPos pos, Vec3i offset, Vec3i scale, int color) implements C2SEditBlockEntity {
	public static final PacketCodec<RegistryByteBuf, Vec3i> VEC3I = PacketCodec.tuple(
		PacketCodecs.INTEGER, Vec3i::getX,
		PacketCodecs.INTEGER, Vec3i::getY,
		PacketCodecs.INTEGER, Vec3i::getZ,
		Vec3i::new
	);

	public static final Id<C2SEditWireframeBlock> ID = new Id<>(Glowcase.id("channel.wireframe.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditWireframeBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditWireframeBlock::pos,
		VEC3I, C2SEditWireframeBlock::offset,
		VEC3I, C2SEditWireframeBlock::scale,
		PacketCodecs.INTEGER, C2SEditWireframeBlock::color,
		C2SEditWireframeBlock::new
	);

	public static C2SEditWireframeBlock of(WireframeBlockEntity be) {
		return new C2SEditWireframeBlock(be.getPos(), be.offset, be.scale, be.color);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof WireframeBlockEntity be)) return;

		be.offset = this.offset();
		be.scale = this.scale();
		be.color = this.color();

		be.markDirty();
		be.dispatch();
	}
}
