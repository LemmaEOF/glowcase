package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemDisplayBlock extends GlowcaseBlock implements BlockEntityProvider {
	private static final VoxelShape OUTLINE = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	public ItemDisplayBlock() {
		super();
		this.setDefaultState(this.getDefaultState().with(Properties.ROTATION, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(Properties.ROTATION));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(Properties.ROTATION, MathHelper.floor((double)((ctx.getPlayerYaw()) * 16.0F / 360.0F) + 0.5D) & 15);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if(!(world.getBlockEntity(pos) instanceof ItemDisplayBlockEntity be)) return ActionResult.CONSUME;
		
		ItemStack handStack = player.getStackInHand(hand);
		
		if(canEditGlowcase(player, pos)) {
			boolean holdingGlowcaseItem = handStack.isIn(Glowcase.ITEM_TAG);
			boolean holdingSameAsDisplay = ItemStack.areItemsEqual(be.getDisplayedStack(), handStack);
			
			if(!be.hasItem() && !handStack.isEmpty()) {
				if(!world.isClient) be.setStack(handStack);
				return ActionResult.SUCCESS;
			} else if(holdingSameAsDisplay) {
				if(world.isClient) Glowcase.proxy.openItemDisplayBlockEditScreen(pos);
				return ActionResult.SUCCESS;
			} else if(holdingGlowcaseItem) {
				if(!world.isClient) be.setStack(ItemStack.EMPTY);
				return ActionResult.SUCCESS;
			}
		}
		
		if(be.canGiveTo(player) && handStack.isEmpty()) {
			if(!world.isClient) be.giveTo(player, hand);
			return ActionResult.SUCCESS;
		}

		return ActionResult.CONSUME;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ItemDisplayBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, Glowcase.ITEM_DISPLAY_BLOCK_ENTITY, ItemDisplayBlockEntity::tick);
	}
}