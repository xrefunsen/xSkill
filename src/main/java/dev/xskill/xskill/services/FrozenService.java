package dev.xskill.xskill.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FrozenService {
    private final Map<UUID, Long> frozenUntilMs = new ConcurrentHashMap<>();

    public boolean isFrozen(UUID playerId, long nowMs) {
        Long until = frozenUntilMs.get(playerId);
        return until != null && until > nowMs;
    }

    public void freeze(UUID playerId, long untilMs) {
        frozenUntilMs.put(playerId, untilMs);
    }

    public void unfreeze(UUID playerId) {
        frozenUntilMs.remove(playerId);
    }

    public void clear() {
        frozenUntilMs.clear();
    }
}

