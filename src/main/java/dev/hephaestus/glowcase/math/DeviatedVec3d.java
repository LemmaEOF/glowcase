package dev.hephaestus.glowcase.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

import java.util.function.Supplier;

public record DeviatedVec3d(Vec3d mean, Vec3d stdDev) implements DeviatedValue<Vec3d> {
	public static final DeviatedVec3d ZERO = new DeviatedVec3d(Vec3d.ZERO, Vec3d.ZERO);

	public static final Codec<DeviatedVec3d> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Vec3d.CODEC.fieldOf("mean").forGetter(DeviatedVec3d::mean),
		Vec3d.CODEC.fieldOf("std_dev").forGetter(DeviatedVec3d::stdDev)
	).apply(instance, DeviatedVec3d::new));

	public static final PacketCodec<ByteBuf, DeviatedVec3d> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.VECTOR3F.xmap(Vec3d::new, Vec3d::toVector3f),
		DeviatedVec3d::mean,
		PacketCodecs.VECTOR3F.xmap(Vec3d::new, Vec3d::toVector3f),
		DeviatedVec3d::stdDev,
		DeviatedVec3d::new
	);

	@Override
	public Vec3d get(Supplier<Double> random) {
		return new Vec3d(
			mean.x + random.get() * stdDev.x,
			mean.y + random.get() * stdDev.y,
			mean.z + random.get() * stdDev.z
		);
	}
}
