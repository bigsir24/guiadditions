package bigsir.guiadditions.utils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileSystemFolder implements AutoCloseable {
	private final FileSystem fileSystem;
	private final Path root;

	protected FileSystemFolder(FileSystem fileSystem, Path root) {
		this.fileSystem = fileSystem;
		this.root = root;
	}

	public Path root() {
		return root;
	}

	public Path resolve(String other) {
		return root.resolve(other);
	}

	@Override
	public void close() throws IOException {
		if (fileSystem != FileSystems.getDefault()) {
			fileSystem.close();
		}
	}
}
