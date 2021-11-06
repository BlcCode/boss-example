package ru.blc.example.boss.impl.hologram;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.blc.example.boss.BossPlugin;
import ru.blc.example.boss.api.HoloLine;
import ru.blc.example.boss.api.Hologram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class SimpleHologram implements Hologram {

    private static final NmsHoloController controller = new NmsHoloController(BossPlugin.getPlugin(BossPlugin.class));

    public static Hologram create(List<String> lines, Location location) {
        List<HoloLine> holoLines = new ArrayList<>();
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
            String line = lines.get(i);
            Location lineLocation = location.clone().add(0, 0.5 * (linesSize - i - 1), 0);
            holoLines.add(NmsHoloLine.create(line, lineLocation, controller));
        }
        return new SimpleHologram(holoLines);
    }

    List<HoloLine> lines;

    @Override
    public void remove() {
        lines.forEach(HoloLine::remove);
    }

    @Override
    public void show(@NotNull Player player) {
        lines.forEach(line -> line.show(player));
    }

    @Override
    public void hide() {
        lines.forEach(HoloLine::hide);
    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return lines.stream().allMatch(line -> line.canSee(player));
    }

    @Override
    public @NotNull @UnmodifiableView List<@NotNull HoloLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
