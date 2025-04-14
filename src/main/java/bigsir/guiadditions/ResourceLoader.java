package bigsir.guiadditions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class ResourceLoader {
	private static final String RESOURCE_DIR = FabricLoader.getInstance().getGameDir().toString() + "/guiResources/";
	private static final Gson GSON = new GsonBuilder().create();
	public static final List<Path> resourceCandidates = new ArrayList<>();
	public static final List<TextureNamed> hotbarTextures = new ArrayList<>();
	public static final List<TextureNamed> slotTextures = new ArrayList<>();

	public static void loadResources() {
		loadDefault("hotbar", hotbarTextures, 256, 64);
		loadDefault("slot", slotTextures, 32, 32);

		exploreCandidates();
    }

	public static void findResourceCandidates() {
		Path resourceDir = Paths.get(RESOURCE_DIR);
		if (!Files.exists(resourceDir)) {
			try {
				Files.createDirectory(resourceDir);
			}catch (IOException e) {
				GuiAdditions.LOGGER.error("Failed to create resource directory!");
			}
			return;
		}

		//Add potential resource packs to list
		try (DirectoryStream<Path> dirs = Files.newDirectoryStream(resourceDir)) {
			for (Path p : dirs) {
				if (Files.isDirectory(p)) resourceCandidates.add(p);
			}
		} catch (IOException e) {
			GuiAdditions.LOGGER.error("Failed to load resources!");
		}
	}

	private static void exploreCandidates() {
		for (Path resourcePath : resourceCandidates) {
			Meta meta;

			if ((meta = getMeta(resourcePath)) == null) continue;

			String keyRoot = getInternalKey(meta);

			loadHotbarTextures(resourcePath, keyRoot);
			loadSlotTextures(resourcePath, keyRoot);
        }
	}

	public static String getInternalKey(Meta meta) {
		return formattedString(meta.author) + "." + formattedString(meta.name);
	}

	private static void loadHotbarTextures(Path resourcePath, String key) {
		Path hotbarDirectory = Paths.get(resourcePath + (resourcePath.endsWith("/") ? "" : "/") + "hotbar/");
		if (Files.notExists(hotbarDirectory)) return;

		try (DirectoryStream<Path> paths = Files.newDirectoryStream(hotbarDirectory)) {
			for (Path path : paths) {
				if (isPng(path)) {
					String fileName = path.getFileName().toString();
					String textureKey = key + ".hotbar." + fileName.substring(0, fileName.lastIndexOf(".png"));

					TextureNamed texture = new TextureNamed(ImageIO.read(path.toFile()), textureKey);

					if(texture.getWidth() != 256 || texture.getHeight() != 64) {
						GuiAdditions.LOGGER.error(fileName + " has incorrect size! Expected: 256x32");
						texture.delete();
						continue;
					}

					hotbarTextures.add(texture);
				}
			}
        } catch (IOException e) {
			GuiAdditions.LOGGER.error("Failed to load hotbar selector textures!");
		}
    }

	private static void loadSlotTextures(Path resourcePath, String key) {
		Path slotDirectory = Paths.get(resourcePath + (resourcePath.endsWith("/") ? "" : "/") + "slot/");
		if (Files.notExists(slotDirectory)) return;

		try (DirectoryStream<Path> paths = Files.newDirectoryStream(slotDirectory)) {
			for (Path path : paths) {
				if (isPng(path)) {
					String fileName = path.getFileName().toString();
					String textureKey = key + ".slot." + fileName.substring(0, fileName.lastIndexOf(".png"));

					TextureNamed texture = new TextureNamed(ImageIO.read(path.toFile()), textureKey);

					if(texture.getWidth() != 32 || texture.getHeight() != 32) {
						GuiAdditions.LOGGER.error(fileName + " has incorrect size! Expected: 32x32");
						texture.delete();
					}

					slotTextures.add(texture);
				}
			}
		} catch (IOException e) {
			GuiAdditions.LOGGER.error("Failed to load slot selector textures!");
		}
	}

	private static @Nullable URI getUri(URL url) {
		try {
			return url == null ? null : url.toURI();
		} catch (URISyntaxException e) {
            return null;
        }
    }

	private static @Nullable Stream<Path> getPathStreamFixed(String resourcesPath) {
		URL url = ResourceLoader.class.getResource(resourcesPath);
		URI uri = getUri(url);
		if (uri == null) return null;

		Map<String, String> env = new HashMap<>();

		try {
			return Files.list(Paths.get(uri));
		} catch (IOException | FileSystemNotFoundException e) {
			try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
				return Files.list(fs.getPath(resourcesPath));
			} catch (IOException ex) {
				return null;
			}
        }
	}

	private static void loadTexture (Path path, String folder, List<TextureNamed> textures, int width, int height) {
		if (isPng(path)) {
			String fileName = path.getFileName().toString();
			String textureKey = "guiadditions." + folder + "." + fileName.substring(0, fileName.lastIndexOf(".png"));

			TextureNamed texture;
			try {
				texture = new TextureNamed(ImageIO.read(path.toUri().toURL()), textureKey);
			} catch (IOException e) {
				return;
			}

			if(texture.getWidth() != width || texture.getHeight() != height) {
				GuiAdditions.LOGGER.error(String.format("%s has incorrect size! Expected: %dx%d", fileName, width, height));
				texture.delete();
			}

			textures.add(texture);
		}
	}

	private static void loadDefault(String folder, List<TextureNamed> textures, int width, int height) {
		String path = "/assets/guiadditions/textures/gui/" + folder;

		try	(Stream<Path> paths = getPathStreamFixed(path)) {
			if(paths == null) {
				GuiAdditions.LOGGER.error("Failed to load resources from " + folder);
				return;
			}

			paths.forEach(p -> loadTexture(p, folder, textures, width, height));
		}
	}

	private static FileSystem getFileSystem(URI uri) {
		try {
			return FileSystems.getFileSystem(uri);
		}catch (Exception e) {
			try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())){
				return fs;
			} catch (IOException ex) {
				return  null;
			}
		}
	}

	private static boolean isPng(Path path) {
		try {
			return Files.probeContentType(path).equals("image/png");
		}catch (IOException e) {
            return false;
        }
	}

	private static String formattedString(String string) {
		String str = string.replaceAll("[ .\n\t]", "").toLowerCase();
		return str.substring(0, Math.min(str.length(), 48));
	}

	public static Meta getMeta(Path resourcePath) {
		Path path = Paths.get((resourcePath.endsWith("/") ? resourcePath.toString() : resourcePath + "/") + "meta.json");
		if (Files.notExists(path)) return null;

		try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path))){
			return GSON.fromJson(reader, Meta.class);
		} catch (IOException e) {
			GuiAdditions.LOGGER.error("An error has occurred while loading metadata of " + resourcePath.getFileName() + "!");
			return null;
		}
	}

	public static class Meta {
		public String author;
		public String name;
		public int version;
	}
}
