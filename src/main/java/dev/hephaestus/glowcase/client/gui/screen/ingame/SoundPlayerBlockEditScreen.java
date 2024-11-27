package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.block.entity.SoundPlayerBlockEntity;
import dev.hephaestus.glowcase.client.gui.widget.ingame.Vec3FieldsWidget;
import dev.hephaestus.glowcase.math.ParseUtil;
import dev.hephaestus.glowcase.packet.C2SEditSoundBlock;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class SoundPlayerBlockEditScreen extends GlowcaseScreen {
	private final SoundPlayerBlockEntity soundBlock;

	private TextFieldWidget soundId;
	private ButtonWidget categoryButton;

	private TextFieldWidget volume;
	private TextFieldWidget pitch;
	private TextFieldWidget repeatDelay;

	private TextFieldWidget distance;
	private ButtonWidget relativeButton;
	private Vec3FieldsWidget soundPosition;

	public SoundPlayerBlockEditScreen(SoundPlayerBlockEntity soundBlock) {
		this.soundBlock = soundBlock;
	}

	@Override
	protected void init() {
		super.init();
		Objects.requireNonNull(this.client);
//		RegistryWrapper.WrapperLookup lookup = Objects.requireNonNull(client.world).getRegistryManager();

		this.soundId = new TextFieldWidget(
			this.client.textRenderer,
			width / 10, 60,
			5 * width / 10, 20,
			Text.empty());
		this.soundId.setMaxLength(1024);
		this.soundId.setText(soundBlock.soundId.toString());
		this.addDrawableChild(soundId);

		this.categoryButton = new ButtonWidget.Builder(Text.stringifiedTranslatable("gui.glowcase.sound_category", this.soundBlock.category.getName()), (action) -> {
			soundBlock.cycleCategory();
			this.categoryButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.sound_category", this.soundBlock.category.getName()));
		}).dimensions(7 * width / 10, 20, 2 * width / 10, 20).build();
		this.addDrawableChild(this.categoryButton);

		this.volume = new TextFieldWidget(
			this.client.textRenderer,
			width / 10, 110,
			2 * width / 10, 20,
			Text.empty());
		this.volume.setMaxLength(16);
		this.volume.setText(String.valueOf(soundBlock.volume));
		this.volume.setTextPredicate(ParseUtil::canParseDouble);
		this.addDrawableChild(this.volume);

		this.pitch = new TextFieldWidget(
			this.client.textRenderer,
			4 * width / 10, 110,
			2 * width / 10, 20,
			Text.empty());
		this.pitch.setMaxLength(16);
		this.pitch.setText(String.valueOf(soundBlock.pitch));
		this.pitch.setTextPredicate(ParseUtil::canParseDouble);
		this.addDrawableChild(this.pitch);

		this.repeatDelay = new TextFieldWidget(
			this.client.textRenderer,
			7 * width / 10, 110,
			2 * width / 10, 20,
			Text.empty());
		this.repeatDelay.setMaxLength(16);
		this.repeatDelay.setText(String.valueOf(soundBlock.repeatDelay));
		this.repeatDelay.setTextPredicate(ParseUtil::canParseInt);
		this.addDrawableChild(this.repeatDelay);

		this.distance = new TextFieldWidget(
			this.client.textRenderer,
			width / 10, 160,
			2 * width / 10, 20,
			Text.empty());
		this.distance.setMaxLength(16);
		this.distance.setText(String.valueOf(soundBlock.distance));
		this.distance.setTextPredicate(ParseUtil::canParseDouble);
		this.addDrawableChild(this.distance);

		this.relativeButton = new ButtonWidget.Builder(Text.stringifiedTranslatable("gui.glowcase.sound_positioning", soundBlock.relative), (action) -> {
			soundBlock.relative = !soundBlock.relative;
			this.relativeButton.setMessage(Text.stringifiedTranslatable("gui.glowcase.sound_positioning", soundBlock.relative));
			soundBlock.soundPosition = soundBlock.relative ? Vec3d.ZERO : soundBlock.getPos().toCenterPos();
			this.soundPosition.setVec(soundBlock.soundPosition);
//			soundBlock.soundX = soundBlock.relative ? 0 : soundBlock.getPos().getX();
//			soundBlock.soundY = soundBlock.relative ? 0 : soundBlock.getPos().getY();
//			soundBlock.soundZ = soundBlock.relative ? 0 : soundBlock.getPos().getZ();
		}).dimensions(6 * width / 10, 160, 150, 20).build();
		this.addDrawableChild(this.relativeButton);

		this.soundPosition = new Vec3FieldsWidget(
			width / 10, 210,
			8 * width / 10, 20,
			this.client,
			soundBlock.soundPosition);
		this.addDrawableChild(this.soundPosition);

//		this.soundX = new TextFieldWidget(
//			this.client.textRenderer,
//			width / 10, 210,
//			2 * width / 10, 20,
//			Text.empty());
//		this.soundX.setMaxLength(16);
//		this.soundX.setText(String.valueOf(soundBlock.soundX));
//		this.addDrawableChild(this.soundX);
//
//		this.soundY = new TextFieldWidget(
//			this.client.textRenderer,
//			4 * width / 10, 210,
//			2 * width / 10, 20,
//			Text.empty());
//		this.soundY.setMaxLength(16);
//		this.soundY.setText(String.valueOf(soundBlock.soundY));
//		this.addDrawableChild(this.soundY);
//
//		this.soundZ = new TextFieldWidget(
//			this.client.textRenderer,
//			7 * width / 10, 210,
//			2 * width / 10, 20,
//			Text.empty());
//		this.soundZ.setMaxLength(16);
//		this.soundZ.setText(String.valueOf(soundBlock.soundZ));
//		this.addDrawableChild(this.soundZ);
	}

	@Override
	public void close() {
		setSound();

		soundBlock.volume = (float) ParseUtil.parseOrDefault(this.volume.getText(),soundBlock.volume);
		soundBlock.pitch = (float) ParseUtil.parseOrDefault(this.pitch.getText(), soundBlock.pitch);
		soundBlock.repeatDelay = ParseUtil.parseOrDefault(this.repeatDelay.getText(), soundBlock.repeatDelay);

		soundBlock.distance = (float) ParseUtil.parseOrDefault(this.distance.getText(), soundBlock.distance);
		soundBlock.soundPosition = this.soundPosition.value();
//		soundBlock.soundX = ParseUtil.parseOrDefault(this.soundX.getText(), soundBlock.soundX);
//		soundBlock.soundY = ParseUtil.parseOrDefault(this.soundY.getText(), soundBlock.soundY);
//		soundBlock.soundZ = ParseUtil.parseOrDefault(this.soundZ.getText(), soundBlock.soundZ);

        super.close();
	}

	private void setSound() {
		Objects.requireNonNull(this.client);

		String idText = this.soundId.getText();
		Identifier id = Identifier.tryParse(idText);
		soundBlock.soundId = id;
//		if (id == null) return;
//
//		RegistryWrapper.WrapperLookup lookup = Objects.requireNonNull(this.client.world).getRegistryManager();
//		RegistryKey<SoundEvent> key = RegistryKey.of(RegistryKeys.SOUND_EVENT, id);
//
//		Optional<RegistryEntry.Reference<SoundEvent>> optionalSound =
//			lookup.getWrapperOrThrow(RegistryKeys.SOUND_EVENT).getOptional(key);
//		if (optionalSound.isEmpty()) return;
//
//		blockEntity.sound = optionalSound.get().value();

		C2SEditSoundBlock.of(soundBlock).send();
	}
}
