package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

public class HyperlinkBlockEntity extends BlockEntity {
	public String url = "";

	public HyperlinkBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.HYPERLINK_BLOCK_ENTITY, pos, state);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		writeNbt(tag);
		return tag;
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

	@Override
	public void markDirty() {
		PlayerLookup.tracking(this).forEach(player -> player.networkHandler.sendPacket(toUpdatePacket()));
		super.markDirty();
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
}
