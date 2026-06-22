package ru.hubplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.hubplugin.HubPlugin;

public class TeleportManager {

    private final HubPlugin plugin;

    public TeleportManager(HubPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean teleportToHub(Player player) {
        String worldName = plugin.getConfig().getString("worlds.hub", "world_hub");
        return teleportToWorld(player, worldName,
            plugin.colorize(plugin.getConfig().getString("messages.teleported-hub", "&aВы перемещены в хаб!")));
    }

    public boolean teleportToSurvival(Player player) {
        String worldName = plugin.getConfig().getString("worlds.survival", "world");
        return teleportToWorld(player, worldName,
            plugin.colorize(plugin.getConfig().getString("messages.teleported-survival", "&aВы перемещены в выживание!")));
    }

    public boolean teleportToHardcore(Player player) {
        if (plugin.getBanManager().isBanned(player.getUniqueId())) {
            String msg = plugin.colorize(plugin.getConfig().getString("messages.banned-sent-to-hub",
                "&cВы забанены в хардкоре! Бан истекает: %date%"));
            msg = msg.replace("%date%", plugin.getBanManager().getFormattedExpiry(player.getUniqueId()));
            player.sendMessage(msg);
            return false;
        }
        String worldName = plugin.getConfig().getString("worlds.hardcore", "world_hardcore");
        return teleportToWorld(player, worldName,
            plugin.colorize(plugin.getConfig().getString("messages.teleported-hardcore", "&aВы перемещены в хардкор!")));
    }

    private boolean teleportToWorld(Player player, String worldName, String successMsg) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(plugin.colorize("&cМир &f" + worldName + " &cне найден! Обратитесь к администратору."));
            return false;
        }
        player.teleport(world.getSpawnLocation());
        player.sendMessage(successMsg);
        return true;
    }
}
