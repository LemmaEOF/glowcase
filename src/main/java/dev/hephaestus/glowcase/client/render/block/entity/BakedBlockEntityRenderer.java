package dev.hephaestus.glowcase.client.render.block.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.util.*;

public abstract class BakedBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	protected final BlockEntityRendererFactory.Context context;

	protected BakedBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		this.context = context;
	}

	/**
	 * Handles invalidation and passing of rendered vertices to the baking system.
	 * Override {@link #renderBaked(BlockEntity, MatrixStack, VertexConsumerProvider, int, int)} and
	 * {@link #renderBaked(BlockEntity, MatrixStack, VertexConsumerProvider, int, int)} instead of this method.
	 */
	@Override
	public final void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		renderUnbaked(entity, tickDelta, matrices, vertexConsumers, light, overlay);
		Manager.activateRegion(entity.getPos());
	}

	/**
	 * Render vertices to be baked into the render region. This method will be called every time the render region is rebuilt - so
	 * you should only render vertices that don't move here. You can call {@link Manager#markForRebuild(BlockPos)} to
	 * cause the render region to be rebuilt, but do not call this too frequently as it will affect performance.
	 * You must use the provided VertexConsumerProvider and MatrixStack to render your vertices - any use of Tessellator
	 * or RenderSystem here will not work. If you need custom rendering settings, you can use a custom RenderLayer.
	 */
	public abstract void renderBaked(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

	/**
	 * Render vertices immediately. This works exactly the same way as a normal BER render method, and can be used for dynamic
	 * rendering that changes every frame. In this method you can also check for render invalidation and call {@link Manager#markForRebuild(BlockPos)}
	 * as appropriate.
	 */
	public abstract void renderUnbaked(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

	public abstract boolean shouldBake(T entity);

	private record RenderRegionPos(int x, int z, @NotNull BlockPos origin) {
		public RenderRegionPos(int x, int z) {
			this(x, z, new BlockPos(x << Manager.REGION_SHIFT, 0, z << Manager.REGION_SHIFT));
		}

		public RenderRegionPos(BlockPos pos) {
			this(pos.getX() >> Manager.REGION_SHIFT, pos.getZ() >> Manager.REGION_SHIFT);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RenderRegionPos that = (RenderRegionPos) o;
			return x == that.x &&
				z == that.z;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, z);
		}
	}

	public static class Manager {
		// 2x2 chunks size for regions
		public static final int REGION_FROMCHUNK_SHIFT = 1;
		public static final int REGION_SHIFT = 4 + REGION_FROMCHUNK_SHIFT;
		public static final int MAX_XZ_IN_REGION = (16 << REGION_FROMCHUNK_SHIFT) - 1;
		public static final int VIEW_RADIUS = 3;

		private static final ReferenceArrayList<VertexBuffer> USEABLE_VERTEX_BUFFERS = new ReferenceArrayList<>(256);

		private static final Object2ReferenceMap<RenderRegionPos, RegionBuffer> regions = new Object2ReferenceOpenHashMap<>();
		private static final Set<RenderRegionPos> needsRebuild = Sets.newHashSet();

		private static class CachedVertexConsumerProvider implements VertexConsumerProvider {

			private final Reference2ReferenceMap<RenderLayer, BufferAllocator> allocators = new Reference2ReferenceOpenHashMap<>();
			private final Reference2ReferenceMap<RenderLayer, BufferBuilder> builders = new Reference2ReferenceOpenHashMap<>();

			@Override
			public VertexConsumer getBuffer(RenderLayer l) {
				var allocator = allocators.computeIfAbsent(l, l1 -> new BufferAllocator(l.getExpectedBufferSize()));
                var builder = builders.computeIfAbsent(l, l1 -> new BufferBuilder(
								allocator,
								l.getDrawMode(),
								l.getVertexFormat()));
				return builder;
			}

			/**
			 * Resets the provider so another scene can be rendered
			 */
			public void reset() {
				allocators.forEach((layer, allocator) -> allocator.reset());
				builders.clear();
			}
		}

		private static final CachedVertexConsumerProvider vcp = new CachedVertexConsumerProvider();

		private static ClientWorld currentWorld = null;

		private static final Logger LOGGER = LogUtils.getLogger();

		private static VertexBuffer getVertexBuffer() {
			if (!USEABLE_VERTEX_BUFFERS.isEmpty()) return USEABLE_VERTEX_BUFFERS.pop();
			return new VertexBuffer(VertexBuffer.Usage.STATIC);
		}

		private static void releaseVertexBuffer(VertexBuffer buf) {
			USEABLE_VERTEX_BUFFERS.add(buf);
		}

		private static class RegionBuffer {
			private final Map<RenderLayer, VertexBuffer> layerBuffers = new Reference2ReferenceOpenHashMap<>();
			private final Set<RenderLayer> uploadedLayers = new ObjectOpenHashSet<>();

			public void render(RenderLayer l, MatrixStack matrices, Matrix4f projectionMatrix) {
				VertexBuffer buf = layerBuffers.get(l);
				buf.bind();
				l.startDrawing();
				buf.draw(matrices.peek().getPositionMatrix(), projectionMatrix, RenderSystem.getShader());
				l.endDrawing();
				VertexBuffer.unbind();
			}

			public void reset() {
				uploadedLayers.clear();
			}

			public void upload(RenderLayer l, BufferBuilder newBuf) {
				VertexBuffer buf = layerBuffers.computeIfAbsent(l, renderLayer -> getVertexBuffer());
				buf.bind();
				buf.upload(newBuf.end());
				VertexBuffer.unbind();

				uploadedLayers.add(l);
			}

			public void release() {
				layerBuffers.values().forEach(Manager::releaseVertexBuffer);
				uploadedLayers.clear();
			}
		}

		/**
		 * Causes the render region containing this BlockEntity to be rebuilt -
		 * do not call this too frequently as it will affect performance.
		 * An invalidation will not immediately cause the next frame to contain an updated view (and call to renderBaked)
		 * as all render region rebuilds must call every BER that is to be rendered, otherwise they will be missing from the
		 * vertex buffer.
		 */
		public static void markForRebuild(BlockPos pos) {
			needsRebuild.add(new RenderRegionPos(pos));
		}

		// TODO: move chunk baking off-thread?

		private static boolean isVisiblePos(RenderRegionPos rrp, Vec3d cam) {
			return Math.abs(rrp.x - ((int)cam.getX() >> REGION_SHIFT)) <= VIEW_RADIUS && Math.abs(rrp.z - ((int)cam.getZ() >> REGION_SHIFT)) <= VIEW_RADIUS;
		}

		@SuppressWarnings("unchecked")
		public static void render(WorldRenderContext wrc) {
			wrc.profiler().push("glowcase:baked_block_entity_rendering");

			Vec3d cam = wrc.camera().getPos();

			if (!needsRebuild.isEmpty()) {
				wrc.profiler().push("rebuild");

				// Make builders for regions that are marked for rebuild, render and upload to RegionBuffers
				Set<RenderRegionPos> removing = Sets.newHashSet();
				List<BlockEntity> blockEntities = new ArrayList<>();
				MatrixStack bakeMatrices = new MatrixStack();
				for (RenderRegionPos rrp : needsRebuild) {
					if (isVisiblePos(rrp, cam)) {
						// For the current region, rebuild each render layer using the buffer builders
						// Find all block entities in this region
						for (int chunkX = rrp.x << REGION_FROMCHUNK_SHIFT; chunkX < (rrp.x + 1) << REGION_FROMCHUNK_SHIFT; chunkX++) {
							for (int chunkZ = rrp.z << REGION_FROMCHUNK_SHIFT; chunkZ < (rrp.z + 1) << REGION_FROMCHUNK_SHIFT; chunkZ++) {
								blockEntities.addAll(currentWorld.getChunk(chunkX, chunkZ).getBlockEntities().values());
							}
						}

						if (!blockEntities.isEmpty()) {
							boolean bakedAnything = false;
							
							for (BlockEntity be : blockEntities) {
								if (mc.getBlockEntityRenderDispatcher().get(be) instanceof BakedBlockEntityRenderer renderer && renderer.shouldBake(be)) {
									BlockPos pos = be.getPos();
									bakeMatrices.push();
									bakeMatrices.translate(pos.getX() & MAX_XZ_IN_REGION, pos.getY(), pos.getZ() & MAX_XZ_IN_REGION);
									try {
										renderer.renderBaked(be, bakeMatrices, vcp, WorldRenderer.getLightmapCoordinates(currentWorld, pos), OverlayTexture.DEFAULT_UV);
										bakedAnything = true;
									} catch (Throwable t) {
										LOGGER.error("Block entity renderer threw exception during baking : ", t);
									}
									bakeMatrices.pop();
								}
							}
							blockEntities.clear();

							if (bakedAnything) {
								RegionBuffer buf = regions.computeIfAbsent(rrp, k -> new RegionBuffer());
								buf.reset();
								vcp.builders.forEach(buf::upload);
								vcp.reset();
							} else {
								removing.add(rrp);
							}
						} else {
							removing.add(rrp);
						}
					}
				}
				// We've processed all pending rebuilds now
				needsRebuild.clear();
				// These regions no longer contain anything
				removing.forEach(rrp -> {
					RegionBuffer buf = regions.get(rrp);
					if (buf != null) {
						buf.release();
						regions.remove(rrp, buf);
					}
				});

				wrc.profiler().pop();
			}

			if (!regions.isEmpty()) {
				wrc.profiler().push("render");

				/*
				 * Set the fog end to an extremely high value, this is a total hack but.
				 * It's needed to make fog not bleed into text blocks
				 */
				float originalFogEnd = RenderSystem.getShaderFogEnd();
				RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
				// Iterate over all RegionBuffers, render visible and remove non-visible RegionBuffers
				MatrixStack matrices = wrc.matrixStack();
				matrices.push();
				matrices.multiplyPositionMatrix(wrc.positionMatrix());
				matrices.translate(-cam.x, -cam.y, -cam.z);
				var iter = regions.object2ReferenceEntrySet().iterator();
				while (iter.hasNext()) {
					var entry = iter.next();
					RenderRegionPos rrp = entry.getKey();
					RegionBuffer regionBuffer = entry.getValue();
					if (isVisiblePos(entry.getKey(), cam)) {
						// Iterate over used render layers in the region, render them
						matrices.push();
						matrices.translate(rrp.origin.getX(), rrp.origin.getY(), rrp.origin.getZ());
						for (RenderLayer l : regionBuffer.uploadedLayers)
							regionBuffer.render(l, matrices, wrc.projectionMatrix());
						matrices.pop();
					} else {
						regionBuffer.release();
						iter.remove();
					}
				}
				RenderSystem.setShaderFogEnd(originalFogEnd);
				matrices.pop();

				wrc.profiler().pop();
			}

			RenderSystem.setShaderColor(1, 1, 1, 1);

			wrc.profiler().pop();
		}

		public static void activateRegion(BlockPos pos) {
			RenderRegionPos rrp = new RenderRegionPos(pos);
			if (!regions.containsKey(rrp)) {
				markForRebuild(pos);
			}
		}

		public static void reset() {
			regions.values().forEach(RegionBuffer::release);
			regions.clear();
			needsRebuild.clear();
		}

		public static void setWorld(ClientWorld world) {
			reset();
			currentWorld = world;
		}
	}
}
