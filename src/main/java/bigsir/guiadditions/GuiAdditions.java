package bigsir.guiadditions;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.OptionBoolean;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.lang.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;

public class GuiAdditions implements ModInitializer, ClientStartEntrypoint {
    public static final String MOD_ID = "guiadditions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static OptionBoolean showBackground;
	public static OptionChoice customSlotSelector;
	public static OptionChoice selectorStyle;

    @Override
    public void onInitialize() {
		ResourceLoader.findResourceCandidates();
        LOGGER.info("Gui Additions initialized.");
    }

	@Override
	public void beforeClientStart() {
	}

	@Override
	public void afterClientStart() {
		ResourceLoader.loadResources();

		selectorStyle.setValues(ResourceLoader.hotbarTextures.size() + 1);
		customSlotSelector.setValues(ResourceLoader.slotTextures.size() + 1);

		selectorStyle.value = Math.min(selectorStyle.value, selectorStyle.highest);
		customSlotSelector.value = Math.min(customSlotSelector.value, customSlotSelector.highest);

		OptionsPage page = OptionsPages.register(new OptionsPage(langKey("options"), Blocks.SAND.getDefaultStack()));
		page.withComponent(
			new OptionsCategory(langKey("category.visuals"))
				.withComponent(new BooleanOptionComponent(showBackground))
				.withComponent(new ToggleableOptionComponent<>(selectorStyle))
				.withComponent(new ToggleableOptionComponent<>(customSlotSelector))
		);
	}

	public static TextureNamed getSlotTexture() {
		return ResourceLoader.slotTextures.get(customSlotSelector.value - 1);
	}

	public static TextureNamed getHotbarTexture() {
		return ResourceLoader.hotbarTextures.get(selectorStyle.value - 1);
	}

	public static String getSlotName(int i) {
		if (i == 0) return I18n.getInstance().translateKey("guiadditions.slot.default");

		return I18n.getInstance().translateKey(ResourceLoader.slotTextures.get(i - 1).getKey());
	}

	public static String getHotbarName(int i) {
		if (i == 0) return I18n.getInstance().translateKey("guiadditions.hotbar.default");

		return I18n.getInstance().translateKey(ResourceLoader.hotbarTextures.get(i - 1).getKey());
	}

	public static String langKey(String string) {
		return MOD_ID + "." + string;
	}

	public static int getPrecedence(String name) {
		return Precedence.valueOf(name.substring(name.indexOf(".") + 1).replace(".", "_").toUpperCase()).precedence;
	}

	@SuppressWarnings("unused")
	private enum Precedence {
		HOTBAR_ARROWS,
		HOTBAR_OUTLINE,
		HOTBAR_ORNATE,
		HOTBAR_DIAMOND_RUSH,
		HOTBAR_FANTASY,
		HOTBAR_LOVELY,
		HOTBAR_WORM,
		SLOT_CORNER,
		SLOT_THICK;

		private final int precedence;
		private static int incr = 0;

		private int getId() {
			return ++incr;
		}

		Precedence() {
			this.precedence = getId();
		}
	}
}
