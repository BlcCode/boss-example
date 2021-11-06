package ru.blc.example.boss.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.command.api.AbstractBaseCommand;
import ru.blc.example.boss.command.api.BaseCommand;

import java.util.Set;

public class BossHelpCommand extends AbstractBaseCommand<CommandSender> {

    public BossHelpCommand() {
        super(CommandSender.class, "help", "", "shows help", "");
    }

    @Override
    public void dispatch(@Nullable BaseCommand<? super CommandSender> parent, @NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
        assert parent != null : "parent command can not be null!";
        commandSender.sendMessage(getHelpMessage(parent, parent.getName()));
    }


    public String getHelpMessage(BaseCommand<? super CommandSender> parent, String alias) {
        StringBuilder help = new StringBuilder("Bosses commands:\n");
        final String usagePrefix = "/" + alias;
        Set.copyOf(parent.getChildren().values()).forEach(c -> appendHelpRecursive(usagePrefix, help, c));
        return help.toString();
    }

    protected void appendHelpRecursive(String usagePrefix, StringBuilder builder, BaseCommand<?> command) {
        if (command.getChildren().isEmpty()) {
            builder.append(usagePrefix).append(" ").append(command.getName());
            if (!command.getUsageNode().isEmpty()) builder.append(" ").append(command.getUsageNode());
            builder.append(" - ").append(command.getDescription()).append("\n");
        } else {
            String finalUsagePrefix = usagePrefix + " " + command.getName();
            command.getChildren().values().forEach(c -> appendHelpRecursive(finalUsagePrefix, builder, c));
        }
    }
}
