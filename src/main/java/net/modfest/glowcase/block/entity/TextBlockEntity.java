package net.modfest.glowcase.block.entity;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.modfest.glowcase.Glowcase;
import net.modfest.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextBlockEntity extends BlockEntity {

	public static final NodeParser PARSER = TagParser.DEFAULT;

	public List<MutableText> lines = new ArrayList<>();

	public TextAlignment textAlignment = TextAlignment.CENTER;
	public ShadowType shadowType = ShadowType.DROP;
	public float scale = 1.0f;
	public float zOffset = 0.0f;

	public boolean renderDirty = true;

	public TextBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.TEXT_BLOCK_ENTITY, pos, state);
		lines.add(Text.empty());
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		var nbtLines = nbt.getList("lines", NbtElement.STRING_TYPE);
		for (var text : lines) {
			nbtLines.add(NbtString.of(Text.Serialization.toJsonString(text, registryLookup)));
		}
		nbt.put("lines", nbtLines);
		nbt.putInt("text_alignment", textAlignment.ordinal());
		nbt.putInt("shadow_type", shadowType.ordinal());
		nbt.putFloat("scale", scale);
		nbt.putFloat("z_offset", zOffset);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		lines = new ArrayList<>();
		var nbtLines = nbt.getList("lines", 8);
		for (var line : nbtLines) {
			if (line.getType() == NbtElement.END_TYPE) {
				break;
			}
			lines.add(Text.Serialization.fromJson(line.asString(), registryLookup));
		}
		textAlignment = TextAlignment.values()[nbt.getInt("text_alignment")];
		shadowType = ShadowType.values()[nbt.getInt("shadow_type")];
		scale = nbt.getFloat("scale");
		zOffset = nbt.getFloat("zOffset");
		renderDirty = true;
	}

	// TODO: Patbox says there's a better way to write these
	public String getRawLine(int i) {
		var line = lines.get(i);
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
			lines.add(i, Text.literal(string));
		}
		else {
			lines.add(i, Text.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(string)));
		}
	}

	public void setRawLine(int i, String string) {
		var parsed = PARSER.parseText(string, ParserContext.of());
		if (parsed.getString().equals(string)) {
			lines.set(i, Text.literal(string));
		}
		else {
			lines.set(i, Text.empty().append(parsed).setStyle(Style.EMPTY.withInsertion(string)));
		}
	}

	public enum TextAlignment {
		LEFT, CENTER, RIGHT
	}

	public enum ShadowType {
		DROP, PLATE, NONE
	}

	@SuppressWarnings({ "MethodCallSideOnly", "VariableUseSideOnly" })
	@Override
	public void markRemoved() {
		if (world != null && world.isClient) {
			BakedBlockEntityRenderer.Manager.markForRebuild(getPos());
		}
		super.markRemoved();
	}

	public void dispatch() {
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.getChunkManager().markForUpdate(pos);
		}
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
}
