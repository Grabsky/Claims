package net.skydistrict.claims.utils;

import io.papermc.lib.PaperLib;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.configuration.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportH {
    /** Teleport player to a location asynchronously */
    public static void teleportAsync(Player player, Location location, int delay) {
        Location initialLoc = player.getLocation();
        if (player.hasPermission("skydistrict.claims.bypass.teleportdelay")) {
            PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(status -> {
                if (status) player.sendMessage(Lang.TELEPORT_SUCCESS);
                else player.sendMessage(Lang.TELEPORT_FAILED_UNKNOWN);
            });
            return;
        }
        new BukkitRunnable() {
            int sec = 0;
            @Override
            public void run() {
                sec++;
                if (initialLoc.distanceSquared(player.getLocation()) > 1D) {
                    player.sendMessage(Lang.TELEPORT_FAILED);
                    this.cancel();
                }
                if(sec == delay) {
                    PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept(status -> {
                        if (status) player.sendMessage(Lang.TELEPORT_SUCCESS);
                        else player.sendMessage(Lang.TELEPORT_FAILED_UNKNOWN);
                        this.cancel();
                    });
                }
            }
        }.runTaskTimer(Claims.getInstance(), 0L, 20L);
    }
}
