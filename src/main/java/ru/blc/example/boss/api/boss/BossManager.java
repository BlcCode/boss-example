package ru.blc.example.boss.api.boss;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

public interface BossManager {

    @Nullable Boss getBossByEntity(@NotNull LivingEntity entity);

    /**
     * Returns regular boss for specified boss type<br>
     * There is only one regular boss with for each type on server
     *
     * @param type boss type
     * @return boss
     */
    @NotNull Boss getRegularBoss(@NotNull BossType type);

    /**
     * Returns set with all irregular bosses<br>
     * Bosses that was spawned and then died is not there
     *
     * @param type boss type
     * @return set with all undefeated irregular bosses
     */
    @UnmodifiableView
    @NotNull Set<@NotNull Boss> getIrregularBosses(@NotNull BossType type);

    /**
     * Summons irregular boss
     *
     * @param type     boss type
     * @param location boss spawn location
     * @return spawned boss
     */
    @Contract("_,_->new")
    @NotNull Boss spawnIrregularBoss(@NotNull BossType type, @NotNull Location location);

    /**
     * Creates irregular boss<br>
     * Boss would not spawn until {@link Boss#spawn()} wasn't executed
     *
     * @param type     boss type
     * @param location boss spawn location
     * @return created boss
     */
    @NotNull Boss createIrregularBoss(@NotNull BossType type, @NotNull Location location);

    /**
     * Kills all known bosses
     */
    void killAll();
}
