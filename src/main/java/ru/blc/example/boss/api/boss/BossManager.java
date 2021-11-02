package ru.blc.example.boss.api.boss;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface BossManager {

    @NotNull Boss getRegularBoss(@NotNull BossType type);

    @NotNull Set<@NotNull Boss> getIrregularBosses(@NotNull BossType type);

    @NotNull Boss spawnIrregularBoss(@NotNull BossType type, @NotNull Location location);

    void killAll();
}
