package net.dragoria.cores.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.dragoria.cores.Cores;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Singleton
public class ConfigService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private final Cores plugin;
    private final File configFile;

    @NotNull
    @Getter
    private CoresConfig config = new CoresConfig();

    @Inject
    public ConfigService(Cores plugin) {
        this.plugin = plugin;

        File dataFolder = plugin.getDataFolder();
        this.configFile = new File(dataFolder, "config.json");
    }

    public void loadFormFile() {
        File dataFolder = this.plugin.getDataFolder();

        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            if (Files.notExists(this.configFile.toPath())) {
                Files.write(this.configFile.toPath(), GSON.toJson(this.config).getBytes(), StandardOpenOption.CREATE_NEW);
            }

            this.config = GSON.fromJson(new String(Files.readAllBytes(this.configFile.toPath()), StandardCharsets.UTF_8), CoresConfig.class);
        } catch (IOException exception) {
            plugin.getLogger().error("Failed to load config file", exception);
        }
    }

    public void storeConfig() {
        try {
            Files.write(this.configFile.toPath(), GSON.toJson(this.config).getBytes());
        } catch (IOException exception) {
            plugin.getLogger().error("Failed to store config file", exception);
        }
    }
}
