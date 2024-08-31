package dev.hephaestus.glowcase.item;

import dev.hephaestus.glowcase.mixin.LockableContainerBlockEntityAccessor;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class LockItem extends Item {
	public LockItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		if (world.isClient ||
			player == null ||
			!player.isCreative() ||
			!(world.getBlockEntity(context.getBlockPos()) instanceof LockableContainerBlockEntity be)) {
			return ActionResult.PASS;
		}

		var bea = (LockableContainerBlockEntityAccessor) be;
		Text message;

		if (bea.getLock().equals(ContainerLock.EMPTY)) {
			bea.setLock(new ContainerLock("glowcase"));
			message = Text.translatable("gui.glowcase.locked_block", be.getDisplayName());
		} else {
			bea.setLock(ContainerLock.EMPTY);
			message = Text.translatable("gui.glowcase.unlocked_block", be.getDisplayName());
		}

		player.sendMessage(message, true);
		player.playSoundToPlayer(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		be.markDirty();

		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack itemStack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
		tooltip.add(Text.translatable("item.glowcase.lock.tooltip.0").formatted(Formatting.GRAY));
	}
}
