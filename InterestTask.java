package de.lukas.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class InterestTask extends BukkitRunnable {

    private static final long INTERVAL_TICKS = 20L * 60 * 60 * 24 * 2; // 2 Tage

    public static void start() {
        long delayTicks = calculateDelayTicksToNextNoon();

        Bukkit.getScheduler().runTaskLater(EconomyPlugin.getInstance(), () -> {
            new InterestTask().run();
            Bukkit.getScheduler().runTaskTimer(EconomyPlugin.getInstance(), new InterestTask(), INTERVAL_TICKS, INTERVAL_TICKS);
        }, delayTicks);
    }

    private static long calculateDelayTicksToNextNoon() {
        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextNoon = now.withHour(12).withMinute(0).withSecond(0).withNano(0);

        if (!now.isBefore(nextNoon)) {
            nextNoon = nextNoon.plusDays(2);
        }

        Duration duration = Duration.between(now, nextNoon);
        long seconds = duration.getSeconds();

        return seconds * 20;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            var data = ConfigManager.get(uuid);

            if (!data.contains("bank")) return;

            double bankBalance = data.getDouble("bank");
            if (bankBalance <= 0) return;

            // Max 5% Zinsen bei 10 Mio Coins
            double maxBalance = 10_000_000;
            double maxInterestRate = 0.05;

            double interestRate = (bankBalance / maxBalance) * maxInterestRate;
            if (interestRate > maxInterestRate) interestRate = maxInterestRate;

            double interest = bankBalance * interestRate;

            data.set("bank", bankBalance + interest);

            // Speichere Transaktion
            TransactionManager.addTransaction(uuid, "Zinsen", interest);

            player.sendMessage(ChatColor.GOLD + "Es wurden Zinsen in Höhe von " + interest + " Coins auf dein Konto ausgezahlt!");
        });

        // Offline Spieler benachrichtigen
        ConfigManager.getAllOfflinePlayers().forEach(uuid -> {
            var data = ConfigManager.get(uuid);
            if (!data.contains("bank")) return;

            double bankBalance = data.getDouble("bank");
            if (bankBalance <= 0) return;

            double maxBalance = 10_000_000;
            double maxInterestRate = 0.05;
            double interestRate = (bankBalance / maxBalance) * maxInterestRate;
            if (interestRate > maxInterestRate) interestRate = maxInterestRate;

            double interest = bankBalance * interestRate;

            data.set("bank", bankBalance + interest);
            TransactionManager.addTransaction(uuid, "Zinsen", interest);

            // Offline Nachricht speichern (z.B. in der Config, damit bei nächstem Login gezeigt)
            OfflineMessageManager.addOfflineInterestMessage(uuid, interest);
        });

        Bukkit.broadcastMessage(ChatColor.GREEN + "Die Verzinsung wurde ausgezahlt!");
    }
}
