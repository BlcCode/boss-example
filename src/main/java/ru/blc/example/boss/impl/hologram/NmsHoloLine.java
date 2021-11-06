package ru.blc.example.boss.impl.hologram;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.blc.example.boss.api.HoloLine;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class NmsHoloLine implements HoloLine {

    public static NmsHoloLine create(String title, Location location, NmsHoloController controller) {
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle(),
                location.getX(), location.getY(), location.getZ());
        stand.setInvisible(true);
        stand.setCustomNameVisible(true);
        NmsHoloLine line = new NmsHoloLine(stand, new HashSet<>(), controller);
        line.setTitle(title);
        return line;
    }

    @Getter
    EntityArmorStand stand;
    Set<EntityPlayer> watchers;
    NmsHoloController controller;

    @Override
    public void remove() {
        stand.die();
        controller.lines.remove(this);
        hide();
    }

    public boolean canSee(EntityPlayer player) {
        return watchers.contains(player);
    }

    @Override
    public void show(@NotNull Player player) {
        EntityPlayer nmsPlayer;
        if (watchers.add(nmsPlayer = ((CraftPlayer) player).getHandle())) {
            controller.sendSpawn(stand, nmsPlayer);
        }
    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return watchers.contains(((CraftPlayer) player).getHandle());
    }

    @Override
    public void hide() {
        Set<EntityPlayer> remove = new HashSet<>(this.watchers);
        watchers.clear();
        remove.forEach(nmsPlayer -> controller.sendDestroy(stand, nmsPlayer));
    }

    @Override
    public @NotNull String getTitle() {
        var name = stand.getCustomName();
        return name == null ? "" : name.getString();
    }

    @Override
    public void setTitle(@NotNull String title) {
        stand.setCustomName(CraftChatMessage.fromStringOrNull(title));
        watchers.forEach(nmsPlayer -> controller.updateName(stand, nmsPlayer));
    }
}
