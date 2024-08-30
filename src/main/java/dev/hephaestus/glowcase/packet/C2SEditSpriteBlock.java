package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.SpriteBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record C2SEditSpriteBlock(BlockPos pos, TextBlockEntity.ZOffset offset, int rotation, String sprite) implements C2SEditBlockEntity {
	public static final Id<C2SEditSpriteBlock> ID = new Id<>(Glowcase.id("channel.sprite.save"));
	public static final PacketCodec<RegistryByteBuf, C2SEditSpriteBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditSpriteBlock::pos,
		PacketCodecs.INTEGER.xmap(index -> TextBlockEntity.ZOffset.values()[index], TextBlockEntity.ZOffset::ordinal), C2SEditSpriteBlock::offset,
		PacketCodecs.INTEGER, C2SEditSpriteBlock::rotation,
		PacketCodecs.STRING, C2SEditSpriteBlock::sprite,
		C2SEditSpriteBlock::new
	);

	public static C2SEditSpriteBlock of(SpriteBlockEntity be) {
		return new C2SEditSpriteBlock(be.getPos(), be.zOffset, be.rotation, be.sprite);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof SpriteBlockEntity be)) return;

		be.zOffset = this.offset();
		be.rotation = this.rotation();
		be.setSprite(this.sprite());

		be.markDirty();
		be.dispatch();
	}
}
