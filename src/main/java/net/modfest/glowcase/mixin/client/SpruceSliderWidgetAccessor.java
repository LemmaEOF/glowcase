package net.modfest.glowcase.mixin.client;

import dev.lambdaurora.spruceui.widget.SpruceSliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpruceSliderWidget.class)
public interface SpruceSliderWidgetAccessor {

	@Accessor("value")
	void setValueRaw(double value);
}
