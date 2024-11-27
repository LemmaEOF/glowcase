package dev.hephaestus.glowcase.block.entity;

import com.mojang.logging.LogUtils;
import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SoundPlayerBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();

	public Identifier soundId = SoundEvents.ENTITY_CAT_PURREOW.getId();
	public SoundCategory category = SoundCategory.BLOCKS;
	public float volume = 1;
	public float pitch = 1;
	public int repeatDelay = 0;
	public float distance = 16;
	public boolean relative = false;
	public Vec3d soundPosition;

	private AbstractSoundInstance nowPlaying = null;

	public SoundPlayerBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.SOUND_BLOCK_ENTITY.get(), pos, state);
		this.soundPosition = pos.toCenterPos();
//		this.soundX = pos.getX();
//		this.soundY = pos.getY();
//		this.soundZ = pos.getZ();
	}

	public void cycleCategory() {
		switch (this.category) {
			case SoundCategory.MASTER -> this.category = SoundCategory.MUSIC;
			case SoundCategory.MUSIC -> this.category = SoundCategory.RECORDS;
			case SoundCategory.RECORDS -> this.category = SoundCategory.WEATHER;
			case SoundCategory.WEATHER -> this.category = SoundCategory.BLOCKS;
			case SoundCategory.BLOCKS -> this.category = SoundCategory.HOSTILE;
			case SoundCategory.HOSTILE -> this.category = SoundCategory.NEUTRAL;
			case SoundCategory.NEUTRAL -> this.category = SoundCategory.PLAYERS;
			case SoundCategory.PLAYERS -> this.category = SoundCategory.AMBIENT;
			case SoundCategory.AMBIENT -> this.category = SoundCategory.VOICE;
			case SoundCategory.VOICE -> this.category = SoundCategory.MASTER;
		}
	}

	@Override
	protected void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		RegistryOps<NbtElement> ops = registryLookup.getOps(NbtOps.INSTANCE);
		Identifier.CODEC.encodeStart(ops, this.soundId)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("sound", result));
		tag.putString("category", this.category.toString());
		tag.putFloat("volume", this.volume);
		tag.putFloat("pitch", this.pitch);
		tag.putInt("repeatDelay", this.repeatDelay);
		tag.putFloat("distance", this.distance);
		tag.putBoolean("relative", this.relative);
		Vec3d.CODEC.encodeStart(ops, this.soundPosition)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("soundPosition", result));
	}

	@Override
	protected void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		RegistryOps<NbtElement> ops = registryLookup.getOps(NbtOps.INSTANCE);
		if (tag.contains("sound"))
			Identifier.CODEC.parse(ops, tag.get("sound"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.soundId = result);
		this.category = SoundCategory.valueOf(tag.getString("category"));
		this.volume = tag.getFloat("volume");
		this.pitch = tag.getFloat("pitch");
		this.repeatDelay = tag.getInt("repeatDelay");
		this.distance = tag.getFloat("distance");
		this.relative = tag.getBoolean("relative");
		if (tag.contains("soundPosition"))
			Vec3d.CODEC.parse(ops, tag.get("soundPosition"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.soundPosition = result);
	}

	// standard blockentity boilerplate

	public void dispatch() {
		if (world instanceof ServerWorld sworld) sworld.getChunkManager().markForUpdate(pos);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return super.toUpdatePacket();
	}

	@Environment(EnvType.CLIENT)
	public static void clientTick(World world, BlockPos pos, BlockState state, SoundPlayerBlockEntity entity) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player instanceof ClientPlayerEntity player) {
//			if (entity.sound == null) return;
//
//			RegistryWrapper.WrapperLookup lookup = world.getRegistryManager();
//			RegistryKey<SoundEvent> key = RegistryKey.of(RegistryKeys.SOUND_EVENT, entity.sound);
//
//			Optional<RegistryEntry.Reference<SoundEvent>> optionalSound =
//				lookup.getWrapperOrThrow(RegistryKeys.SOUND_EVENT).getOptional(key);
//			if (optionalSound.isEmpty()) return;
//
//			player.playSoundToPlayer(optionalSound.get().value(), SoundCategory.BLOCKS, 1, 1);

			// TODO: check if any of the other properties changed beyond id. need an equals check or something
			if (entity.nowPlaying == null || !entity.nowPlaying.getId().equals(entity.soundId) || (entity.nowPlaying instanceof TickableSoundInstance tickable && tickable.isDone())) {
//				AbstractSoundInstance sound = new PositionedSoundLoop(
//					entity.sound, SoundCategory.BLOCKS,
//					1, 1,
//					SoundInstance.createRandom(),
//					true, 0,
//					SoundInstance.AttenuationType.LINEAR,
//					pos.getX(), pos.getY(), pos.getZ(),
//					false
//				);
				// change xyz to 000 when switching to relative (and back to blockpos for absolute)
//				AbstractSoundInstance sound = new PositionedSoundLoop(
//					entity.soundId, SoundCategory.BLOCKS,
//					1, 1,
//					0,
//					16,
//					false,
//					pos.getX(), pos.getY(), pos.getZ(),
//					player
//				);
				AbstractSoundInstance sound = new PositionedSoundLoop(
					entity.soundId, entity.category,
					entity.volume, entity.pitch, entity.repeatDelay,
					entity.distance,
					entity.relative,
					entity.soundPosition,
					player,
					entity.getPos()
				);

				LOGGER.info("playing sound");
//				WeightedSoundSet sounds = mc.getSoundManager().get(entity.sound);
//				if (sounds != null) {
//					sounds.getSound(SoundInstance.createRandom());
//
//				}
				mc.getSoundManager().stop(entity.nowPlaying);
				mc.getSoundManager().play(sound);
				//if (mc.player.clientWorld.isPosLoaded(pos))
				entity.nowPlaying = sound;
			}
		}
	}

	public static class PositionedSoundLoop extends PositionedSoundInstance implements TickableSoundInstance {
		private final PlayerEntity player;
		private final BlockPos soundBlockPos;

		private final float squaredDistance;

		private boolean done;

		public PositionedSoundLoop(Identifier id, SoundCategory category, float volume, float pitch, int repeatDelay, float distance, boolean relative, Vec3d pos, PlayerEntity player, BlockPos soundBlockPos) {
			super(
				id, category,
				volume, pitch,
				SoundInstance.createRandom(),
				true, repeatDelay,
				AttenuationType.LINEAR,
				pos.x, pos.y, pos.z,
				relative
			);
			this.player = player;
			this.soundBlockPos = soundBlockPos;
			this.squaredDistance = distance * distance;
			this.done = false;
		}

//		public PositionedSoundLoop(Identifier id, SoundCategory category, float volume, float pitch, Random random, boolean repeat, int repeatDelay, AttenuationType attenuationType, double x, double y, double z, boolean relative) {
//			super(id, category, volume, pitch, random, repeat, repeatDelay, attenuationType, x, y, z, relative);
//		}

		@Override
		public boolean isDone() {
			return this.done;
		}

		public void setDone() {
			this.done = true;
		}

		@Override
		public void tick() {
			// stops track-stacking when reloading the block
			if (!canPlay()) {
				setDone();
			}
		}

		@Override
		public boolean canPlay() {
			return this.player.squaredDistanceTo(this.soundBlockPos.toCenterPos()) <= this.squaredDistance;
		}
	}
}
