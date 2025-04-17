package bigsir.guiadditions;

import bigsir.guiadditions.utils.FileSystemFolder;
import bigsir.guiadditions.utils.ZipUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceLoader {
	private static final String RESOURCE_DIR = FabricLoader.getInstance().getGameDir().toString() + "/guiResources/";
	private static final Gson GSON = new GsonBuilder().create();
	public static final List<ResourcePack> resourcePacks = new ArrayList<>();
	public static final List<TextureNamed> hotbarTextures = new ArrayList<>();
	public static final List<TextureNamed> slotTextures = new ArrayList<>();

	public static void loadResources() {
		loadDefault("hotbar", hotbarTextures, 256, 64);
		loadDefault("slot", slotTextures, 32, 32);
		hotbarTextures.sort(Comparator.comparingInt(a -> GuiAdditions.getPrecedence(a.getKey())));
		slotTextures.sort(Comparator.comparingInt(a -> GuiAdditions.getPrecedence(a.getKey())));

		for (ResourcePack resourcePack : resourcePacks) {
			String keyRoot = resourcePack.getKey();

			try (FileSystemFolder fsd = ZipUtils.getNewFileSystem(resourcePack.path)) {
				if(fsd == null) continue;
				Path fileSystemPath = fsd.root();

				loadResourceTexture(fileSystemPath, keyRoot, hotbarTextures, "hotbar", 256, 64);
				loadResourceTexture(fileSystemPath, keyRoot, slotTextures, "slot", 32, 32);
			} catch (IOException e) {
                GuiAdditions.LOGGER.error("Failed to load resources for {}!", resourcePack.name);
            }

		}
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
		try (Stream<Path> stream = Files.list(resourceDir)){
			List<Path> dirs = stream.collect(Collectors.toList());
			for (Path path : dirs) {
				if(Files.isDirectory(path) || isZip(path)) {
					ResourcePack pack = getResourcePack(path);
					if(pack != null) resourcePacks.add(pack);
				}
			}
		} catch (IOException e) {
		}
	}

	private static boolean isZip(Path p) {
		try (RandomAccessFile file = new RandomAccessFile(p.toFile(), "r")) {
			int sign = file.readInt();
			return sign == 0x504B0304;
		} catch (IOException ignored) {
			return false;
		}
    }

	private static void loadResourceTexture(Path resourcePath, String key, List<TextureNamed> textures, String folder, int width, int height) {
		Path hotbarDirectory = resourcePath.resolve(folder);

		if (Files.notExists(hotbarDirectory)) return;

		try (Stream<Path> paths = Files.list(hotbarDirectory)) {
			paths.forEach(p -> loadTexture(p, key, folder, textures, width, height));
        } catch (IOException e) {
			GuiAdditions.LOGGER.error("Failed to load hotbar selector textures!");
		}
    }

	private static void loadTexture(Path path, String prefix, String folder, List<TextureNamed> textures, int width, int height) {
		if (isPng(path)) {
			String fileName = path.getFileName().toString();
			String textureKey = prefix + "." + folder + "." + fileName.substring(0, fileName.lastIndexOf(".png"));

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

	@SuppressWarnings("OptionalGetWithoutIsPresent") //This should never fail
	private static void loadDefault(String folder, List<TextureNamed> textures, int width, int height) {
		String path = "assets/guiadditions/textures/gui/" + folder;

		ModContainer mod = FabricLoader.getInstance().getModContainer(GuiAdditions.MOD_ID).get();
		Optional<Path> optionalPath = mod.findPath(path);
		if (!optionalPath.isPresent()) {
			GuiAdditions.LOGGER.error("Failed to load defaults from {}! (Missing Path)", folder);
			return;
		}

		try	(Stream<Path> paths = Files.list(optionalPath.get())) {
			paths.forEach(p -> loadTexture(p, GuiAdditions.MOD_ID, folder, textures, width, height));
		} catch (IOException e) {
			GuiAdditions.LOGGER.error("Failed to load defaults from {}! (IO Exception)", folder);
        }
    }

	private static boolean isPng(Path path) {
		try {
			return Files.probeContentType(path).equals("image/png");
		}catch (IOException e) {
            return false;
        }
	}

	public static ResourcePack getResourcePack(Path path) {
		try (FileSystemFolder fsd = ZipUtils.getNewFileSystem(path)) {
			if(fsd == null) return null;
			Path resourcePath = fsd.root();

			Path p = resourcePath.resolve("meta.json");
			if (Files.notExists(p)) return null;


			try (InputStreamReader reader = new InputStreamReader(p.toUri().toURL().openStream())){
				return GSON.fromJson(reader, ResourcePack.class).withPath(path);
			} catch (IOException e) {
				GuiAdditions.LOGGER.error("An error has occurred while loading metadata of {}!", path.getFileName());
				return null;
			}
		} catch (IOException e) {
			GuiAdditions.LOGGER.error("An error has occurred while loading metadata of {}", path.getFileName());
			return null;
        }

    }

	public static class ResourcePack {
		private Path path;
		private final String author;
		private final String name;
		private final int version;

		public ResourcePack(String author, String name, int version) {
			this.author = author;
			this.name = name;
			this.version = version;
		}

		public ResourcePack withPath(Path path) {
			this.path = path;
			return this;
		}

		public Path getPath() {
			return path;
		}

		public String getName() {
			return name;
		}

		private String formattedString(String string) {
			String str = string.replaceAll("[ .\n\t]", "").toLowerCase();
			return str.substring(0, Math.min(str.length(), 48));
		}

		public String getKey() {
			return formattedString(this.author) + "." + formattedString(this.name);
		}
	}
}
