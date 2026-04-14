package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.warps.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HomeCommand implements CommandExecutor {

    private final WarpManager warpManager;

    public HomeCommand(CsmpCore plugin) {
        this.warpManager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (command.getName().equalsIgnoreCase("sethome")) {
            String name = (args.length > 0) ? args[0] : "home";

            // Check if exist (Update is free?)
            // Design Doc doesn't say update fee. Let's assume Update is Free, only NEW SLOT
            // costs.
            // Wait, "First 2 free, then $5k". If I have 2 homes, setting "home3" costs $5k.
            // If I overwrite "home1", it should be free.

            boolean exists = warpManager.getHome(player, name) != null;
            if (!exists) {
                // New Home Logic
                int count = warpManager.getHomeCount(player);
                double cost = warpManager.getHomeCost(player, count);

                if (cost > 0) {
                    if (warpManager.getEconomy().getBalance(player) < cost) {
                        player.sendMessage(Component.text(
                                "You need $" + cost + " to set your " + (count + 1) + "th home.", NamedTextColor.RED));
                        return true;
                    }
                    warpManager.getEconomy().withdrawPlayer(player, cost);
                    player.sendMessage(Component.text("Paid $" + cost + " for new home slot.", NamedTextColor.YELLOW));
                }
            }

            warpManager.setHome(player, name);
            player.sendMessage(Component.text("Home '" + name + "' set!", NamedTextColor.GREEN));
            return true;
        }

        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 0) {
                // Determine default home or list?
                Location def = warpManager.getHome(player, "home");
                if (def != null) {
                    player.teleport(def);
                    player.sendMessage(Component.text("Teleported to 'home'.", NamedTextColor.GREEN));
                } else {
                    // If no 'home' but has others, maybe list?
                    int count = warpManager.getHomeCount(player);
                    if (count > 0) {
                        player.sendMessage(
                                Component.text("Home 'home' not found. Try /homes to list.", NamedTextColor.RED));
                    } else {
                        player.sendMessage(Component.text("No homes set. Use /sethome.", NamedTextColor.RED));
                    }
                }
                return true;
            }

            String name = args[0];
            Location loc = warpManager.getHome(player, name);
            if (loc == null) {
                player.sendMessage(Component.text("Home '" + name + "' not found.", NamedTextColor.RED));
                return true;
            }
            player.teleport(loc);
            player.sendMessage(Component.text("Teleported to '" + name + "'.", NamedTextColor.GREEN));
            return true;
        }

        if (command.getName().equalsIgnoreCase("delhome")) {
            String name = (args.length > 0) ? args[0] : "home";
            if (warpManager.getHome(player, name) == null) {
                player.sendMessage(Component.text("Home '" + name + "' not found.", NamedTextColor.RED));
                return true;
            }
            warpManager.delHome(player, name);
            player.sendMessage(Component.text("Home '" + name + "' deleted.", NamedTextColor.GREEN));
            return true;
        }

        if (command.getName().equalsIgnoreCase("homes")) {
            Map<String, Location> homes = warpManager.getHomes(player);
            if (homes.isEmpty()) {
                player.sendMessage(Component.text("You have no homes set.", NamedTextColor.YELLOW));
                return true;
            }

            player.sendMessage(Component.text("--- Your Homes ---", NamedTextColor.GOLD));
            for (String h : homes.keySet()) {
                player.sendMessage(Component.text("- " + h, NamedTextColor.YELLOW));
            }
            return true;
        }

        return true;
    }
}
