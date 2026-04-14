package com.cirexx.csmp.stats;

import com.cirexx.csmp.CsmpCore;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StatsManager implements org.bukkit.event.Listener {

    private final CsmpCore plugin;
    private final Map<UUID, PlayerStats> statsCache = new ConcurrentHashMap<>();

    public StatsManager(CsmpCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initTable();
    }

    private void initTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS player_stats (" +
                                    "uuid VARCHAR(36) PRIMARY KEY, " +
                                    "event_wins INT NOT NULL DEFAULT 0)")) {
                stmt.execute();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create player_stats table", e);
            }
        });
    }

    @org.bukkit.event.EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("SELECT event_wins FROM player_stats WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    statsCache.put(uuid, new PlayerStats(rs.getInt("event_wins")));
                } else {
                    statsCache.put(uuid, new PlayerStats(0));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load stats for " + uuid, e);
            }
        });
    }

    @org.bukkit.event.EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Save then unload
        if (statsCache.containsKey(uuid)) {
            savePlayer(uuid);
            // Delay removal slightly or just remove?
            // Remove immediately is safe if savePlayer captures the object or value.
            // savePlayer uses cache.get(uuid). So we must remove AFTER scheduling the save?
            // No, savePlayer schedules an async task that reads from cache.
            // If we remove from cache immediately, the async task might run after removal
            // and find nothing.
            // Correction: pass the PlayerStats object directly to database task, NOT UUID.

            // Refactoring savePlayer to accept Stats object is safer, but for minimal
            // changes:
            // We'll trust the current savePlayer (which reads cache) and just NOT remove it
            // yet?
            // No, we want to free memory.

            // Better approach: Synchronous fetch of data, async write.
            PlayerStats stats = statsCache.remove(uuid); // Remove and get
            if (stats != null) {
                savePlayerStatsDirectly(uuid, stats);
            }
        }
    }

    public void save() {
        // Save all currently online players (for onDisable)
        // ... implementation below ...
        // Save all cache to DB (Async)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_stats (uuid, event_wins) VALUES (?, ?) ON DUPLICATE KEY UPDATE event_wins = ?")) {
                    for (Map.Entry<UUID, PlayerStats> entry : statsCache.entrySet()) {
                        stmt.setString(1, entry.getKey().toString());
                        stmt.setInt(2, entry.getValue().getEventWins());
                        stmt.setInt(3, entry.getValue().getEventWins());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save stats", e);
            }
        });
    }

    public void addEventWin(UUID uuid) {
        statsCache.computeIfAbsent(uuid, k -> new PlayerStats(0)).incrementWins();
        savePlayer(uuid);
    }

    private void savePlayer(UUID uuid) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats != null) {
            savePlayerStatsDirectly(uuid, stats);
        }
    }

    private void savePlayerStatsDirectly(UUID uuid, PlayerStats stats) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO player_stats (uuid, event_wins) VALUES (?, ?) ON DUPLICATE KEY UPDATE event_wins = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, stats.getEventWins());
                stmt.setInt(3, stats.getEventWins());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save stats for " + uuid, e);
            }
        });
    }

    public int getEventWins(UUID uuid) {
        return statsCache.getOrDefault(uuid, new PlayerStats(0)).getEventWins();
    }

    public static class PlayerStats {
        private int eventWins;

        public PlayerStats(int eventWins) {
            this.eventWins = eventWins;
        }

        public int getEventWins() {
            return eventWins;
        }

        public void setEventWins(int eventWins) {
            this.eventWins = eventWins;
        }

        public void incrementWins() {
            this.eventWins++;
        }
    }
}
