package ru.hubplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.hubplugin.HubPlugin;

import java.util.Map;
import java.util.UUID;

public class AdminBanCommand implements CommandExecutor {

    private final HubPlugin plugin;

    public AdminBanCommand(HubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubplugin.admin")) {
            sender.sendMessage(plugin.colorize("&cНедостаточно прав!"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listBans(sender);
                break;
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(plugin.colorize("&cИспользование: /adminhubban remove <ник>"));
                    return true;
                }
                removeBan(sender, args[1]);
                break;
            case "add":
                if (args.length < 2) {
                    sender.sendMessage(plugin.colorize("&cИспользование: /adminhubban add <ник>"));
                    return true;
                }
                addBan(sender, args[1]);
                break;
            default:
                showHelp(sender);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&6=== HubPlugin Admin ==="));
        sender.sendMessage(plugin.colorize("&e/adminhubban list &7— список забаненных"));
        sender.sendMessage(plugin.colorize("&e/adminhubban add <ник> &7— выдать хардкор-бан"));
        sender.sendMessage(plugin.colorize("&e/adminhubban remove <ник> &7— снять хардкор-бан"));
    }

    private void listBans(CommandSender sender) {
        Map<UUID, Long> bans = plugin.getBanManager().getAllBans();
        if (bans.isEmpty()) {
            sender.sendMessage(plugin.colorize("&aНет активных хардкор-банов."));
            return;
        }
        sender.sendMessage(plugin.colorize("&6=== Активные хардкор-баны ==="));
        for (Map.Entry<UUID, Long> entry : bans.entrySet()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
            String name = op.getName() != null ? op.getName() : entry.getKey().toString();
            sender.sendMessage(plugin.colorize("&c" + name + " &7— до &f" +
                plugin.getBanManager().getFormattedExpiry(entry.getKey())));
        }
    }

    @SuppressWarnings("deprecation")
    private void removeBan(CommandSender sender, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!plugin.getBanManager().isBanned(target.getUniqueId())) {
            sender.sendMessage(plugin.colorize("&cИгрок &f" + playerName + " &cне имеет хардкор-бана."));
            return;
        }
        plugin.getBanManager().removeBan(target.getUniqueId());
        sender.sendMessage(plugin.colorize("&aХардкор-бан игрока &f" + playerName + " &aснят!"));
    }

    @SuppressWarnings("deprecation")
    private void addBan(CommandSender sender, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        plugin.getBanManager().banPlayer(target.getUniqueId());
        sender.sendMessage(plugin.colorize("&cХардкор-бан выдан игроку &f" + playerName + " &cна 7 дней!"));
    }
}
