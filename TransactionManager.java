package de.lukas.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionManager {

    public static void addTransaction(UUID uuid, String type, double amount) {
        FileConfiguration config = ConfigManager.get(uuid);

        List<String> transactions = config.getStringList("transactions");
        if (transactions == null) transactions = new ArrayList<>();

        String entry = type + ":" + amount + ":" + System.currentTimeMillis();
        transactions.add(entry);

        // Max 10 Transaktionen speichern
        if (transactions.size() > 10) {
            transactions = transactions.subList(transactions.size() - 10, transactions.size());
        }

        config.set("transactions", transactions);
        ConfigManager.save(uuid);
    }

    public static List<String> getLastTransactions(UUID uuid) {
        FileConfiguration config = ConfigManager.get(uuid);
        return config.getStringList("transactions");
    }
}
