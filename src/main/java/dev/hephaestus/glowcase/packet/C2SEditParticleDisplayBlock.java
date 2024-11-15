package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ParticleDisplayBlockEntity;
import dev.hephaestus.glowcase.math.DeviatedInteger;
import dev.hephaestus.glowcase.math.DeviatedVec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record C2SEditParticleDisplayBlock(
	ParticleEffect particle,
	DeviatedInteger count,
	DeviatedVec3d velocity,
	DeviatedVec3d position,
	DeviatedInteger tickRate,
	BlockPos blockPos
) implements C2SEditBlockEntity {
	public static final Id<C2SEditParticleDisplayBlock> ID = new Id<>(Glowcase.id("channel.particle_display.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditParticleDisplayBlock> PACKET_CODEC = PacketCodec.tuple(
		ParticleTypes.PACKET_CODEC, C2SEditParticleDisplayBlock::particle,
		DeviatedInteger.PACKET_CODEC, C2SEditParticleDisplayBlock::count,
		DeviatedVec3d.PACKET_CODEC, C2SEditParticleDisplayBlock::velocity,
		DeviatedVec3d.PACKET_CODEC, C2SEditParticleDisplayBlock::position,
		DeviatedInteger.PACKET_CODEC, C2SEditParticleDisplayBlock::tickRate,
		BlockPos.PACKET_CODEC, C2SEditParticleDisplayBlock::blockPos,
		C2SEditParticleDisplayBlock::new
	);

	public static C2SEditParticleDisplayBlock of(ParticleDisplayBlockEntity be) {
		return new C2SEditParticleDisplayBlock(be.particle, be.count, be.velocity, be.position, be.tickRate, be.getPos());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public BlockPos pos() {
		return blockPos;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof ParticleDisplayBlockEntity be)) return;

		be.particle = this.particle();
		be.count = this.count();
		be.velocity = this.velocity();
		be.position = this.position();
		be.tickRate = this.tickRate();

		be.markDirty();
		be.dispatch();
	}
}
