package net.modfest.glowcase.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.modfest.glowcase.Glowcase;
import org.jetbrains.annotations.Nullable;

public class GlowcaseBlock extends Block {

	private static final VoxelShape PSEUDO_EMPTY_SHAPE = VoxelShapes.cuboid(0, -1000, 0, 0.1, -999.9, 0.1);

	public GlowcaseBlock() {
		super(Settings.create().nonOpaque().strength(-1, Integer.MAX_VALUE));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (context != ShapeContext.absent() && context instanceof EntityShapeContext econtext &&
				econtext.getEntity() instanceof LivingEntity living && living.getMainHandStack().isIn(Glowcase.ITEM_TAG)) {
			return VoxelShapes.fullCube();
		}
		return PSEUDO_EMPTY_SHAPE;
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

	public boolean canPlayerEdit(PlayerEntity player, BlockPos pos) {
		return player.isCreative() && player.canModifyAt(player.getWorld(), pos);
	}
}
