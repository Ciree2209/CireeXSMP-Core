package com.cirexx.csmp.ranks;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.stats.StatsManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankManager {

    private final CsmpCore plugin;
    private final StatsManager statsManager;
    private Economy economy;
    private Permission permissions;

    private final Map<String, RankNode> ranks = new HashMap<>();
    private final List<String> rankOrder = new ArrayList<>();

    public RankManager(CsmpCore plugin) {
        this.plugin = plugin;
        this.statsManager = plugin.getStatsManager();
        setupVault();
        defineRanks();
    }

    private void setupVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Rank system will fail.");
            return;
        }

        RegisteredServiceProvider<Economy> rspEco = plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rspEco != null)
            economy = rspEco.getProvider();

        RegisteredServiceProvider<Permission> rspPerm = plugin.getServer().getServicesManager()
                .getRegistration(Permission.class);
        if (rspPerm != null)
            permissions = rspPerm.getProvider();
    }

    private void defineRanks() {
        // Define sequence
        rankOrder.add("default"); // Wanderer
        rankOrder.add("settler");
        rankOrder.add("trader");
        rankOrder.add("merchant");
        rankOrder.add("tycoon");
        rankOrder.add("legend");

        // Define requirements
        // Wanderer -> Settler
        ranks.put("settler", new RankNode("settler", "Settler")
                .addReq(new Requirement() {
                    @Override
                    public boolean meets(Player p) {
                        // Playtime check requires PlaceholderAPI or Statistic.PLAY_ONE_MINUTE
                        // 10 hours = 720,000 ticks
                        return p.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) >= 720000;
                    }

                    @Override
                    public String getDescription() {
                        return "10 Hours Playtime";
                    }
                })
                .addReq(createMoneyReq(5000)));

        // Settler -> Trader
        ranks.put("trader", new RankNode("trader", "Trader")
                .addReq(createMoneyReq(25000))); // Playtime ignored for brevity of example

        // ... (Other ranks would go here)

        // Tycoon -> Legend (Requires Event Win)
        ranks.put("legend", new RankNode("legend", "Legend")
                .addReq(createMoneyReq(2000000))
                .addReq(new Requirement() {
                    @Override
                    public boolean meets(Player p) {
                        return statsManager.getEventWins(p.getUniqueId()) >= 1;
                    }

                    @Override
                    public String getDescription() {
                        return "1 Event Win";
                    }
                }));
    }

    private Requirement createMoneyReq(double amount) {
        return new Requirement() {
            @Override
            public boolean meets(Player p) {
                return economy != null && economy.has(p, amount);
            }

            @Override
            public String getDescription() {
                return "$" + amount;
            }
        };
    }

    public String getNextRank(Player player) {
        String primaryGroup = getPrimaryGroup(player);
        int index = rankOrder.indexOf(primaryGroup.toLowerCase());

        if (index == -1) {
            // Unknown group, assume default if they have none?
            // Or maybe they are Donator/Admin.
            // For now, assume if not found, start at default index 0
            index = 0;
        }

        if (index + 1 < rankOrder.size()) {
            return rankOrder.get(index + 1);
        }
        return null; // Max rank
    }

    public RankNode getRankNode(String rankName) {
        return ranks.get(rankName);
    }

    public String getPrimaryGroup(Player player) {
        if (permissions == null)
            return "default";
        return permissions.getPrimaryGroup(player);
    }

    public boolean promote(Player player) {
        String nextRank = getNextRank(player);
        if (nextRank == null)
            return false;

        RankNode node = ranks.get(nextRank);
        if (node == null)
            return false; // Should not happen if config aligns

        // Check requirements
        for (Requirement req : node.requirements) {
            if (!req.meets(player))
                return false;
        }

        // Take money
        for (Requirement req : node.requirements) {
            // Ugly hack to find money req, ideally Requirement has "take()" method
            if (req.getDescription().startsWith("$")) {
                double amt = Double.parseDouble(req.getDescription().substring(1));
                economy.withdrawPlayer(player, amt);
            }
        }

        // Execute promotion
        // Using Vault to add group (assuming inheritance structure)
        permissions.playerAddGroup(player, nextRank);
        // Remove old group? Depends on if LuckPerms inherits.
        // Usually promote = add new, remove old, or just add new + inheritance.
        // Let's assume simple add for now.

        return true;
    }

    public static class RankNode {
        String id;
        String displayName;
        List<Requirement> requirements = new ArrayList<>();

        public RankNode(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public RankNode addReq(Requirement req) {
            requirements.add(req);
            return this;
        }

        public List<Requirement> getRequirements() {
            return requirements;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
