package com.cirexx.csmp.auction;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.auction.AuctionManager.AuctionListing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionGUI implements Listener {

    private final CsmpCore plugin;
    private final AuctionManager auctionManager;

    public AuctionGUI(CsmpCore plugin) {
        this.plugin = plugin;
        this.auctionManager = plugin.getAuctionManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGlobalView(Player player, int page) {
        Component title = plugin.getMessage("auction.page_title", "%page%", String.valueOf(page + 1));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<AuctionListing> allListings = auctionManager.getActiveListings();
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, allListings.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            AuctionListing listing = allListings.get(i);

            ItemStack icon = listing.getItem().clone();
            ItemMeta meta = icon.getItemMeta();
            List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
            if (lore == null)
                lore = new ArrayList<>();

            lore.add(Component.text(""));
            lore.add(plugin.getMessage("auction.seller", "%seller%", listing.getSellerName()));
            lore.add(plugin.getMessage("auction.price", "%price%", String.valueOf(listing.getPrice())));

            long timeLeft = (listing.getExpiry() - System.currentTimeMillis()) / 1000;
            lore.add(plugin.getMessage("auction.expires", "%time%", formatTime(timeLeft)));

            lore.add(Component.text(""));
            if (listing.getSeller().equals(player.getUniqueId())) {
                lore.add(plugin.getMessage("auction.click_cancel"));
            } else {
                lore.add(plugin.getMessage("auction.click_buy"));
            }

            meta.lore(lore);
            icon.setItemMeta(meta);

            inv.setItem(slot++, icon);
        }

        // Toolbar
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.displayName(Component.text("Previous Page", NamedTextColor.YELLOW));
            prev.setItemMeta(meta);
            inv.setItem(45, prev);
        }

        if (endIndex < allListings.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.displayName(Component.text("Next Page", NamedTextColor.YELLOW));
            next.setItemMeta(meta);
            inv.setItem(53, next);
        }

        // Collection Bin Button (Center)
        List<ItemStack> expired = auctionManager.getExpiredItems(player.getUniqueId());
        ItemStack bin = new ItemStack(Material.CHEST);
        ItemMeta meta = bin.getItemMeta();
        if (!expired.isEmpty()) {
            meta.displayName(Component.text("Collect Expired Items (" + expired.size() + ")", NamedTextColor.RED));
        } else {
            meta.displayName(Component.text("Collection Bin (Empty)", NamedTextColor.GRAY));
        }
        bin.setItemMeta(meta);
        inv.setItem(49, bin);

        player.openInventory(inv);
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        // Check title using startsWith because of page number
        if (!event.getView().title().toString().contains("Auction House"))
            return;

        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR)
            return;

        // Extract Page Number from Title
        int page = 0;
        try {
            String title = ((net.kyori.adventure.text.TextComponent) event.getView().title()).content();
            if (title.contains("(Page ")) {
                String num = title.substring(title.indexOf("(Page ") + 6, title.indexOf(")"));
                page = Integer.parseInt(num) - 1;
            }
        } catch (Exception ignored) {
            // Fallback to page 0 if parsing fails
        }

        // Navigation
        if (event.getSlot() == 45 && current.getType() == Material.ARROW) {
            openGlobalView(player, Math.max(0, page - 1));
            return;
        }
        if (event.getSlot() == 53 && current.getType() == Material.ARROW) {
            openGlobalView(player, page + 1);
            return;
        }

        // Collection Bin
        if (event.getSlot() == 49 && current.getType() == Material.CHEST) {
            collectExpired(player);
            return;
        }

        // Listings (Slots 0-44)
        if (event.getSlot() >= 45)
            return;

        List<AuctionListing> allListings = auctionManager.getActiveListings();
        int index = (page * 45) + event.getSlot();

        if (index < allListings.size()) {
            AuctionListing target = allListings.get(index);

            if (target.getSeller().equals(player.getUniqueId())) {
                // Cancel
                auctionManager.getActiveListings().remove(target);
                auctionManager.deleteListing(target.getId());

                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(target.getItem());
                    player.sendMessage(plugin.getMessage("auction.cancelled"));
                } else {
                    auctionManager.addExpiredItem(player.getUniqueId(), target.getItem());
                    player.sendMessage(plugin.getMessage("auction.cancelled_bin"));
                }

                openGlobalView(player, page);
            } else {
                auctionManager.buy(player, target);
                openGlobalView(player, page);
            }
        }
    }

    private void collectExpired(Player player) {
        List<ItemStack> items = auctionManager.getExpiredItems(player.getUniqueId());
        boolean full = false;
        for (ItemStack item : items) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                full = true;
                break;
            }
        }

        if (!full) {
            auctionManager.clearExpired(player.getUniqueId());
            player.sendMessage(plugin.getMessage("auction.collect_success"));
        } else {
            // Partial collection logic is complex, for MVP just say full
            player.sendMessage(plugin.getMessage("auction.inventory_full"));
        }
        player.closeInventory();
    }
}
