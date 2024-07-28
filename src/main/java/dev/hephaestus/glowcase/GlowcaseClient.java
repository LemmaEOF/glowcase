package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.block.entity.MailboxBlockEntity;
import dev.hephaestus.glowcase.client.GlowcaseClientProxy;
import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.HyperlinkBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.ItemDisplayBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.TextBlockEntityRenderer;
import dev.hephaestus.glowcase.networking.RegisterS2CNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.Window;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

import java.util.List;

public class GlowcaseClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Glowcase.proxy = new GlowcaseClientProxy();

		BlockEntityRendererFactories.register(Glowcase.TEXT_BLOCK_ENTITY.get(), TextBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.HYPERLINK_BLOCK_ENTITY.get(), HyperlinkBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY.get(), ItemDisplayBlockEntityRenderer::new);

		WorldRenderEvents.AFTER_TRANSLUCENT.register(BakedBlockEntityRenderer.Manager::render);
		InvalidateRenderStateCallback.EVENT.register(BakedBlockEntityRenderer.Manager::reset);

		RegisterS2CNetworking.initialize();

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();

			if (client.world != null && client.crosshairTarget instanceof BlockHitResult hitResult && client.world.getBlockEntity(hitResult.getBlockPos()) instanceof MailboxBlockEntity mailbox && mailbox.messageCount() > 0 && mailbox.owner().equals(client.getSession().getUuidOrNull())) {
				Window window = client.getWindow();
				TextRenderer textRenderer = client.textRenderer;
				MailboxBlockEntity.Message message = mailbox.getMessage();
				List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(message.message()), window.getWidth() / 2);
				Text reminder2 = Text.translatable("glowcase.mailbox.reminder2");

				int padding = 3;

				int lineHeight = (textRenderer.fontHeight + 3);

				int contentWidth = Math.max(window.getScaledWidth() / 2, textRenderer.getWidth(reminder2));
				int totalWidth = contentWidth + padding * 2;
				int totalHeight = (lines.size() + 6) * lineHeight;

				int startX = window.getScaledWidth() / 2 - totalWidth / 2;
				int startY = window.getScaledHeight() / 2 - totalHeight / 2;

				context.fill(startX, startY, startX + totalWidth, startY + totalHeight, 0x80000000);

				int y = startY + lineHeight * 2;

				for (OrderedText line : lines) {
					context.drawText(client.textRenderer, line, startX + 3, y, -1, false);
					y += lineHeight;
				}

				context.drawText(client.textRenderer, Text.translatable("glowcase.mailbox.sender", message.senderName()), startX + 3, startY + 3, -1, false);

				Text messageCount = Text.literal("1/" + mailbox.messageCount());
				context.drawText(client.textRenderer, messageCount, startX + totalWidth - 3 - textRenderer.getWidth(messageCount), y + lineHeight, -1, false);

				Text reminder1 = Text.translatable("glowcase.mailbox.reminder1");
				context.drawText(client.textRenderer, reminder1, startX + totalWidth - 3 - textRenderer.getWidth(reminder1), y + lineHeight * 2, 0xFFAAAAAA, false);

				context.drawText(client.textRenderer, reminder2, startX + totalWidth - 3 - textRenderer.getWidth(reminder2), y + lineHeight * 3, 0xFFAAAAAA, false);

			}
		});
	}
}
