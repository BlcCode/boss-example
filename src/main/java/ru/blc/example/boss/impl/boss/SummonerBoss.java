package ru.blc.example.boss.impl.boss;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.BossPlugin;
import ru.blc.example.boss.api.Hologram;
import ru.blc.example.boss.api.boss.BossType;
import ru.blc.example.boss.util.ItemBuilder;

import java.util.*;

public class SummonerBoss extends SimpleBoss {

    private final Random RANDOM = new Random();
    private final Map<Zombie, Boolean> minions = new WeakHashMap<>();
    private BukkitTask minionsSummonTask = null;
    private BukkitTask equipClearTask = null;
    private final FixedMetadataValue minionMeta;

    private final ItemStack helmet = ItemBuilder.get(Material.LEATHER_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
    private final ItemStack chestplate = ItemBuilder.get(Material.LEATHER_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
    private final ItemStack leggings = ItemBuilder.get(Material.LEATHER_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
    private final ItemStack boots = ItemBuilder.get(Material.LEATHER_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build();
    private final ItemStack sword = ItemBuilder.get(Material.STONE_SWORD).enchant(Enchantment.DAMAGE_ALL, 1).build();

    protected SummonerBoss(@NotNull BossPlugin plugin,
                           @Nullable LivingEntity entity,
                           @NotNull BossType type,
                           @NotNull Location spawnLocation,
                           @NotNull String name,
                           long respawnDelay, double maxHealth, double baseDamage,
                           @NotNull Map<@NotNull OfflinePlayer, @NotNull DamageData> damagers,
                           List<String> holoLines, Map<Player, Hologram> holograms,
                           SimpleBoss.RespawnRunnable respawnRunnable) {
        super(plugin, entity, type, spawnLocation, name, respawnDelay, maxHealth, baseDamage, damagers, holoLines, holograms, respawnRunnable);
        minionMeta = new FixedMetadataValue(plugin, true);
    }

    @Override
    public void spawn() {
        super.spawn();
        assert entity != null;
        var eqip = entity.getEquipment();
        if (eqip != null) {
            Arrays.stream(EquipmentSlot.values()).forEach(slot -> eqip.setDropChance(slot, 0.0F));
            eqip.setHelmet(helmet);
            eqip.setChestplate(chestplate);
            eqip.setLeggings(leggings);
            eqip.setBoots(boots);
            eqip.setItemInMainHand(sword);
        }
        minionsSummonTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (int i = 0, limit = RANDOM.nextInt(3) + 1; i < limit; i++) {
                spawnMinion();
            }
        }, 0, 20 * 60);
        equipClearTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (int i = 0, limit = RANDOM.nextInt(3) + 1; i < limit; i++) {
                EntityEquipment equipment;
                if (entity != null && (equipment = entity.getEquipment()) != null) {
                    equipment.clear();
                }
            }
        }, 20 * 30);

    }

    protected void spawnMinion() {
        if (entity == null) return;
        Zombie minion = entity.getLocation().getWorld().spawn(entity.getLocation(), Zombie.class);
        minion.setPersistent(true);
        minion.setBaby();
        minion.setMetadata("summoner_boss_minion", minionMeta);
        minions.put(minion, true);
    }

    @Override
    public void onDamage(@NotNull Player player, double damage) {
        super.onDamage(player, damage);

    }

    @Override
    public void onKill() {
        super.onKill();
        minions.keySet().forEach(Zombie::remove);
        minions.clear();
        if (minionsSummonTask != null) {
            minionsSummonTask.cancel();
            minionsSummonTask = null;
        }
        if (equipClearTask != null) {
            if (!equipClearTask.isCancelled()) equipClearTask.cancel();
            equipClearTask = null;
        }
    }

    @Override
    public boolean canTarget(EntityType entityType) {
        return entityType != EntityType.VILLAGER;
    }
}
