package com.cirexx.csmp.scoreboard;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.events.GameEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager implements Listener {

    private final CsmpCore plugin;
    private Economy economy;
    private Permission permissions;

    public ScoreboardManager(CsmpCore plugin) {
        this.plugin = plugin;
        setupVault();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startUpdater();
    }

    private void setupVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return;

        RegisteredServiceProvider<Economy> rspEco = plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rspEco != null)
            economy = rspEco.getProvider();

        RegisteredServiceProvider<Permission> rspPerm = plugin.getServer().getServicesManager()
                .getRegistration(Permission.class);
        if (rspPerm != null)
            permissions = rspPerm.getProvider();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        createNewScoreboard(event.getPlayer());
    }

    private void startUpdater() {
        // Update every second (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateScoreboard(p);
            }
        }, 20L, 20L);
    }

    private void createNewScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        // Register objective
        Objective obj = board.registerNewObjective("sidebar", Criteria.DUMMY,
                LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(plugin.getConfig().getString("scoreboard.title", "&6&lCiRexxSMP")));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Register Teams for lines (Simple anti-flicker method: Update prefix/suffix of
        // named teams/entries)
        // For simplicity in this core implementation without external libs like
        // FastBoard,
        // we will just clear and set scores for the dynamic lines if we want simple
        // code,
        // OR use the Team approach.

        // Let's use the simple "Redraw" approach first.
        // Modern 1.21 clients handle scoreboard updates much better than 1.8.
        // If flickering occurs, we can switch to Teams later.

        player.setScoreboard(board);
        updateScoreboard(player);
    }

    private void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("sidebar");
        if (obj == null)
            return;

        // Clean slate usually causes flicker.
        // Better approach: Calculate lines, then set them.

        // Let's use a dynamic clearing method or just overwriting specific scores.
        // Actually, for a robust core, let's just do the "Lines List" approach and
        // reset.
        // Note: This WILL flicker on some clients.
        // Since I cannot import a library like FastBoard easily without shading,
        // I will implement a basic redraw.

        // Dynamic Values
        String rank = "None";
        if (permissions != null)
            rank = permissions.getPrimaryGroup(player);
        rank = capitalize(rank);

        double bal = 0;
        if (economy != null)
            bal = economy.getBalance(player);

        int tokens = plugin.getTokenManager().getBalance(player);

        GameEvent event = plugin.getEventManager().getCurrentEvent();
        String eventName = (event != null) ? event.getName() : "None";

        List<String> lines = new ArrayList<>();
        lines.add("&7&m------------------");
        lines.add("&fRank: &e" + rank);
        lines.add("&fMoney: &a$" + String.format("%.0f", bal));
        lines.add("&fTokens: &b⛀" + tokens);
        lines.add("");
        lines.add("&fEvent: &d" + eventName);
        if (event != null) {
            // Could add time left if accessible
        }
        lines.add("");
        lines.add(plugin.getConfig().getString("scoreboard.footer", "&eplay.cirexx.com"));
        lines.add("&7&m------------------");

        // Clear old entries
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // Set new lines (Reverse order because score 1 is bottom)
        int score = lines.size();
        for (String line : lines) {
            // content variable was unused, we use the String directly for compatibility
            // We need to use String strings for entries to work with legacy colors properly
            // often
            // But getScore(String) deprecated.
            // We'll use the deprecated string method for compatibility/ease or adventure if
            // supported.
            // Paper 1.21 -> getScore(String) is visible but maybe not best.
            // Let's use a unique string builder approach to avoid duplicate line collision.

            // Hack for duplicates: Append unique color codes to make lines distinct
            // invisible
            String uniqueLine = line + getUniqueColorCode(score);
            ScoreboardHelper_setLine(obj, uniqueLine, score);
            score--;
        }
    }

    private void ScoreboardHelper_setLine(Objective obj, String text, int score) {
        // Using legacy serializer string for the entry name
        String entry = LegacyComponentSerializer.legacyAmpersand().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(text));
        obj.getScore(entry).setScore(score);
    }

    private String getUniqueColorCode(int index) {
        return "§r".repeat(Math.max(0, index));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
