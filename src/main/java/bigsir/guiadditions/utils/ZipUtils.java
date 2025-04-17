package bigsir.guiadditions.utils;

import bigsir.guiadditions.GuiAdditions;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZipUtils {
	private static final FileSystem DEFAULT = FileSystems.getDefault();

	private static boolean isZip(Path p) {
		try (RandomAccessFile file = new RandomAccessFile(p.toFile(), "r")) {
			int sign = file.readInt();
			return sign == 0x504B0304;
		} catch (IOException ignored) {
			return false;
		}
	}

	public static FileSystemFolder getNewFileSystem(Path directoryPath) {
		if (Files.isDirectory(directoryPath)) return new FileSystemFolder(DEFAULT, directoryPath);

		try {
			FileSystem fs = FileSystems.newFileSystem(directoryPath, (ClassLoader) null);
			return new FileSystemFolder(fs, fs.getPath(""));
		}catch (IOException e) {
            GuiAdditions.LOGGER.warn("Failed to create FileSystem! {}", e.getMessage());
        }

        return null;
    }
}
