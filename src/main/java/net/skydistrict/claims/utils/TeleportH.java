package net.skydistrict.claims.utils;

import io.papermc.lib.PaperLib;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.configuration.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportH {

    // Teleport player to a location asynchronously
    public static void teleportAsync(Player player, Location location, int delay) {
        final Location initialLoc = player.getLocation();
        if (delay == 0) {
            PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(status -> {
                if (status) {
                    Lang.send(player, Lang.TELEPORT_SUCCESS);
                } else {
                    Lang.send(player, Lang.TELEPORT_FAIL_UNKNOWN);
                }
            });
            return;
        }
        new BukkitRunnable() {
            int secondsLeft = delay;
            @Override
            public void run() {
                secondsLeft--;
                if (initialLoc.distanceSquared(player.getLocation()) > 1D) {
                    Lang.send(player, Lang.TELEPORT_FAIL);
                    this.cancel();
                }
                if(secondsLeft == 0) {
                    PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(status -> {
                        if (status) {
                            Lang.send(player, Lang.TELEPORT_SUCCESS);
                        } else {
                            Lang.send(player, Lang.TELEPORT_FAIL_UNKNOWN);
                        }
                    });
                    this.cancel();
                }
            }
        }.runTaskTimer(Claims.getInstance(), 0L, 20L);
    }
}
