package ru.blc.example.boss.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HoloLine {

    void remove();

    void show(@NotNull Player player);

    boolean canSee(@NotNull Player player);

    void hide();

    @NotNull String getTitle();

    void setTitle(@NotNull String title);
}
