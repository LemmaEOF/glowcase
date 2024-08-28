package dev.hephaestus.glowcase.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public abstract class GlowcaseRenderLayers extends RenderLayer {
	// Use a custom render layer to render the text plate - mimics DrawableHelper's RenderSystem call
	public static final RenderLayer TEXT_PLATE = RenderLayer.of("glowcase_text_plate", VertexFormats.POSITION_COLOR,
		VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
			.texture(NO_TEXTURE)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(COLOR_MASK)
			.program(COLOR_PROGRAM)
			.build(false));

	public GlowcaseRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}
}
