package de.lukas.economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bank")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Nur Spieler können das benutzen.");
                return true;
            }
            GUIHandler.openBankMenu(p);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("bankinfo")) {
            if (!sender.hasPermission("economy.admin")) {
                sender.sendMessage(ChatColor.RED + "Keine Rechte.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Nutze: /bankinfo <Spieler>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                return true;
            }

            UUID uuid = target.getUniqueId();
            var data = ConfigManager.get(uuid);

            if (!data.contains("piggy")) {
                sender.sendMessage(ChatColor.RED + "Der Spieler war noch nie auf dem Server.");
                return true;
            }

            double piggy = data.getDouble("piggy");
            double bank = data.getDouble("bank");

            sender.sendMessage(ChatColor.GOLD + "Bankinfo von " + target.getName() + ":");
            sender.sendMessage(ChatColor.GREEN + "Piggybank: " + piggy);
            sender.sendMessage(ChatColor.GREEN + "Bankkonto: " + bank);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("payinterest")) {
            if (!sender.hasPermission("economy.admin")) {
                sender.sendMessage(ChatColor.RED + "Keine Rechte.");
                return true;
            }
            InterestTask task = new InterestTask();
            task.run();
            sender.sendMessage(ChatColor.GREEN + "Zinsen wurden ausgezahlt.");
            return true;
        }

        return false;
    }
}
