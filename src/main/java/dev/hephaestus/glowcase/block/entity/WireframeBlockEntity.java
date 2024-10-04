package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WireframeBlockEntity extends BlockEntity {
	public Vec3i offset = Vec3i.ZERO;
	public Vec3i scale = new Vec3i(1, 1, 1);
	public int color = 0xFFFFFF;

	public WireframeBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.WIREFRAME_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putIntArray("offset", List.of(this.offset.getX(), this.offset.getY(), this.offset.getZ()));
		tag.putIntArray("scale", List.of(this.scale.getX(), this.scale.getY(), this.scale.getZ()));
		tag.putInt("color", this.color);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		int[] offset = tag.getIntArray("offset");
		int[] scale = tag.getIntArray("scale");

		this.offset = new Vec3i(offset[0], offset[1], offset[2]);
		this.scale = new Vec3i(scale[0], scale[1], scale[2]);
		this.color = tag.getInt("color");
	}

	@SuppressWarnings({"MethodCallSideOnly", "VariableUseSideOnly"})
	@Override
	public void markRemoved() {
		if (world != null && world.isClient) {
			BakedBlockEntityRenderer.Manager.markForRebuild(getPos());
		}
		super.markRemoved();
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
}
