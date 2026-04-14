package com.cirexx.csmp.shop;

import com.cirexx.csmp.CsmpCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    private final CsmpCore plugin;
    private final Map<String, ShopCategory> categories = new HashMap<>();
    private File file;
    private YamlConfiguration config;

    public ShopManager(CsmpCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection cats = config.getConfigurationSection("categories");
        if (cats == null)
            return;

        categories.clear();
        for (String key : cats.getKeys(false)) {
            ConfigurationSection sec = cats.getConfigurationSection(key);
            String name = sec.getString("name", key);
            Material icon = Material.valueOf(sec.getString("icon", "STONE"));
            int slot = sec.getInt("slot", 0);

            List<ShopItem> items = new ArrayList<>();
            ConfigurationSection itemSec = sec.getConfigurationSection("items");
            if (itemSec != null) {
                for (String itemKey : itemSec.getKeys(false)) {
                    ConfigurationSection is = itemSec.getConfigurationSection(itemKey);
                    Material mat = Material.valueOf(is.getString("material", "STONE"));
                    double buy = is.getDouble("buy", 0);
                    double sell = is.getDouble("sell", 0);
                    items.add(new ShopItem(mat, buy, sell));
                }
            }

            categories.put(key, new ShopCategory(key, name, icon, slot, items));
        }
    }

    public List<ShopCategory> getCategories() {
        return new ArrayList<>(categories.values());
    }

    public static class ShopCategory {
        String id;
        String displayName;
        Material icon;
        int slot;
        List<ShopItem> items;

        public ShopCategory(String id, String displayName, Material icon, int slot, List<ShopItem> items) {
            this.id = id;
            this.displayName = displayName;
            this.icon = icon;
            this.slot = slot;
            this.items = items;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }

        public int getSlot() {
            return slot;
        }

        public List<ShopItem> getItems() {
            return items;
        }
    }

    public static class ShopItem {
        Material material;
        double buyPrice;
        double sellPrice;

        public ShopItem(Material material, double buyPrice, double sellPrice) {
            this.material = material;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }

        public Material getMaterial() {
            return material;
        }

        public double getBuyPrice() {
            return buyPrice;
        }

        public double getSellPrice() {
            return sellPrice;
        }
    }
}
