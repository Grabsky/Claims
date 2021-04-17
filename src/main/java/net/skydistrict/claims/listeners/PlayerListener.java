package net.skydistrict.claims.listeners;

import com.sk89q.worldguard.protection.managers.RegionManager;
import net.skydistrict.claims.Claims;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final RegionManager regionManger;

    public PlayerListener(Claims instance) {
        this.regionManger = instance.getRegionManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    }

}
