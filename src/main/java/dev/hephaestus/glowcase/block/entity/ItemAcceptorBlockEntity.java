package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemAcceptorBlockEntity extends BlockEntity {
	private Identifier item = Identifier.ofVanilla("air");
	public int count = 1;
	public boolean isItemTag = false;
	private List<Item> itemTagList = List.of();

	public ItemAcceptorBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ITEM_ACCEPTOR_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putString("item", this.item.toString());
		tag.putInt("count", this.count);
		tag.putBoolean("is_item_tag", this.isItemTag);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(tag, registryLookup);

		setItem(Identifier.tryParse(tag.getString("item")));
		this.count = tag.getInt("count");
		this.isItemTag = tag.getBoolean("is_item_tag");
	}

	public Identifier getItem() {
		return item;
	}

	public void setItem(Identifier item) {
		this.item = item;

		TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, item);
		itemTagList = Registries.ITEM.stream().filter(it -> it.getDefaultStack().isIn(itemTag)).toList();
	}

	public ItemStack getDisplayItemStack() {
		if (isItemTag) {
			if (itemTagList.isEmpty()) {
				return ItemStack.EMPTY;
			}

			return itemTagList.get((int) (Util.getMeasuringTimeMs() / 1000f) % itemTagList.size()).getDefaultStack();
		} else {
			return Registries.ITEM.get(item).getDefaultStack();
		}
	}

	public boolean isItemAccepted(ItemStack stack) {
		boolean isEqual = isItemTag
			? stack.isIn(TagKey.of(RegistryKeys.ITEM, item))
			: stack.isOf(Registries.ITEM.get(item));

		return isEqual && stack.getCount() >= count;
	}

	@SuppressWarnings({"MethodCallSideOnly", "VariableUseSideOnly"})
	@Override
	public void markRemoved() {
		if (world != null && world.isClient) {
			BakedBlockEntityRenderer.Manager.markForRebuild(getPos());
		}
		super.markRemoved();
	}

	// standard blockentity boilerplate

	public void dispatch() {
		if (world instanceof ServerWorld sworld) sworld.getChunkManager().markForUpdate(pos);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
}
