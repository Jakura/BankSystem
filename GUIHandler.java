package de.lukas.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

public class GUIHandler implements Listener {

    private static final int INVENTORY_SIZE = 36;
    private static final Material BACKGROUND_MATERIAL = Material.BLACK_STAINED_GLASS_PANE;

    // Öffnet das Hauptbank-Menü
    public static void openBankMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "Bank Menu");

        fillBackground(inv);

        inv.setItem(11, createItem(Material.CHEST, ChatColor.GREEN + "Einzahlen"));
        inv.setItem(13, createItem(Material.DROPPER, ChatColor.GREEN + "Abheben"));
        inv.setItem(15, createItem(Material.FILLED_MAP, ChatColor.GREEN + "Letzte 10 Transaktionen"));
        inv.setItem(31, createItem(Material.BARRIER, ChatColor.RED + "Schließen"));
        inv.setItem(32, createRedstoneTorchWithTimer());

        player.openInventory(inv);

        startTimerUpdate(player, inv);
    }

    // Einzahlmenü öffnen
    public static void openDepositMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "Einzahlen");

        fillBackground(inv);

        inv.setItem(11, createItemAmount(Material.CHEST, ChatColor.GREEN + "Alles Einzahlen", 64));
        inv.setItem(13, createItemAmount(Material.CHEST, ChatColor.GREEN + "Die Hälfte Einzahlen", 32));
        inv.setItem(15, createItem(Material.SIGN, ChatColor.GREEN + "Spezifischen Betrag Einzahlen"));
        inv.setItem(31, createItem(Material.ARROW, ChatColor.YELLOW + "Zurück"));

        player.openInventory(inv);
    }

    // Abhebemenü öffnen
    public static void openWithdrawMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "Abheben");

        fillBackground(inv);

        inv.setItem(10, createItemAmount(Material.DROPPER, ChatColor.GREEN + "Alles Abheben", 64));
        inv.setItem(12, createItemAmount(Material.DROPPER, ChatColor.GREEN + "Die Hälfte Abheben", 32));
        inv.setItem(14, createItemAmount(Material.DROPPER, ChatColor.GREEN + "20% Abheben", 1));
        inv.setItem(16, createItem(Material.SIGN, ChatColor.GREEN + "Spezifischen Betrag Abheben"));
        inv.setItem(31, createItem(Material.ARROW, ChatColor.YELLOW + "Zurück"));

        player.openInventory(inv);
    }

    // Transaktionen anzeigen
    public static void openTransactionsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "Letzte Transaktionen");

        fillBackground(inv);

        UUID uuid = player.getUniqueId();
        var transactions = TransactionManager.getLastTransactions(uuid);

        int slot = 10;
        if (transactions != null) {
            for (String entry : transactions) {
                String[] parts = entry.split(":");
                if (parts.length >= 3) {
                    String type = parts[0];
                    String amount = parts[1];
                    long timestamp = Long.parseLong(parts[2]);

                    ItemStack item = createItem(Material.PAPER, ChatColor.AQUA + type,
                            ChatColor.GRAY + "Betrag: " + amount,
                            ChatColor.DARK_GRAY + "Zeit: " + new java.util.Date(timestamp).toString());

                    if (slot > 25) break;
                    inv.setItem(slot, item);
                    slot++;
                }
            }
        }

        inv.setItem(31, createItem(Material.ARROW, ChatColor.YELLOW + "Zurück"));

        player.openInventory(inv);
    }

    private static void fillBackground(Inventory inv) {
        ItemStack bg = new ItemStack(BACKGROUND_MATERIAL);
        ItemMeta meta = bg.getItemMeta();
        meta.setDisplayName(" ");
        bg.setItemMeta(meta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createItemAmount(Material material, String name, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
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
        }, 20L, 20L); // alle 20 Ticks = 1 Sekunde
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        Inventory inv = e.getInventory();
        if (inv == null) return;
        String title = inv.getTitle();

        if (title.equals("Bank Menu")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == BACKGROUND_MATERIAL) return;

            switch (e.getRawSlot()) {
                case 11 -> openDepositMenu(player);
                case 13 -> openWithdrawMenu(player);
                case 15 -> openTransactionsMenu(player);
                case 31 -> player.closeInventory();
                case 32 -> {
                    // Konto info - einfach Nachricht zeigen, kann man erweitern
                    player.sendMessage(ChatColor.GREEN + "Piggybank: " + ConfigManager.get(player.getUniqueId()).getDouble("piggy"));
                    player.sendMessage(ChatColor.GREEN + "Bankkonto: " + ConfigManager.get(player.getUniqueId()).getDouble("bank"));
                }
            }
        } else if (title.equals("Einzahlen")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == BACKGROUND_MATERIAL) return;

            UUID uuid = player.getUniqueId();
            var data = ConfigManager.get(uuid);
            double piggy = data.getDouble("piggy");
            double bank = data.getDouble("bank");

            switch (e.getRawSlot()) {
                case 11 -> { // Alles einzahlen
                    if (piggy <= 0) {
                        player.sendMessage(ChatColor.RED + "Du hast keine Coins in der Piggybank.");
                        return;
                    }
                    data.set("piggy", 0);
                    data.set("bank", bank + piggy);
                    TransactionManager.addTransaction(uuid, "Einzahlung", piggy);
                    ConfigManager.save(uuid);
                    player.sendMessage(ChatColor.GREEN + "Alles eingezahlt: " + piggy + " Coins.");
                    openBankMenu(player);
                }
                case 13 -> { // Hälfte einzahlen
                    if (piggy <= 0) {
                        player.sendMessage(ChatColor.RED + "Du hast keine Coins in der Piggybank.");
                        return;
                    }
                    double half = piggy / 2;
                    data.set("piggy", piggy - half);
                    data.set("bank", bank + half);
                    TransactionManager.addTransaction(uuid, "Einzahlung", half);
                    ConfigManager.save(uuid);
                    player.sendMessage(ChatColor.GREEN + "Die Hälfte eingezahlt: " + half + " Coins.");
                    openBankMenu(player);
                }
                case 15 -> {
                    // Schild Eingabe Menu öffnen (hier musst du deinen SignInputHandler antriggern)
                    SignInputHandler.openSignEditor(player, true); // true = Einzahlen
                    player.closeInventory();
                }
                case 31 -> openBankMenu(player);
            }

        } else if (title.equals("Abheben")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == BACKGROUND_MATERIAL) return;

            UUID uuid = player.getUniqueId();
            var data = ConfigManager.get(uuid);
            double piggy = data.getDouble("piggy");
            double bank = data.getDouble("bank");

            switch (e.getRawSlot()) {
                case 10 -> { // Alles abheben
                    if (bank <= 0) {
                        player.sendMessage(ChatColor.RED + "Du hast keine Coins auf dem Bankkonto.");
                        return;
                    }
                    data.set("piggy", piggy + bank);
                    data.set("bank", 0);
                    TransactionManager.addTransaction(uuid, "Abhebung", bank);
                    ConfigManager.save(uuid);
                    player.sendMessage(ChatColor.GREEN + "Alles abgehoben: " + bank + " Coins.");
                    openBankMenu(player);
                }
                case 12 -> { // Hälfte abheben
                    if (bank <= 0) {
                        player.sendMessage(ChatColor.RED + "Du hast keine Coins auf dem Bankkonto.");
                        return;
                    }
                    double half = bank / 2;
                    data.set("piggy", piggy + half);
                    data.set("bank", bank - half);
                    TransactionManager.addTransaction(uuid, "Abhebung", half);
                    ConfigManager.save(uuid);
                    player.sendMessage(ChatColor.GREEN + "Die Hälfte abgehoben: " + half + " Coins.");
                    openBankMenu(player);
                }
                case 14 -> { // 20% abheben
                    if (bank <= 0) {
                        player.sendMessage(ChatColor.RED + "Du hast keine Coins auf dem Bankkonto.");
                        return;
                    }
                    double twentyPercent = bank * 0.2;
                    data.set("piggy", piggy + twentyPercent);
                    data.set("bank", bank - twentyPercent);
                    TransactionManager.addTransaction(uuid, "Abhebung", twentyPercent);
                    ConfigManager.save(uuid);
                    player.sendMessage(ChatColor.GREEN + "20% abgehoben: " + twentyPercent + " Coins.");
                    openBankMenu(player);
                }
                case 16 -> {
                    // Schild Eingabe Menu öffnen (Abheben)
                    SignInputHandler.openSignEditor(player, false); // false = Abheben
                    player.closeInventory();
                }
                case 31 -> openBankMenu(player);
            }
        } else if (title.equals("Letzte Transaktionen")) {
            e.setCancelled(true);
            if (e.getRawSlot() == 31) {
                openBankMenu(player);
            }
        }
    }
}
