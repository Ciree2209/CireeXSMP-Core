package com.cirexx.csmp.pvp;

import com.cirexx.csmp.CsmpCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PvPManager implements Listener {

    private final CsmpCore plugin;
    private final Set<UUID> pvpEnabled = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    // Internal tracker for "combat tag" fallback if no plugin specific hook
    // Ideally we'd use CombatLogX API, but for this core we'll use a simple
    // "last damage dealt/taken" timestamp to prevent toggling mid-fight.
    private final Map<UUID, Long> lastCombatInteraction = new HashMap<>();

    private static final long COMBAT_TAG_DURATION_MS = 15000;
    private static final String COOLDOWN_KEY = "pvp.cooldown-seconds";

    public PvPManager(CsmpCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean isPvPEnabled(Player player) {
        return pvpEnabled.contains(player.getUniqueId());
    }

    public void setPvPEnabled(Player player, boolean enabled) {
        if (enabled) {
            pvpEnabled.add(player.getUniqueId());
            player.sendMessage(Component.text("PvP enabled!", NamedTextColor.GREEN));
        } else {
            pvpEnabled.remove(player.getUniqueId());
            player.sendMessage(Component.text("PvP disabled!", NamedTextColor.RED));
        }
    }

    public void togglePvP(Player player) {
        UUID uuid = player.getUniqueId();
        boolean currentlyEnabled = isPvPEnabled(player);

        if (isInCombat(player)) {
            player.sendMessage(Component.text("You are in combat! Cannot toggle PvP.", NamedTextColor.RED));
            return;
        }

        if (currentlyEnabled) {
            // Trying to disable. Check cooldown.
            long cooldownSeconds = plugin.getConfig().getLong(COOLDOWN_KEY, 300);
            if (cooldowns.containsKey(uuid)) {
                long secondsLeft = ((cooldowns.get(uuid) / 1000) + cooldownSeconds)
                        - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    player.sendMessage(
                            Component.text("Wait " + secondsLeft + "s before disabling PvP.", NamedTextColor.RED));
                    return;
                }
            }
            setPvPEnabled(player, false);
        } else {
            // Enabling is free, but starts the cooldown for disabling
            setPvPEnabled(player, true);
            cooldowns.put(uuid, System.currentTimeMillis());
        }
    }

    private boolean isInCombat(Player player) {
        Long lastInteraction = lastCombatInteraction.get(player.getUniqueId());
        return lastInteraction != null && (System.currentTimeMillis() - lastInteraction) < COMBAT_TAG_DURATION_MS;
    }

    @EventHandler
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        // Check if either has PvP disabled
        if (!isPvPEnabled(victim)) {
            attacker.sendMessage(Component.text("That player has PvP disabled!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        if (!isPvPEnabled(attacker)) {
            attacker.sendMessage(Component.text("You have PvP disabled! Run /pvp to enable it.", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Valid PvP, update combat timers
        long now = System.currentTimeMillis();
        lastCombatInteraction.put(victim.getUniqueId(), now);
        lastCombatInteraction.put(attacker.getUniqueId(), now);
    }
}
