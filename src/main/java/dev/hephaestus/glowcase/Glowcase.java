package dev.hephaestus.glowcase;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.hephaestus.glowcase.block.HyperlinkBlock;
import dev.hephaestus.glowcase.block.ItemDisplayBlock;
import dev.hephaestus.glowcase.block.MailboxBlock;
import dev.hephaestus.glowcase.block.TextBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.MailboxBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.networking.GlowcaseCommonNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";

	public static GlowcaseCommonProxy proxy = new GlowcaseCommonProxy(); //Overridden in GlowcaseClient
	
	public static final TagKey<Item> ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("items"));


	public static final HyperlinkBlock HYPERLINK_BLOCK = Registry.register(Registries.BLOCK, id("hyperlink_block"), new HyperlinkBlock());
	public static final Item HYPERLINK_BLOCK_ITEM = Registry.register(Registries.ITEM, id("hyperlink_block"), new BlockItem(HYPERLINK_BLOCK, new FabricItemSettings()));
	public static final BlockEntityType<HyperlinkBlockEntity> HYPERLINK_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("hyperlink_block"), FabricBlockEntityTypeBuilder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK).build());

	public static final ItemDisplayBlock ITEM_DISPLAY_BLOCK = Registry.register(Registries.BLOCK, id("item_display_block"), new ItemDisplayBlock());
	public static final Item ITEM_DISPLAY_BLOCK_ITEM = Registry.register(Registries.ITEM, id("item_display_block"), new BlockItem(ITEM_DISPLAY_BLOCK, new FabricItemSettings()));
	public static final BlockEntityType<ItemDisplayBlockEntity> ITEM_DISPLAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("item_display_block"), FabricBlockEntityTypeBuilder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK).build());

	public static final MailboxBlock MAILBOX_BLOCK = Registry.register(Registries.BLOCK, id("mailbox"), new MailboxBlock());
	public static final Item MAILBOX_ITEM = Registry.register(Registries.ITEM, id("mailbox"), new BlockItem(MAILBOX_BLOCK, new FabricItemSettings()));
	public static final BlockEntityType<MailboxBlockEntity> MAILBOX_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("mailbox"), FabricBlockEntityTypeBuilder.create(MailboxBlockEntity::new, MAILBOX_BLOCK).build());

	public static final TextBlock TEXT_BLOCK = Registry.register(Registries.BLOCK, id("text_block"), new TextBlock());
	public static final Item TEXT_BLOCK_ITEM = Registry.register(Registries.ITEM, id("text_block"), new BlockItem(TEXT_BLOCK, new FabricItemSettings()));
	public static final BlockEntityType<TextBlockEntity> TEXT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("text_block"), FabricBlockEntityTypeBuilder.create(TextBlockEntity::new, TEXT_BLOCK).build());

	public static final ItemGroup ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, id("items"), FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.glowcase.items"))
		.icon(() -> new ItemStack(Items.GLOWSTONE))
		.entries((displayContext, entries) -> {
			entries.add(HYPERLINK_BLOCK_ITEM);
			entries.add(ITEM_DISPLAY_BLOCK_ITEM);
			entries.add(MAILBOX_ITEM);
			entries.add(TEXT_BLOCK_ITEM);
		})
		.build()
	);

	public static Identifier id(String... path) {
		return new Identifier(MODID, String.join(".", path));
	}

	@Override
	public void onInitialize() {
		GlowcaseCommonNetworking.onInitialize();
		
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			dispatcher.register(
					LiteralArgumentBuilder.<ServerCommandSource>literal("mail")
						.then(CommandManager.argument("pos", new BlockPosArgumentType())
								.then(CommandManager.argument("message", StringArgumentType.greedyString()).executes(this::sendMessage)))
			);
		});
	}

	private int sendMessage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");
		String message = ctx.getArgument("message", String.class);
		PlayerEntity sender = ctx.getSource().getPlayer();

		if (sender != null) {
			if (sender.getWorld().getBlockEntity(pos) instanceof MailboxBlockEntity mailbox) {
				mailbox.addMessage(new MailboxBlockEntity.Message(sender.getUuid(), sender.getEntityName(), message));
				ctx.getSource().sendFeedback(() -> Text.translatable("command.glowcase.message_sent"), false);
				return 0;
			} else {
				ctx.getSource().sendError(Text.translatable("command.glowcase.failed.no_mailbox"));
				return 100;
			}
		} else {
			ctx.getSource().sendError(Text.translatable("command.glowcase.failed.no_world"));
			return 100;
		}
	}
}
