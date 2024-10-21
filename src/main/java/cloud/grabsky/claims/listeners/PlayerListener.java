package cloud.grabsky.claims.listeners;

import cloud.grabsky.claims.Claims;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class PlayerListener implements Listener {

    private final @NotNull Claims plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerQuitEvent event) {
        // Getting UUID of the player associated with this event.
        final UUID player = event.getPlayer().getUniqueId();
        // Clearing border entities, as these are no longer visible.
        plugin.getClaimManager().getClaimPlayer(player).getBorderEntities().clear();
    }

}
