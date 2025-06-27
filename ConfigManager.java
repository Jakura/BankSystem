package de.lukas.economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfigManager {

    private static final Map<UUID, FileConfiguration> playerConfigs = new HashMap<>();
    private static final File pluginFolder = EconomyPlugin.getInstance().getDataFolder();

    public static void loadAllPlayerData() {
        if (!pluginFolder.exists()) pluginFolder.mkdirs();

        for (File file : pluginFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                playerConfigs.put(uuid, config);
            }
        }
    }

    public static FileConfiguration get(UUID uuid) {
        if (playerConfigs.containsKey(uuid)) return playerConfigs.get(uuid);

        File file = new File(pluginFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        playerConfigs.put(uuid, config);
        return config;
    }

    public static void save(UUID uuid) {
        FileConfiguration config = playerConfigs.get(uuid);
        if (config == null) return;
        try {
            config.save(new File(pluginFolder, uuid.toString() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Iterable<UUID> getAllOfflinePlayers() {
        return playerConfigs.keySet();
    }
}
