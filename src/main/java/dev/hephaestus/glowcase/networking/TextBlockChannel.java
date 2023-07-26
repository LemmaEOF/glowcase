package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TextBlockChannel implements ModInitializer, ClientModInitializer {
    private static final Identifier ID = Glowcase.id("channel", "text_block");

    @Environment(EnvType.CLIENT)
    public static void sync(TextBlockEntity textBlockEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(textBlockEntity.getPos());
        buf.writeFloat(textBlockEntity.scale);
        buf.writeVarInt(textBlockEntity.lines.size());
        buf.writeEnumConstant(textBlockEntity.textAlignment);
        buf.writeVarInt(textBlockEntity.color);
        buf.writeEnumConstant(textBlockEntity.zOffset);
        buf.writeEnumConstant(textBlockEntity.shadowType);

        for (MutableText text : textBlockEntity.lines) {
            buf.writeText(text);
        }

        ClientPlayNetworking.send(ID, buf);
    }

    public static void openScreen(ServerPlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(player, ID, new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register(this::registerListener);
    }

    @Environment(EnvType.CLIENT)
    private void registerListener(ClientPlayNetworkHandler handler, MinecraftClient client) {
        ClientPlayNetworking.registerReceiver(ID, this::openScreen);
    }

    @Environment(EnvType.CLIENT)
    private void openScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        client.execute(new ScreenOpener(client, buf.readBlockPos()));
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register(((handler, server) ->
                ServerPlayNetworking.registerReceiver(handler, ID, this::save))
        );
    }

    private void save(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        BlockPos pos = buf.readBlockPos();
        float scale = buf.readFloat();
        int lineCount = buf.readVarInt();
        TextBlockEntity.TextAlignment alignment = buf.readEnumConstant(TextBlockEntity.TextAlignment.class);
        int color = buf.readVarInt();
        TextBlockEntity.ZOffset zOffset = buf.readEnumConstant(TextBlockEntity.ZOffset.class);
        TextBlockEntity.ShadowType shadowType = buf.readEnumConstant(TextBlockEntity.ShadowType.class);

        List<MutableText> lines = new ArrayList<>();
        for (int i = 0; i < lineCount; ++i) {
            lines.add((MutableText) buf.readText());
        }

        server.execute(() -> {
            BlockEntity blockEntity = player.world.getBlockEntity(pos);
            if (blockEntity instanceof TextBlockEntity) {
                ((TextBlockEntity) blockEntity).scale = scale;
                ((TextBlockEntity) blockEntity).lines = lines;
                ((TextBlockEntity) blockEntity).textAlignment = alignment;
                ((TextBlockEntity) blockEntity).color = color;
                ((TextBlockEntity) blockEntity).zOffset = zOffset;
                ((TextBlockEntity) blockEntity).shadowType = shadowType;
                blockEntity.markDirty();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private record ScreenOpener(MinecraftClient client, BlockPos pos) implements Runnable {
        @Override
        public void run() {
            if (this.client.world != null && this.client.world.getBlockEntity(pos) instanceof TextBlockEntity be) {
                MinecraftClient.getInstance().setScreen(new TextBlockEditScreen(be));
            }
        }
    }
}
