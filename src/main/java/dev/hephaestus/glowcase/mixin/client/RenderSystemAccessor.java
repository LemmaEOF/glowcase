package dev.hephaestus.glowcase.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor
{
	@Accessor(remap = false)
	static Vector3f[] getShaderLightDirections() {
		throw new AssertionError();
	}
}
