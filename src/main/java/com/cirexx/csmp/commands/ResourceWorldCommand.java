package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.world.ResourceWorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResourceWorldCommand implements CommandExecutor {

    private final ResourceWorldManager rwManager;

    public ResourceWorldCommand(CsmpCore plugin) {
        this.rwManager = plugin.getResourceWorldManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("back")) {
            rwManager.teleportBack(player);
        } else {
            rwManager.teleportToResourceWorld(player);
        }

        return true;
    }
}
