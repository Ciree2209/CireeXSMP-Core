package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PerkCommand implements CommandExecutor {

    public PerkCommand(CsmpCore plugin) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return true;
        }

        if (command.getName().equalsIgnoreCase("hat")) {
            if (!player.hasPermission("csmp.hat")) {
                player.sendMessage(Component.text("You don't have permission (VIP+).", NamedTextColor.RED));
                return true;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage(Component.text("Hold an item to wear it.", NamedTextColor.RED));
                return true;
            }
            ItemStack head = player.getInventory().getHelmet();
            player.getInventory().setHelmet(hand);
            player.getInventory().setItemInMainHand(head);
            player.sendMessage(Component.text("Nice hat!", NamedTextColor.GREEN));
            return true;
        }

        if (command.getName().equalsIgnoreCase("workbench")) {
            if (!player.hasPermission("csmp.wb")) {
                player.sendMessage(Component.text("You don't have permission (VIP+).", NamedTextColor.RED));
                return true;
            }
            player.openWorkbench(null, true);
            return true;
        }

        if (command.getName().equalsIgnoreCase("ec")) {
            if (!player.hasPermission("csmp.ec")) {
                player.sendMessage(Component.text("You don't have permission (Merchant+).", NamedTextColor.RED));
                return true;
            }
            player.openInventory(player.getEnderChest());
            return true;
        }

        if (command.getName().equalsIgnoreCase("nick")) {
            if (!player.hasPermission("csmp.nick")) {
                player.sendMessage(Component.text("You don't have permission (Legend+).", NamedTextColor.RED));
                return true;
            }
            if (args.length == 0) {
                // Reset
                player.displayName(Component.text(player.getName()));
                player.playerListName(Component.text(player.getName()));
                player.sendMessage(Component.text("Nickname reset.", NamedTextColor.YELLOW));
                return true;
            }

            String nick = args[0];
            Component fancyName = LegacyComponentSerializer.legacyAmpersand().deserialize(nick);

            player.displayName(fancyName);
            player.playerListName(fancyName);

            player.sendMessage(Component.text("Nickname set to: ", NamedTextColor.GREEN).append(fancyName));
            return true;
        }

        return true;
    }
}
