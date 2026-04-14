package com.cirexx.csmp.chat;

import com.cirexx.csmp.CsmpCore;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ChatLogic implements Listener {

    private final CsmpCore plugin;
    private Chat vaultChat;

    public ChatLogic(CsmpCore plugin) {
        this.plugin = plugin;
        setupVaultChat();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupVaultChat() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return;
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null)
            vaultChat = rsp.getProvider();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        // String format = plugin.getConfig().getString("chat.format", "<gray>[{prefix}]
        // <white>{name}: {message}");

        // String prefix = "";
        // if (vaultChat != null) {
        // prefix = vaultChat.getPlayerPrefix(event.getPlayer());
        // }

        // Simple formatting
        // We use Legacy serializer for prefix since Vault usually returns legacy colors
        // (&4)
        // Then we wrap it in a proper Component

        // Note: Paper 1.21 suggests using ChatRenderer, but for simple substitution
        // modifying the renderer
        // or constructing a formatted component is fine.

        // Let's stick to a simple replacement for now using MiniMessage for the main
        // format structure if possible,
        // or just construct the component manually.

        // Actually, easiest way with modern Paper is to set the renderer.
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            String finalPrefix = "";
            if (vaultChat != null) {
                finalPrefix = vaultChat.getPlayerPrefix(source);
            }

            // Convert legacy prefix/name to component
            Component prefixComp = LegacyComponentSerializer.legacyAmpersand().deserialize(finalPrefix);

            return Component.text()
                    .append(prefixComp)
                    .append(sourceDisplayName)
                    .append(Component.text(": "))
                    .append(message)
                    .build();
        });
    }
}
