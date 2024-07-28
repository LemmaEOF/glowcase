package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.MailboxBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MailboxBlock extends Block implements BlockEntityProvider {
	public static final BooleanProperty HAS_MAIL = BooleanProperty.of("has_mail");

	private static final VoxelShape SHAPE_NS = VoxelShapes.cuboid(0.3125, 0, 0.0625, 0.6875, 0.5, 0.9375);
	private static final VoxelShape SHAPE_EW = VoxelShapes.cuboid(0.0625, 0, 0.3125, 0.9375, 0.5, 0.6875);

	public MailboxBlock() {
		super(Settings.create().nonOpaque().strength(-1, Integer.MAX_VALUE));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.HORIZONTAL_FACING, HAS_MAIL);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(HAS_MAIL, false);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(Properties.HORIZONTAL_FACING)) {
			case NORTH, SOUTH -> SHAPE_NS;
			case EAST, WEST -> SHAPE_EW;
			default -> VoxelShapes.fullCube();
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.getOutlineShape(state, world, pos, context);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MailboxBlockEntity(pos, state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (world.getBlockEntity(pos) instanceof MailboxBlockEntity mailbox && placer instanceof ServerPlayerEntity player) {
			mailbox.setOwner(player);
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof MailboxBlockEntity be)) return ActionResult.CONSUME;
		boolean mine = Objects.equals(player.getUuid(), be.owner());

		if (world.isClient && !mine) {
			Glowcase.proxy.prefillMailboxChat(pos);
		}

		if (!world.isClient && mine) {
			if (player.isSneaking()) be.removeAllMessagesFromMostRecentSender();
			else be.removeMessage();
		}

		return ActionResult.SUCCESS;
	}
}
