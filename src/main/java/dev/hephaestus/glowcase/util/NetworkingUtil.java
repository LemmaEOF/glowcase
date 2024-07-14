package dev.hephaestus.glowcase.util;

import dev.hephaestus.glowcase.block.GlowcaseBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class NetworkingUtil {
    private NetworkingUtil() {
    }

    public static boolean cantEditGlowcase(ServerPlayerEntity player, BlockPos pos, GlowcaseBlock glowcase) {
        if (player.getServerWorld() == null) return true;
        if (!player.getServerWorld().isChunkLoaded(ChunkPos.toLong(pos))) return true;
        if (!(player.squaredDistanceTo(pos.toCenterPos()) <= 12 * 12)) return true;
        return !glowcase.canEditGlowcase(player, pos);
    }
}
