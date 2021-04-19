package net.skydistrict.claims.utils;

import io.papermc.lib.PaperLib;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.configuration.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportH {
    public static void asyncTeleport(Player player, Location location, int delay) {
        Location initialLoc = player.getLocation();
        if (player.hasPermission("skydistrict.claims.bypass.teleportdelay")) {
            PaperLib.getChunkAtAsync(location).thenAccept(chunk -> {
                player.teleport(location);
                player.sendMessage(Lang.TELEPORT_SUCCESS);
            });
        } else {
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
                        PaperLib.getChunkAtAsync(location).thenAccept(chunk -> {
                            player.teleport(location);
                            player.sendMessage(Lang.TELEPORT_SUCCESS);
                            this.cancel();
                        });
                    }
                }
            }.runTaskTimer(Claims.getInstance(), 0L, 20L);
        }
    }
}
