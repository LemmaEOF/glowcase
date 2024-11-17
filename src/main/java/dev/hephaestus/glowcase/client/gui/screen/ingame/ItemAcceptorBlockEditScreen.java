package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.google.common.primitives.Ints;
import dev.hephaestus.glowcase.block.entity.ItemAcceptorBlockEntity;
import dev.hephaestus.glowcase.packet.C2SEditItemAcceptorBlock;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemAcceptorBlockEditScreen extends GlowcaseScreen {
	private final ItemAcceptorBlockEntity itemAcceptorBlockEntity;

	private TextFieldWidget itemWidget;
	private TextFieldWidget countWidget;

	public ItemAcceptorBlockEditScreen(ItemAcceptorBlockEntity itemAcceptorBlockEntity) {
		this.itemAcceptorBlockEntity = itemAcceptorBlockEntity;
	}

	@Override
	public void init() {
		super.init();

		if (this.client == null) return;

		Identifier item = this.itemAcceptorBlockEntity.getItem();

		this.itemWidget = new TextFieldWidget(this.textRenderer, width / 2 - 75, height / 2 - 25, 150, 20, Text.empty());
		if (!item.equals(Identifier.ofVanilla("air"))) {
			this.itemWidget.setText((this.itemAcceptorBlockEntity.isItemTag ? "#" : "") + item);
		}
		this.itemWidget.setPlaceholder(Text.translatable("gui.glowcase.item_or_tag"));
		this.itemWidget.setTextPredicate(s -> s.matches("#?[a-z0-9_.-]*:?[a-z0-9_./-]*"));

		this.countWidget = new TextFieldWidget(this.textRenderer, width / 2 - 75, height / 2 + 5, 150, 20, Text.empty());
		this.countWidget.setText(String.valueOf(this.itemAcceptorBlockEntity.count));
		this.countWidget.setPlaceholder(Text.translatable("gui.glowcase.count"));
		this.countWidget.setTextPredicate(s -> s.matches("\\d*"));

		this.addDrawableChild(this.itemWidget);
		this.addDrawableChild(this.countWidget);
	}

	@Override
	public void close() {
		String text = itemWidget.getText();
		boolean isItemTag = text.startsWith("#");
		if (isItemTag) {
			text = text.substring(1);
		}

		if (!text.isEmpty() && Identifier.tryParse(text) instanceof Identifier id) {
			this.itemAcceptorBlockEntity.setItem(id);
			this.itemAcceptorBlockEntity.isItemTag = isItemTag;
		} else {
			this.itemAcceptorBlockEntity.setItem(Identifier.ofVanilla("air"));
		}

		if (Ints.tryParse(countWidget.getText()) instanceof Integer integer) {
			this.itemAcceptorBlockEntity.count = Math.max(0, integer);
		}

		C2SEditItemAcceptorBlock.of(this.itemAcceptorBlockEntity).send();
		super.close();
	}
}
