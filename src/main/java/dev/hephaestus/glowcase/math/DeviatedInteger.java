package dev.hephaestus.glowcase.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

public record DeviatedInteger(Integer mean, Integer stdDev) implements DeviatedValue<Integer> {
	public static final DeviatedInteger ZERO = new DeviatedInteger(0, 0);

	public static final Codec<DeviatedInteger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("mean").forGetter(DeviatedInteger::mean),
		Codec.INT.fieldOf("std_dev").forGetter(DeviatedInteger::stdDev)
	).apply(instance, DeviatedInteger::new));

	public static final PacketCodec<ByteBuf, DeviatedInteger> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.VAR_INT,
		DeviatedInteger::mean,
		PacketCodecs.VAR_INT,
		DeviatedInteger::stdDev,
		DeviatedInteger::new
	);

	@Override
	public Integer get(Supplier<Double> random) {
		return mean + MathHelper.floor(random.get() * stdDev);
	}
}
