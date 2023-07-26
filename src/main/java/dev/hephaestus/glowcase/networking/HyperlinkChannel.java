package dev.hephaestus.glowcase.networking;

import java.util.regex.Pattern;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HyperlinkChannel implements ModInitializer, ClientModInitializer {
    private static final Pattern URL = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private static final Identifier OPEN = Glowcase.id("channel", "hyperlink", "open");
    private static final Identifier SAVE = Glowcase.id("channel", "hyperlink", "save");
    private static final Identifier CONFIRMATION = Glowcase.id("channel", "hyperlink", "confirmation");

    public static void openScreen(ServerPlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(player, OPEN, new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos));
    }

    public static void confirm(ServerPlayerEntity player, String url) {
        ServerPlayNetworking.send(player, CONFIRMATION, new PacketByteBuf(Unpooled.buffer()).writeString(url));
    }

    @Environment(EnvType.CLIENT)
    public static void save(BlockPos pos, String url) {
        ClientPlayNetworking.send(SAVE, new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos).writeString(url));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register(this::registerListeners);
    }

    @Environment(EnvType.CLIENT)
    private void registerListeners(ClientPlayNetworkHandler handler, MinecraftClient client) {
        ClientPlayNetworking.registerReceiver(OPEN, this::openScreen);
        ClientPlayNetworking.registerReceiver(CONFIRMATION, this::openConfirmationScreen);
    }

    @Environment(EnvType.CLIENT)
    private void openScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        client.execute(new ScreenOpener(client, buf.readBlockPos()));
    }

    @Environment(EnvType.CLIENT)
    private void openConfirmationScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        client.execute(new UrlOpener(client, buf.readString()));
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> ServerPlayNetworking.registerReceiver(handler, SAVE, this::save));
    }

    private void save(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readString();

        server.execute(() -> {
            BlockEntity blockEntity = player.world.getBlockEntity(pos);

            if (blockEntity instanceof HyperlinkBlockEntity && URL.matcher(url).matches()) {
                ((HyperlinkBlockEntity) blockEntity).url = url;
                blockEntity.markDirty();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private record ScreenOpener(MinecraftClient client, BlockPos pos) implements Runnable {
        @Override
        public void run() {
            if (this.client.world != null && this.client.world.getBlockEntity(this.pos) instanceof HyperlinkBlockEntity be) {
                MinecraftClient.getInstance().setScreen(new HyperlinkBlockEditScreen(be));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private record UrlOpener(MinecraftClient client, String url) implements Runnable, BooleanConsumer {
        @Override
        public void run() {
            this.client.setScreen(new ConfirmLinkScreen(this, url, false));
        }


        @Override
        public void accept(boolean bl) {
            if (bl) {
                Util.getOperatingSystem().open(url);
            }

            this.client.setScreen(null);
        }
    }
}
