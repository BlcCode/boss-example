package ru.blc.example.boss.api.boss;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Boss {

    @Nullable Entity getEntity();

    @NotNull BossType getType();

    boolean isAlive();

    long getRespawnLeftTime();

    long getRespawnDelay();

    void spawn();

    void kill();
}
