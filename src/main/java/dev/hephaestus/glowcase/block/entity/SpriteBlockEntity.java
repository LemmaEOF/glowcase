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
import org.jetbrains.annotations.Nullable;

public class SpriteBlockEntity extends BlockEntity {
	public String sprite = "arrow";
	public int rotation = 0;
	public TextBlockEntity.ZOffset zOffset = TextBlockEntity.ZOffset.CENTER;
	public int color = 0xFFFFFF;

	public SpriteBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.SPRITE_BLOCK_ENTITY.get(), pos, state);
	}

	public void setSprite(String newSprite) {
		sprite = newSprite;
		markDirty();
		dispatch();
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putString("sprite", this.sprite);
		tag.putInt("rotation", this.rotation);
		tag.putString("z_offset", this.zOffset.name());
		tag.putInt("color", this.color);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		this.sprite = tag.getString("sprite");
		this.rotation = tag.getInt("rotation");
		this.zOffset = TextBlockEntity.ZOffset.valueOf(tag.getString("z_offset"));
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
