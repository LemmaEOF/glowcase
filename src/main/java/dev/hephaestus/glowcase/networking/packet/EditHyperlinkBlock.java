package dev.hephaestus.glowcase.networking.packet;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.util.NetworkingUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record EditHyperlinkBlock(BlockPos pos, String url) implements CustomPayload {
    private static final int URL_MAX_LENGTH = 1024;

    public static final PacketCodec<RegistryByteBuf, EditHyperlinkBlock> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EditHyperlinkBlock::pos,
            PacketCodecs.STRING, EditHyperlinkBlock::url,
            EditHyperlinkBlock::new
    );

    public static final Id<EditHyperlinkBlock> PACKET_ID = new Id<>(Glowcase.id("channel.hyperlink.save"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

    public void send() {
        ClientPlayNetworking.send(this);
    }

    public void receive(ServerPlayNetworking.Context context) {
        ServerWorld serverWorld = context.player().getServerWorld();
        if (NetworkingUtil.cantEditGlowcase(context.player(), this.pos(), Glowcase.HYPERLINK_BLOCK)) return;
        if (!(serverWorld.getBlockEntity(this.pos()) instanceof HyperlinkBlockEntity link)) return;
        if (this.url().length() <= URL_MAX_LENGTH) {
            link.setUrl(this.url());
        }
    }
}