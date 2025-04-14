package bigsir.guiadditions.mixin;

import bigsir.guiadditions.GuiAdditions;
import bigsir.guiadditions.OptionChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(value = GameSettings.class, remap = false)
public abstract class GameSettingsMixin {
	@Inject(method = "<init>", at = @At(value = "NEW", target = "(Ljava/io/File;Ljava/lang/String;)Ljava/io/File;"))
	public void init(Minecraft minecraft, File file, CallbackInfo ci) {
		GameSettings ref = (GameSettings) (Object) this;

		GuiAdditions.showBackground = new OptionBoolean(ref, GuiAdditions.langKey("showBackground"), true);
		GuiAdditions.selectorStyle = new OptionChoice(ref, GuiAdditions.langKey("hotbarSelectorStyle"), 1, 2,
			GuiAdditions::getHotbarName);
		GuiAdditions.customSlotSelector = new OptionChoice(ref, GuiAdditions.langKey("slotSelectorStyle"), 0, 2,
			GuiAdditions::getSlotName);
	}
}
