package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.pvp.PvPManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PvPCommand implements CommandExecutor {

    private final PvPManager pvpManager;

    public PvPCommand(CsmpCore plugin) {
        this.pvpManager = plugin.getPvPManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            if (arg.equals("on") || arg.equals("enable")) {
                if (!pvpManager.isPvPEnabled(player)) {
                    pvpManager.togglePvP(player);
                } else {
                    player.sendMessage(Component.text("PvP is already enabled!", NamedTextColor.RED));
                }
            } else if (arg.equals("off") || arg.equals("disable")) {
                if (pvpManager.isPvPEnabled(player)) {
                    pvpManager.togglePvP(player);
                } else {
                    player.sendMessage(Component.text("PvP is already disabled!", NamedTextColor.RED));
                }
            } else {
                player.sendMessage(Component.text("Usage: /pvp [on|off]", NamedTextColor.RED));
            }
        } else {
            pvpManager.togglePvP(player);
        }

        return true;
    }
}
