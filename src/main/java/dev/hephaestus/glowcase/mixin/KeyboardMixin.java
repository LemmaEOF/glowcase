package dev.hephaestus.glowcase.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Keyboard.class)
public class KeyboardMixin {

	@ModifyExpressionValue(
		method = "onKey",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/NarratorManager;isActive()Z")
	)
	private boolean preventNarratorToggleOnTextBlockScreen(boolean original) {
		//prevents the narrator from being toggled when pressing "ctrl+b" to hotkey bold formatting in the text block
		return original && !(MinecraftClient.getInstance().currentScreen instanceof TextBlockEditScreen);
	}

}
