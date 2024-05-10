package net.modfest.glowcase.mixin.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {

	@Invoker
	void invokeResetBuilding();
}
