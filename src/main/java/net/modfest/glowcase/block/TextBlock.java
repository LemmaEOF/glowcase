package net.modfest.glowcase.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.modfest.glowcase.Glowcase;
import net.modfest.glowcase.block.entity.TextBlockEntity;
import org.jetbrains.annotations.Nullable;

public class TextBlock extends GlowcaseBlock implements BlockEntityProvider {

	public TextBlock() {
		super();
		setDefaultState(getDefaultState().with(Properties.ROTATION, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.ROTATION);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(Properties.ROTATION, MathHelper.floor((double) ((180.0f + ctx.getPlayerYaw()) * 16.0f / 360.0f) + 0.5d) & 15);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TextBlockEntity(pos, state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (world.isClient && placer instanceof PlayerEntity player && canPlayerEdit(player, pos)) {
			var blockEntityData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
			if (blockEntityData != null && world.getBlockEntity(pos) instanceof TextBlockEntity blockEntity) {
				blockEntityData.applyToBlockEntity(blockEntity, world.getRegistryManager());
			}
			Glowcase.proxy.openTextBlockEditScreen(pos);
		}
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof TextBlockEntity)) {
			return ItemActionResult.CONSUME;
		}
		if (world.isClient && player.getStackInHand(hand).isIn(Glowcase.ITEM_TAG) && canPlayerEdit(player, pos)) {
			Glowcase.proxy.openTextBlockEditScreen(pos);
		}
		return ItemActionResult.SUCCESS;
	}
}
