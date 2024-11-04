package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.client.gui.widget.ingame.ColorPickerWidget;
import net.minecraft.util.Formatting;

public interface ColorPickerIncludedScreen {
	ColorPickerWidget colorPickerWidget();
	void toggleColorPicker(boolean active);
	void insertHexTag(String hex);
	void insertFormattingTag(Formatting formatting);
}
