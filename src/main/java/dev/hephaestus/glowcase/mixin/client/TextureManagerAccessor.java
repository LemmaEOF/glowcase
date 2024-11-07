package dev.hephaestus.glowcase.mixin.client;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {

	@Accessor("resourceContainer")
	ResourceManager glowcase$getResourceManager();
}
