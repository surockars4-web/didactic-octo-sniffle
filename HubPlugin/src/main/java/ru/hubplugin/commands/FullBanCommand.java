package ru.hubplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hubplugin.HubPlugin;
import ru.hubplugin.managers.BanManager;
import ru.hubplugin.managers.TeleportManager;

public class FullBanCommand implements CommandExecutor {
    private final HubPlugin plugin;
    public FullBanCommand(HubPlugin plugin) { this.plugin = plugin; }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubplugin.admin")) {
            sender.sendMessage(plugin.colorize("&cНедостаточно прав!")); return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.colorize("&cИспользование: /ban <ник> [дни] [причина]")); return true;
        }
        String name = args[0];
        int days = args.length >= 2 ? parseIntSafe(args[1], 30) : 30;
        String reason = args.length >= 3 ? joinFrom(args, 2) : "Бан";

        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        plugin.getBanManager().banPlayer(target.getUniqueId(), BanManager.BanType.FULL, days, reason);
        sender.sendMessage(plugin.colorize("&aИгрок &f" + name + " &aполностью забанен на &f" + days + " &aдней. Причина: &f" + reason));

        // Если онлайн — телепортировать в хаб
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            String hubWorld = plugin.getConfig().getString("worlds.hub", "world_hub");
            if (!online.getWorld().getName().equals(hubWorld)) {
                new TeleportManager(plugin).teleportToHub(online);
            }
            online.sendMessage(plugin.colorize("&c&lВы полностью забанены!\n&7Причина: &f" + reason + "\n&7Бан истекает: &f" +
                plugin.getBanManager().formatExpiry(plugin.getBanManager().getActiveBan(online.getUniqueId(), BanManager.BanType.FULL).expiry)));
        }
        return true;
    }

    private int parseIntSafe(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private String joinFrom(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) { if (i > from) sb.append(" "); sb.append(args[i]); }
        return sb.toString();
    }
}
