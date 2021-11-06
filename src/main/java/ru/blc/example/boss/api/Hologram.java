package ru.blc.example.boss.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface Hologram {

    void remove();

    void show(@NotNull Player player);

    void hide();

    boolean canSee(@NotNull Player player);

    @NotNull
    @UnmodifiableView
    List<@NotNull HoloLine> getLines();
}
