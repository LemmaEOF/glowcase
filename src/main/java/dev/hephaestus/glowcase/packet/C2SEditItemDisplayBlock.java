package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.Glowcase;
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

public record C2SEditItemDisplayBlock(BlockPos pos, ItemDisplayBlockEntity.RotationType rotationType, ItemDisplayBlockEntity.GivesItem givesItem, ItemDisplayBlockEntity.Offset offset, ItemDisplayBlockValues values) implements C2SEditBlockEntity {
	public static final Id<C2SEditItemDisplayBlock> ID = new Id<>(Glowcase.id("channel.item_display"));
	public static final PacketCodec<RegistryByteBuf, C2SEditItemDisplayBlock> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, C2SEditItemDisplayBlock::pos,
		PacketCodecs.BYTE.xmap(index -> ItemDisplayBlockEntity.RotationType.values()[index], rotation -> (byte) rotation.ordinal()), C2SEditItemDisplayBlock::rotationType,
		PacketCodecs.BYTE.xmap(index -> ItemDisplayBlockEntity.GivesItem.values()[index], givesItem -> (byte) givesItem.ordinal()), C2SEditItemDisplayBlock::givesItem,
		PacketCodecs.BYTE.xmap(index -> ItemDisplayBlockEntity.Offset.values()[index], offset -> (byte) offset.ordinal()), C2SEditItemDisplayBlock::offset,
		ItemDisplayBlockValues.PACKET_CODEC, C2SEditItemDisplayBlock::values,
		C2SEditItemDisplayBlock::new
	);

	public static C2SEditItemDisplayBlock of(ItemDisplayBlockEntity be) {
		return new C2SEditItemDisplayBlock(be.getPos(), be.rotationType, be.givesItem, be.offset, new ItemDisplayBlockValues(be.getCachedState().get(Properties.ROTATION), be.showName, be.pitch, be.yaw));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Override
	public void receive(ServerWorld world, BlockEntity blockEntity) {
		if (!(blockEntity instanceof ItemDisplayBlockEntity be)) return;
		if (this.values().rotation() < 0 || this.values().rotation() >= RotationPropertyHelper.getMax()) return;

		be.givesItem = this.givesItem();
		be.rotationType = this.rotationType();
		be.offset = this.offset();
		be.pitch = this.values().pitch();
		be.yaw = this.values().yaw();
		be.showName = this.values().showName();

		world.setBlockState(this.pos(), world.getBlockState(this.pos()).with(Properties.ROTATION, this.values().rotation()));

		be.markDirty();
		be.dispatch();
	}

	// separated for tuple call
	public record ItemDisplayBlockValues(int rotation, boolean showName, float pitch, float yaw) {
		public static final PacketCodec<RegistryByteBuf, ItemDisplayBlockValues> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, ItemDisplayBlockValues::rotation,
			PacketCodecs.BOOL, ItemDisplayBlockValues::showName,
			PacketCodecs.FLOAT, ItemDisplayBlockValues::pitch,
			PacketCodecs.FLOAT, ItemDisplayBlockValues::yaw,
			ItemDisplayBlockValues::new
		);
	}
}
