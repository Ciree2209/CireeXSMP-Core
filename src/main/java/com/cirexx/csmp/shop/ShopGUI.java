package com.cirexx.csmp.shop;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.shop.ShopManager.ShopCategory;
import com.cirexx.csmp.shop.ShopManager.ShopItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI implements Listener {

    private final CsmpCore plugin;
    private final ShopManager shopManager;
    private Economy economy;

    public ShopGUI(CsmpCore plugin) {
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
        setupEconomy();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            economy = rsp.getProvider();
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Shop Categories"));

        for (ShopCategory cat : shopManager.getCategories()) {
            ItemStack icon = new ItemStack(cat.getIcon());
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(cat.getDisplayName()));
            icon.setItemMeta(meta);

            if (cat.getSlot() >= 0 && cat.getSlot() < 27) {
                inv.setItem(cat.getSlot(), icon);
            } else {
                inv.addItem(icon);
            }
        }

        player.openInventory(inv);
    }

    public void openCategory(Player player, ShopCategory category) {
        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text("Shop: " + LegacyComponentSerializer.legacyAmpersand().serialize(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(category.getDisplayName()))));

        for (ShopItem item : category.getItems()) {
            ItemStack icon = new ItemStack(item.getMaterial());
            ItemMeta meta = icon.getItemMeta();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("Buy: $" + item.getBuyPrice(), NamedTextColor.GREEN));
            lore.add(Component.text("Sell: $" + item.getSellPrice(), NamedTextColor.RED));
            lore.add(Component.text(""));
            lore.add(Component.text("Left-Click: Buy 1", NamedTextColor.GRAY));
            lore.add(Component.text("Shift-Left: Buy 64", NamedTextColor.GRAY));
            lore.add(Component.text("Right-Click: Sell 1", NamedTextColor.GRAY));
            lore.add(Component.text("Shift-Right: Sell 64", NamedTextColor.GRAY));

            meta.lore(lore);
            icon.setItemMeta(meta);

            inv.addItem(icon);
        }

        // Add Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("Back", NamedTextColor.RED));
        back.setItemMeta(meta);
        inv.setItem(49, back); // Bottom center

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        // Handle logic based on title/holder
        if (event.getView().title().contains(Component.text("Shop Categories"))) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType() == Material.AIR)
                return;

            // Find category by icon/name
            for (ShopCategory cat : shopManager.getCategories()) {
                if (cat.getIcon() == current.getType()) {
                    openCategory(player, cat);
                    return;
                }
            }
        } else if (event.getView().title().toString().contains("Shop: ")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType() == Material.AIR)
                return;

            if (current.getType() == Material.ARROW) {
                openMainMenu(player);
                return;
            }

            // It's a shop item
            Material mat = current.getType();
            // Find item in current category (Need efficient lookup or just search all for
            // simplicity)
            ShopItem shopItem = null;
            // Hacky: Search all loaded items for this material
            for (ShopCategory cat : shopManager.getCategories()) {
                for (ShopItem item : cat.getItems()) {
                    if (item.getMaterial() == mat) {
                        shopItem = item;
                        break;
                    }
                }
            }
            if (shopItem == null)
                return;

            int amount = event.isShiftClick() ? 64 : 1;
            boolean isBuy = event.isLeftClick();

            if (isBuy) {
                handleBuy(player, shopItem, amount);
            } else {
                handleSell(player, shopItem, amount);
            }
        }
    }

    private void handleBuy(Player player, ShopItem item, int amount) {
        double cost = item.getBuyPrice() * amount;
        if (economy == null) {
            player.sendMessage(Component.text("Economy not available.", NamedTextColor.RED));
            return;
        }

        if (economy.getBalance(player) < cost) {
            player.sendMessage(Component.text("Insufficient funds! You need $" + cost, NamedTextColor.RED));
            return;
        }

        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.text("Inventory full!", NamedTextColor.RED));
            return;
        }

        economy.withdrawPlayer(player, cost);
        player.getInventory().addItem(new ItemStack(item.getMaterial(), amount));
        player.sendMessage(Component.text("Bought " + amount + "x " + item.getMaterial().name() + " for $" + cost,
                NamedTextColor.GREEN));
    }

    private void handleSell(Player player, ShopItem item, int amount) {
        double value = item.getSellPrice() * amount;
        ItemStack toRemove = new ItemStack(item.getMaterial(), amount);

        if (!player.getInventory().containsAtLeast(toRemove, amount)) {
            player.sendMessage(Component.text("You don't have enough items!", NamedTextColor.RED));
            return;
        }

        player.getInventory().removeItem(toRemove);
        economy.depositPlayer(player, value);
        player.sendMessage(Component.text("Sold " + amount + "x " + item.getMaterial().name() + " for $" + value,
                NamedTextColor.GREEN));
    }
}
