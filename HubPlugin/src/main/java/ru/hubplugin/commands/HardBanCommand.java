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

public class HardBanCommand implements CommandExecutor {
    private final HubPlugin plugin;
    public HardBanCommand(HubPlugin plugin) { this.plugin = plugin; }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hubplugin.admin")) {
            sender.sendMessage(plugin.colorize("&cНедостаточно прав!")); return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.colorize("&cИспользование: /hardban <ник> [дни] [причина]")); return true;
        }
        String name = args[0];
        int days = args.length >= 2 ? parseIntSafe(args[1], 7) : 7;
        String reason = args.length >= 3 ? joinFrom(args, 2) : "Бан в хардкоре";

        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        plugin.getBanManager().banPlayer(target.getUniqueId(), BanManager.BanType.HARDCORE, days, reason);
        sender.sendMessage(plugin.colorize("&aИгрок &f" + name + " &aзабанен в хардкоре на &f" + days + " &aдней. Причина: &f" + reason));

        // Если онлайн и в хардкоре — телепортировать в хаб
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            String hardcoreWorld = plugin.getConfig().getString("worlds.hardcore", "world_hardcore");
            if (online.getWorld().getName().equals(hardcoreWorld)) {
                new TeleportManager(plugin).teleportToHub(online);
                online.sendMessage(plugin.colorize("&cВас забанили в хардкоре!\n&7Причина: &f" + reason));
            }
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
