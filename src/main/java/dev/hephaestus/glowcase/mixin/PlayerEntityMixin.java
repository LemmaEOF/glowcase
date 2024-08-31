package dev.hephaestus.glowcase.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);

	@ModifyReturnValue(
		method = "shouldCancelInteraction",
		at = @At(value = "RETURN")
	)
	private boolean directLockInteraction(boolean original) {
		return getEquippedStack(EquipmentSlot.MAINHAND).getItem().equals(Glowcase.LOCK_ITEM.get()) || original;
	}
}
