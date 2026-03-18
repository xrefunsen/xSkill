package dev.xskill.xskill.data;

import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;
    private double xp;

    public PlayerData(UUID uuid, double xp) {
        this.uuid = uuid;
        this.xp = xp;
    }

    public UUID uuid() {
        return uuid;
    }

    public double xp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = Math.max(0.0, xp);
    }
}

