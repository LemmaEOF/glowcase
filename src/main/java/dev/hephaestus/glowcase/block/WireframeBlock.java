package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.WireframeBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WireframeBlock extends GlowcaseBlock implements BlockEntityProvider {
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new WireframeBlockEntity(pos, state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (world.isClient && placer instanceof PlayerEntity player && canEditGlowcase(player, pos)) {
			//load any ctrl-picked NBT clientside
			NbtComponent blockEntityTag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
			if (blockEntityTag != null && world.getBlockEntity(pos) instanceof WireframeBlockEntity be) {
				blockEntityTag.applyToBlockEntity(be, world.getRegistryManager());
			}

			Glowcase.proxy.openWireframeBlockEditScreen(pos);
		}
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof WireframeBlockEntity be)) return ItemActionResult.CONSUME;

		if (world.isClient && player.getStackInHand(hand).isIn(Glowcase.ITEM_TAG) && canEditGlowcase(player, pos)) {
			Glowcase.proxy.openWireframeBlockEditScreen(pos);
		}

		return ItemActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack itemStack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
		tooltip.add(Text.translatable("block.glowcase.wireframe_block.tooltip.0").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("block.glowcase.generic.tooltip").formatted(Formatting.DARK_GRAY));
	}
}
