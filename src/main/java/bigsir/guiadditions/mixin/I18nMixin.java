package bigsir.guiadditions.mixin;

import bigsir.guiadditions.ResourceLoader;
import io.github.prospector.modmenu.mixin.LanguageAccessor;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

@Mixin(value = I18n.class, remap = false)
public abstract class I18nMixin {
	@Shadow
	private Language currentLanguage;

	@Inject(method = "reload(Ljava/lang/String;Z)V", at = @At("TAIL"))
	public void load(String languageCode, boolean save, CallbackInfo ci) {
		for (Path resourcePath : ResourceLoader.resourceCandidates) {
			Path langPath = Paths.get(resourcePath + "/lang/" + languageCode + ".lang");
			ResourceLoader.Meta meta = ResourceLoader.getMeta(resourcePath);
			if(Files.notExists(langPath) || meta == null) continue;

			try (InputStreamReader reader = new InputStreamReader(langPath.toUri().toURL().openStream())) {
				Properties tempProperties = new Properties();
				Properties currentProperties = ((LanguageAccessor)this.currentLanguage).getEntries();
				tempProperties.load(reader);

				for (Map.Entry<Object, Object> entry : tempProperties.entrySet()) {
					currentProperties.setProperty(ResourceLoader.getInternalKey(meta) + "." + entry.getKey(), (String) entry.getValue());
				}
				((LanguageAccessor)this.currentLanguage).getEntries().load(reader);
			} catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
	}
}
