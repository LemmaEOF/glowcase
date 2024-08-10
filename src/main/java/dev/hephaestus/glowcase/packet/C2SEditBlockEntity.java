package dev.hephaestus.glowcase.packet;

import dev.hephaestus.glowcase.block.GlowcaseBlock;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public interface C2SEditBlockEntity extends CustomPayload {

	BlockPos pos();

	void receive(ServerWorld world, BlockEntity blockEntity);

	default void receive(ServerPlayNetworking.Context context) {
		if (!canEdit(context.player())) return;
		receive(context.player().getServerWorld(), context.player().getServerWorld().getBlockEntity(this.pos()));
	}

	default void send() {
		ClientPlayNetworking.send(this);
	}

	default boolean canEdit(ServerPlayerEntity player) {
		if (!player.getServerWorld().isChunkLoaded(ChunkPos.toLong(pos()))) return false;
		if (player.squaredDistanceTo(pos().toCenterPos()) > (12 * 12)) return false;
		return player.getServerWorld().getBlockState(pos()).getBlock() instanceof GlowcaseBlock block && block.canEditGlowcase(player, pos());
	}
}
