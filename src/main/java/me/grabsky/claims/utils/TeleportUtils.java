package me.grabsky.claims.utils;

import io.papermc.lib.PaperLib;
import me.grabsky.claims.Claims;
import me.grabsky.claims.configuration.Config;
import me.grabsky.claims.configuration.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportUtils {

    // Teleport player to a location asynchronously
    public static void teleportAsync(Player player, Location location, int delay) {
        final Location initialLoc = player.getLocation();
        // Skipping teleport delay if requirements are met
        if (player.hasPermission("skydisitrict.bypass.claims.teleportdelay") || delay == 0) {
            PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(status -> Lang.send(player, getStatusMessage(status)));
            return;
        }
        // Sending TELEPORTING message to player and running triggering delay mechanic
        Lang.send(player, Lang.TELEPORTING.replace("%cooldown%", String.valueOf(Config.TELEPORT_DELAY)));
        new BukkitRunnable() {
            int secondsLeft = delay;
            @Override
            public void run() {
                secondsLeft--;
                if (initialLoc.distanceSquared(player.getLocation()) > 1D) {
                    Lang.send(player, Lang.TELEPORT_CANCELLED);
                    this.cancel();
                }
                if(secondsLeft == 0) {
                    PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(status -> Lang.send(player, getStatusMessage(status)));
                    this.cancel();
                }
            }
        }.runTaskTimer(Claims.getInstance(), 0L, 20L);
    }

    private static Component getStatusMessage(boolean status) {
        if (status) {
            return Lang.TELEPORT_SUCCEED;
        }
        return Lang.TELEPORT_FAILED;
    }
}
