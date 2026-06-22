package ru.hubplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.hubplugin.HubPlugin;
import ru.hubplugin.managers.BanManager;

public class UnbanCommand implements CommandExecutor {
    private final HubPlugin plugin;
    public UnbanCommand(HubPlugin plugin) { this.plugin = plugin; }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubplugin.admin")) {
            sender.sendMessage(plugin.colorize("&cНедостаточно прав!")); return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.colorize("&cИспользование: /unban <ник> [hard|surviv|all]")); return true;
        }
        String name = args[0];
        String type = args.length >= 2 ? args[1].toLowerCase() : "all";
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        BanManager bm = plugin.getBanManager();

        switch (type) {
            case "hard":
                bm.removeBan(target.getUniqueId(), BanManager.BanType.HARDCORE);
                sender.sendMessage(plugin.colorize("&aСнят хардкор-бан с &f" + name));
                break;
            case "surviv":
                bm.removeBan(target.getUniqueId(), BanManager.BanType.SURVIVAL);
                sender.sendMessage(plugin.colorize("&aСнят бан выживания с &f" + name));
                break;
            default:
                bm.removeAllBans(target.getUniqueId());
                sender.sendMessage(plugin.colorize("&aСняты все баны с &f" + name));
        }
        return true;
    }
}
