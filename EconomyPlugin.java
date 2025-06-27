package de.lukas.economy;

import org.bukkit.plugin.java.JavaPlugin;

public class EconomyPlugin extends JavaPlugin {

    private static EconomyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.loadAllPlayerData();

        getCommand("bank").setExecutor(new CommandHandler());
        getCommand("bankinfo").setExecutor(new CommandHandler());
        getCommand("payinterest").setExecutor(new CommandHandler());

        getServer().getPluginManager().registerEvents(new GUIHandler(), this);
        getServer().getPluginManager().registerEvents(new SignInputHandler(), this);
        getServer().getPluginManager().registerEvents(new JoinQuitHandler(), this);

        InterestTask.start();
    }

    public static EconomyPlugin getInstance() {
        return instance;
    }
}
