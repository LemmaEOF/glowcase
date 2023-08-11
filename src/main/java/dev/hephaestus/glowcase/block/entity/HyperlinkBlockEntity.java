package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HyperlinkBlockEntity extends BlockEntity {
	private String url = "";

	public HyperlinkBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.HYPERLINK_BLOCK_ENTITY, pos, state);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String newUrl) {
		url = newUrl;
		markDirty();
		dispatch();
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putString("url", this.url);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		this.url = tag.getString("url");
	}

	// standard blockentity boilerplate

	public void dispatch() {
		if (world instanceof ServerWorld sworld) sworld.getChunkManager().markForUpdate(pos);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
}
