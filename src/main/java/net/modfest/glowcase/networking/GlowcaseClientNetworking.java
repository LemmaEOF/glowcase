package net.modfest.glowcase.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.modfest.glowcase.block.entity.ItemDisplayBlockEntity;
import net.modfest.glowcase.block.entity.TextBlockEntity;

import java.util.ArrayList;

public class GlowcaseClientNetworking {

	public static void editHyperlinkBlock(BlockPos pos, String url) {
		ClientPlayNetworking.send(new GlowcaseCommonNetworking.EditHyperlinkBlock(pos, url));
	}

	//TODO: Pretty spicy, copied from the old code. Should maybe break this into more packets, or dispatch off the type of property I'm setting.
	public static void editItemDisplayBlock(ItemDisplayBlockEntity be, boolean updatePitchAndYaw) {
		if (updatePitchAndYaw && MinecraftClient.getInstance().getCameraEntity() != null) {
			Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(MinecraftClient.getInstance().getCameraEntity(), be.getPos());
			be.pitch = pitchAndYaw.x;
			be.yaw = pitchAndYaw.y;
		}
		ClientPlayNetworking.send(new GlowcaseCommonNetworking.EditItemDisplayBlockSettings(
				be.getPos(),
				be.rotationType,
				be.givesItem,
				be.offset,
				new GlowcaseCommonNetworking.ItemDisplayBlockValues(
						be.getCachedState().get(Properties.ROTATION),
						be.showName,
						be.pitch,
						be.yaw
				)
		));
	}

	//TODO: Pretty spicy, copied from the old code. Should maybe break this into more packets, or dispatch off the type of property I'm setting.
	public static void editTextBlock(TextBlockEntity be) {
		ClientPlayNetworking.send(new GlowcaseCommonNetworking.EditTextBlock(
				be.getPos(),
				be.textAlignment,
				be.zOffset,
				be.shadowType,
				new GlowcaseCommonNetworking.TextBlockValues(
						be.scale,
						be.lines.size(),
						(ArrayList<Text>) (Object) be.lines
				)
		));
	}
}
