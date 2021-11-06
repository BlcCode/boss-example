package ru.blc.example.boss.impl.boss;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.BossListener;
import ru.blc.example.boss.BossPlugin;
import ru.blc.example.boss.api.boss.Boss;
import ru.blc.example.boss.api.boss.BossManager;
import ru.blc.example.boss.api.boss.BossType;

import java.util.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleBossManager implements BossManager {

    public static @NotNull SimpleBossManager create(@NonNull BossPlugin plugin) {
        final var bossManager = new SimpleBossManager(plugin,
                new EnumMap<>(BossType.class), new EnumMap<>(BossType.class), new EnumMap<>(BossType.class));
        bossManager.createRegularBosses();
        plugin.getServer().getPluginManager().registerEvents(BossListener.create(plugin, bossManager), plugin);
        return bossManager;
    }

    @NotNull BossPlugin plugin;
    @NotNull Map<@NotNull BossType, @NotNull Boss> regularBosses;
    @NotNull Map<@NotNull BossType, @NotNull BossSettings> bossesSettings;
    @NotNull Map<@NotNull BossType, @NotNull Set<@NotNull Boss>> irregularBosses;


    @Override
    public @Nullable Boss getBossByEntity(@NotNull LivingEntity entity) {
        for (MetadataValue meta : entity.getMetadata("boss_meta")) {
            if (meta.value() instanceof Boss boss) return boss;
        }
        return null;
    }

    @Override
    public @NotNull Boss getRegularBoss(@NonNull BossType type) {
        return regularBosses.get(type);
    }

    @Override
    public @NotNull Set<@NotNull Boss> getIrregularBosses(@NonNull BossType type) {
        return Collections.unmodifiableSet(irregularBosses.getOrDefault(type, Collections.emptySet()));
    }

    @Override
    public @NotNull Boss spawnIrregularBoss(@NonNull BossType type, @NonNull Location location) {
        final var boss = createIrregularBoss(type, location);
        boss.spawn();
        return boss;
    }

    @Override
    public @NotNull Boss createIrregularBoss(@NotNull BossType type, @NotNull Location location) {
        final var boss = createBoss(type, location, bossesSettings.get(type).toIrregular());
        irregularBosses.computeIfAbsent(type, key -> new HashSet<>()).add(boss);
        return boss;
    }

    @Override
    public void killAll() {
        regularBosses.values().forEach(Boss::kill);
        irregularBosses.values().stream().flatMap(Set::stream).forEach(Boss::kill);
    }

    private @NotNull Boss createBoss(@NotNull BossType type, @NotNull Location location, @NotNull BossSettings settings) {
        return SimpleBoss.create(plugin, type, location,
                settings.name(), settings.respawnDelayTicks(), settings.maxHealth(), settings.baseDamage());
    }

    private void createRegularBosses() {
        for (BossType bossType : BossType.values()) {
            final var bossConfig = Objects.requireNonNull(
                    plugin.getConfig().getConfigurationSection("bosses." + bossType.name().toLowerCase(Locale.ROOT)),
                    "boss " + bossType + " not configured");

            Preconditions.checkState(bossConfig.contains("health"), bossConfig.getCurrentPath() + "doesn't contains health");
            Preconditions.checkState(bossConfig.contains("respawn-delay"), bossConfig.getCurrentPath() + "doesn't contains respawn-delay");
            Preconditions.checkState(bossConfig.isLocation("spawn-location"), bossConfig.getCurrentPath() + "doesn't contains spawn-location or it's not a bukkit location format");

            double maxHealth = bossConfig.getDouble("health");
            double baseDamage = bossConfig.getDouble("base-damage", -1.0D);
            long respawnDelay = bossConfig.getLong("respawn-delay") * 60 * 20;
            Location spawn = bossConfig.getLocation("spawn-location");
            assert spawn != null : "Unreachable via preconditions check";

            regularBosses.put(bossType, createBoss(bossType, spawn,
                    new BossSettings(baseDamage, maxHealth, plugin.getTranslation(bossType + "_NAME"), respawnDelay)));
        }
    }

    private record BossSettings(double baseDamage, double maxHealth, @NotNull String name, long respawnDelayTicks) {
        BossSettings toIrregular() {
            return new BossSettings(baseDamage(), maxHealth(), name(), -1);
        }
    }
}
