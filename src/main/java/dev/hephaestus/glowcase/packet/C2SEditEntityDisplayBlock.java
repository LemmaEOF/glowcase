package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.EntityDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationPropertyHelper;

public record C2SEditEntityDisplayBlock(BlockPos pos, ItemDisplayBlockEntity.RotationType rotationType, ItemDisplayBlockEntity.GivesItem givesItem, ItemDisplayBlockEntity.Offset offset, EntityDisplayBlockValues values) implements C2SEditBlockEntity {
	public static final Id<C2SEditEntityDisplayBlock> ID = new Id<>(Glowcase.id("channel.entity_display"));
	public static final PacketCodec<RegistryByteBuf, C2SEditEntityDisplayBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditEntityDisplayBlock::pos,
		PacketCodecs.BYTE.xmap(index -> ItemDisplayBlockEntity.RotationType.values()[index], rotation -> (byte) rotation.ordinal()), C2SEditEntityDisplayBlock::rotationType,
		PacketCodecs.BYTE.xmap(index -> ItemDisplayBlockEntity.GivesItem.values()[index], givesItem -> (byte) givesItem.ordinal()), C2SEditEntityDisplayBlock::givesItem,
		PacketCodecs.BYTE.xmap(index -> ItemDisplayBlockEntity.Offset.values()[index], offset -> (byte) offset.ordinal()), C2SEditEntityDisplayBlock::offset,
		EntityDisplayBlockValues.PACKET_CODEC, C2SEditEntityDisplayBlock::values,
		C2SEditEntityDisplayBlock::new
	);

	public static C2SEditEntityDisplayBlock of(EntityDisplayBlockEntity be) {
		return new C2SEditEntityDisplayBlock(be.getPos(), be.rotationType, be.givesItem, be.offset, new EntityDisplayBlockValues(be.tickEntity, be.displayScale, be.getCachedState().get(Properties.ROTATION), be.showName, be.pitch, be.yaw));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof EntityDisplayBlockEntity be)) return;
		if (this.values().rotation() < 0 || this.values().rotation() >= RotationPropertyHelper.getMax()) return;

		be.givesItem = this.givesItem();
		be.rotationType = this.rotationType();
		be.offset = this.offset();
		be.pitch = this.values().pitch();
		be.yaw = this.values().yaw();
		be.showName = this.values().showName();
		be.tickEntity = this.values().tickEntity();
		be.displayScale = this.values().scale();
		be.setShouldEditScale(false);

		world.setBlockState(this.pos(), world.getBlockState(this.pos()).with(Properties.ROTATION, this.values().rotation()));

		be.markDirty();
		be.dispatch();
	}

	// separated for tuple call
	public record EntityDisplayBlockValues(boolean tickEntity, float scale, int rotation, boolean showName, float pitch, float yaw) {
		public static final PacketCodec<RegistryByteBuf, EntityDisplayBlockValues> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL, EntityDisplayBlockValues::tickEntity,
			PacketCodecs.FLOAT, EntityDisplayBlockValues::scale,
			PacketCodecs.INTEGER, EntityDisplayBlockValues::rotation,
			PacketCodecs.BOOL, EntityDisplayBlockValues::showName,
			PacketCodecs.FLOAT, EntityDisplayBlockValues::pitch,
			PacketCodecs.FLOAT, EntityDisplayBlockValues::yaw,
			EntityDisplayBlockValues::new
		);
	}
}
