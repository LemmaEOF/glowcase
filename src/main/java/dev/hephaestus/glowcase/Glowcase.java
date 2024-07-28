package dev.hephaestus.glowcase;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(Glowcase.MODID)
public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";

	public Glowcase(IEventBus modBus) {
		modBus.addListener((FMLCommonSetupEvent e) -> onInitialize());
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
		BLOCK_ENTITIES.register(modBus);
		ITEM_GROUPS.register(modBus);
	}

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
	public static final DeferredRegister<ItemGroup> ITEM_GROUPS = DeferredRegister.create(Registries.ITEM_GROUP, MODID);

	public static GlowcaseCommonProxy proxy = new GlowcaseCommonProxy(); //Overridden in GlowcaseClient

	public static final TagKey<Item> ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("items"));

	public static final Supplier<HyperlinkBlock> HYPERLINK_BLOCK = registerBlock("hyperlink_block", HyperlinkBlock::new);
	public static final Supplier<BlockItem> HYPERLINK_BLOCK_ITEM = registerItem("hyperlink_block", () -> new BlockItem(HYPERLINK_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<HyperlinkBlockEntity>> HYPERLINK_BLOCK_ENTITY = registerBlockEntity("hyperlink_block", () -> BlockEntityType.Builder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK.get()).build(null));

	public static final Supplier<ItemDisplayBlock> ITEM_DISPLAY_BLOCK = registerBlock("item_display_block", ItemDisplayBlock::new);
	public static final Supplier<BlockItem> ITEM_DISPLAY_BLOCK_ITEM = registerItem("item_display_block", () -> new BlockItem(ITEM_DISPLAY_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<ItemDisplayBlockEntity>> ITEM_DISPLAY_BLOCK_ENTITY = registerBlockEntity("item_display_block", () -> BlockEntityType.Builder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK.get()).build(null));

	public static final Supplier<MailboxBlock> MAILBOX_BLOCK = registerBlock("mailbox", MailboxBlock::new);
	public static final Supplier<BlockItem> MAILBOX_ITEM = registerItem("mailbox", () -> new BlockItem(MAILBOX_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<MailboxBlockEntity>> MAILBOX_BLOCK_ENTITY = registerBlockEntity("mailbox", () -> BlockEntityType.Builder.create(MailboxBlockEntity::new, MAILBOX_BLOCK.get()).build(null));

	public static final Supplier<TextBlock> TEXT_BLOCK = registerBlock("text_block", TextBlock::new);
	public static final Supplier<BlockItem> TEXT_BLOCK_ITEM = registerItem("text_block", () -> new BlockItem(TEXT_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<TextBlockEntity>> TEXT_BLOCK_ENTITY = registerBlockEntity("text_block", () -> BlockEntityType.Builder.create(TextBlockEntity::new, TEXT_BLOCK.get()).build(null));

	public static final Supplier<ItemGroup> ITEM_GROUP = registerItemGroup("items", () -> FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.glowcase.items"))
		.icon(() -> new ItemStack(Items.GLOWSTONE))
		.entries((displayContext, entries) -> {
			entries.add(HYPERLINK_BLOCK_ITEM.get());
			entries.add(ITEM_DISPLAY_BLOCK_ITEM.get());
			entries.add(MAILBOX_ITEM.get());
			entries.add(TEXT_BLOCK_ITEM.get());
		})
		.build()
	);

	public static Identifier id(String... path) {
		return Identifier.of(MODID, String.join(".", path));
	}

	public static <T extends Block> Supplier<T> registerBlock(String path, Supplier<T> supplier) {
		return BLOCKS.register(path, supplier);
	}

	public static <T extends Item> Supplier<T> registerItem(String path, Supplier<T> supplier) {
		return ITEMS.register(path, supplier);
	}

	public static <T extends ItemGroup> Supplier<T> registerItemGroup(String path, Supplier<T> supplier) {
		return ITEM_GROUPS.register(path, supplier);
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path, Supplier<BlockEntityType<T>> supplier) {
		return BLOCK_ENTITIES.register(path, supplier);
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

	private int sendMessage(CommandContext<ServerCommandSource> ctx) {
		BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");
		String message = ctx.getArgument("message", String.class);
		PlayerEntity sender = ctx.getSource().getPlayer();

		if (sender != null) {
			if (sender.getWorld().getBlockEntity(pos) instanceof MailboxBlockEntity mailbox) {
				mailbox.addMessage(new MailboxBlockEntity.Message(sender.getUuid(), sender.getNameForScoreboard(), message));
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
