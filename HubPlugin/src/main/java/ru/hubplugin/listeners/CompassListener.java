package ru.hubplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hubplugin.HubPlugin;
import ru.hubplugin.managers.BanManager;
import ru.hubplugin.managers.BanManager.BanEntry;
import ru.hubplugin.managers.TeleportManager;

public class CompassListener implements Listener {

    private final HubPlugin plugin;
    private final TeleportManager teleportManager;

    public CompassListener(HubPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = new TeleportManager(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.COMPASS) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!meta.getDisplayName().contains("Навигатор")) return;

        event.setCancelled(true);
        plugin.getNavigatorManager().openNavigator(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String menuTitle = plugin.colorize(plugin.getConfig().getString("compass.title", "&6Выбор мира"));
        if (!event.getView().getTitle().equals(menuTitle)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        int slot = event.getSlot();
        BanManager bm = plugin.getBanManager();

        switch (slot) {
            case 12: // Survival
                player.closeInventory();
                if (bm.isBanned(player.getUniqueId(), BanManager.BanType.SURVIVAL)) {
                    BanEntry ban = bm.getActiveBan(player.getUniqueId(), BanManager.BanType.SURVIVAL);
                    sendBanMessage(player, "выживание", ban);
                } else {
                    teleportManager.teleportToSurvival(player);
                }
                break;
            case 14: // Hardcore
                player.closeInventory();
                if (bm.isBanned(player.getUniqueId(), BanManager.BanType.HARDCORE)) {
                    BanEntry ban = bm.getActiveBan(player.getUniqueId(), BanManager.BanType.HARDCORE);
                    sendBanMessage(player, "хардкор", ban);
                } else {
                    teleportManager.teleportToHardcore(player);
                }
                break;
        }
    }

    private void sendBanMessage(Player player, String world, BanEntry ban) {
        if (ban == null) return;
        player.sendMessage(plugin.colorize("&c&lДоступ запрещён!"));
        player.sendMessage(plugin.colorize("&7Мир: &f" + world));
        player.sendMessage(plugin.colorize("&7Причина: &f" + ban.reason));
        player.sendMessage(plugin.colorize("&7Бан истекает: &f" + plugin.getBanManager().formatExpiry(ban.expiry)));
    }
}
