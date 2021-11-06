package ru.blc.example.boss.command.api;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BaseCommand<T extends CommandSender> {

    @NotNull Class<T> getRequiredSenderClass();

    @NotNull @Unmodifiable Map<String, BaseCommand<T>> getChildren();

    @NotNull String getPermission();

    @NotNull String getDescription();

    @NotNull String getUsageNode();

    @NotNull String getName();

    @NotNull @Unmodifiable Set<String> getAliases();

    void dispatch(@Nullable BaseCommand<? super T> parent, @NotNull T commandSender, @NotNull String alias, @NotNull String[] args);

    List<String> tabComplete(@Nullable BaseCommand<? super T> parent, @NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args);
}
