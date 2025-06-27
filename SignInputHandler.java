package de.lukas.economy;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.block.Action;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SignInputHandler implements Listener {

    private static final HashMap<UUID, Boolean> waitingForInput = new HashMap<>(); // true = einzahlen, false = abheben

    // Methode um das Schild-Input-Menü zu öffnen (man simuliert, dass der Spieler ein Schild bearbeiten soll)
    public static void openSignEditor(Player player, boolean isDeposit) {
        waitingForInput.put(player.getUniqueId(), isDeposit);
        player.sendMessage(ChatColor.YELLOW + "Bitte gib im Chat den Betrag ein, den du " + (isDeposit ? "einzahlen" : "abheben") + " möchtest:");
        player.sendMessage(ChatColor.GRAY + "(Gib eine Zahl ein oder tippe 'abbrechen')");
    }

    // Chat Event, um Eingabe vom Spieler zu bekommen
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!waitingForInput.containsKey(uuid)) {
            return; // Spieler gibt kein Betrag gerade ein
        }

        event.setCancelled(true); // Nachricht wird nicht normal im Chat gezeigt

        String msg = event.getMessage();

        if (msg.equalsIgnoreCase("abbrechen")) {
            waitingForInput.remove(uuid);
            player.sendMessage(ChatColor.RED + "Eingabe abgebrochen.");
            return;
        }

        boolean isDeposit = waitingForInput.get(uuid);

        double amount;
        try {
            amount = Double.parseDouble(msg);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Bitte gib eine Zahl größer als 0 ein.");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Ungültige Zahl, bitte versuche es erneut.");
            return;
        }

        // Hier wird die Aktion ausgeführt (Einzahlen oder Abheben)
        Bukkit.getScheduler().runTask(EconomyPlugin.getInstance(), () -> {
            var data = ConfigManager.get(uuid);
            double piggy = data.getDouble("piggy");
            double bank = data.getDouble("bank");

            if (isDeposit) {
                if (piggy < amount) {
                    player.sendMessage(ChatColor.RED + "Du hast nicht genug Coins in der Piggybank.");
                    waitingForInput.remove(uuid);
                    return;
                }
                data.set("piggy", piggy - amount);
                data.set("bank", bank + amount);
                TransactionManager.addTransaction(uuid, "Einzahlung", amount);
                player.sendMessage(ChatColor.GREEN + "Du hast " + amount + " Coins eingezahlt.");
            } else {
                if (bank < amount) {
                    player.sendMessage(ChatColor.RED + "Du hast nicht genug Coins auf dem Bankkonto.");
                    waitingForInput.remove(uuid);
                    return;
                }
                data.set("piggy", piggy + amount);
                data.set("bank", bank - amount);
                TransactionManager.addTransaction(uuid, "Abhebung", amount);
                player.sendMessage(ChatColor.GREEN + "Du hast " + amount + " Coins abgehoben.");
            }
            ConfigManager.save(uuid);
            waitingForInput.remove(uuid);

            // Öffne danach das Hauptbankmenü wieder
            openBankMenu(player);
        });
    }

    // Falls Spieler während der Eingabe den Server verlässt, entfernen wir ihn aus der Map
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        waitingForInput.remove(event.getPlayer().getUniqueId());
    }
}
