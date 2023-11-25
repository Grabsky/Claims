package cloud.grabsky.claims.waypoints;

import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.util.LazyLocation;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    private static final NamespacedKey DECORATION_KEY = new NamespacedKey("claims", "waypoint_decoration");

    @SuppressWarnings("deprecation") // Supperssing @Deprecated annotations introduced by adventure...
    public static void decorateBlock(final @NotNull Waypoint waypoint) {
        final @Nullable Location location = waypoint.getLocation().complete();
        // ...
        if (location == null)
            return;
        // ...
        // Playing place sound.
        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.0F, 1.0F);
        // Creating TextDisplay above placed block.
        location.getWorld().spawnEntity(location.clone().add(0F, 0.75F, 0F), EntityType.TEXT_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM, (entity) -> {
            if (entity instanceof TextDisplay display) {
                // Setting PDC to easily distinguish from other entities.
                display.getPersistentDataContainer().set(DECORATION_KEY, PersistentDataType.BYTE, (byte) 1);
                // Setting other visual properties.
                display.setText(waypoint.displayName);
                display.setBillboard(Display.Billboard.CENTER);
                display.setShadowed(true);
                display.setBackgroundColor(TRANSPARENT); // DRAFT API; NOTHING TO WORRY ABOUT
                display.setViewRange(0.2F);
            }
        });
    }

    public static void undecorateBlock(final @NotNull Waypoint waypoint) {
        final @Nullable Location location = waypoint.getLocation().complete();
        // Skipping in case world does not exist anymore.
        if (location == null)
            return;
        // Displaying visual effects.
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
        // Removing TextDisplay above the block.
        location.getNearbyEntities(2, 2, 2).stream().filter(TextDisplay.class::isInstance).forEach(entity -> {
            if (entity.getPersistentDataContainer().has(DECORATION_KEY, PersistentDataType.BYTE) == true)
                entity.remove();
        });
    }


    public static void invalidateRenameSessions(final @NotNull Waypoint waypoint) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final UUID onlineUniqueId = player.getUniqueId();
            final @Nullable Session<?> session = Session.Listener.CURRENT_EDIT_SESSIONS.getIfPresent(onlineUniqueId);
            if (session != null) {
                final @Nullable Location sessionAccessBlockLocation = session.getAssociatedPanel().getAccessBlockLocation();
                // Skipping unrelated sessions.
                if (sessionAccessBlockLocation != null && (waypoint.getLocation().equals(sessionAccessBlockLocation) == true || session.getSubject().equals(waypoint) == true)) {
                    final @Nullable Player sessionOperator = Bukkit.getPlayer(onlineUniqueId);
                    // Invalidating and clearing the title.
                    if (sessionOperator != null && sessionOperator.isOnline() == true) {
                        Session.Listener.CURRENT_EDIT_SESSIONS.invalidate(onlineUniqueId);
                        sessionOperator.clearTitle();
                    }
                }
            }
            // Closing open panels.
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel cPanel)
                if (waypoint.getLocation().complete() != null && waypoint.getLocation().complete().equals(cPanel.getAccessBlockLocation()) == true)
                    player.closeInventory();
        });
    }

}
