package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SoundPlayerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record C2SEditSoundBlock(SoundInfo soundInfo, PositionalInfo positionalInfo, BlockPos blockPos) implements C2SEditBlockEntity {
	public static final Id<C2SEditSoundBlock> ID = new Id<>(Glowcase.id("channel.sound_block.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditSoundBlock> PACKET_CODEC = PacketCodec.tuple(
		SoundInfo.PACKET_CODEC, C2SEditSoundBlock::soundInfo,
		PositionalInfo.PACKET_CODEC, C2SEditSoundBlock::positionalInfo,
		BlockPos.PACKET_CODEC, C2SEditSoundBlock::blockPos,
		C2SEditSoundBlock::new
	);

	public static C2SEditSoundBlock of(SoundPlayerBlockEntity be) {
		return new C2SEditSoundBlock(
			new SoundInfo(be.soundId, be.category.toString(), be.volume, be.pitch, be.repeatDelay, be.cancelOthers),
			new PositionalInfo(be.distance, be.relative, be.soundPosition),
			be.getPos()
		);
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
		if (!(blockEntity instanceof SoundPlayerBlockEntity be)) return;

		be.soundId = soundInfo.id;
		be.category = SoundCategory.valueOf(soundInfo.category);
		be.volume = soundInfo.volume;
		be.pitch = soundInfo.pitch;
		be.repeatDelay = soundInfo.repeatDelay;
		be.cancelOthers = soundInfo.cancelOthers;

		be.distance = positionalInfo.distance;
		be.relative = positionalInfo.relative;
		be.soundPosition = positionalInfo.position;

		be.markDirty();
		be.dispatch();
	}

	public record SoundInfo(Identifier id, String category, float volume, float pitch, int repeatDelay, boolean cancelOthers) {
		public static final PacketCodec<RegistryByteBuf, SoundInfo> PACKET_CODEC = PacketCodec.tuple(
			Identifier.PACKET_CODEC, SoundInfo::id,
			PacketCodecs.STRING, SoundInfo::category,
			PacketCodecs.FLOAT, SoundInfo::volume,
			PacketCodecs.FLOAT, SoundInfo::pitch,
			PacketCodecs.INTEGER, SoundInfo::repeatDelay,
			PacketCodecs.BOOL, SoundInfo::cancelOthers,
			SoundInfo::new
		);
	}

	public record PositionalInfo(float distance, boolean relative, Vec3d position) {
		public static final PacketCodec<RegistryByteBuf, PositionalInfo> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.FLOAT, PositionalInfo::distance,
			PacketCodecs.BOOL, PositionalInfo::relative,
			PacketCodecs.codec(Vec3d.CODEC), PositionalInfo::position,
			PositionalInfo::new
		);
	}
}
