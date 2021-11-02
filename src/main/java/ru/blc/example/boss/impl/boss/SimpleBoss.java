package ru.blc.example.boss.impl.boss;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.api.boss.Boss;
import ru.blc.example.boss.api.boss.BossType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SimpleBoss implements Boss {

    public static @NotNull SimpleBoss create(@NotNull Plugin plugin,
                                             @NotNull BossType type,
                                             @NotNull Location location,
                                             @NotNull final String name,
                                             long respawnDelay,
                                             final double maxHealth,
                                             final double baseDamage) {
        return new SimpleBoss(plugin, null, type, location.clone(),
                name, respawnDelay, maxHealth, baseDamage,
                new Object2DoubleOpenHashMap<>());
    }

    @NotNull Plugin plugin;
    @Nullable LivingEntity entity;
    @NotNull final BossType type;
    @NotNull final Location spawnLocation;
    @NotNull final String name;
    long respawnDelay;
    final double maxHealth;
    final double baseDamage;
    @NotNull final Object2DoubleMap<@NotNull OfflinePlayer> damagers;

    @Override
    public @NotNull Location getSpawnLocation() {
        return this.spawnLocation.clone();
    }

    @Override
    public boolean isAlive() {
        return this.getEntity() != null && !this.getEntity().isDead();
    }

    @Override
    public void spawn() {
        if (this.isAlive()) return;
        entity = this.getSpawnLocation().getWorld().spawn(this.getSpawnLocation(), this.getType().getEntityType());
        if (baseDamage > 0) {
            entity.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(baseDamage);
        }
        entity.registerAttribute(Attribute.GENERIC_MAX_HEALTH);
        Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);
        entity.setHealth(maxHealth);
        entity.setMetadata("boss_meta", new FixedMetadataValue(plugin, this));
        damagers.clear();
    }

    @Override
    public void kill() {
        if (!this.isAlive()) return;
        assert this.getEntity() != null : "Unreachable via if entity is null, isAlive() returns false";
        this.getEntity().setHealth(0);
        entity = null;
    }

    @Override
    public void onKill() {

    }

    @Override
    public void onDamage(@NotNull Player player, double damage) {
        damagers.computeDouble(player, (key, value) -> value == null ? damage : value + damage);
    }

    @Override
    public @NotNull List<@NotNull OfflinePlayer> getAllDamagers() {
        var result = new ArrayList<>(damagers.keySet());
        result.sort(Comparator.comparingDouble(damagers::getDouble).reversed());
        return result;
    }
}
