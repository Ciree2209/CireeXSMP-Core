package com.cirexx.csmp.warps;

import com.cirexx.csmp.CsmpCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class WarpManager implements Listener {

    private final CsmpCore plugin;
    private Economy economy;

    private final Map<UUID, Map<String, Location>> homeCache = new ConcurrentHashMap<>();
    private final Map<String, Location> warpCache = new ConcurrentHashMap<>();
    private final Map<String, UUID> warpOwners = new ConcurrentHashMap<>();

    public WarpManager(CsmpCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupEconomy();
        initTables();
        loadWarps(); // Load global warps
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            economy = rsp.getProvider();
    }

    private void initTables() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // Homes
                try (PreparedStatement stmt = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS player_homes (" +
                                "uuid VARCHAR(36), " +
                                "name VARCHAR(32), " +
                                "world VARCHAR(64), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, " +
                                "PRIMARY KEY (uuid, name))")) {
                    stmt.execute();
                }
                // PWarps
                try (PreparedStatement stmt = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS pwarps (" +
                                "name VARCHAR(32) PRIMARY KEY, " +
                                "owner VARCHAR(36), " +
                                "world VARCHAR(64), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT)")) {
                    stmt.execute();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to init warp tables", e);
            }
        });
    }

    private void loadWarps() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pwarps")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("name");
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    Location loc = new Location(
                            Bukkit.getWorld(rs.getString("world")),
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                    warpCache.put(name.toLowerCase(), loc);
                    warpOwners.put(name.toLowerCase(), owner);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load warps", e);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM player_homes WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                Map<String, Location> homes = new HashMap<>();
                while (rs.next()) {
                    String name = rs.getString("name");
                    Location loc = new Location(
                            Bukkit.getWorld(rs.getString("world")),
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                    homes.put(name.toLowerCase(), loc);
                }
                homeCache.put(uuid, homes);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load homes for " + uuid, e);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        homeCache.remove(event.getPlayer().getUniqueId());
    }

    /**
     * @return The number of homes set by the player.
     */
    public int getHomeCount(Player player) {
        return homeCache.getOrDefault(player.getUniqueId(), new HashMap<>()).size();
    }

    public double getHomeCost(Player player, int currentCount) {
        int nextHomeIndex = currentCount + 1;
        if (nextHomeIndex <= 2)
            return 0;
        if (nextHomeIndex == 3)
            return 5000;
        if (nextHomeIndex == 4)
            return 10000;
        if (nextHomeIndex == 5)
            return 25000;
        return 25000;
    }

    public void setHome(Player player, String name) {
        Location loc = player.getLocation();
        homeCache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(name.toLowerCase(), loc);

        // Async Save
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "REPLACE INTO player_homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, name.toLowerCase());
                stmt.setString(3, loc.getWorld().getName());
                stmt.setDouble(4, loc.getX());
                stmt.setDouble(5, loc.getY());
                stmt.setDouble(6, loc.getZ());
                stmt.setFloat(7, loc.getYaw());
                stmt.setFloat(8, loc.getPitch());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save home " + name, e);
            }
        });
    }

    public Location getHome(Player player, String name) {
        return homeCache.getOrDefault(player.getUniqueId(), new HashMap<>()).get(name.toLowerCase());
    }

    public void delHome(Player player, String name) {
        Map<String, Location> homes = homeCache.get(player.getUniqueId());
        if (homes != null)
            homes.remove(name.toLowerCase());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("DELETE FROM player_homes WHERE uuid = ? AND name = ?")) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, name.toLowerCase());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete home " + name, e);
            }
        });
    }

    public Map<String, Location> getHomes(Player player) {
        return new HashMap<>(homeCache.getOrDefault(player.getUniqueId(), new HashMap<>()));
    }

    /**
     * Creates a public warp for a price.
     * 
     * @param player The creator
     * @param name   The warp name
     * @return true if successful
     */
    public boolean createPlayerWarp(Player player, String name) {
        if (warpCache.containsKey(name.toLowerCase())) {
            player.sendMessage("§cWarp '" + name + "' already exists!");
            return false;
        }

        double cost = 50000;
        if (economy.getBalance(player) < cost) {
            player.sendMessage("§cYou need §e$" + cost + "§c to create a warp.");
            return false;
        }

        economy.withdrawPlayer(player, cost);
        Location loc = player.getLocation();
        warpCache.put(name.toLowerCase(), loc);
        warpOwners.put(name.toLowerCase(), player.getUniqueId());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO pwarps (name, owner, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, name.toLowerCase());
                stmt.setString(2, player.getUniqueId().toString());
                stmt.setString(3, loc.getWorld().getName());
                stmt.setDouble(4, loc.getX());
                stmt.setDouble(5, loc.getY());
                stmt.setDouble(6, loc.getZ());
                stmt.setFloat(7, loc.getYaw());
                stmt.setFloat(8, loc.getPitch());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save warp " + name, e);
            }
        });

        player.sendMessage("§aWarp '" + name + "' created for §e$" + cost);
        return true;
    }

    public Location getWarp(String name) {
        return warpCache.get(name.toLowerCase());
    }

    public void deleteWarp(String name) {
        warpCache.remove(name.toLowerCase());
        warpOwners.remove(name.toLowerCase());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM pwarps WHERE name = ?")) {
                stmt.setString(1, name.toLowerCase());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete warp " + name, e);
            }
        });
    }

    public Map<String, Location> getWarps() {
        return new HashMap<>(warpCache);
    }

    public Economy getEconomy() {
        return economy;
    }
}
