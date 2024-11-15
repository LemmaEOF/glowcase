package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.hephaestus.glowcase.block.entity.ParticleDisplayBlockEntity;
import dev.hephaestus.glowcase.client.gui.widget.ingame.Vec3FieldsWidget;
import dev.hephaestus.glowcase.math.DeviatedInteger;
import dev.hephaestus.glowcase.math.DeviatedVec3d;
import dev.hephaestus.glowcase.math.ParseUtil;
import dev.hephaestus.glowcase.packet.C2SEditParticleDisplayBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;

public class ParticleDisplayEditScreen extends GlowcaseScreen {
	private final ParticleDisplayBlockEntity blockEntity;
	private TextFieldWidget particleId;

	private Vec3FieldsWidget positionMean;
	private Vec3FieldsWidget positionStdDev;

	private Vec3FieldsWidget velocityMean;
	private Vec3FieldsWidget velocityStdDev;

	private TextFieldWidget countMean;
	private TextFieldWidget countStdDev;

	private TextFieldWidget tickRateMean;
	private TextFieldWidget tickRateStdDev;

	public ParticleDisplayEditScreen(ParticleDisplayBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
	}

	@Override
	protected void init() {
		super.init();
		Objects.requireNonNull(this.client);
		RegistryWrapper.WrapperLookup lookup = Objects.requireNonNull(client.world).getRegistryManager();


		// region Particle ID
		particleId = new TextFieldWidget(
			this.client.textRenderer,
			width / 10, 60,
			8 * width / 10, 20,
			Text.empty()
		);

		String optionsString = effectToTag(blockEntity.particle, lookup.getOps(NbtOps.INSTANCE)).asString();
		if (optionsString.startsWith("{}")) optionsString = "";

		particleId.setText(Registries.PARTICLE_TYPE.getId(blockEntity.particle.getType()) + optionsString);

		this.addDrawableChild(particleId);
		// endregion

		// region Position
		positionMean = new Vec3FieldsWidget(
			width / 10, 110,
			(4 * width / 10) - 6, 20,
			this.client,
			blockEntity.position.mean()
		);

		this.addDrawableChild(positionMean);

		positionStdDev = new Vec3FieldsWidget(
			width / 10 + (4 * width / 10) + 6, 110,
			(4 * width / 10) - 6, 20,
			this.client,
			blockEntity.position.stdDev()
		);

		this.addDrawableChild(positionStdDev);
		// endregion

		// region Velocity
		velocityMean = new Vec3FieldsWidget(
			width / 10, 160,
			(4 * width / 10) - 6, 20,
			this.client,
			blockEntity.velocity.mean()
		);

		this.addDrawableChild(velocityMean);

		velocityStdDev = new Vec3FieldsWidget(
			width / 10 + (4 * width / 10) + 6, 160,
			(4 * width / 10) - 6, 20,
			this.client,
			blockEntity.velocity.stdDev()
		);

		this.addDrawableChild(velocityStdDev);
		// endregion

		// region Count
		countMean = new TextFieldWidget(
			this.client.textRenderer,
			width / 10, 210,
			(4 * width / 10) - 6, 20,
			Text.empty()
		);

		countMean.setText(String.valueOf(blockEntity.count.mean()));
		countMean.setTextPredicate(ParseUtil::canParseInt);

		this.addDrawableChild(countMean);

		countStdDev = new TextFieldWidget(
			this.client.textRenderer,
			width / 10 + (4 * width / 10) + 6, 210,
			(4 * width / 10) - 6, 20,
			Text.empty()
		);

		countStdDev.setText(String.valueOf(blockEntity.count.stdDev()));
		countStdDev.setTextPredicate(ParseUtil::canParseInt);

		this.addDrawableChild(countStdDev);
		// endregion

		// region Tick Rate
		tickRateMean = new TextFieldWidget(
			this.client.textRenderer,
			width / 10, 260,
			(4 * width / 10) - 6, 20,
			Text.empty()
		);

		tickRateMean.setText(String.valueOf(blockEntity.tickRate.mean()));
		tickRateMean.setTextPredicate(ParseUtil::canParseInt);

		this.addDrawableChild(tickRateMean);

		tickRateStdDev = new TextFieldWidget(
			this.client.textRenderer,
			width / 10 + (4 * width / 10) + 6, 260,
			(4 * width / 10) - 6, 20,
			Text.empty()
		);

		tickRateStdDev.setText(String.valueOf(blockEntity.tickRate.stdDev()));
		tickRateStdDev.setTextPredicate(ParseUtil::canParseInt);

		this.addDrawableChild(tickRateStdDev);
		// endregion
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		Objects.requireNonNull(this.client);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.position_mean"),
			width / 10, 90,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.position_std_dev"),
			width / 10 + (4 * width / 10) + 6, 90,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.velocity_mean"),
			width / 10, 140,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.velocity_std_dev"),
			width / 10 + (4 * width / 10) + 6, 140,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.count_mean"),
			width / 10, 190,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.count_std_dev"),
			width / 10 + (4 * width / 10) + 6, 190,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.tick_rate_mean"),
			width / 10, 240,
			0xFFFFFFFF
		);

