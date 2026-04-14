package com.cirexx.csmp.events;

import com.cirexx.csmp.CsmpCore;
import org.bukkit.event.Listener;

public abstract class GameEvent implements Listener {

    protected final CsmpCore plugin;

    public GameEvent(CsmpCore plugin) {
        this.plugin = plugin;
    }

    public abstract String getName();

    /**
     * Duration in seconds.
     */
    public abstract long getDuration();

    public abstract void start();

    public abstract void stop();

    public void onTick() {
        // Override if needed
    }
}
