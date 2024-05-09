package net.modfest.glowcase.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class GlowcaseRenderLayers extends RenderPhase {
	// Use a custom render layer to render the text plate - mimics DrawableHelper's RenderSystem call
	public static final RenderLayer TEXT_PLATE = RenderLayer.of("glowcase_text_plate", VertexFormats.POSITION_COLOR,
		VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
			.texture(NO_TEXTURE)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(COLOR_MASK)
			.program(COLOR_PROGRAM)
			.build(false));

	private GlowcaseRenderLayers() {
		super(null, null, null);
	}
}
