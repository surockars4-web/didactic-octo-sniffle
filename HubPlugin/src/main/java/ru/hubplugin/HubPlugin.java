package ru.hubplugin;

import org.bukkit.plugin.java.JavaPlugin;
import ru.hubplugin.commands.*;
import ru.hubplugin.listeners.CompassListener;
import ru.hubplugin.listeners.DeathListener;
import ru.hubplugin.listeners.JoinListener;
import ru.hubplugin.managers.BanManager;
import ru.hubplugin.managers.NavigatorManager;

public class HubPlugin extends JavaPlugin {

    private static HubPlugin instance;
    private BanManager banManager;
    private NavigatorManager navigatorManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        banManager = new BanManager(this);
        navigatorManager = new NavigatorManager(this);

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);

        getCommand("navigator").setExecutor(new NavigatorCommand(this));
        getCommand("ban").setExecutor(new FullBanCommand(this));
        getCommand("hardban").setExecutor(new HardBanCommand(this));
        getCommand("survivban").setExecutor(new SurvivBanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("hubban").setExecutor(new BanInfoCommand(this));

        getLogger().info("HubPlugin запущен!");
    }

    @Override
    public void onDisable() {
        if (banManager != null) banManager.saveBans();
        getLogger().info("HubPlugin остановлен.");
    }

    public static HubPlugin getInstance() { return instance; }
    public BanManager getBanManager() { return banManager; }
    public NavigatorManager getNavigatorManager() { return navigatorManager; }

    public String colorize(String text) {
        return text.replace("&", "§");
    }
}
