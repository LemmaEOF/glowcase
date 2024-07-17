package dev.hephaestus.glowcase.networking.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.util.NetworkingUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record EditTextBlock(BlockPos pos, TextBlockEntity.TextAlignment alignment, TextBlockEntity.ZOffset offset,
                            TextBlockEntity.ShadowType shadowType,
                            TextBlockValues values) implements CustomPayload {

    public static final PacketCodec<RegistryByteBuf, EditTextBlock> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EditTextBlock::pos,
            PacketCodecs.BYTE.xmap(index -> TextBlockEntity.TextAlignment.values()[index], textAlignment -> (byte) textAlignment.ordinal()), EditTextBlock::alignment,
            PacketCodecs.BYTE.xmap(index -> TextBlockEntity.ZOffset.values()[index], zOffset -> (byte) zOffset.ordinal()), EditTextBlock::offset,
            PacketCodecs.BYTE.xmap(index -> TextBlockEntity.ShadowType.values()[index], shadow -> (byte) shadow.ordinal()), EditTextBlock::shadowType,
            TextBlockValues.PACKET_CODEC, EditTextBlock::values,
            EditTextBlock::new
    );

    public static final Id<EditTextBlock> PACKET_ID = new Id<>(Glowcase.id("channel.text_block"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    public void send() {
        ClientPlayNetworking.send(this);
    }

    public void receive(ServerPlayNetworking.Context context) {
        ServerWorld serverWorld = context.player().getServerWorld();
        if (!NetworkingUtil.canEditGlowcase(context.player(), this.pos(), Glowcase.TEXT_BLOCK)) return;
        if (!(serverWorld.getBlockEntity(this.pos()) instanceof TextBlockEntity be)) return;

        be.scale = this.values().scale();
        be.lines = this.values().lines();
        be.textAlignment = this.alignment();
        be.color = this.values().color();
        be.zOffset = this.offset();
        be.shadowType = this.shadowType();

        be.markDirty();
        be.dispatch();
    }

    // separated for tuple call
    public record TextBlockValues(float scale, int color, List<Text> lines) {
        public static final PacketCodec<RegistryByteBuf, TextBlockValues> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.FLOAT, TextBlockValues::scale,
                PacketCodecs.INTEGER, TextBlockValues::color,
                PacketCodecs.collection(ArrayList::new, TextCodecs.REGISTRY_PACKET_CODEC), TextBlockValues::lines,
                TextBlockValues::new
        );
    }
}