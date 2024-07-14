package dev.hephaestus.glowcase.networking.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.util.NetworkingUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationPropertyHelper;

public record EditItemDisplayBlockSettings(BlockPos pos, ItemDisplayBlockEntity.RotationType rotationType,
                                           ItemDisplayBlockEntity.GivesItem givesItem,
                                           ItemDisplayBlockEntity.Offset offset,
                                           ItemDisplayBlockValues values) implements CustomPayload {
    public static final Id<EditItemDisplayBlockSettings> PACKET_ID = new Id<>(Glowcase.id("channel.item_display"));

    public static final PacketCodec<RegistryByteBuf, EditItemDisplayBlockSettings> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EditItemDisplayBlockSettings::pos,
            PacketCodecs.SHORT.xmap(index -> ItemDisplayBlockEntity.RotationType.values()[index], rotation -> (short) rotation.ordinal()), EditItemDisplayBlockSettings::rotationType,
            PacketCodecs.SHORT.xmap(index -> ItemDisplayBlockEntity.GivesItem.values()[index], givesItem -> (short) givesItem.ordinal()), EditItemDisplayBlockSettings::givesItem,
            PacketCodecs.SHORT.xmap(index -> ItemDisplayBlockEntity.Offset.values()[index], offset -> (short) offset.ordinal()), EditItemDisplayBlockSettings::offset,
            ItemDisplayBlockValues.PACKET_CODEC, EditItemDisplayBlockSettings::values,
            EditItemDisplayBlockSettings::new
    );

    public void send() {
        ClientPlayNetworking.send(this);
    }

    public void receive(ServerPlayNetworking.Context context) {
        ServerWorld serverWorld = context.player().getServerWorld();
        if (this.values().rotation() < 0 || this.values().rotation() >= RotationPropertyHelper.getMax()) return;
        if (NetworkingUtil.cantEditGlowcase(context.player(), this.pos(), Glowcase.ITEM_DISPLAY_BLOCK)) return;
        if (!(serverWorld.getBlockEntity(this.pos()) instanceof ItemDisplayBlockEntity be)) return;

        be.givesItem = this.givesItem();
        be.rotationType = this.rotationType();
        be.offset = this.offset();
        be.pitch = this.values().pitch();
        be.yaw = this.values().yaw();
        be.showName = this.values().showName();

        serverWorld.setBlockState(this.pos(), context.player().getWorld()
                .getBlockState(this.pos()).with(Properties.ROTATION, this.values().rotation()));

        be.markDirty();
        be.dispatch();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
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