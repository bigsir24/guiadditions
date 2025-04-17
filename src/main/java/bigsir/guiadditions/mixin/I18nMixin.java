package bigsir.guiadditions.mixin;

import bigsir.guiadditions.GuiAdditions;
import bigsir.guiadditions.ResourceLoader;
import bigsir.guiadditions.utils.FileSystemFolder;
import bigsir.guiadditions.utils.ZipUtils;
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
import java.util.Map;
import java.util.Properties;

@Mixin(value = I18n.class, remap = false)
public abstract class I18nMixin {
	@Shadow
	private Language currentLanguage;

	@Inject(method = "reload(Ljava/lang/String;Z)V", at = @At("TAIL"))
	public void load(String languageCode, boolean save, CallbackInfo ci) {
		String relativePath = "lang/" + languageCode + ".lang";
		for (ResourceLoader.ResourcePack pack : ResourceLoader.resourcePacks) {

			FileSystemFolder fsd = ZipUtils.getNewFileSystem(pack.getPath());
			if(fsd == null) continue;

			Path langPath = fsd.resolve(relativePath);

			if(Files.notExists(langPath)) continue;

			try (InputStreamReader reader = new InputStreamReader(langPath.toUri().toURL().openStream())) {
				Properties tempProperties = new Properties();
				Properties currentProperties = ((LanguageAccessor)this.currentLanguage).getEntries();
				tempProperties.load(reader);

				for (Map.Entry<Object, Object> entry : tempProperties.entrySet()) {
					currentProperties.setProperty(pack.getKey()+ "." + entry.getKey(), (String) entry.getValue());
				}
				((LanguageAccessor)this.currentLanguage).getEntries().load(reader);
			} catch (IOException e) {
				GuiAdditions.LOGGER.error("Failed to load lang files of {}", pack.getName());
            }finally {
				try {
					fsd.close();
				} catch (IOException e) {
				}
			}
        }
	}
}
