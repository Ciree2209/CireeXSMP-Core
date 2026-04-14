package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.warps.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportRequestCommand implements CommandExecutor {

    private final WarpManager warpManager;

    // Target -> Sender (Simple 1:1 request for now)
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();
    // Target -> Sender (Here requests)
    private final Map<UUID, UUID> tpahereRequests = new HashMap<>();

    public TeleportRequestCommand(CsmpCore plugin) {
        this.warpManager = plugin.getWarpManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (command.getName().equalsIgnoreCase("tpa")) {
            if (args.length < 1) {
                player.sendMessage(Component.text("Usage: /tpa <player>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return true;
            }

            tpaRequests.put(target.getUniqueId(), player.getUniqueId());
            target.sendMessage(Component.text(player.getName() + " wants to teleport to you. Type /tpaccept to accept.",
                    NamedTextColor.GOLD));
            player.sendMessage(Component.text("Request sent.", NamedTextColor.GREEN));
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpahere")) {
            if (args.length < 1) {
                player.sendMessage(Component.text("Usage: /tpahere <player>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return true;
            }

            // Check cost $100
            double cost = 100;
            if (warpManager.getEconomy().getBalance(player) < cost) {
                player.sendMessage(
                        Component.text("You need $" + cost + " to send a TPA Here request.", NamedTextColor.RED));
                return true;
            }

            warpManager.getEconomy().withdrawPlayer(player, cost);
            tpahereRequests.put(target.getUniqueId(), player.getUniqueId());
            target.sendMessage(
                    Component.text(player.getName() + " wants you to teleport to them. Type /tpaccept to accept.",
                            NamedTextColor.GOLD));
            player.sendMessage(Component.text("Request sent. Paid $" + cost + ".", NamedTextColor.GREEN));
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpaccept")) {
            if (tpaRequests.containsKey(player.getUniqueId())) {
                UUID senderId = tpaRequests.remove(player.getUniqueId());
                Player requester = Bukkit.getPlayer(senderId);
                if (requester != null) {
                    requester.teleport(player);
                    requester.sendMessage(Component.text("Teleported!", NamedTextColor.GREEN));
                    player.sendMessage(Component.text("Accepted.", NamedTextColor.GREEN));
                }
            } else if (tpahereRequests.containsKey(player.getUniqueId())) {
                UUID senderId = tpahereRequests.remove(player.getUniqueId());
                Player requester = Bukkit.getPlayer(senderId);
                if (requester != null) {
                    player.teleport(requester);
                    player.sendMessage(Component.text("Teleported!", NamedTextColor.GREEN));
                    requester.sendMessage(Component.text("Accepted.", NamedTextColor.GREEN));
                }
            } else {
                player.sendMessage(Component.text("No pending requests.", NamedTextColor.RED));
            }
            return true;
        }

        return true;
    }
}
