package dev.hephaestus.glowcase.util;

import dev.hephaestus.glowcase.block.GlowcaseBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class NetworkingUtil {
	private NetworkingUtil() {
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean canEditGlowcase(ServerPlayerEntity player, BlockPos pos, GlowcaseBlock glowcase) {
		if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;
		if (!serverWorld.isChunkLoaded(ChunkPos.toLong(pos))) return false;
		if (!(player.squaredDistanceTo(pos.toCenterPos()) > 12 * 12)) return false;
		return glowcase.canEditGlowcase(player, pos);
	}
}
