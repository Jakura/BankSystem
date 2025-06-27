package de.lukas.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class OfflineMessageManager {

    public static void addOfflineInterestMessage(UUID uuid, double amount) {
        FileConfiguration config = ConfigManager.get(uuid);

        String key = "offlineMessages.interest";

        double current = config.getDouble(key, 0);
        config.set(key, current + amount);

        ConfigManager.save(uuid);
    }

    public static double getOfflineInterest(UUID uuid) {
        FileConfiguration config = ConfigManager.get(uuid);
        return config.getDouble("offlineMessages.interest", 0);
    }

    public static void clearOfflineInterest(UUID uuid) {
        FileConfiguration config = ConfigManager.get(uuid);
        config.set("offlineMessages.interest", 0);
        ConfigManager.save(uuid);
    }
}
