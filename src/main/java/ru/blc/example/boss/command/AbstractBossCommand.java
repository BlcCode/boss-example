package ru.blc.example.boss.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import ru.blc.example.boss.api.boss.BossManager;
import ru.blc.example.boss.command.api.AbstractBaseCommand;

@Getter
public abstract class AbstractBossCommand<T extends CommandSender> extends AbstractBaseCommand<T> {
    private final BossManager bossManager;

    public AbstractBossCommand(BossManager bossManager, Class<T> requiredSenderClass, String name, String permission, String description, String usageNode, String... aliases) {
        super(requiredSenderClass, name, permission, description, usageNode, aliases);
        this.bossManager = bossManager;
    }
}
