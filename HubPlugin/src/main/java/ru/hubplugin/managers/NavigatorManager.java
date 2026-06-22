package ru.hubplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hubplugin.HubPlugin;

import java.util.ArrayList;
import java.util.List;

public class NavigatorManager {

    private final HubPlugin plugin;

    public NavigatorManager(HubPlugin plugin) {
        this.plugin = plugin;
    }

    public void openNavigator(Player player) {
        String title = plugin.colorize(plugin.getConfig().getString("compass.title", "&6Выбор мира"));
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Fill with glass pane border
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Survival - slot 12 (left-center)
        inv.setItem(12, buildSurvivalItem());

        // Hardcore - slot 14 (right-center)
        inv.setItem(14, buildHardcoreItem(player));

        player.openInventory(inv);
    }

    private ItemStack buildSurvivalItem() {
        String name = plugin.colorize(plugin.getConfig().getString("compass.survival-name", "&aВыживание"));
        List<String> loreRaw = plugin.getConfig().getStringList("compass.survival-lore");
        List<String> lore = colorizeList(loreRaw);
        return createItem(Material.GRASS_BLOCK, name, lore);
    }

    private ItemStack buildHardcoreItem(Player player) {
        String name = plugin.colorize(plugin.getConfig().getString("compass.hardcore-name", "&cХардкор"));
        List<String> loreRaw = plugin.getConfig().getStringList("compass.hardcore-lore");
        List<String> lore = colorizeList(loreRaw);

        if (plugin.getBanManager().isBanned(player.getUniqueId())) {
            lore.add("");
            lore.add(plugin.colorize("&cВы забанены до: &f" +
                plugin.getBanManager().getFormattedExpiry(player.getUniqueId())));
        }

        return createItem(Material.SKELETON_SKULL, name, lore);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> colorizeList(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(plugin.colorize(s));
        }
        return result;
    }
}
