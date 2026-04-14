package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.ranks.RankManager;
import com.cirexx.csmp.ranks.Requirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankupCommand implements CommandExecutor {

    private final RankManager rankManager;

    public RankupCommand(CsmpCore plugin) {
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can rankup.", NamedTextColor.RED));
            return true;
        }

        String nextRankId = rankManager.getNextRank(player);
        if (nextRankId == null) {
            player.sendMessage(Component.text("You are at the maximum rank!", NamedTextColor.GOLD));
            return true;
        }

        RankManager.RankNode nextRank = rankManager.getRankNode(nextRankId);

        // Attempt promotion
        boolean success = rankManager.promote(player);
        if (success) {
            player.sendMessage(Component.text("--------------------------------", NamedTextColor.GREEN));
            player.sendMessage(Component.text("PROMOTED TO " + nextRank.getDisplayName().toUpperCase() + "!",
                    NamedTextColor.GOLD));
            player.sendMessage(Component.text("--------------------------------", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("You do not meet the requirements for " + nextRank.getDisplayName() + ":",
                    NamedTextColor.RED));
            for (Requirement req : nextRank.getRequirements()) {
                boolean met = req.meets(player);
                NamedTextColor color = met ? NamedTextColor.GREEN : NamedTextColor.RED;
                String icon = met ? "✔" : "✖";
                player.sendMessage(Component.text(icon + " " + req.getDescription(), color));
            }
        }

        return true;
    }
}
