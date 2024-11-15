package dev.hephaestus.glowcase.math;

import java.util.function.Supplier;

public interface DeviatedValue<T> {
	T mean();
	T stdDev();

	T get(Supplier<Double> random);
}
