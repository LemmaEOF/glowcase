package dev.hephaestus.glowcase;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.hephaestus.glowcase.block.*;
import dev.hephaestus.glowcase.block.entity.*;
import dev.hephaestus.glowcase.compat.PolydexCompatibility;
import dev.hephaestus.glowcase.item.LockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";

	public static GlowcaseCommonProxy proxy = new GlowcaseCommonProxy(); //Overridden in GlowcaseClient

	public static final TagKey<Item> ITEM_TAG = TagKey.of(RegistryKeys.ITEM, id("items"));

	public static final Supplier<HyperlinkBlock> HYPERLINK_BLOCK = registerBlock("hyperlink_block", HyperlinkBlock::new);
	public static final Supplier<BlockItem> HYPERLINK_BLOCK_ITEM = registerItem("hyperlink_block", () -> new BlockItem(HYPERLINK_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<HyperlinkBlockEntity>> HYPERLINK_BLOCK_ENTITY = registerBlockEntity("hyperlink_block", () -> BlockEntityType.Builder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK.get()).build(null));

	public static final Supplier<ItemDisplayBlock> ITEM_DISPLAY_BLOCK = registerBlock("item_display_block", ItemDisplayBlock::new);
	public static final Supplier<BlockItem> ITEM_DISPLAY_BLOCK_ITEM = registerItem("item_display_block", () -> new BlockItem(ITEM_DISPLAY_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<ItemDisplayBlockEntity>> ITEM_DISPLAY_BLOCK_ENTITY = registerBlockEntity("item_display_block", () -> BlockEntityType.Builder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK.get()).build(null));

	public static final Supplier<ParticleDisplayBlock> PARTICLE_DISPLAY = registerBlock("particle_display", ParticleDisplayBlock::new);
	public static final Supplier<BlockItem> PARTICLE_DISPLAY_ITEM = registerItem("particle_display", () -> new BlockItem(PARTICLE_DISPLAY.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<ParticleDisplayBlockEntity>> PARTICLE_DISPLAY_BLOCK_ENTITY = registerBlockEntity("particle_display", () -> BlockEntityType.Builder.create(ParticleDisplayBlockEntity::new, PARTICLE_DISPLAY.get()).build(null));

	public static final Supplier<SoundPlayerBlock> SOUND_BLOCK = registerBlock("sound_block", SoundPlayerBlock::new);
	public static final Supplier<BlockItem> SOUND_BLOCK_ITEM = registerItem("sound_block", () -> new BlockItem(SOUND_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<SoundPlayerBlockEntity>> SOUND_BLOCK_ENTITY = registerBlockEntity("particle_display", () -> BlockEntityType.Builder.create(SoundPlayerBlockEntity::new, SOUND_BLOCK.get()).build(null));

	public static final Supplier<TextBlock> TEXT_BLOCK = registerBlock("text_block", TextBlock::new);
	public static final Supplier<BlockItem> TEXT_BLOCK_ITEM = registerItem("text_block", () -> new BlockItem(TEXT_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<TextBlockEntity>> TEXT_BLOCK_ENTITY = registerBlockEntity("text_block", () -> BlockEntityType.Builder.create(TextBlockEntity::new, TEXT_BLOCK.get()).build(null));

	public static final Supplier<PopupBlock> POPUP_BLOCK = registerBlock("popup_block", PopupBlock::new);
	public static final Supplier<BlockItem> POPUP_BLOCK_ITEM = registerItem("popup_block", () -> new BlockItem(POPUP_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<PopupBlockEntity>> POPUP_BLOCK_ENTITY = registerBlockEntity("popup_block", () -> BlockEntityType.Builder.create(PopupBlockEntity::new, POPUP_BLOCK.get()).build(null));

	public static final Supplier<SpriteBlock> SPRITE_BLOCK = registerBlock("sprite_block", SpriteBlock::new);
	public static final Supplier<BlockItem> SPRITE_BLOCK_ITEM = registerItem("sprite_block", () -> new BlockItem(SPRITE_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<SpriteBlockEntity>> SPRITE_BLOCK_ENTITY = registerBlockEntity("sprite_block", () -> BlockEntityType.Builder.create(SpriteBlockEntity::new, SPRITE_BLOCK.get()).build(null));

	public static final Supplier<OutlineBlock> OUTLINE_BLOCK = registerBlock("outline_block", OutlineBlock::new);
	public static final Supplier<BlockItem> OUTLINE_BLOCK_ITEM = registerItem("outline_block", () -> new BlockItem(OUTLINE_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<OutlineBlockEntity>> OUTLINE_BLOCK_ENTITY = registerBlockEntity("outline_block", () -> BlockEntityType.Builder.create(OutlineBlockEntity::new, OUTLINE_BLOCK.get()).build(null));

	public static final Supplier<ItemAcceptorBlock> ITEM_ACCEPTOR_BLOCK = registerBlock("item_acceptor_block", ItemAcceptorBlock::new);
	public static final Supplier<BlockItem> ITEM_ACCEPTOR_BLOCK_ITEM = registerItem("item_acceptor_block", () -> new BlockItem(ITEM_ACCEPTOR_BLOCK.get(), new Item.Settings()));
	public static final Supplier<BlockEntityType<ItemAcceptorBlockEntity>> ITEM_ACCEPTOR_BLOCK_ENTITY = registerBlockEntity("item_acceptor_block", () -> BlockEntityType.Builder.create(ItemAcceptorBlockEntity::new, ITEM_ACCEPTOR_BLOCK.get()).build(null));

	public static final Supplier<Item> LOCK_ITEM = registerItem("lock", () -> new LockItem(new Item.Settings()));

	public static final Supplier<ItemGroup> ITEM_GROUP = registerItemGroup("items", () -> FabricItemGroup.builder()
		.displayName(Text.translatable("itemGroup.glowcase.items"))
		.icon(() -> new ItemStack(Items.GLOWSTONE))
		.entries((displayContext, entries) -> {
			entries.add(TEXT_BLOCK_ITEM.get());
			entries.add(SPRITE_BLOCK_ITEM.get());
			entries.add(OUTLINE_BLOCK_ITEM.get());
			entries.add(PARTICLE_DISPLAY_ITEM.get());
			entries.add(SOUND_BLOCK_ITEM.get());
			entries.add(ITEM_DISPLAY_BLOCK_ITEM.get());
			entries.add(ITEM_ACCEPTOR_BLOCK_ITEM.get());
			entries.add(HYPERLINK_BLOCK_ITEM.get());
			entries.add(POPUP_BLOCK_ITEM.get());
			entries.add(LOCK_ITEM.get());
		})
		.build()
	);

	public static Identifier id(String... path) {
		return Identifier.of(MODID, String.join(".", path));
	}

	public static <T extends Block> Supplier<T> registerBlock(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.BLOCK, id(path), supplier.get()));
	}

	public static <T extends Item> Supplier<T> registerItem(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.ITEM, id(path), supplier.get()));
	}

	public static <T extends ItemGroup> Supplier<T> registerItemGroup(String path, Supplier<T> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.ITEM_GROUP, id(path), supplier.get()));
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path, Supplier<BlockEntityType<T>> supplier) {
		return Suppliers.ofInstance(Registry.register(Registries.BLOCK_ENTITY_TYPE, id(path), supplier.get()));
	}

	@Override
	public void onInitialize() {
		GlowcaseNetworking.init();

		if (FabricLoader.getInstance().isModLoaded("polydex2")) {
			PolydexCompatibility.onInitialize();
		}
	}
}
