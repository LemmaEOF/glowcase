package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class GlowcaseBlock extends Block {
	public GlowcaseBlock() {
		super(FabricBlockSettings.of(Material.METAL).nonOpaque().strength(-1, Integer.MAX_VALUE));
	}

	private static final VoxelShape PSEUDO_EMPTY = VoxelShapes.cuboid(0, -1000, 0, 0.1, -999.9, 0.1);

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (context != ShapeContext.absent() && context instanceof EntityShapeContext econtext &&
			econtext.getEntity() instanceof LivingEntity living &&
			living.getMainHandStack().isIn(Glowcase.ITEM_TAG)
		) {
			return VoxelShapes.fullCube();
		} else {
			return PSEUDO_EMPTY;
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
	}
}
