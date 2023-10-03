package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemDisplayBlockEntity extends BlockEntity {
	private ItemStack stack = ItemStack.EMPTY;
	private Entity displayEntity = null;

	public RotationType rotationType = RotationType.TRACKING;
	public GivesItem givesItem = GivesItem.YES;
	public Offset offset = Offset.CENTER;
	public boolean showName = true;
	public float pitch;
	public float yaw;
	public Set<UUID> givenTo = new HashSet<>();

	public ItemDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.put("item", this.stack.writeNbt(new NbtCompound()));
		tag.putString("rotation_type", this.rotationType.name());
		tag.putFloat("pitch", this.pitch);
		tag.putFloat("yaw", this.yaw);
		tag.putBoolean("show_name", this.showName);
		tag.putString("gives_item", this.givesItem.name());
		tag.putString("offset", this.offset.name());
		NbtList given = new NbtList();
		for (UUID id : givenTo) {
			NbtCompound givenTag = new NbtCompound();
			givenTag.putUuid("id", id);
			given.add(givenTag);
		}
		tag.put("given_to", given);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);

		this.stack = ItemStack.fromNbt(tag.getCompound("item"));
		this.clearDisplayEntity();

		if (tag.contains("tracking")) {
			this.rotationType = tag.getBoolean("tracking") ? RotationType.TRACKING : RotationType.LOCKED;
		} else if (tag.contains("rotation_type")) {
			this.rotationType = RotationType.valueOf(tag.getString("rotation_type"));
		} else {
			this.rotationType = RotationType.TRACKING;
		}

		if (tag.contains("gives_item")) {
			this.givesItem = GivesItem.valueOf(tag.getString("gives_item"));
		} else {
			this.givesItem = GivesItem.YES;
		}

		if (tag.contains("offset")) {
			this.offset = Offset.valueOf(tag.getString("offset"));
		} else {
			this.offset = Offset.CENTER;
		}

		if (tag.contains("pitch")) {
			this.pitch = tag.getFloat("pitch");
			this.yaw = tag.getFloat("yaw");
		}

		if (tag.contains("show_name")) {
			this.showName = tag.getBoolean("show_name");
		}

		givenTo.clear();
		if (tag.contains("given_to")) {
			NbtList given = tag.getList("given_to", NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : given) {
				NbtCompound comp = ((NbtCompound) elem);
				givenTo.add(comp.getUuid("id"));
			}
		}
	}

	public boolean hasItem() {
		return this.stack != null && !this.stack.isEmpty();
	}

	public void setStack(ItemStack stack) {
		this.stack = stack.copy();

		this.givenTo.clear();
		this.clearDisplayEntity();
		this.markDirty();
		this.dispatch();
	}

	private void clearDisplayEntity() {
		this.displayEntity = null;
	}

	public Entity getDisplayEntity() {
		if (this.displayEntity == null && this.world != null && this.stack.getItem() instanceof SpawnEggItem eggItem) {
			this.displayEntity = eggItem.getEntityType(this.stack.getNbt()).create(this.world);
		}

		return this.displayEntity;
	}

	public ItemStack getDisplayedStack() {
		return this.stack;
	}

	//TODO: these cycleXxx methods are only used on ItemDisplayBlockEditScreen, and can probably be moved there
	// -> yes, that means the setBlockState call is wacky
	public void cycleRotationType(PlayerEntity playerEntity) {
		switch (this.rotationType) {
			case TRACKING -> {
				this.rotationType = RotationType.HORIZONTAL;
				if (this.world != null) {
					this.world.setBlockState(this.pos, this.getCachedState().with(Properties.ROTATION, MathHelper.floor((double) ((playerEntity.getYaw()) * 16.0F / 360.0F) + 0.5D) & 15));
				}
			}
			case HORIZONTAL -> this.rotationType = RotationType.LOCKED;
			case LOCKED -> this.rotationType = RotationType.TRACKING;
		}
		markDirty();
		dispatch();
	}

	public void cycleGiveType() {
		switch (this.givesItem) {
			case YES -> this.givesItem = GivesItem.NO;
			case NO -> this.givesItem = GivesItem.ONCE;
			case ONCE -> this.givesItem = GivesItem.YES;
		}
		givenTo.clear();
		markDirty();
		dispatch();
	}

	public void cycleOffset() {
		switch (this.offset) {
			case CENTER -> this.offset = Offset.BACK;
			case BACK -> this.offset = Offset.FRONT;
			case FRONT -> this.offset = Offset.CENTER;
		}
		markDirty();
		dispatch();
	}
	
	public boolean canGiveTo(PlayerEntity player) {
		if(!hasItem()) return false;
		else return switch(this.givesItem) {
			case YES -> true;
			case NO -> false;
			case ONCE -> player.isCreative() || !givenTo.contains(player.getUuid());
		};
	}
	
	public void giveTo(PlayerEntity player, Hand hand) {
		player.setStackInHand(hand, getDisplayedStack().copy());
		if (!player.isCreative()) {
			givenTo.add(player.getUuid());
			markDirty();
		}
	}

	@Environment(EnvType.CLIENT)
	public static Vec2f getPitchAndYaw(Entity camera, BlockPos pos) {
		double d = pos.getX() - camera.getPos().x + 0.5;
		double e = pos.getY() - camera.getEyeY() + 0.5;
		double f = pos.getZ() - camera.getPos().z + 0.5;
		double g = MathHelper.sqrt((float) (d * d + f * f));

		float pitch = (float) ((-MathHelper.atan2(e, g)));
		float yaw = (float) (-MathHelper.atan2(f, d) + Math.PI / 2);

		return new Vec2f(pitch, yaw);
	}

	public static void tick(World world, BlockPos blockPos, BlockState state, ItemDisplayBlockEntity blockEntity) {
		if (blockEntity.getDisplayEntity() != null) {
			blockEntity.displayEntity.tick();
			++blockEntity.displayEntity.age;
		}
	}

	public enum RotationType {
		LOCKED, TRACKING, HORIZONTAL
	}

	public enum GivesItem {
		YES, NO, ONCE
	}

	public enum Offset {
		CENTER, BACK, FRONT
	}

	// standard blockentity boilerplate

	public void dispatch() {
		if (world instanceof ServerWorld sworld) sworld.getChunkManager().markForUpdate(pos);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
}