package com.cirexx.csmp.database;

import com.cirexx.csmp.CsmpCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final CsmpCore plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(CsmpCore plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        // Load config
        File dbFile = new File(plugin.getDataFolder(), "database.yml");
        if (!dbFile.exists()) {
            plugin.saveResource("database.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(dbFile);

        String host = config.getString("host", "127.0.0.1");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "csmp_core");
        String username = config.getString("username", "root");
        String password = config.getString("password", "password");
        boolean useSSL = config.getBoolean("use-ssl", false);
        int poolSize = config.getInt("pool-size", 10);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(
                "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&characterEncoding=utf8");
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setPoolName("CsmpCore-Pool");

        // Performance properties
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            plugin.getLogger().info("Successfully connected to MySQL database!");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL! Features requiring database will fail.",
                    e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database source is null (Connection failed?)");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
