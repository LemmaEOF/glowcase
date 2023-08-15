package dev.hephaestus.glowcase.compat;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import eu.pb4.polydex.api.v1.hover.HoverDisplayBuilder;
import eu.pb4.polydex.impl.PolydexImpl;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

/**
 * Makes Polydex hover display more correct information
 *
 * @author Patbox
 */
public class PolydexCompatibility {
    public static void onInitialize() {
        HoverDisplayBuilder.register(Glowcase.ITEM_DISPLAY_BLOCK, PolydexCompatibility::setupItemDisplayBlock);
        HoverDisplayBuilder.register(Glowcase.HYPERLINK_BLOCK, PolydexCompatibility::setupHyperlinkBlock);
    }

    private static void setupHyperlinkBlock(HoverDisplayBuilder hoverDisplayBuilder) {
        var target = hoverDisplayBuilder.getTarget();
        if (target.player().isCreative()) {
            return;
        }

        if (target.blockEntity() instanceof HyperlinkBlockEntity blockEntity && !blockEntity.getUrl().isEmpty()) {
            hoverDisplayBuilder.setComponent(HoverDisplayBuilder.NAME, Text.literal(blockEntity.getUrl()));
            hoverDisplayBuilder.setComponent(HoverDisplayBuilder.MOD_SOURCE, Text.literal("Internet"));
        }
    }

    private static void setupItemDisplayBlock(HoverDisplayBuilder hoverDisplayBuilder) {
        var target = hoverDisplayBuilder.getTarget();
        if (target.player().isCreative()) {
            return;
        }

        if (target.blockEntity() instanceof ItemDisplayBlockEntity blockEntity && blockEntity.hasItem()) {
            var item = blockEntity.getDisplayedStack();
            hoverDisplayBuilder.setComponent(HoverDisplayBuilder.NAME, item.getName());
            // I won't break this I promise
            hoverDisplayBuilder.setComponent(HoverDisplayBuilder.MOD_SOURCE, PolydexImpl.getMod(Registries.ITEM.getId(item.getItem())));
        }
    }
}
