package ru.blc.example.boss.api.boss;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Boss {

    /**
     * Returns entity referring for current boss<br>
     * If boss is alive,entity isn't null
     *
     * @return current entity or null
     */
    @Nullable LivingEntity getEntity();

    /**
     * @return Boss type
     */
    @NotNull BossType getType();

    /**
     * @return boss spawn location
     */
    @NotNull Location getSpawnLocation();

    @NotNull String getName();

    boolean canTarget(EntityType entityType);

    double getMaxHealth();

    double getBaseDamage();

    /**
     * check if boss is alive
     *
     * @return true if boss is alive otherwise false
     */
    boolean isAlive();

    /**
     * delay is -1 for irregular bosses
     *
     * @return respawn delay in ticks
     */
    long getRespawnDelay();

    /**
     * spawns current boss<br>
     * if boss already spawned, method falls silent
     */
    void spawn();

    /**
     * kills current boss<br>
     * if boss already died, method falls silent
     */
    void kill();

    void onKill();

    void onDamage(@NotNull Player player, double damage);

    @NotNull List<@NotNull DamageData> getAllDamagers();

    void createHologram(@NotNull Player player);

    void removeHologram(@NotNull Player player);


    @RequiredArgsConstructor
    @AllArgsConstructor
    @Getter
    class DamageData {
        private final OfflinePlayer player;
        private double damage;

        public void addDamage(double damage) {
            this.damage += damage;
        }

        @Override
        public String toString() {
            return "{" +
                    "player: " + player.getName() + ", " +
                    "damage: " + damage +
                    '}';
        }
    }
}
