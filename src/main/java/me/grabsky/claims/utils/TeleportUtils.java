package me.grabsky.claims.utils;

import io.papermc.lib.PaperLib;
import me.grabsky.claims.Claims;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.configuration.ClaimsLang;
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
            PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(status -> ClaimsLang.send(player, getStatusMessage(status)));
            return;
        }
        // Sending TELEPORTING message to player and running triggering delay mechanic
        ClaimsLang.send(player, ClaimsLang.TELEPORTING.replace("%cooldown%", String.valueOf(ClaimsConfig.TELEPORT_DELAY)));
        new BukkitRunnable() {
            int secondsLeft = delay;
            @Override
            public void run() {
                secondsLeft--;
                if (initialLoc.distanceSquared(player.getLocation()) > 1D) {
                    ClaimsLang.send(player, ClaimsLang.TELEPORT_CANCELLED);
                    this.cancel();
                }
                if(secondsLeft == 0) {
                    PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(status -> ClaimsLang.send(player, getStatusMessage(status)));
                    this.cancel();
                }
            }
        }.runTaskTimer(Claims.getInstance(), 0L, 20L);
    }

    private static Component getStatusMessage(boolean status) {
        if (status) {
            return ClaimsLang.TELEPORT_SUCCEED;
        }
        return ClaimsLang.TELEPORT_FAILED;
    }
}
