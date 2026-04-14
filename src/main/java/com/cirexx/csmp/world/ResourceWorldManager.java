package com.cirexx.csmp.world;

import com.cirexx.csmp.CsmpCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResourceWorldManager {

    private final CsmpCore plugin;
    private final Map<UUID, Location> lastMainWorldLocation = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    // Config keys
    private static final String RW_NAME_KEY = "resource-world.name";
    private static final String COOLDOWN_KEY = "resource-world.back-cooldown-seconds";

    public ResourceWorldManager(CsmpCore plugin) {
        this.plugin = plugin;
    }

    public void teleportToResourceWorld(Player player) {
        String worldName = plugin.getConfig().getString(RW_NAME_KEY, "resource_world");
        World rw = Bukkit.getWorld(worldName);

        if (rw == null) {
            player.sendMessage(Component.text("Resource world is not loaded or does not exist!", NamedTextColor.RED));
            return;
        }

        // Save location if they are NOT already in the resource world
        if (!player.getWorld().getName().equals(worldName)) {
            lastMainWorldLocation.put(player.getUniqueId(), player.getLocation());
        }

        player.teleport(rw.getSpawnLocation());
        player.sendMessage(Component.text("Teleported to Resource World!", NamedTextColor.GREEN));
    }

    public void teleportBack(Player player) {
        UUID uuid = player.getUniqueId();

        // Check cooldown
        long cooldownSeconds = plugin.getConfig().getLong(COOLDOWN_KEY, 300);
        if (cooldowns.containsKey(uuid)) {
            long secondsLeft = ((cooldowns.get(uuid) / 1000) + cooldownSeconds) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                player.sendMessage(Component.text("You must wait " + secondsLeft + "s before using /rw back again.",
                        NamedTextColor.RED));
                return;
            }
        }

        Location target = lastMainWorldLocation.get(uuid);
        if (target == null) {
            // Fallback to main world spawn if valid
            World main = Bukkit.getWorlds().get(0); // Usually the first world is main
            if (main != null) {
                target = main.getSpawnLocation();
            } else {
                player.sendMessage(Component.text("No return location found!", NamedTextColor.RED));
                return;
            }
        }

        player.teleport(target);
        player.sendMessage(Component.text("Returned to Main World!", NamedTextColor.GREEN));

        // Set cooldown
        cooldowns.put(uuid, System.currentTimeMillis());
        // Clear cached location to prevent repeated abuse of "back" if checking
        // distinct logic,
        // though for now we keep it simple or remove it.
        // Design doc says "/rw back - Return to main world".
        // We'll keep the location cached in case they want to go back again later after
        // exploring,
        // but typically "back" implies "where I came from".
        // Removing it ensures they have to travel there again to "set" a new back
        // point.
        lastMainWorldLocation.remove(uuid);
    }
}
