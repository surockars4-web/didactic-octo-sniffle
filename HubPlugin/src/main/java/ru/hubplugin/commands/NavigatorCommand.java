package ru.hubplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hubplugin.HubPlugin;

public class NavigatorCommand implements CommandExecutor {

    private final HubPlugin plugin;

    public NavigatorCommand(HubPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
            return true;
        }
        plugin.getNavigatorManager().openNavigator((Player) sender);
        return true;
    }
}
