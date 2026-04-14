package com.cirexx.csmp.events.impl;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.events.GameEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;

import java.util.Random;

public class ChatQuizEvent extends GameEvent {

    private int answer;
    private final Random random = new Random();
    private boolean solved = false;

    public ChatQuizEvent(CsmpCore plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "Chat Quiz";
    }

    @Override
    public long getDuration() {
        return 60; // 60 seconds
    }

    @Override
    public void start() {
        solved = false;
        int a = random.nextInt(50) + 1;
        int b = random.nextInt(50) + 1;

        // Simple math for now
        if (random.nextBoolean()) {
            answer = a + b;
            Bukkit.broadcast(Component.text("QUICK! What is " + a + " + " + b + "?", NamedTextColor.YELLOW));
        } else {
            // Ensure positive result for subtraction simplicity
            if (b > a) {
                int temp = a;
                a = b;
                b = temp;
            }
            answer = a - b;
            Bukkit.broadcast(Component.text("QUICK! What is " + a + " - " + b + "?", NamedTextColor.YELLOW));
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        if (!solved) {
            Bukkit.broadcast(Component.text("Time's up! The answer was: " + answer, NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (solved)
            return;

        String msg = PlainTextComponentSerializer.plainText().serialize(event.message());
        try {
            int guess = Integer.parseInt(msg.trim());
            if (guess == answer) {
                solved = true;
                Player p = event.getPlayer();

                // Must run rewards on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.broadcast(Component.text(p.getName() + " won the quiz!", NamedTextColor.GREEN));
                    int reward = 5;
                    plugin.getTokenManager().addTokens(p.getUniqueId(), reward);
                    Bukkit.broadcast(Component.text("Reward: " + reward + " Event Tokens", NamedTextColor.GOLD));

                    // Stop event early since it's done
                    // But we can't call manager.stopCurrentEvent() easily from here without
                    // circular dep or passing manager
                    // So we just let the duration run out or handled by the boolean flag "solved"
                    // To be cleaner, we could expose a "end()" method in GameEvent,
                    // but for this simple system, letting it timeout (now harmlessly) is okay,
                    // OR we force the manager to stop.
                    // Let's just create a quick task to force stop via plugin main logic if
                    // exposed,
                    // but for now relying on timeout is safe enough as listener ignores subsequent
                    // chats.
                    // Actually, "Winner declared" messages might get spammy if we don't unregister.
                    HandlerList.unregisterAll(this);
                });
            }
        } catch (NumberFormatException ignored) {
            // Not a number, ignore
        }
    }
}
