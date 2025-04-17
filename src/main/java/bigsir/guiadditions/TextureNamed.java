package bigsir.guiadditions;

import net.minecraft.client.render.texture.TextureBuffered;
import net.minecraft.core.lang.I18n;

import java.awt.image.BufferedImage;

public class TextureNamed extends TextureBuffered {
	private final String key;
	public TextureNamed(BufferedImage image, String key) {
		super(image, false, false, false);
		this.image = null;
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getTranslated() {
		return I18n.getInstance().translateKey(key);
	}
}