		context.drawTextWithShadow(
			client.textRenderer,
			Text.translatable("gui.glowcase.tick_rate_std_dev"),
			width / 10 + (4 * width / 10) + 6, 240,
			0xFFFFFFFF
		);
	}

	@Override
	public void close() {
		setParticle();

		blockEntity.position = new DeviatedVec3d(positionMean.value(), positionStdDev.value());
		blockEntity.velocity = new DeviatedVec3d(velocityMean.value(), velocityStdDev.value());

		blockEntity.count = new DeviatedInteger(
			ParseUtil.parseOrDefault(countMean.getText(), blockEntity.count.mean()),
			ParseUtil.parseOrDefault(countStdDev.getText(), blockEntity.count.stdDev())
		);

		blockEntity.tickRate = new DeviatedInteger(
			ParseUtil.parseOrDefault(tickRateMean.getText(), blockEntity.tickRate.mean()),
			ParseUtil.parseOrDefault(tickRateStdDev.getText(), blockEntity.tickRate.stdDev())
		);

		C2SEditParticleDisplayBlock.of(blockEntity).send();
		super.close();
	}

	@SuppressWarnings("unchecked")
	private void setParticle() {
		Objects.requireNonNull(this.client);

		String idText = particleId.getText();

		int paramStart = idText.indexOf('{');

		Identifier id = Identifier.tryParse(
			paramStart == -1 ? idText : idText.substring(0, paramStart));
		if (id == null) return;

		RegistryWrapper.WrapperLookup lookup = Objects.requireNonNull(this.client.world).getRegistryManager();

		RegistryKey<ParticleType<?>> key = RegistryKey.of(RegistryKeys.PARTICLE_TYPE, id);

		Optional<RegistryEntry.Reference<ParticleType<?>>> optionalType =
			lookup.getWrapperOrThrow(RegistryKeys.PARTICLE_TYPE).getOptional(key);
		if (optionalType.isEmpty()) return;

		ParticleType<ParticleEffect> type = (ParticleType<ParticleEffect>) optionalType.get().value();

		NbtCompound nbtCompound;
		try {
			nbtCompound = paramStart == -1 ?
				new NbtCompound() :
				StringNbtReader.parse(idText.substring(paramStart));
		} catch (CommandSyntaxException e) {
			return;
		}


		DataResult<ParticleEffect> effect = type.getCodec().codec().parse(lookup.getOps(NbtOps.INSTANCE), nbtCompound);

		if (effect.result().isEmpty()) return;

		blockEntity.particle = effect.result().get();
	}

	@SuppressWarnings("unchecked")
	private <T extends ParticleEffect> NbtElement effectToTag(T effect, DynamicOps<NbtElement> ops) {
		Codec<T> codec = (Codec<T>) effect.getType().getCodec().codec();
		return codec.encodeStart(ops, effect).getOrThrow();
	}
}
