package dev.hephaestus.glowcase.client.render.block.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;

import dev.hephaestus.glowcase.mixin.client.render.BufferBuilderAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	 * Override renderBaked and renderUnbaked instead of this method.
	 */
	@Override
	public final void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		renderUnbaked(entity, tickDelta, matrices, vertexConsumers, light, overlay);
		BakedBlockEntityRendererManager.activateRegion(entity.getPos());
	}

	/**
	 * Render vertices to be baked into the render region. This method will be called every time the render region is rebuilt - so
	 * you should only render vertices that don't move here. You can call invalidateSelf or VertexBufferManager.invalidate to
	 * cause the render region to be rebuilt, but do not call this too frequently as it will affect performance.
	 *
	 * You must use the provided VertexConsumerProvider and MatrixStack to render your vertices - any use of Tessellator
	 * or RenderSystem here will not work. If you need custom rendering settings, you can use a custom RenderLayer.
	 */
	public abstract void renderBaked(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

	/**
	 * Render vertices immediately. This works exactly the same way as a normal BER render method, and can be used for dynamic
	 * rendering that changes every frame. In this method you can also check for render invalidation and call invalidateSelf
	 * as appropriate.
	 */
	public abstract void renderUnbaked(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

	public abstract boolean shouldBake(T entity);

	private static record RenderRegionPos(int x, int z, @Nullable BlockPos origin) {
		public RenderRegionPos(int x, int z) {
			this(x, z, new BlockPos(x << BakedBlockEntityRendererManager.REGION_SHIFT, 0, z << BakedBlockEntityRendererManager.REGION_SHIFT));
		}

		public RenderRegionPos(BlockPos pos) {
			this(pos.getX() >> BakedBlockEntityRendererManager.REGION_SHIFT, pos.getZ() >> BakedBlockEntityRendererManager.REGION_SHIFT);
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

	public static class BakedBlockEntityRendererManager {
		// 2x2 chunks size for regions
		public static final int REGION_FROMCHUNK_SHIFT = 1;
		public static final int REGION_SHIFT = 4 + REGION_FROMCHUNK_SHIFT;
		public static final int MAX_XZ_IN_REGION = (16 << REGION_FROMCHUNK_SHIFT) - 1;
		public static final int VIEW_RADIUS = 3;

		private static final Map<RenderRegionPos, RegionBuffer> regions = new Object2ObjectOpenHashMap<>();

		private static final Set<RenderRegionPos> needsRebuild = Sets.newHashSet();
		private static final Map<RenderRegionPos, RegionBufferBuilder> builders = new Object2ObjectOpenHashMap<>();

		private static final Matrix3f MATRIX3F_IDENTITY = new Matrix3f();
		static { MATRIX3F_IDENTITY.loadIdentity(); }

		private static ClientWorld currentWorld = null;

		private static final Logger LOGGER = LogUtils.getLogger();

		private static class RegionBuffer {
			private final Map<RenderLayer, VertexBuffer> layerBuffers = new Object2ObjectArrayMap<>();
			private final Map<RenderLayer, VertexBuffer> uploadedLayerBuffers = new Reference2ReferenceArrayMap<>();

			public void render(RenderLayer l, MatrixStack matrices, Matrix4f projectionMatrix) {
				VertexBuffer buf = uploadedLayerBuffers.get(l);
				buf.bind();
				buf.draw(matrices.peek().getPositionMatrix(), projectionMatrix, RenderSystem.getShader());
			}

			public void reset() {
				uploadedLayerBuffers.clear();
			}

			public void upload(RenderLayer l, BufferBuilder newBuf) {
				VertexBuffer buf = layerBuffers.computeIfAbsent(l, renderLayer -> new VertexBuffer());
 
				buf.bind();
				buf.upload(newBuf.end());

				uploadedLayerBuffers.put(l, buf);
			}

			public void deallocate() {
				layerBuffers.values().forEach(VertexBuffer::close);
				uploadedLayerBuffers.clear();
			}

			public Set<RenderLayer> getAllUploadedLayers() {
				return uploadedLayerBuffers.keySet();
			}
		}

		private static class RegionBufferBuilder implements VertexConsumerProvider, Iterable<Map.Entry<RenderLayer, BufferBuilder>> {
			private final Map<RenderLayer, BufferBuilder> layerBuffers = new Object2ObjectArrayMap<>();
			private final Map<RenderLayer, BufferBuilder> usedLayerBuffers = new Reference2ReferenceArrayMap<>();

			public void reset() {
				layerBuffers.values().forEach(buf -> ((BufferBuilderAccessor) buf).invokeResetBuilding());
				usedLayerBuffers.clear();
			}

			@Override
			public VertexConsumer getBuffer(RenderLayer layer) {
				return layerBuffers.compute(layer, (l, buf) -> {
					if (buf == null) buf = new BufferBuilder(l.getExpectedBufferSize());
					if (!buf.isBuilding()) buf.begin(l.getDrawMode(), l.getVertexFormat());
					usedLayerBuffers.put(layer, buf);
					return buf;
				});
			}

			public @NotNull Iterator<Map.Entry<RenderLayer, BufferBuilder>> iterator() {
				return usedLayerBuffers.entrySet().iterator();
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

		@SuppressWarnings({"rawtypes", "unchecked"})
		public static void render(WorldRenderContext wrc) {
			wrc.profiler().push("glowcase:baked_block_entity_rendering");

			Vec3d cam = wrc.camera().getPos();

			if (!needsRebuild.isEmpty()) {
				wrc.profiler().push("rebuild");
				//  Make builders for regions that are marked for rebuild, render and upload to RegionBuffers
				Set<RenderRegionPos> rebuilt = Sets.newLinkedHashSet();
				Set<RenderRegionPos> removing = Sets.newLinkedHashSet();
				List<BlockEntity> blockEntities = new ArrayList<>();
				MatrixStack bakeMatrices = new MatrixStack();
				for (RenderRegionPos rrp : needsRebuild) {
					if (isVisiblePos(rrp, cam)) {
						// For the current region, rebuild each render layer using the buffer builders
						// Find all block entities in this region
						if (currentWorld == null) {
							break;
						}

						RegionBufferBuilder builder = builders.compute(rrp, (k, v) -> {
							if (v != null) v.reset();
							else v = new RegionBufferBuilder();
							return v;
						});

						for (int chunkX = rrp.x << REGION_FROMCHUNK_SHIFT; chunkX < (rrp.x + 1) << REGION_FROMCHUNK_SHIFT; chunkX++) {
							for (int chunkZ = rrp.z << REGION_FROMCHUNK_SHIFT; chunkZ < (rrp.z + 1) << REGION_FROMCHUNK_SHIFT; chunkZ++) {
								blockEntities.addAll(currentWorld.getChunk(chunkX, chunkZ).getBlockEntities().values());
							}
						}

						if (!blockEntities.isEmpty()) {
							boolean bakedMaybeAnything = false;

							for (BlockEntity be : blockEntities) {
								if (mc.getBlockEntityRenderDispatcher().get(be) instanceof BakedBlockEntityRenderer renderer && renderer.shouldBake(be)) {
									BlockPos pos = be.getPos();
									bakeMatrices.push();
									bakeMatrices.translate(pos.getX() & MAX_XZ_IN_REGION, pos.getY(), pos.getZ() & MAX_XZ_IN_REGION);
									try {
										renderer.renderBaked(be, bakeMatrices, builder, WorldRenderer.getLightmapCoordinates(currentWorld, pos), OverlayTexture.DEFAULT_UV);
									} catch (Throwable t) {
										LOGGER.error("Block entity renderer threw exception during baking : ");
										t.printStackTrace();
									} finally {
										bakedMaybeAnything = true;
									}
									bakeMatrices.pop();
								}
							}
							blockEntities.clear();

							if (bakedMaybeAnything) {
								RegionBuffer buf = regions.computeIfAbsent(rrp, k -> new RegionBuffer());
								buf.reset();

								builder.forEach(layerBuilder -> buf.upload(layerBuilder.getKey(), layerBuilder.getValue()));
								rebuilt.add(rrp);
							} else {
								removing.add(rrp);
							}
						} else {
							removing.add(rrp);
						}
					}
				}
				rebuilt.forEach(needsRebuild::remove);
				removing.forEach(needsRebuild::remove);
				wrc.profiler().pop();
				removing.forEach(rrp -> {
					RegionBuffer buf = regions.get(rrp);

					if (buf != null) {
						buf.deallocate();
						regions.remove(rrp, buf);
					}
				});
			}

			if (!regions.isEmpty()) {
				wrc.profiler().swap("render");
				/**
				 * Set the inverse view rotation matrix to the identity matrix, this fixes the fog color bleeding into the color of the rendered object at close distances,
				 * this isn't a complete fix since fog still looks a bit funky at far distances
				*/
				Matrix3f originalViewRotationMatrix = RenderSystem.getInverseViewRotationMatrix();
				RenderSystem.setInverseViewRotationMatrix(MATRIX3F_IDENTITY);
				// Iterate over all RegionBuffers, render visible and remove non-visible RegionBuffers
				Iterator<Map.Entry<RenderRegionPos, RegionBuffer>> iterBuffers = regions.entrySet().iterator();
				MatrixStack matrices = wrc.matrixStack();
				matrices.push();
				matrices.translate(-cam.x, -cam.y, -cam.z);
				while (iterBuffers.hasNext()) {
					Map.Entry<RenderRegionPos, RegionBuffer> entry = iterBuffers.next();
					RenderRegionPos rrp = entry.getKey();
					RegionBuffer regionBuffer = entry.getValue();
					if (isVisiblePos(entry.getKey(), cam)) {
						// Iterate over used render layers in the region, render them
						matrices.push();
						matrices.translate(rrp.origin.getX(), rrp.origin.getY(), rrp.origin.getZ());
						for (RenderLayer layer : regionBuffer.getAllUploadedLayers()) {
							layer.startDrawing();
							regionBuffer.render(layer, matrices, wrc.projectionMatrix());
							layer.endDrawing();
							VertexBuffer.unbind();
						}
						matrices.pop();
					} else {
						regionBuffer.deallocate();
						iterBuffers.remove();
					}
				}
				RenderSystem.setInverseViewRotationMatrix(originalViewRotationMatrix);
				matrices.pop();
			}
			wrc.profiler().pop();
			RenderSystem.setShaderColor(1, 1, 1, 1);
		}

		public static void activateRegion(BlockPos pos) {
			RenderRegionPos rrp = new RenderRegionPos(pos);
			if (!regions.containsKey(rrp)) {
				markForRebuild(pos);
			}
		}

		public static void reset() {
			// Reset everything
			regions.values().forEach(RegionBuffer::deallocate);
			regions.clear();
			needsRebuild.clear();
			builders.clear();
		}

		public static void setWorld(ClientWorld world) {
			reset();
			currentWorld = world;
		}
	}
}
