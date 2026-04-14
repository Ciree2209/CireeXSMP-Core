package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.auction.AuctionGUI;
import com.cirexx.csmp.auction.AuctionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuctionCommand implements CommandExecutor {

    private final AuctionManager auctionManager;
    private final AuctionGUI auctionGUI;

    public AuctionCommand(CsmpCore plugin) {
        this.auctionManager = plugin.getAuctionManager();
        this.auctionGUI = plugin.getAuctionGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use the auction house.", NamedTextColor.RED));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("sell")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /ah sell <price>", NamedTextColor.RED));
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid price.", NamedTextColor.RED));
                return true;
            }

            if (price <= 0) {
                player.sendMessage(Component.text("Price must be positive.", NamedTextColor.RED));
                return true;
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage(Component.text("You must hold an item to sell.", NamedTextColor.RED));
                return true;
            }

            if (!auctionManager.canList(player)) {
                player.sendMessage(Component.text("You have reached your listing limit!", NamedTextColor.RED));
                return true;
            }

            auctionManager.list(player, hand, price);
            player.getInventory().setItemInMainHand(null);
            return true;
        }

        auctionGUI.openGlobalView(player, 0);
        return true;
    }
}
