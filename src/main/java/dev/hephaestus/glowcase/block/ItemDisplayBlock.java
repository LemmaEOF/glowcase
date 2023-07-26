package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.networking.ItemDisplayBlockChannel;
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
import net.minecraft.server.network.ServerPlayerEntity;
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
		super.appendProperties(builder);
		builder.add(Properties.ROTATION);
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
		if (!world.isClient) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			ItemStack handStack = player.getStackInHand(hand);
			if (blockEntity instanceof ItemDisplayBlockEntity display) {
				if (canGiveTo(display, player) && display.hasItem() && handStack.isEmpty()) {
					player.setStackInHand(hand, display.getUseStack().copy());
					if (!player.isCreative()) {
						display.givenTo.add(player.getUuid());
						display.markDirty();
					}
					return ActionResult.SUCCESS;
				} else if (player.isCreative() && world.canPlayerModifyAt(player, pos)) {
					if (!display.hasItem() && !handStack.isEmpty()) {
						display.setStack(handStack.copy());
						return ActionResult.SUCCESS;
					} else if (display.hasItem() && display.getUseStack().isItemEqualIgnoreDamage(handStack)) {
						ItemDisplayBlockChannel.openScreen((ServerPlayerEntity) player, pos);

						return ActionResult.SUCCESS;
					} else if (display.hasItem() && handStack.isIn(Glowcase.ITEM_TAG)) {
						display.setStack(ItemStack.EMPTY);
						return ActionResult.SUCCESS;
					}
				}
			}

			return ActionResult.PASS;
		}

		return ActionResult.SUCCESS;
	}

	private boolean canGiveTo(ItemDisplayBlockEntity be, PlayerEntity player) {
		if (be.givesItem == ItemDisplayBlockEntity.GivesItem.ONCE) return !be.givenTo.contains(player.getUuid()) || player.isCreative();
		return be.givesItem == ItemDisplayBlockEntity.GivesItem.YES;
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