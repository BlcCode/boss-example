package ru.blc.example.boss.command.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Getter
public abstract class AbstractBaseCommand<T extends CommandSender> implements BaseCommand<T> {
    Class<T> requiredSenderClass;
    String name;
    String permission;
    String description;
    String usageNode;
    Set<String> aliases;

    public AbstractBaseCommand(Class<T> requiredSenderClass, String name, String permission,
                               String description, String usageNode, String... aliases) {
        this.requiredSenderClass = requiredSenderClass;
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usageNode = usageNode;
        this.aliases = Set.of(aliases);
    }

    @Override
    public @NotNull @Unmodifiable Map<String, BaseCommand<T>> getChildren() {
        return Collections.emptyMap();
    }

    @Override
    public List<String> tabComplete(@Nullable BaseCommand<? super T> parent, @NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
