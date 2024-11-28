package dev.hephaestus.glowcase.client.gui.widget.ingame;

import dev.hephaestus.glowcase.math.ParseUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Vec3FieldsWidget extends ContainerWidget {
	private final TextFieldWidget x;
	private final TextFieldWidget y;
	private final TextFieldWidget z;

	private Vec3d value;

	public Vec3FieldsWidget(int x, int y, int width, int height, MinecraftClient client, Vec3d defaultValue) {
		super(x, y, width, height, Text.empty());
		this.x = new TextFieldWidget(
			client.textRenderer,
			x, y,
			width / 3, height,
			Text.empty()
		);

		this.y = new TextFieldWidget(
			client.textRenderer,
			x + width / 3, y,
			width / 3, height,
			Text.empty()
		);

		this.z = new TextFieldWidget(
			client.textRenderer,
			x + (width / 3 * 2), y,
			width / 3, height,
			Text.empty()
		);

		this.value = defaultValue;

		this.x.setText(String.valueOf(defaultValue.x));
		this.y.setText(String.valueOf(defaultValue.y));
		this.z.setText(String.valueOf(defaultValue.z));

		this.x.setTextPredicate(ParseUtil::canParseDouble);
		this.y.setTextPredicate(ParseUtil::canParseDouble);
		this.z.setTextPredicate(ParseUtil::canParseDouble);

		this.x.setChangedListener(s -> value = new Vec3d(ParseUtil.parseOrDefault(s, value.x), value.y , value.z));
		this.y.setChangedListener(s -> value = new Vec3d(value.x, ParseUtil.parseOrDefault(s, value.y), value.z));
		this.z.setChangedListener(s -> value = new Vec3d(value.x, value.y, ParseUtil.parseOrDefault(s, value.z)));
	}

	public void setVec(Vec3d newVec) {
		this.x.setText(String.valueOf(newVec.x));
		this.y.setText(String.valueOf(newVec.y));
		this.z.setText(String.valueOf(newVec.z));
	}

	@Override
	public List<? extends Element> children() {
		return List.of(x, y, z);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		x.renderWidget(context, mouseX, mouseY, delta);
		y.renderWidget(context, mouseX, mouseY, delta);
		z.renderWidget(context, mouseX, mouseY, delta);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		x.appendClickableNarrations(builder);
		y.appendClickableNarrations(builder);
		z.appendClickableNarrations(builder);
	}

	public Vec3d value() {
		return value;
	}
}
