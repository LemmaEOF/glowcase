package dev.hephaestus.glowcase.mixin.client.render.ber;

import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(method = "setWorld", at = @At("RETURN"))
	public void onSetWorld(ClientWorld clientWorld, CallbackInfo ci) {
		BakedBlockEntityRenderer.Manager.setWorld(clientWorld);
	}
}