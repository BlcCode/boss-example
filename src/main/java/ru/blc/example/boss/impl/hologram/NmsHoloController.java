package ru.blc.example.boss.impl.hologram;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class NmsHoloController implements Listener {

    public NmsHoloController(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    protected final Set<NmsHoloLine> lines = new HashSet<>();

    public void sendSpawn(@NotNull EntityArmorStand entity, @NotNull EntityPlayer player) {
        sendPacket(entity.P(), player);
        //entity spawn packet does not contains entity metadata, so send this
        if (!entity.getDataWatcher().d()) {
            sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true), player);
        }
    }

    public void sendDestroy(@NotNull EntityArmorStand entity, @NotNull EntityPlayer player) {
        sendPacket(new PacketPlayOutEntityDestroy(entity.getId()), player);
    }

    public void update(@NotNull EntityArmorStand entity, @NotNull EntityPlayer player) {
        DataWatcher datawatcher = entity.getDataWatcher();
        if (datawatcher.a()) {
            this.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), datawatcher, false), player);
        }
    }

    public void updateName(@NotNull EntityArmorStand entity, @NotNull EntityPlayer player) {
        update(entity, player);
    }

    public boolean canSee(EntityArmorStand entity, EntityPlayer player) {
        return entity.getWorld() == player.getWorld();
    }

    private void sendPacket(@NotNull Packet<?> packet, @NotNull EntityPlayer player) {
        player.playerConnection.sendPacket(packet);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        for (NmsHoloLine line : lines) {
            if (line.canSee(event.getPlayer())) {
                final var nmsPlayer = ((CraftPlayer) event.getPlayer()).getHandle();
                EntityArmorStand nmsStand;
                if ((nmsStand = line.getStand()).getWorld() == ((CraftWorld) event.getFrom()).getHandle()) {
                    sendDestroy(nmsStand, nmsPlayer);
                    continue;
                }
                if (nmsStand.getWorld() == nmsPlayer.getWorld()) {
                    sendSpawn(nmsStand, nmsPlayer);
                }
            }
        }

    }
}
