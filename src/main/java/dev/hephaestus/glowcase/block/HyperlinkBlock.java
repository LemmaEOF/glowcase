package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HyperlinkBlock extends GlowcaseBlock implements BlockEntityProvider {
	private static final VoxelShape OUTLINE = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new HyperlinkBlockEntity(pos, state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (world.isClient && placer instanceof PlayerEntity player && canEdit(player, pos)) {
			//load any ctrl-picked NBT clientside
			NbtCompound blockEntityTag = BlockItem.getBlockEntityNbt(stack);
			if(blockEntityTag != null && world.getBlockEntity(pos) instanceof HyperlinkBlockEntity be) {
				be.readNbt(blockEntityTag);
			}
			
			Glowcase.proxy.openHyperlinkBlockEditScreen(pos);
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof HyperlinkBlockEntity be)) return ActionResult.CONSUME;

		if (world.isClient) {
			if (player.getStackInHand(hand).isIn(Glowcase.ITEM_TAG) && canEdit(player, pos)) {
				Glowcase.proxy.openHyperlinkBlockEditScreen(pos);
			} else {
				Glowcase.proxy.openUrlWithConfirmation(be.getUrl());
			}
		}

		return ActionResult.SUCCESS;
	}
	
	public static boolean canEdit(PlayerEntity player, BlockPos pos) {
		return player.isCreative() && player.canModifyAt(player.getWorld(), pos);
	}
}