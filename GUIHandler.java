package de.lukas.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class GUIHandler implements Listener {

    private static final int INVENTORY_SIZE = 36;

    public static void openBankMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "Bank Menu");

        // Hintergrund füllen
        ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        background.setItemMeta(bgMeta);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inv.setItem(i, background);
        }

        // Redstone Torch auf Slot 32 mit Timer
        inv.setItem(32, createRedstoneTorchWithTimer());

        // TODO: Andere Items (Kiste, Dropper, Barrier, Map...) setzen hier

        player.openInventory(inv);

        startTimerUpdate(player, inv);
    }

    private static ItemStack createRedstoneTorchWithTimer() {
        ItemStack torch = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = torch.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Kontoinformationen");

        String timeString = getTimeUntilNextInterest();
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "Nächste Verzinsung in:", ChatColor.GREEN + timeString));

        torch.setItemMeta(meta);
        return torch;
    }

    private static String getTimeUntilNextInterest() {
        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextNoon = now.withHour(12).withMinute(0).withSecond(0).withNano(0);

        if (!now.isBefore(nextNoon)) {
            nextNoon = nextNoon.plusDays(2);
        }

        Duration duration = Duration.between(now, nextNoon);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static void startTimerUpdate(Player player, Inventory inv) {
        Bukkit.getScheduler().runTaskTimer(EconomyPlugin.getInstance(), task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }
            if (player.getOpenInventory().getTopInventory().equals(inv)) {
                inv.setItem(32, createRedstoneTorchWithTimer());
            } else {
                task.cancel();
            }
        }, 20L, 20L);
    }

    // Hier kommen noch die Klick-Events rein (z.B. Listener onInventoryClick)
}
