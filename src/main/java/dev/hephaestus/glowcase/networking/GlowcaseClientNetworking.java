package dev.hephaestus.glowcase.networking;

import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

public class GlowcaseClientNetworking {
	public static void onInitializeClient() {
		//...
	}
	
	public static void editHyperlinkBlock(BlockPos pos, String url) {
		ClientPlayNetworking.send(GlowcaseCommonNetworking.EDIT_HYPERLINK_BLOCK, new PacketByteBuf(Unpooled.buffer())
				.writeBlockPos(pos)
				.writeString(url));
	}
	
	//TODO: Pretty spicy, copied from the old code. Should maybe break this into more packets, or dispatch off the type of property I'm setting.
	public static void editItemDisplayBlock(ItemDisplayBlockEntity be, boolean updatePitchAndYaw) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(be.getPos());
		buf.writeEnumConstant(be.rotationType);
		buf.writeEnumConstant(be.givesItem);
		buf.writeEnumConstant(be.offset);
		buf.writeVarInt(be.getCachedState().get(Properties.ROTATION));
		buf.writeBoolean(be.showName);

		if (updatePitchAndYaw && MinecraftClient.getInstance().player != null) {
			Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(MinecraftClient.getInstance().player, be.getPos());
			be.pitch = pitchAndYaw.x;
			be.yaw = pitchAndYaw.y;
		}

		buf.writeFloat(be.pitch);
		buf.writeFloat(be.yaw);
		
		ClientPlayNetworking.send(GlowcaseCommonNetworking.EDIT_ITEM_DISPLAY_BLOCK_SETTINGS, buf);
	}
}
