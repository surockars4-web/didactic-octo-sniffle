package ru.hubplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hubplugin.HubPlugin;
import ru.hubplugin.managers.BanManager;
import ru.hubplugin.managers.BanManager.BanEntry;

import java.util.List;

public class BanInfoCommand implements CommandExecutor {
    private final HubPlugin plugin;
    public BanInfoCommand(HubPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Только для игроков!"); return true; }
        Player player = (Player) sender;
        List<BanEntry> bans = plugin.getBanManager().getAllActiveBans(player.getUniqueId());

        if (bans.isEmpty()) {
            player.sendMessage(plugin.colorize("&aУ вас нет активных банов."));
            return true;
        }
        player.sendMessage(plugin.colorize("&6=== Ваши активные баны ==="));
        for (BanEntry ban : bans) {
            String typeName = ban.type == BanManager.BanType.HARDCORE ? "Хардкор"
                : ban.type == BanManager.BanType.SURVIVAL ? "Выживание" : "Все миры";
            player.sendMessage(plugin.colorize("&c" + typeName + " &7| &fПричина: " + ban.reason +
                " &7| Истекает: &f" + plugin.getBanManager().formatExpiry(ban.expiry)));
        }
        return true;
    }
}
