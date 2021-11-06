package ru.blc.example.boss;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.api.boss.Boss;
import ru.blc.example.boss.api.boss.BossManager;
import ru.blc.example.boss.api.boss.BossType;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BossListener implements Listener {
    private static final int PROGRESS_LENGTH = 25;
    private static final String baseChar = "|";

    public static Listener create(@NotNull BossPlugin plugin,
                                  @NotNull BossManager manager) {
        return new BossListener(plugin, manager);
    }

    @NotNull BossPlugin plugin;
    @NotNull BossManager manager;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            final var boss = manager.getBossByEntity(entity);
            if (boss == null) return;
            if (event instanceof EntityDamageByEntityEvent byEntityEvent) {
                Player damager = extractPlayer(byEntityEvent.getDamager());
                if (damager != null) {
                    boss.onDamage(damager, event.getFinalDamage());
                    damager.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(
                            plugin.getTranslation("BOSS_DAMAGED", buildProgressString(entity.getHealth() / boss.getMaxHealth())))
                    );
                }
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!boss.isAlive()) {
                    boss.onKill();
                }
            });
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (BossType type : BossType.values()) {
            Boss boss = manager.getRegularBoss(type);
            boss.createHologram(event.getPlayer());
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (BossType type : BossType.values()) {
            Boss boss = manager.getRegularBoss(type);
            boss.removeHologram(event.getPlayer());
        }
    }

    private @Nullable Player extractPlayer(@NotNull Entity damager) {
        Object source = null;
        if (damager.getType() == EntityType.PLAYER) source = damager;
        else if (damager.getType() == EntityType.PRIMED_TNT) source = ((TNTPrimed) damager).getSource();
        else if (damager instanceof Projectile projectile) source = projectile.getShooter();
        if (source instanceof Player) return (Player) source;
        return null;
    }

    public static String buildProgressString(double progress) {
        int filledCount = (int) (PROGRESS_LENGTH * progress);
        String filled = baseChar.repeat(filledCount);
        String empty = baseChar.repeat(PROGRESS_LENGTH - filledCount);
        //since java 9 using normally concatenation instead of StringBuilder is recommended
        return "§3" + filled + "§8" + empty;
    }
}
