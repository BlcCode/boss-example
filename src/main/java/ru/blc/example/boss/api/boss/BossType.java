package ru.blc.example.boss.api.boss;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Zombie;

@Getter
@AllArgsConstructor
public enum BossType {
    SUMMONER(Zombie.class),
    RAVAGER(Pillager.class),
    ;

    private final Class<? extends LivingEntity> entityType;
}
