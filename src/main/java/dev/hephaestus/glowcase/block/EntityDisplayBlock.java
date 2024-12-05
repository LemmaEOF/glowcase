package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.EntityDisplayBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityDisplayBlock extends GlowcaseBlock implements BlockEntityProvider {
	private static final VoxelShape OUTLINE = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	public EntityDisplayBlock() {
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
		return this.getDefaultState().with(Properties.ROTATION, MathHelper.floor((double) ((ctx.getPlayerYaw()) * 16.0F / 360.0F) + 0.5D) & 15);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof EntityDisplayBlockEntity be)) return ActionResult.CONSUME;

		if (be.canGiveTo(player)) {
			if (!world.isClient) be.giveTo(player);
			return ActionResult.SUCCESS;
		}

		return ActionResult.CONSUME;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof EntityDisplayBlockEntity be)) return ItemActionResult.CONSUME;

		if (canEditGlowcase(player, pos)) {
			boolean holdingGlowcaseItem = stack.isIn(Glowcase.ITEM_TAG);
			boolean holdingSameAsDisplay = ItemStack.areItemsEqual(be.getDisplayedStack(), stack);
			boolean isSpawnEgg = stack.getItem() instanceof SpawnEggItem;

			if (!be.hasItem() && isSpawnEgg) {
				if (!world.isClient) be.setStack(stack);
				return ItemActionResult.SUCCESS;
			} else if (holdingSameAsDisplay) {
				if (world.isClient) Glowcase.proxy.openEntityDisplayBlockEditScreen(pos);
				return ItemActionResult.SUCCESS;
			} else if (holdingGlowcaseItem) {
				if (!world.isClient) be.setStack(ItemStack.EMPTY);
				return ItemActionResult.SUCCESS;
			}
		}

		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EntityDisplayBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, Glowcase.ENTITY_DISPLAY_BLOCK_ENTITY.get(), EntityDisplayBlockEntity::tick);
	}

	@Override
	public void appendTooltip(ItemStack itemStack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
		tooltip.add(Text.translatable("block.glowcase.entity_display_block.tooltip.0").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("block.glowcase.entity_display_block.tooltip.1").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("block.glowcase.entity_display_block.tooltip.2").formatted(Formatting.DARK_GRAY));
	}

}
