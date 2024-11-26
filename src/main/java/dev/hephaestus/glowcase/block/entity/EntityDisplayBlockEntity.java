package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

public class EntityDisplayBlockEntity extends ItemDisplayBlockEntity {

	private Entity displayEntity = null;

	public boolean tickEntity = false;
	public float displayScale = 0.5f;
	public boolean shouldEditScale = true;

	public EntityDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ENTITY_DISPLAY_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(tag, registryLookup);

		tag.putBoolean("tick_entity", this.tickEntity);
		tag.putFloat("display_scale", this.displayScale);
		tag.putBoolean("scale_edited", this.shouldEditScale);
	}

	@Override
	public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		this.clearDisplayEntity();
		super.readNbt(tag, registryLookup);

		this.tickEntity = tag.getBoolean("tick_entity");
		this.displayScale = tag.getFloat("display_scale");
		this.shouldEditScale = tag.getBoolean("scale_edited");
	}

	@Override
	public void setStack(ItemStack stack) {
		super.setStack(stack);
		this.setShouldEditScale(true);
		this.clearDisplayEntity();
		this.markDirty();
	}

	public void setShouldEditScale(boolean shouldEditScale) {
		this.shouldEditScale = shouldEditScale;
		this.markDirty();
	}

	private void clearDisplayEntity() {
		this.displayEntity = null;
	}

	public Entity getDisplayEntity() {
		if (this.displayEntity == null && this.world != null && this.stack.getItem() instanceof SpawnEggItem eggItem) {
			this.displayEntity = eggItem.getEntityType(this.stack).create(this.world);
			if(shouldEditScale && this.displayEntity != null) {
				//make the default scale of the entity the roughly same size as the block
				//shouldEditScale gets set to true after changing the entity
				float calcScale = displayEntity.getHeight() >= displayEntity.getWidth() ? 1F / displayEntity.getHeight() : 0.5F;
				float roundedScale = (float) Math.round(calcScale / 0.125f) * 0.125f;
				if(Math.abs(roundedScale) > 3) {
					roundedScale = 0.5f; //just in case
				}
				this.displayScale = roundedScale;
			}
		}

		return this.displayEntity;
	}

	public static Vec2f getPitchAndYaw(Entity camera, EntityDisplayBlockEntity entityDisplay, float delta) {
		BlockPos pos = entityDisplay.getPos();
		float displayScale = entityDisplay.displayScale;
		float displayedEntityHeight = entityDisplay.displayEntity == null ? 0 : entityDisplay.displayEntity.getHeight();
		double d = pos.getX() - camera.getLerpedPos(delta).x + 0.5;
		double e = pos.getY() + (displayScale * displayedEntityHeight - 0.5) - camera.getEyeY() + 0.5;
		double f = pos.getZ() - camera.getLerpedPos(delta).z + 0.5;
		double g = MathHelper.sqrt((float) (d * d + f * f));

		float pitch = (float) ((-MathHelper.atan2(e, g)));
		float yaw = (float) (-MathHelper.atan2(f, d) + Math.PI / 2);

		return new Vec2f(pitch, yaw);
	}

	public static void tick(World world, BlockPos blockPos, BlockState state, EntityDisplayBlockEntity blockEntity) {
		if(blockEntity.getDisplayEntity() != null) {
			++blockEntity.displayEntity.age;
//			if(blockEntity.tickEntity) {
//				blockEntity.displayEntity.tick();
//			}
		}
	}

}
