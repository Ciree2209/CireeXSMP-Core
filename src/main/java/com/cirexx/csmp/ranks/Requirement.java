package com.cirexx.csmp.ranks;

import org.bukkit.entity.Player;

public interface Requirement {
    boolean meets(Player p);

    String getDescription();
}
