package ru.blc.example.boss.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.command.api.BaseCommand;
import ru.blc.example.boss.command.api.RootBaseCommand;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class BossRootCommand implements RootBaseCommand<CommandSender> {

    @SafeVarargs
    public static RootBaseCommand<CommandSender> create(String name, String description, String permission, Set<String> aliases,
                                                        BaseCommand<CommandSender>... children) {
        Map<String, BaseCommand<CommandSender>> childrenMap = new HashMap<>();
        for (BaseCommand<CommandSender> child : children) {
            childrenMap.put(child.getName().toLowerCase(Locale.ROOT), child);
            for (String alias : child.getAliases()) {
                childrenMap.put(alias.toLowerCase(Locale.ROOT), child);
            }
        }
        return new BossRootCommand(CommandSender.class, childrenMap, permission, description, name, aliases);
    }

    Class<CommandSender> requiredSenderClass;
    Map<String, BaseCommand<CommandSender>> children;
    String permission;
    String description;
    String name;
    Set<String> aliases;
    Command bukkit;

    private BossRootCommand(Class<CommandSender> requiredSenderClass, Map<String, BaseCommand<CommandSender>> children,
                            String permission, String description, String name, Set<String> aliases) {
        this.requiredSenderClass = requiredSenderClass;
        this.children = children;
        this.permission = permission;
        this.description = description;
        this.name = name;
        this.aliases = Collections.unmodifiableSet(aliases);
        this.bukkit = new BukkitCommand(name, new ArrayList<>(aliases));
    }

    @Override
    public @NotNull String getUsageNode() {
        return getName();
    }

    @Override
    public void dispatch(@Nullable BaseCommand<? super CommandSender> parent, @NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 0) {
            String commandAlias = args[0];
            BaseCommand<CommandSender> child = children.get(commandAlias.toLowerCase(Locale.ROOT));
            if (child != null) {
                if (!child.getRequiredSenderClass().isAssignableFrom(commandSender.getClass())) {
                    commandSender.sendMessage("Not for you");
                    return;
                }
                if (!child.getPermission().isEmpty() && !commandSender.hasPermission(child.getPermission())) {
                    commandSender.sendMessage(Bukkit.getPermissionMessage());
                    return;
                }
                child.dispatch(this, commandSender, commandAlias, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }
        if (args.length > 0) {
            commandSender.sendMessage("Unknown command");
            return;
        }
        dispatch(parent, commandSender, alias, new String[]{"help"});
    }

    @Override
    public List<String> tabComplete(@Nullable BaseCommand<? super CommandSender> parent, @NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 0) {
            String commandAlias = args[0];
            BaseCommand<CommandSender> child = children.get(commandAlias.toLowerCase(Locale.ROOT));
            if (child != null) {
                if (!child.getRequiredSenderClass().isAssignableFrom(commandSender.getClass())) {
                    return Collections.emptyList();
                }
                if (!child.getPermission().isEmpty() && !commandSender.hasPermission(child.getPermission())) {
                    return Collections.emptyList();
                }
                return child.tabComplete(this, commandSender, commandAlias, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        { //
            List<String> complete = new ArrayList<>();
            for (BaseCommand<CommandSender> child : children.values()) {
                if (!child.getRequiredSenderClass().isAssignableFrom(commandSender.getClass())) {
                    continue;
                }
                if (!child.getPermission().isEmpty() && !commandSender.hasPermission(child.getPermission())) {
                    continue;
                }
                complete.add(child.getName());
            }
            return complete;
        }
    }

    @Override
    public @NotNull Command asBukkitCommand() {
        return bukkit;
    }

    private class BukkitCommand extends Command {

        protected BukkitCommand(@NotNull String name, @NotNull List<String> aliases) {
            super(name, BossRootCommand.this.getDescription(), BossRootCommand.this.getUsageNode(), aliases);
            setPermission(BossRootCommand.this.getPermission());
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            if (getPermission() != null && !sender.hasPermission(getPermission())) {
                sender.sendMessage(Bukkit.getPermissionMessage());
                return true;
            }
            if (!BossRootCommand.this.getRequiredSenderClass().isAssignableFrom(sender.getClass())) {
                sender.sendMessage("Not for you");
                return true;
            }
            BossRootCommand.this.dispatch(null, sender, commandLabel, args);
            return true;
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            if (!BossRootCommand.this.getRequiredSenderClass().isAssignableFrom(sender.getClass())) {
                return Collections.emptyList();
            }
            return BossRootCommand.this.tabComplete(null, sender, alias, args);
        }
    }
}
