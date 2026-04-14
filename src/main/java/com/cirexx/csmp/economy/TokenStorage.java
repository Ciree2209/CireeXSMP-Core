package com.cirexx.csmp.economy;

import com.cirexx.csmp.CsmpCore;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class TokenStorage {

    private final CsmpCore plugin;

    public TokenStorage(CsmpCore plugin) {
        this.plugin = plugin;
        initTable();
    }

    private void initTable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS player_tokens (" +
                                    "uuid VARCHAR(36) PRIMARY KEY, " +
                                    "amount INT NOT NULL DEFAULT 0)")) {
                stmt.execute();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create player_tokens table", e);
            }
        });
    }

    /**
     * Loads all player token balances into memory.
     * This is designed to be backwards compatible with the previous caching system.
     * For 1000 players, the memory footprint is negligible.
     * 
     * @return Map of UUID to Token Amount
     */
    public Map<UUID, Integer> loadBalances() {
        Map<UUID, Integer> map = new java.util.HashMap<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT uuid, amount FROM player_tokens")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                map.put(UUID.fromString(rs.getString("uuid")), rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load tokens", e);
        }
        return map;
    }

    public void saveBalances(Map<UUID, Integer> balances) {
        // Use Async save
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // Use transaction
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO player_tokens (uuid, amount) VALUES (?, ?) ON DUPLICATE KEY UPDATE amount = ?")) {
                    for (Map.Entry<UUID, Integer> entry : balances.entrySet()) {
                        stmt.setString(1, entry.getKey().toString());
                        stmt.setInt(2, entry.getValue());
                        stmt.setInt(3, entry.getValue());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save tokens", e);
            }
        });
    }
}
