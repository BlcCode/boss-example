package ru.blc.example.boss.impl.boss;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.BossPlugin;
import ru.blc.example.boss.api.Hologram;
import ru.blc.example.boss.api.boss.Boss;
import ru.blc.example.boss.api.boss.BossType;
import ru.blc.example.boss.impl.hologram.SimpleHologram;

import java.util.*;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SimpleBoss implements Boss {

    public static final String ADD_BOSS_ROW = "INSERT `defating`(`boss`, `best_players`) VALUES (?, ?);";

    public static @NotNull SimpleBoss create(@NotNull BossPlugin plugin,
                                             @NotNull BossType type,
                                             @NotNull Location location,
                                             @NotNull final String name,
                                             long respawnDelay,
                                             final double maxHealth,
                                             final double baseDamage) {
        var boss = switch (type) {
            case RAVAGER -> new RavagerBoss(plugin, null, type, location.clone(),
                    name, respawnDelay, maxHealth, baseDamage,
                    new HashMap<>(),
                    plugin.getTranslationList("BOSS_SPAWN_HOLOGRAM"), new WeakHashMap<>(),
                    null);
            case SUMMONER -> new SummonerBoss(plugin, null, type, location.clone(),
                    name, respawnDelay, maxHealth, baseDamage,
                    new HashMap<>(),
                    plugin.getTranslationList("BOSS_SPAWN_HOLOGRAM"), new WeakHashMap<>(),
                    null);
        };
        boss.startRunnable();
        return boss;
    }

    @NotNull BossPlugin plugin;
    @Nullable LivingEntity entity;
    @NotNull final BossType type;
    @NotNull final Location spawnLocation;
    @NotNull final String name;
    long respawnDelay;
    final double maxHealth;
    final double baseDamage;
    @NotNull final Map<@NotNull OfflinePlayer, @NotNull DamageData> damagers;
    final List<String> holoLines;
    final Map<Player, Hologram> holograms;
    RespawnRunnable respawnRunnable;

    @Override
    public @NotNull Location getSpawnLocation() {
        return this.spawnLocation.clone();
    }

    @Override
    public boolean canTarget(EntityType entityType) {
        return true;
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
        entity.setCustomNameVisible(true);
        entity.setCustomName(getName());
        entity.setRemoveWhenFarAway(false);
        damagers.clear();
    }

    @Override
    public void kill() {
        if (!this.isAlive()) return;
        assert this.getEntity() != null : "Unreachable via if entity is null, isAlive() returns false";
        this.getEntity().setHealth(0);
        entity = null;
        onKill();
    }

    @Override
    public void onKill() {
        if (plugin.isEnabled()) { //if plugin is not enabled boss killed by plugin disabling
            plugin.getTranslationList("BOSS_DEFEATED",
                            this.getName(),
                            this.getAllDamagers().stream().limit(3).map(data -> data.getPlayer().getName()).collect(Collectors.joining(", ")))
                    .forEach(line ->
                            plugin.getServer().broadcast(LegacyComponentSerializer.legacySection().deserialize(line)));
            startRunnable();
            plugin.getServer().getOnlinePlayers().forEach(this::createHologram);
            writeSql();
        }
    }

    protected void writeSql() {
        final var sql = plugin.getSqlConnection();
        var damagers = getAllDamagers();
        final String best = damagers.subList(0, Math.min(damagers.size(), 3)).toString();
        sql.prepareStatement(ADD_BOSS_ROW, getType().ordinal(), best)
                .ifPresentOrElse(sql::executeUpdateAsync,
                        () -> plugin.getSLF4JLogger().error("Failed to prepare boss-defeat information for database, data: {}, {}", getType(), best));
    }

    protected void startRunnable() {
        if (getRespawnDelay() <= 0 || !plugin.isEnabled()) return;
        respawnRunnable = new RespawnRunnable();
        respawnRunnable.runTaskTimer(plugin, 0L, 20 * 60L);
    }

    @Override
    public void onDamage(@NotNull Player player, double damage) {
        damagers.computeIfAbsent(player, DamageData::new).addDamage(damage);
    }

    @Override
    public @NotNull List<@NotNull DamageData> getAllDamagers() {
        var result = new ArrayList<>(damagers.values());
        result.sort(Comparator.comparingDouble(DamageData::getDamage).reversed());
        return result;
    }

    @Override
    public void createHologram(@NotNull Player player) {
        if (respawnRunnable != null) {
            var newHolo = SimpleHologram.create(List.of(
                    holoLines.get(0).formatted(player.getName()),
                    holoLines.get(1).formatted(getName(),
                            respawnRunnable.minutes + " " + formatCommon((int) respawnRunnable.minutes, "MINUTES_LEFT"))
            ), getSpawnLocation());
            var old = holograms.put(player, newHolo);
            if (old != null) old.remove();
            newHolo.show(player);
        }
    }

    @Override
    public void removeHologram(@NotNull Player player) {
        var holo = holograms.remove(player);
        if (holo == null) return;
        holo.remove();
    }

    protected final class RespawnRunnable extends BukkitRunnable {

        private long minutes = getRespawnDelay() / 20 / 60;

        @Override
        public void run() {
            if (minutes <= 0) {
                this.cancel();
                spawn();
                plugin.getServer().broadcast(LegacyComponentSerializer.legacySection().deserialize(
                        plugin.getTranslation("BOSS_SPAWNED", getName())
                ));
                holograms.values().forEach(Hologram::remove);
                holograms.clear();
                respawnRunnable = null;
                return;
            }
            for (Hologram value : holograms.values()) {
                value.getLines().get(1).setTitle(
                        holoLines.get(1).formatted(getName(), minutes + " " + formatCommon((int) minutes, "MINUTES_LEFT")));
            }
            minutes--;
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected String formatCommon(int amount, String key) {
        int format;
        int mod = amount % 10;
        if (amount >= 10 && amount <= 20) {
            format = 2;
        } else if (mod == 1) {
            format = 0;
        } else if (mod >= 2 && mod <= 4) {
            format = 1;
        } else {
            format = 2;
        }
        return plugin.getTranslationList(key).get(format);
    }
}
