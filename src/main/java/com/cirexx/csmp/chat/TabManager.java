package com.cirexx.csmp.chat;

import com.cirexx.csmp.CsmpCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class TabManager implements Listener {

    private final CsmpCore plugin;
    private Chat vaultChat;

    public TabManager(CsmpCore plugin) {
        this.plugin = plugin;
        setupVaultChat();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startUpdater();
    }

    private void setupVaultChat() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return;
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null)
            vaultChat = rsp.getProvider();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateTab(event.getPlayer());
    }

    private void startUpdater() {
        // Update every 5 seconds (100 ticks) to catch rank changes or just refresh
        // header/footer
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateTab(p);
            }
        }, 100L, 100L);
    }

    private void updateTab(Player p) {
        String headerText = plugin.getConfig().getString("tab.header", "Welcome to CirexxSMP");
        String footerText = plugin.getConfig().getString("tab.footer", "Online: {online}");

        footerText = footerText.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));

        p.sendPlayerListHeaderAndFooter(
                LegacyComponentSerializer.legacyAmpersand().deserialize(headerText),
                LegacyComponentSerializer.legacyAmpersand().deserialize(footerText));

        // Update player list name (Tab Name)
        if (vaultChat != null) {
            String prefix = vaultChat.getPlayerPrefix(p);
            // Format: [Prefix] Name
            // Using logic similar to chat, but typically shorter
            Component tabName = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + p.getName());
            p.playerListName(tabName);
        }
    }
}
