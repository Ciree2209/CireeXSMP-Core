package com.cirexx.csmp.economy;

import com.cirexx.csmp.CsmpCore;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class EventTokenManager {

    private final TokenStorage storage;
    private final Map<UUID, Integer> tokenCache;

    public EventTokenManager(CsmpCore plugin) {
        this.storage = new TokenStorage(plugin);
        this.tokenCache = storage.loadBalances();
    }

    public void save() {
        storage.saveBalances(tokenCache);
    }

    public int getBalance(UUID uuid) {
        return tokenCache.getOrDefault(uuid, 0);
    }

    public int getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public void setBalance(UUID uuid, int amount) {
        tokenCache.put(uuid, Math.max(0, amount));
    }

    public void addTokens(UUID uuid, int amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean removeTokens(UUID uuid, int amount) {
        int current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        setBalance(uuid, current - amount);
        return true;
    }

    public boolean hasTokens(UUID uuid, int amount) {
        return getBalance(uuid) >= amount;
    }
}
