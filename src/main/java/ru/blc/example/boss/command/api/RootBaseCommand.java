package ru.blc.example.boss.command.api;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface RootBaseCommand<T extends CommandSender> extends BaseCommand<T> {

    @NotNull Command asBukkitCommand();
}
