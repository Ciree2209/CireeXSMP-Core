package com.cirexx.csmp.events;

import com.cirexx.csmp.CsmpCore;
import com.cirexx.csmp.events.impl.ChatQuizEvent;
import com.cirexx.csmp.events.impl.MiningEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventManager {

    private final CsmpCore plugin;
    private final List<GameEvent> events = new ArrayList<>();
    private GameEvent currentEvent;
    private long eventStartTime;
    private BukkitTask timerTask;

    public EventManager(CsmpCore plugin) {
        this.plugin = plugin;
        registerEvents();
        startScheduler();
    }

    private void registerEvents() {
        events.add(new ChatQuizEvent(plugin));
        events.add(new MiningEvent(plugin));
    }

    private void startScheduler() {
        // Run every minute to check schedule or tick current event
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L); // 1-second tick for precision
    }

    private long lastEventEnd = System.currentTimeMillis();
    private final Random random = new Random();

    public GameEvent getCurrentEvent() {
        return currentEvent;
    }

    private void tick() {
        if (currentEvent != null) {
            long elapsedSeconds = (System.currentTimeMillis() - eventStartTime) / 1000;
            if (elapsedSeconds >= currentEvent.getDuration()) {
                stopCurrentEvent();
            } else {
                currentEvent.onTick();
            }
        } else {
            // Check if it's time for a new event
            // For simplicity, we'll just check if X time has passed since last event
            // In a real production server, you might align this to clock time (e.g. xx:00)
            long timeSinceLast = System.currentTimeMillis() - lastEventEnd;
            if (timeSinceLast >= (plugin.getConfig().getLong("events.interval-seconds", 3600) * 1000)) {
                startRandomEvent();
            }
        }
    }

    public void startRandomEvent() {
        if (events.isEmpty())
            return;
        GameEvent next = events.get(random.nextInt(events.size()));
        startEvent(next);
    }

    public void startEvent(GameEvent event) {
        if (currentEvent != null) {
            stopCurrentEvent();
        }
        currentEvent = event;
        eventStartTime = System.currentTimeMillis();

        Bukkit.broadcast(Component.text("--------------------------------", NamedTextColor.GOLD));
        Bukkit.broadcast(Component.text("EVENT STARTING: " + event.getName(), NamedTextColor.GREEN));
        Bukkit.broadcast(Component.text("Duration: " + event.getDuration() + " seconds", NamedTextColor.YELLOW));
        Bukkit.broadcast(Component.text("--------------------------------", NamedTextColor.GOLD));

        event.start();
    }

    public void stopCurrentEvent() {
        if (currentEvent != null) {
            Bukkit.broadcast(Component.text("Event " + currentEvent.getName() + " has ended!", NamedTextColor.GOLD));
            currentEvent.stop();
            currentEvent = null;
            lastEventEnd = System.currentTimeMillis();
        }
    }

    public void disable() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        stopCurrentEvent();
    }
}
