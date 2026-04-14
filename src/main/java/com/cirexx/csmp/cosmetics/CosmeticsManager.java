package com.cirexx.csmp.cosmetics;

import com.cirexx.csmp.CsmpCore;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticsManager implements Listener {

    private final CsmpCore plugin;
    private final Map<UUID, Particle> activeTrails = new HashMap<>();

    public CosmeticsManager(CsmpCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startTasks();
    }

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Particle> entry : activeTrails.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline() && !player.isDead()) {
                    player.getWorld().spawnParticle(entry.getValue(), player.getLocation().add(0, 0.5, 0), 1, 0.2, 0.2,
                            0.2, 0);
                }
            }
        }, 0L, 5L);
    }

    public void setTrail(Player player, Particle particle) {
        if (particle == null) {
            activeTrails.remove(player.getUniqueId());
        } else {
            activeTrails.put(player.getUniqueId(), particle);
        }
    }

    public Particle getTrail(Player player) {
        return activeTrails.get(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        activeTrails.remove(event.getPlayer().getUniqueId());
    }
}
