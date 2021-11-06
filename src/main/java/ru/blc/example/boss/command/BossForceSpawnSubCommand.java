package ru.blc.example.boss.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.example.boss.api.boss.BossManager;
import ru.blc.example.boss.api.boss.BossType;
import ru.blc.example.boss.command.api.BaseCommand;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BossForceSpawnSubCommand extends AbstractBossCommand<CommandSender> {

    private static final List<String> complete = Arrays.stream(BossType.values()).map(BossType::name).collect(Collectors.toList());

    public BossForceSpawnSubCommand(BossManager bossManager) {
        super(bossManager, CommandSender.class, "forcespawn", "", "respawns selected boss", "<boss type>", "fs");
    }

    @Override
    public void dispatch(@Nullable BaseCommand<? super CommandSender> parent, @NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage("Set boss type");
            return;
        }
        BossType type;
        try {
            type = BossType.valueOf(args[0]);
        } catch (NullPointerException exception) {
            commandSender.sendMessage("Unknown boss type " + args[0]);
            return;
        }
        getBossManager().getRegularBoss(type).spawn();
    }

    @Override
    public List<String> tabComplete(@Nullable BaseCommand<? super CommandSender> parent, @NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return complete;
    }
}
