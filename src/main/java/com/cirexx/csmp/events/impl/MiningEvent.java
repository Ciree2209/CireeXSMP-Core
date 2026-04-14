package com.cirexx.csmp.events.impl;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.events.GameEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningEvent extends GameEvent {

    private final Map<UUID, Integer> scores = new HashMap<>();

    public MiningEvent(CsmpCore plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Mining Madness";
    }

    @Override
    public long getDuration() {
        return 600; // 10 minutes
    }

    @Override
    public void start() {
        scores.clear();
        Bukkit.broadcast(
                Component.text("Mine as many blocks as you can in the Resource World!", NamedTextColor.YELLOW));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);

        if (scores.isEmpty()) {
            Bukkit.broadcast(Component.text("No one participated in the mining event!", NamedTextColor.RED));
            return;
        }

        UUID winner = null;
        int maxScore = -1;

        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                winner = entry.getKey();
            }
        }

        if (winner != null) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(winner);
            Bukkit.broadcast(
                    Component.text("Winner: " + p.getName() + " with " + maxScore + " blocks!", NamedTextColor.GREEN));
            // Reward
            int reward = 10; // 10 tokens
            plugin.getTokenManager().addTokens(winner, reward);
            plugin.getStatsManager().addEventWin(winner);
            Bukkit.broadcast(
                    Component.text(p.getName() + " received " + reward + " Event Tokens!", NamedTextColor.GOLD));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!event.getPlayer().getWorld().getName()
                .equals(plugin.getConfig().getString("resource-world.name", "resource_world"))) {
            return;
        }

        // Simple filter: prevent spam breaking trivial blocks like grass/flowers?
        // For simplicity, we count "valid" blocks. Let's exclude AIR (obviously) and
        // maybe instant-break stuff if needed.
        // But for "Madness", everything counts!

        UUID id = event.getPlayer().getUniqueId();
        scores.put(id, scores.getOrDefault(id, 0) + 1);

        // Optional: show actionbar score
        event.getPlayer().sendActionBar(Component.text("Blocks Mined: " + scores.get(id), NamedTextColor.GOLD));
    }
}
