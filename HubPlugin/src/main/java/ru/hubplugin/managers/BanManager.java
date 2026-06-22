package ru.hubplugin.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.hubplugin.HubPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BanManager {

    public enum BanType {
        HARDCORE,    // нельзя только в хардкор
        SURVIVAL,    // нельзя только в выживание
        FULL         // нельзя никуда
    }

    public static class BanEntry {
        public final BanType type;
        public final long expiry;
        public final String reason;

        public BanEntry(BanType type, long expiry, String reason) {
            this.type = type;
            this.expiry = expiry;
            this.reason = reason;
        }
    }

    private final HubPlugin plugin;
    private final File bansFile;
    private FileConfiguration bansConfig;
    private final Map<UUID, List<BanEntry>> banCache = new HashMap<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public BanManager(HubPlugin plugin) {
        this.plugin = plugin;
        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        loadBans();
    }

    private void loadBans() {
        if (!bansFile.exists()) {
            try { bansFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать bans.yml: " + e.getMessage());
            }
        }
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);

        if (bansConfig.contains("bans")) {
            for (String key : bansConfig.getConfigurationSection("bans").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<BanEntry> entries = new ArrayList<>();
                    if (bansConfig.contains("bans." + key)) {
                        for (String idx : bansConfig.getConfigurationSection("bans." + key).getKeys(false)) {
                            String path = "bans." + key + "." + idx;
                            long expiry = bansConfig.getLong(path + ".expiry");
                            if (expiry > System.currentTimeMillis()) {
                                BanType type = BanType.valueOf(bansConfig.getString(path + ".type", "FULL"));
                                String reason = bansConfig.getString(path + ".reason", "Без причины");
                                entries.add(new BanEntry(type, expiry, reason));
                            }
                        }
                    }
                    if (!entries.isEmpty()) banCache.put(uuid, entries);
                } catch (Exception ignored) {}
            }
        }
        plugin.getLogger().info("Загружено банов: " + banCache.size());
    }

    public void saveBans() {
        bansConfig.set("bans", null);
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, List<BanEntry>> entry : banCache.entrySet()) {
            int idx = 0;
            for (BanEntry ban : entry.getValue()) {
                if (ban.expiry > now) {
                    String path = "bans." + entry.getKey() + "." + idx;
                    bansConfig.set(path + ".expiry", ban.expiry);
                    bansConfig.set(path + ".type", ban.type.name());
                    bansConfig.set(path + ".reason", ban.reason);
                    idx++;
                }
            }
        }
        try { bansConfig.save(bansFile); } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить bans.yml: " + e.getMessage());
        }
    }

    public void banPlayer(UUID uuid, BanType type, int days, String reason) {
        long expiry = System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000;
        banCache.computeIfAbsent(uuid, k -> new ArrayList<>())
                .removeIf(b -> b.type == type); // убрать старый бан того же типа
        banCache.get(uuid).add(new BanEntry(type, expiry, reason));
        saveBans();
    }

    public boolean isBanned(UUID uuid, BanType type) {
        List<BanEntry> entries = banCache.get(uuid);
        if (entries == null) return false;
        long now = System.currentTimeMillis();
        entries.removeIf(b -> b.expiry <= now);
        if (entries.isEmpty()) { banCache.remove(uuid); return false; }

        for (BanEntry b : entries) {
            if (b.type == type || b.type == BanType.FULL) return true;
        }
        return false;
    }

    public boolean isFullyBanned(UUID uuid) {
        return isBanned(uuid, BanType.FULL);
    }

    public BanEntry getActiveBan(UUID uuid, BanType type) {
        List<BanEntry> entries = banCache.get(uuid);
        if (entries == null) return null;
        long now = System.currentTimeMillis();
        for (BanEntry b : entries) {
            if (b.expiry > now && (b.type == type || b.type == BanType.FULL)) return b;
        }
        return null;
    }

    public List<BanEntry> getAllActiveBans(UUID uuid) {
        List<BanEntry> entries = banCache.get(uuid);
        if (entries == null) return Collections.emptyList();
        long now = System.currentTimeMillis();
        List<BanEntry> active = new ArrayList<>();
        for (BanEntry b : entries) {
            if (b.expiry > now) active.add(b);
        }
        return active;
    }

    public void removeBan(UUID uuid, BanType type) {
        List<BanEntry> entries = banCache.get(uuid);
        if (entries != null) {
            entries.removeIf(b -> b.type == type);
            if (entries.isEmpty()) banCache.remove(uuid);
        }
        saveBans();
    }

    public void removeAllBans(UUID uuid) {
        banCache.remove(uuid);
        saveBans();
    }

    public String formatExpiry(long expiry) {
        return DATE_FORMAT.format(new Date(expiry));
    }

    public Map<UUID, List<BanEntry>> getAllBans() {
        long now = System.currentTimeMillis();
        banCache.values().forEach(list -> list.removeIf(b -> b.expiry <= now));
        banCache.entrySet().removeIf(e -> e.getValue().isEmpty());
        return new HashMap<>(banCache);
    }
}
