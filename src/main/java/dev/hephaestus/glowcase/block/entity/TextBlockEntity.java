package dev.hephaestus.glowcase.block.entity;

import java.util.ArrayList;
import java.util.List;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import dev.hephaestus.glowcase.Glowcase;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class TextBlockEntity extends BlockEntity {
	public static final NodeParser PARSER = TextParserV1.DEFAULT;
	public List<MutableText> lines = new ArrayList<>();
	public TextAlignment textAlignment = TextAlignment.CENTER;
	public ZOffset zOffset = ZOffset.CENTER;
	public ShadowType shadowType = ShadowType.DROP;
	public float scale = 1F;
	public int color = 0xFFFFFF;
	public boolean renderDirty = true;

	public TextBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.TEXT_BLOCK_ENTITY, pos, state);
		lines.add((MutableText) Text.empty());
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);

		tag.putFloat("scale", this.scale);
		tag.putInt("color", this.color);

		tag.putString("text_alignment", this.textAlignment.name());
		tag.putString("z_offset", this.zOffset.name());
		tag.putString("shadow_type", this.shadowType.name());

		NbtList lines = tag.getList("lines", 8);
		for (MutableText text : this.lines) {
			lines.add(NbtString.of(Text.Serializer.toJson(text)));
		}

		tag.put("lines", lines);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);

		this.lines = new ArrayList<>();
		this.scale = tag.getFloat("scale");
		this.color = tag.getInt("color");

		this.textAlignment = TextAlignment.valueOf(tag.getString("text_alignment"));
		this.zOffset = ZOffset.valueOf(tag.getString("z_offset"));
		this.shadowType = tag.contains("shadow_type") ? ShadowType.valueOf(tag.getString("shadow_type")) : ShadowType.DROP;

		NbtList lines = tag.getList("lines", 8);

		for (NbtElement line : lines) {
			if (line.getType() == NbtElement.END_TYPE) break;
			this.lines.add(Text.Serializer.fromJson(line.asString()));
		}
		
		this.renderDirty = true;
	}

	public String getRawLine(int i) {
		var line = this.lines.get(i);

		if (line.getStyle() == null) {
			return line.getString();
		}

		var insert = line.getStyle().getInsertion();

		if (insert == null) {
			return line.getString();
		}
		return insert;
	}

	public void addRawLine(int i, String string) {
		var parsed = PARSER.parseText(string, ParserContext.of());

		if (parsed.getString().equals(string)) {
			this.lines.add(i, Text.literal(string));
		} else {
			this.lines.add(i, Text.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(string)));
		}
	}

	public void setRawLine(int i, String string) {
		var parsed = PARSER.parseText(string, ParserContext.of());

		if (parsed.getString().equals(string)) {
			this.lines.set(i, Text.literal(string));
		} else {
			this.lines.set(i, Text.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(string)));
		}
	}

	public enum TextAlignment {
		LEFT, CENTER, RIGHT
	}

	public enum ZOffset {
		FRONT, CENTER, BACK
	}

	public enum ShadowType {
		DROP, PLATE, NONE
	}

	@SuppressWarnings({"MethodCallSideOnly", "VariableUseSideOnly"})
	@Override
	public void markRemoved() {
		if (world != null && world.isClient) {
			BakedBlockEntityRenderer.Manager.markForRebuild(getPos());
		}
		super.markRemoved();
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
