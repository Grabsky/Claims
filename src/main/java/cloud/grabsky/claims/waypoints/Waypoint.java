package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.util.LazyLocation;
import cloud.grabsky.claims.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.claims.util.Utilities.toChunkPosition;
import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkDataKey;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Waypoint {

    /**
     * Creates a new {@link Waypoint} instance of {@link Source#COMMAND Source.COMMAND} source.
     */
    public static @NotNull Waypoint fromCommand(final String name, final Location location) {
        return new Waypoint(
                name,
                name,
                System.currentTimeMillis(),
                LazyLocation.fromLocation(location),
                Source.COMMAND
        );
    }

    /**
     * Creates a new {@link Waypoint} instance of {@link Source#BLOCK Source.BLOCK} source.
     */
    public static @NotNull Waypoint fromBlock(final String displayName, final Location location) {
        return new Waypoint(
                location.x() + "_" + location.y() + "_" + location.z(),
                displayName,
                System.currentTimeMillis(),
                LazyLocation.fromLocation(location),
                Source.BLOCK
        );
    }

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull String name;

    @Getter(AccessLevel.PUBLIC)
    private @NotNull String displayName;

    /**
     * Updates display name of this {@link Waypoint}.
     */
    public void setDisplayName(final @NotNull String displayName) {
        this.displayName = displayName;
    }



    @Getter(AccessLevel.PUBLIC)
    private final long createdOn;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull LazyLocation location;

    @Getter(AccessLevel.PUBLIC)
    private transient boolean isPendingRename = false;

    /**
     * Updates state of whether this {@link Waypoint} is currently under ongoing "rename session".
     *
     * @apiNote For internal use only.
     */
    @Internal
    public void setPendingRename(final boolean state) {
        this.isPendingRename = state;
    }

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull Source source;


    /**
     * Represents source context of how {@link Waypoint} has been created.
     */
    public enum Source {
        COMMAND, BLOCK
    }


    private static final Color TRANSPARENT = Color.fromARGB(0, 0, 0, 0);

    // NOTE: Untested, some methods may be triggered asynchronously when they're not supposed to...
    public CompletableFuture<Void> create(final @NotNull WaypointManager manager, final @NotNull UUID uniqueId) {
        return manager.createWaypoint(uniqueId, this).thenCompose(isSuccess -> {
            // Completing in case removal has failed.
            if (isSuccess == false)
                return CompletableFuture.completedFuture(null);
            // Getting the Location object, this will be null in case associated world does not exist.
            final @Nullable Location location =  this.getLocation().complete();
            // Completing in case location is null.
            if (location == null)
                return CompletableFuture.completedFuture(null);
            // Completing in case waypoint was not created by placing a block.
            if (this.getSource() != Source.BLOCK)
                return CompletableFuture.completedFuture(null);
            // Decoragint the block...
            return location.getWorld().getChunkAtAsync(location).thenApply(chunk -> {
                // Tagging the chunk...
                chunk.getPersistentDataContainer().set(toChunkDataKey(toChunkPosition(location)), PersistentDataType.STRING, uniqueId.toString());
                // Playing effects...
                location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
                // Creating TextDisplay above placed block.
                location.getWorld().spawnEntity(location.clone().add(0F, 0.75F, 0F), EntityType.TEXT_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM, (entity) -> {
                    if (entity instanceof TextDisplay display) {
                        // Setting PDC to easily distinguish from other entities.
                        display.getPersistentDataContainer().set(Claims.Key.WAYPOINT_DECORATION, PersistentDataType.BYTE, (byte) 1);
                        // Setting other visual properties.
                        display.setText(this.displayName);
                        display.setBillboard(Display.Billboard.CENTER);
                        display.setShadowed(true);
                        display.setBackgroundColor(TRANSPARENT);
                        display.setViewRange(0.2F);
                    }
                });
                // Returning null, just to satisfy return type of thenApply(...) method.
                return null;
            });
        });
    }

    // NOTE: Untested, some methods may be triggered asynchronously when they're not supposed to...
    public CompletableFuture<Void> destroy(final @NotNull WaypointManager manager, final @NotNull UUID uniqueId) {
        return manager.removeWaypoints(uniqueId, this).thenCompose(isSuccess -> {
            // Completing in case removal has failed.
            if (isSuccess == false)
                return CompletableFuture.completedFuture(null);
            // Getting the Location object, this will be null in case associated world does not exist.
            final @Nullable Location location =  this.getLocation().complete();
            // Completing in case location is null.
            if (location == null)
                return CompletableFuture.completedFuture(null);
            // Invalidating sessions and closing open panels.
            manager.getPlugin().getServer().getOnlinePlayers().forEach(currPlayer -> {
                final UUID currUniqueId = currPlayer.getUniqueId();
                // Getting active session of the current player. this will be null in case no session is currently active.
                final @Nullable Session<?> session = Session.Listener.CURRENT_EDIT_SESSIONS.getIfPresent(currUniqueId);
                // ...
                if (session != null) {
                    final @Nullable Location sessionAccessBlockLocation = session.getAssociatedPanel().getAccessBlockLocation();
                    // Skipping unrelated sessions.
                    if (sessionAccessBlockLocation != null && (this.getLocation().equals(sessionAccessBlockLocation) == true || session.getSubject().equals(this) == true)) {
                        final @Nullable Player sessionOperator = Bukkit.getPlayer(currUniqueId);
                        // Invalidating and clearing the title.
                        if (sessionOperator != null && sessionOperator.isOnline() == true) {
                            Session.Listener.CURRENT_EDIT_SESSIONS.invalidate(currPlayer);
                            sessionOperator.clearTitle();
                        }
                    }
                }
                // Closing open panels.
                if (currPlayer.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel cPanel) {
                    // Getting the access Location object, this will be null in case of trigger by command.
                    final @Nullable Location accessLocation = cPanel.getAccessBlockLocation();
                    // Getting the Location object, this will be null in case associated world does not exist.
                    final @Nullable Location waypointLocation = this.getLocation().complete();
                    // Closing inventory in case both locations are the same.
                    if (Utilities.equalsNonNull(accessLocation, waypointLocation) == true)
                        currPlayer.closeInventory();
                }
            });
            // Completing in case waypoint was not created by placing a block.
            if (this.getSource() != Source.BLOCK)
                return CompletableFuture.completedFuture(null);
            // Undecorating the block...
            return location.getWorld().getChunkAtAsync(location).thenApply(chunk -> {
                // Untagging the chunk...
                chunk.getPersistentDataContainer().remove(toChunkDataKey(toChunkPosition(location)));
                // Playing effects...
                location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
                location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                // Removing TextDisplay above the block.
                location.getNearbyEntities(2, 2, 2).stream().filter(TextDisplay.class::isInstance).forEach(entity -> {
                    if (entity.getPersistentDataContainer().has(Claims.Key.WAYPOINT_DECORATION, PersistentDataType.BYTE) == true)
                        entity.remove();
                });
                // Returning null, just to satisfy return type of thenApply(...) method.
                return null;
            });
        });
    }

}
