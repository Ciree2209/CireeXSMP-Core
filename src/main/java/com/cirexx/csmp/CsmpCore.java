package com.cirexx.csmp;

import com.cirexx.csmp.economy.EventTokenManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CsmpCore extends JavaPlugin {

    private EventTokenManager tokenManager;
    private com.cirexx.csmp.world.ResourceWorldManager resourceWorldManager;
    private com.cirexx.csmp.pvp.PvPManager pvpManager;
    private com.cirexx.csmp.events.EventManager eventManager;
    private com.cirexx.csmp.stats.StatsManager statsManager;
    private com.cirexx.csmp.ranks.RankManager rankManager;
    private com.cirexx.csmp.shop.ShopManager shopManager;
    private com.cirexx.csmp.shop.ShopGUI shopGUI;
    private com.cirexx.csmp.auction.AuctionManager auctionManager;
    private com.cirexx.csmp.auction.AuctionGUI auctionGUI;
    private com.cirexx.csmp.warps.WarpManager warpManager;
    private com.cirexx.csmp.cosmetics.CosmeticsManager cosmeticsManager;
    private com.cirexx.csmp.database.DatabaseManager databaseManager;

    private YamlConfiguration messagesConfig;

    @Override
    public void onEnable() {
        getLogger().info("Enabling CsmpCore v" + getPluginMeta().getVersion());
        saveDefaultConfig();

        saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));

        this.databaseManager = new com.cirexx.csmp.database.DatabaseManager(this);

        this.tokenManager = new EventTokenManager(this);
        this.resourceWorldManager = new com.cirexx.csmp.world.ResourceWorldManager(this);
        this.pvpManager = new com.cirexx.csmp.pvp.PvPManager(this);
        this.statsManager = new com.cirexx.csmp.stats.StatsManager(this);
        this.rankManager = new com.cirexx.csmp.ranks.RankManager(this);

        new com.cirexx.csmp.chat.ChatLogic(this);
        new com.cirexx.csmp.chat.TabManager(this);
        new com.cirexx.csmp.scoreboard.ScoreboardManager(this);

        this.shopManager = new com.cirexx.csmp.shop.ShopManager(this);
        this.shopGUI = new com.cirexx.csmp.shop.ShopGUI(this);
        getCommand("shop").setExecutor(new com.cirexx.csmp.commands.ShopCommand(this));

        this.auctionManager = new com.cirexx.csmp.auction.AuctionManager(this);
        this.auctionGUI = new com.cirexx.csmp.auction.AuctionGUI(this);
        getCommand("ah").setExecutor(new com.cirexx.csmp.commands.AuctionCommand(this));

        this.warpManager = new com.cirexx.csmp.warps.WarpManager(this);
        getCommand("sethome").setExecutor(new com.cirexx.csmp.commands.HomeCommand(this));
        getCommand("home").setExecutor(new com.cirexx.csmp.commands.HomeCommand(this));
        getCommand("delhome").setExecutor(new com.cirexx.csmp.commands.HomeCommand(this));
        getCommand("homes").setExecutor(new com.cirexx.csmp.commands.HomeCommand(this));
        getCommand("pwarp").setExecutor(new com.cirexx.csmp.commands.PlayerWarpCommand(this));

        com.cirexx.csmp.commands.TeleportRequestCommand tpaCmd = new com.cirexx.csmp.commands.TeleportRequestCommand(
                this);
        getCommand("tpa").setExecutor(tpaCmd);
        getCommand("tpahere").setExecutor(tpaCmd);
        getCommand("tpaccept").setExecutor(tpaCmd);

        this.cosmeticsManager = new com.cirexx.csmp.cosmetics.CosmeticsManager(this);
        getCommand("cosmetics").setExecutor(new com.cirexx.csmp.commands.CosmeticsCommand(this));

        com.cirexx.csmp.commands.PerkCommand perkCmd = new com.cirexx.csmp.commands.PerkCommand(this);
        getCommand("hat").setExecutor(perkCmd);
        getCommand("workbench").setExecutor(perkCmd);
        getCommand("ec").setExecutor(perkCmd);
        getCommand("nick").setExecutor(perkCmd);

        this.eventManager = new com.cirexx.csmp.events.EventManager(this);

        getCommand("tokens").setExecutor(new com.cirexx.csmp.commands.TokenCommand(this));
        getCommand("rw").setExecutor(new com.cirexx.csmp.commands.ResourceWorldCommand(this));
        getCommand("pvp").setExecutor(new com.cirexx.csmp.commands.PvPCommand(this));
        getCommand("rankup").setExecutor(new com.cirexx.csmp.commands.RankupCommand(this));
    }

    @Override
    public void onDisable() {
        if (eventManager != null) {
            eventManager.disable();
        }

        if (tokenManager != null) {
            tokenManager.save();
        }
        if (statsManager != null) {
            statsManager.save();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("Disabling CsmpCore v" + getPluginMeta().getVersion());
    }

    public com.cirexx.csmp.database.DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EventTokenManager getTokenManager() {
        return tokenManager;
    }

    public com.cirexx.csmp.world.ResourceWorldManager getResourceWorldManager() {
        return resourceWorldManager;
    }

    public com.cirexx.csmp.pvp.PvPManager getPvPManager() {
        return pvpManager;
    }

    public com.cirexx.csmp.stats.StatsManager getStatsManager() {
        return statsManager;
    }

    public com.cirexx.csmp.ranks.RankManager getRankManager() {
        return rankManager;
    }

    public com.cirexx.csmp.events.EventManager getEventManager() {
        return eventManager;
    }

    public com.cirexx.csmp.shop.ShopManager getShopManager() {
        return shopManager;
    }

    public com.cirexx.csmp.shop.ShopGUI getShopGUI() {
        return shopGUI;
    }

    public com.cirexx.csmp.auction.AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public com.cirexx.csmp.auction.AuctionGUI getAuctionGUI() {
        return auctionGUI;
    }

    public Component getMessage(String key) {
        String msg = messagesConfig.getString(key, "&cMissing message: " + key);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public Component getMessage(String key, String... replacements) {
        String msg = messagesConfig.getString(key, "&cMissing message: " + key);
        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = replacements[i];
            String value = (i + 1 < replacements.length) ? replacements[i + 1] : "";
            msg = msg.replace(placeholder, value);
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public com.cirexx.csmp.warps.WarpManager getWarpManager() {
        return warpManager;
    }

    public com.cirexx.csmp.cosmetics.CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }
}
