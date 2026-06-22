package ru.hubplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hubplugin.HubPlugin;
import ru.hubplugin.managers.BanManager;
import ru.hubplugin.managers.BanManager.BanEntry;
import ru.hubplugin.managers.TeleportManager;

import java.util.Arrays;
import java.util.List;

public class JoinListener implements Listener {

    private final HubPlugin plugin;
    private final TeleportManager teleportManager;

    public JoinListener(HubPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = new TeleportManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BanManager bm = plugin.getBanManager();
        String currentWorld = player.getWorld().getName();
        String hardcoreWorld = plugin.getConfig().getString("worlds.hardcore", "world_hardcore");
        String survivalWorld = plugin.getConfig().getString("worlds.survival", "world");

        boolean kicked = false;

        // Полный бан — телепорт в хаб откуда угодно кроме хаба
        if (bm.isBanned(player.getUniqueId(), BanManager.BanType.FULL)) {
            String hubWorld = plugin.getConfig().getString("worlds.hub", "world_hub");
            if (!currentWorld.equals(hubWorld)) {
                teleportManager.teleportToHub(player);
                kicked = true;
            }
            BanEntry ban = bm.getActiveBan(player.getUniqueId(), BanManager.BanType.FULL);
            if (ban != null) sendBanNotice(player, "все миры", ban);
            return;
        }

        // Бан в хардкоре — выкидывает в хаб если в хардкоре
        if (bm.isBanned(player.getUniqueId(), BanManager.BanType.HARDCORE) && currentWorld.equals(hardcoreWorld)) {
            teleportManager.teleportToHub(player);
            kicked = true;
            BanEntry ban = bm.getActiveBan(player.getUniqueId(), BanManager.BanType.HARDCORE);
            if (ban != null) sendBanNotice(player, "хардкор", ban);
        }

        // Бан в выживании — выкидывает в хаб если в выживании
        if (bm.isBanned(player.getUniqueId(), BanManager.BanType.SURVIVAL) && currentWorld.equals(survivalWorld)) {
            teleportManager.teleportToHub(player);
            BanEntry ban = bm.getActiveBan(player.getUniqueId(), BanManager.BanType.SURVIVAL);
            if (ban != null) sendBanNotice(player, "выживание", ban);
        }

        giveCompassIfNeeded(player);
    }

    private void sendBanNotice(Player player, String world, BanEntry ban) {
        player.sendMessage(plugin.colorize("&c&lВы забанены в: &f" + world));
        player.sendMessage(plugin.colorize("&7Причина: &f" + ban.reason));
        player.sendMessage(plugin.colorize("&7Бан истекает: &f" + plugin.getBanManager().formatExpiry(ban.expiry)));
    }

    private void giveCompassIfNeeded(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Навигатор")) return;
            }
        }
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.colorize("&6&l⚡ Навигатор миров"));
            meta.setLore(Arrays.asList(
                plugin.colorize("&7ПКМ — открыть меню выбора мира"),
                plugin.colorize(""),
                plugin.colorize("&eНажмите правой кнопкой мыши")
            ));
            compass.setItemMeta(meta);
        }
        player.getInventory().setItem(8, compass);
    }
}
