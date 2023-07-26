package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.ItemDisplayBlockEditScreen;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

public class ItemDisplayBlockChannel implements ModInitializer, ClientModInitializer {
    private static final Identifier ID = Glowcase.id("channel", "item_display");

    public static void openScreen(ServerPlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(player, ID, new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos));
    }

    @Environment(EnvType.CLIENT)
    public static void sync(ItemDisplayBlockEntity itemDisplayBlockEntity, boolean updatePitchAndYaw) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(itemDisplayBlockEntity.getPos());
        buf.writeEnumConstant(itemDisplayBlockEntity.rotationType);
        buf.writeEnumConstant(itemDisplayBlockEntity.givesItem);
        buf.writeEnumConstant(itemDisplayBlockEntity.offset);
        buf.writeVarInt(itemDisplayBlockEntity.getCachedState().get(Properties.ROTATION));
        buf.writeBoolean(itemDisplayBlockEntity.showName);

        if (updatePitchAndYaw && MinecraftClient.getInstance().player != null) {
            Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(MinecraftClient.getInstance().player, itemDisplayBlockEntity.getPos());
            itemDisplayBlockEntity.pitch = pitchAndYaw.x;
            itemDisplayBlockEntity.yaw = pitchAndYaw.y;
        }

        buf.writeFloat(itemDisplayBlockEntity.pitch);
        buf.writeFloat(itemDisplayBlockEntity.yaw);

        ClientPlayNetworking.send(ID, buf);

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
    private void openScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        client.execute(new ScreenOpener(client, buf.readBlockPos()));
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register(((handler, server) ->
                ServerPlayNetworking.registerReceiver(handler, ID, this::save)));
    }

    private void save(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        BlockPos pos = buf.readBlockPos();
        ItemDisplayBlockEntity.RotationType rotationType = buf.readEnumConstant(ItemDisplayBlockEntity.RotationType.class);
        ItemDisplayBlockEntity.GivesItem givesItem = buf.readEnumConstant(ItemDisplayBlockEntity.GivesItem.class);
        ItemDisplayBlockEntity.Offset offset = buf.readEnumConstant(ItemDisplayBlockEntity.Offset.class);
        int rotation = buf.readVarInt();
        boolean showName = buf.readBoolean();

        float pitch = buf.readFloat();
        float yaw = buf.readFloat();

        server.execute(() -> {
            if (player.world.getBlockEntity(pos) instanceof ItemDisplayBlockEntity be) {
                be.givesItem = givesItem;
                be.rotationType = rotationType;
                be.offset = offset;
                be.pitch = pitch;
                be.yaw = yaw;
                be.showName = showName;

                player.world.setBlockState(pos, player.world.getBlockState(pos).with(Properties.ROTATION, rotation));

                be.markDirty();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private record ScreenOpener(MinecraftClient client, BlockPos pos) implements Runnable {
        @Override
        public void run() {
            if (this.client.world != null && client.world.getBlockEntity(this.pos) instanceof ItemDisplayBlockEntity be) {
                this.client.setScreen(new ItemDisplayBlockEditScreen(be));
            }
        }
    }
}