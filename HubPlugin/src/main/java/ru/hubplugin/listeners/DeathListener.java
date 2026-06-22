package ru.hubplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.hubplugin.HubPlugin;
import ru.hubplugin.managers.BanManager;
import ru.hubplugin.managers.TeleportManager;

public class DeathListener implements Listener {

    private final HubPlugin plugin;
    private final TeleportManager teleportManager;

    public DeathListener(HubPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = new TeleportManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String hardcoreWorld = plugin.getConfig().getString("worlds.hardcore", "world_hardcore");
        if (!player.getWorld().getName().equals(hardcoreWorld)) return;

        int days = plugin.getConfig().getInt("hardcore-ban.duration-days", 7);
        plugin.getBanManager().banPlayer(player.getUniqueId(), BanManager.BanType.HARDCORE, days, "Смерть в хардкор режиме");

        player.sendMessage(plugin.colorize(plugin.getConfig().getString(
            "hardcore-ban.message", "&cВы умерли в хардкоре! Бан выдан на 7 дней.")));

        new BukkitRunnable() {
            @Override
            public void run() {
                teleportManager.teleportToHub(player);
                player.sendMessage(plugin.colorize("&cВы перемещены в хаб. Доступ в хардкор заблокирован."));
                player.sendMessage(plugin.colorize("&7Бан истекает: &f" +
                    plugin.getBanManager().formatExpiry(
                        plugin.getBanManager().getActiveBan(player.getUniqueId(), BanManager.BanType.HARDCORE).expiry)));
            }
        }.runTaskLater(plugin, 20L);
    }
}
