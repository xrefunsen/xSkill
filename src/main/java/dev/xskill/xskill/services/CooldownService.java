package dev.xskill.xskill.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {
    private final Map<String, Long> cooldownUntilMs = new ConcurrentHashMap<>();

    public long remainingMs(UUID playerId, String key, long nowMs) {
        Long until = cooldownUntilMs.get(playerId + ":" + key);
        if (until == null) return 0L;
        long left = until - nowMs;
        return Math.max(left, 0L);
    }

    public void set(UUID playerId, String key, long durationMs, long nowMs) {
        if (durationMs <= 0) return;
        cooldownUntilMs.put(playerId + ":" + key, nowMs + durationMs);
    }
}

