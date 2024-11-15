package dev.hephaestus.glowcase.block.entity;

import com.mojang.logging.LogUtils;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.math.DeviatedInteger;
import dev.hephaestus.glowcase.math.DeviatedVec3d;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ParticleDisplayBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();

	public ParticleEffect particle = ParticleTypes.FLAME;
	public DeviatedVec3d position = DeviatedVec3d.ZERO;
	public DeviatedVec3d velocity = DeviatedVec3d.ZERO;
	public DeviatedInteger count = DeviatedInteger.ZERO;
	public DeviatedInteger tickRate = DeviatedInteger.ZERO;

	private int tickCounter = 0;

	public ParticleDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.PARTICLE_DISPLAY_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		RegistryOps<NbtElement> ops = registryLookup.getOps(NbtOps.INSTANCE);
		ParticleTypes.TYPE_CODEC.encodeStart(ops, this.particle)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("particle", result));

		DeviatedVec3d.CODEC.encodeStart(ops, position)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("position", result));

		DeviatedVec3d.CODEC.encodeStart(ops, velocity)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("velocity", result));

		DeviatedInteger.CODEC.encodeStart(ops, count)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("count", result));

		DeviatedInteger.CODEC.encodeStart(ops, tickRate)
			.resultOrPartial(LOGGER::error)
			.ifPresent(result -> tag.put("tick_rate", result));
	}

	@Override
	protected void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		RegistryOps<NbtElement> ops = registryLookup.getOps(NbtOps.INSTANCE);

		if (tag.contains("particle"))
			ParticleTypes.TYPE_CODEC.parse(ops, tag.getCompound("particle"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.particle = result);

		if (tag.contains("position"))
			DeviatedVec3d.CODEC.parse(ops, tag.getCompound("position"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.position = result);

		if (tag.contains("velocity"))
			DeviatedVec3d.CODEC.parse(ops, tag.getCompound("velocity"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.velocity = result);

		if (tag.contains("count"))
			DeviatedInteger.CODEC.parse(ops, tag.getCompound("count"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.count = result);

		if (tag.contains("tick_rate"))
			DeviatedInteger.CODEC.parse(ops, tag.getCompound("tick_rate"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(result -> this.tickRate = result);
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
		return BlockEntityUpdateS2CPacket.create(this);
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, ParticleDisplayBlockEntity entity) {
		entity.tickCounter--;
		if (entity.tickCounter > 0) return;

		entity.tickCounter = entity.tickRate.get(world.random::nextDouble);
		for (int i = 0; i < entity.count.get(world.random::nextDouble); i++) {
			Vec3d particlePos = entity.position.get(world.random::nextGaussian).add(pos.toCenterPos());
			Vec3d particleVelocity = entity.velocity.get(world.random::nextGaussian);

			world.addParticle(
				entity.particle,
				particlePos.x, particlePos.y, particlePos.z,
				particleVelocity.x, particleVelocity.y, particleVelocity.z
			);
		}
	}
}
