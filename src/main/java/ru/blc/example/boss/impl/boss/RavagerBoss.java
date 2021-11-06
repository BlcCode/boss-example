package ru.blc.example.boss.impl.boss;

import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.BossPlugin;
import ru.blc.example.boss.api.Hologram;
import ru.blc.example.boss.api.boss.BossType;
import ru.blc.example.boss.util.ItemBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RavagerBoss extends SimpleBoss {

    private BukkitTask jumpTask = null;
    private final ItemStack axe = ItemBuilder.get(Material.IRON_AXE).build();
    private boolean secondStage = false;

    protected RavagerBoss(@NotNull BossPlugin plugin,
                          @Nullable LivingEntity entity,
                          @NotNull BossType type,
                          @NotNull Location spawnLocation,
                          @NotNull String name,
                          long respawnDelay, double maxHealth, double baseDamage,
                          @NotNull Map<@NotNull OfflinePlayer, @NotNull DamageData> damagers,
                          List<String> holoLines, Map<Player, Hologram> holograms,
                          SimpleBoss.RespawnRunnable respawnRunnable) {
        super(plugin, entity, type, spawnLocation, name, respawnDelay, maxHealth, baseDamage, damagers, holoLines, holograms, respawnRunnable);
    }

    @Override
    public void spawn() {
        super.spawn();
        assert entity != null;
        var pillager = (Pillager) entity;
        pillager.setPatrolLeader(false);
        pillager.setCanJoinRaid(false);
        var eqip = entity.getEquipment();
        if (eqip != null) {
            Arrays.stream(EquipmentSlot.values()).forEach(slot -> eqip.setDropChance(slot, 0.0F));
            eqip.setHelmet(null);
        }
        jumpTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (secondStage) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 5, 0));
                var target = pillager.getTarget();
                if (target != null && target.getType() == EntityType.PLAYER) {
                    entity.setVelocity(entity.getVelocity().add(target.getLocation().subtract(entity.getLocation()).toVector()));
                }
            }
        }, 0, 20 * 60);
    }

    @Override
    public void onDamage(@NotNull Player player, double damage) {
        super.onDamage(player, damage);
        assert entity != null : "can't be null via null entity can't get damage";
        if (entity.getHealth() - damage <= getMaxHealth() / 2) {
            if (!secondStage) {
                var eqip = entity.getEquipment();
                if (eqip != null) eqip.setItemInMainHand(axe);
                addMeleeAttackGoal();
                var pillager = (Pillager) entity;
                pillager.setChargingAttack(false);
            }
            secondStage = true;
        }
    }

    protected void addMeleeAttackGoal() { //nms work
        if (entity == null) return;
        EntityLiving nmsEntity = ((CraftLivingEntity) entity).getHandle();
        if (nmsEntity instanceof EntityCreature insentient) {
            insentient.goalSelector.addGoal(4, new PathfinderGoalMeleeAttack(insentient, 1.0D, true));
        }
    }

    @Override
    public void onKill() {
        super.onKill();
        if (jumpTask != null) {
            jumpTask.cancel();
            jumpTask = null;
        }
    }

    @Override
    public boolean canTarget(EntityType entityType) {
        return entityType != EntityType.VILLAGER;
    }
}
