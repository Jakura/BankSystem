package de.lukas.economy;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinQuitHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        var config = ConfigManager.get(uuid);

        // Startguthaben wenn neu
        if (!config.contains("piggy")) {
            config.set("piggy", 5000.0);
            config.set("bank", 0.0);
            ConfigManager.save(uuid);
        }

        // Offline Zinsnachricht anzeigen
        double offlineInterest = OfflineMessageManager.getOfflineInterest(uuid);
        if (offlineInterest > 0) {
            player.sendMessage(ChatColor.GOLD + "Du hast während deiner Abwesenheit " + offlineInterest + " Coins an Zinsen erhalten.");
            OfflineMessageManager.clearOfflineInterest(uuid);
        }
    }
}
