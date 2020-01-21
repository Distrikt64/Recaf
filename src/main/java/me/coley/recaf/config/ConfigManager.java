package me.coley.recaf.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.coley.recaf.util.Log.*;

/**
 * Config manager.
 *
 * @author Matt
 */
public class ConfigManager {
	private static final String KEY_DISPLAY = "display";
	private static final String KEY_KEYBINDING = "keybinding";
	private static final String KEY_DECOMPILE = "decompile";
	private static final String KEY_BACKEND = "backend";
	private final Map<String, Config> configs = new HashMap<>();
	private final Path configDirectory;

	/**
	 * Creates new configuration manager instance
	 *
	 * @param configDirectory
	 *     directory where configuration files are stored
	 */
	public ConfigManager(Path configDirectory) {
		this.configDirectory = Objects.requireNonNull(configDirectory, "configDirectory");
	}

	/**
	 * Setup config instances.
	 *
	 * @throws IOException
	 *     if any I/O error occurs
	 */
	public void initialize() throws IOException {
		// Setup each instance
		configs.put(KEY_DISPLAY, new ConfDisplay());
		configs.put(KEY_KEYBINDING, new ConfKeybinding());
		configs.put(KEY_DECOMPILE, new ConfDecompile());
		configs.put(KEY_BACKEND, new ConfBackend());
		if (!Files.isDirectory(configDirectory)) {
			Files.createDirectories(configDirectory);
		} else {
			// Load initial values
			load();
		}
		// Add shutdown save hook
		Runtime.getRuntime().addShutdownHook(new Thread(this::save));
	}
	// =============================================================- //

	/**
	 * @return Display configuration.
	 */
	public ConfDisplay display() {
		return (ConfDisplay) configs.get(KEY_DISPLAY);
	}

	/**
	 * @return Keybinding configuration.
	 */
	public ConfKeybinding keys() {
		return (ConfKeybinding) configs.get(KEY_KEYBINDING);
	}

	/**
	 * @return Decompiler configuration.
	 */
	public ConfDecompile decompile() {
		return (ConfDecompile) configs.get(KEY_DECOMPILE);
	}

	/**
	 * @return Private configuration.
	 */
	public ConfBackend backend() {
		return (ConfBackend) configs.get(KEY_BACKEND);
	}

	// ============================================================== //

	private void load() {
		for (Config c : configs.values()) {
			Path path = resolveConfigPath(c);
			try {
				if(Files.exists(path))
					c.load(path);
			} catch(IOException ex) {
				error(ex, "Failed to load config: {}" + path);
			}
		}
	}

	private void save() {
		for (Config c : configs.values()) {
			Path path = resolveConfigPath(c);
			try {
				c.save(path);
			} catch(IOException ex) {
				error(ex, "Failed to save config: {}" + path);
			}
		}
	}

	private Path resolveConfigPath(Config config) {
		return configDirectory.resolve(config.getName() + ".json");
	}
}