package com.cirexx.csmp.commands;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.cosmetics.CosmeticsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CosmeticsCommand implements CommandExecutor, Listener {

    private final CosmeticsManager manager;

    public CosmeticsCommand(CsmpCore plugin) {
        this.manager = plugin.getCosmeticsManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (!player.hasPermission("csmp.trails")) {
            player.sendMessage(Component.text("You must be MVP+ to use trails!", NamedTextColor.RED));
            return true;
        }

        openGui(player);
        return true;
    }

    private void openGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Cosmetic Trails"));

        inv.setItem(10, createIcon(Material.BLAZE_POWDER, "Flame Trail", Particle.FLAME));
        inv.setItem(11, createIcon(Material.HEART_OF_THE_SEA, "Heart Trail", Particle.HEART));
        inv.setItem(12, createIcon(Material.GUNPOWDER, "Explosion Trail", Particle.EXPLOSION));

        inv.setItem(13, createIcon(Material.EMERALD, "Happy Trail", Particle.HAPPY_VILLAGER));
        inv.setItem(14, createIcon(Material.NOTE_BLOCK, "Note Trail", Particle.NOTE));
        inv.setItem(15, createIcon(Material.SOUL_SAND, "Soul Trail", Particle.SOUL));

        inv.setItem(22, createIcon(Material.BARRIER, "Clear Trails", null));

        player.openInventory(inv);
    }

    private ItemStack createIcon(Material mat, String name, Particle particle) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.GOLD));
        if (particle == null) {
            meta.lore(Arrays.asList(Component.text("Click to remove active trail.", NamedTextColor.GRAY)));
        } else {
            meta.lore(Arrays.asList(Component.text("Click to equip.", NamedTextColor.GRAY)));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(Component.text("Cosmetic Trails")))
            return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null)
            return;

        Player player = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();

        if (current.getType() == Material.BARRIER) {
            manager.setTrail(player, null);
            player.sendMessage(Component.text("Trails cleared.", NamedTextColor.YELLOW));
            player.closeInventory();
            return;
        }

        Particle type = null;
        switch (current.getType()) {
            case BLAZE_POWDER:
                type = Particle.FLAME;
                break;
            case HEART_OF_THE_SEA:
                type = Particle.HEART;
                break;
            case GUNPOWDER:
                type = Particle.EXPLOSION;
                break;
            case EMERALD:
                type = Particle.HAPPY_VILLAGER;
                break;
            case NOTE_BLOCK:
                type = Particle.NOTE;
                break;
            case SOUL_SAND:
                type = Particle.SOUL;
                break;
            default:
                break;
        }

        if (type != null) {
            manager.setTrail(player, type);
            player.sendMessage(Component.text("Equipped trail!", NamedTextColor.GREEN));
            player.closeInventory();
        }
    }
}
