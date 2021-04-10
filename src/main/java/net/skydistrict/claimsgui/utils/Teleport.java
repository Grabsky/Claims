package net.skydistrict.claimsgui.utils;

import io.papermc.lib.PaperLib;
import net.skydistrict.claimsgui.ClaimsGUI;
import net.skydistrict.claimsgui.configuration.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Teleport {
    public static void asyncTeleport(Player player, Location location, int delay) {
        Location initialLoc = player.getLocation();
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
        }.runTaskTimer(ClaimsGUI.getInstance(), 0L, 20L);
    }
}
