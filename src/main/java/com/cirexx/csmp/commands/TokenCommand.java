package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.economy.EventTokenManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TokenCommand implements CommandExecutor {

    private final EventTokenManager tokenManager;

    public TokenCommand(CsmpCore plugin) {
        this.tokenManager = plugin.getTokenManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            // Balance check (self)
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
                return true;
            }
            showBalance(sender, player);
            return true;
        }

        String sub = args[0].toLowerCase();

        // Admin commands
        if (sub.equals("add") || sub.equals("remove") || sub.equals("set")) {
            if (!sender.hasPermission("csmp.tokens.admin")) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(
                        Component.text("Usage: /tokens <add|remove|set> <player> <amount>", NamedTextColor.RED));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
                return true;
            }

            UUID uuid = target.getUniqueId();
            if (sub.equals("add")) {
                tokenManager.addTokens(uuid, amount);
                sender.sendMessage(
                        Component.text("Added " + amount + " tokens to " + target.getName(), NamedTextColor.GREEN));
            } else if (sub.equals("remove")) {
                if (tokenManager.removeTokens(uuid, amount)) {
                    sender.sendMessage(Component.text("Removed " + amount + " tokens from " + target.getName(),
                            NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Player does not have enough tokens.", NamedTextColor.RED));
                }
            } else if (sub.equals("set")) {
                tokenManager.setBalance(uuid, amount);
                sender.sendMessage(
                        Component.text("Set balance of " + target.getName() + " to " + amount, NamedTextColor.GREEN));
            }
            return true;
        } else if (sub.equals("balance") || sub.equals("bal")) {
            if (args.length > 1) {
                if (!sender.hasPermission("csmp.tokens.admin")) { // View others balance could be admin only or public?
                                                                  // Usually public.
                    // Making it public for now as checking leaderboards etc is common
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                showBalance(sender, target);
            } else {
                if (sender instanceof Player player) {
                    showBalance(sender, player);
                } else {
                    sender.sendMessage(Component.text("Usage: /tokens balance <player>", NamedTextColor.RED));
                }
            }
            return true;
        }

        return false;
    }

    private void showBalance(CommandSender viewer, OfflinePlayer target) {
        int bal = tokenManager.getBalance(target.getUniqueId());
        viewer.sendMessage(Component.textOfChildren(
                Component.text(target.getName() + "'s Token Balance: ", NamedTextColor.GRAY),
                Component.text(bal + " ET", NamedTextColor.GOLD)));
    }
}
