package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.Waypoint.Source;
import io.papermc.paper.math.BlockPosition;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkPosition;
import static java.lang.String.valueOf;
import static java.util.Comparator.comparingLong;
import static net.kyori.adventure.text.Component.text;

// TO-DO: Clean up the mess.
// TO-DO: Figure out the best constructor.
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BrowseWaypoints implements Consumer<Panel> {

    private final Claims plugin;

    private List<Waypoint> waypoints;

    private static final Component INVENTORY_TITLE = text("\u7000\u7300", NamedTextColor.WHITE);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private static final List<Integer> UI_SLOTS = List.of(29, 30, 31, 32, 33);

    @Override
    public void accept(final @NotNull Panel panel) {
        final Player viewer = (Player) panel.getInventory().getViewers().get(0);
        // ...
        this.waypoints = plugin.getWaypointManager().getWaypoints(viewer.getUniqueId()).stream().sorted(comparingLong(Waypoint::getCreatedOn)).toList();
        // ...
        if (panel instanceof ClaimPanel cPanel)
            cPanel.updateClientTitle(INVENTORY_TITLE);
        // ...
        this.renderWaypoints(panel, viewer, 1, UI_SLOTS.size());
    }

    public void renderWaypoints(final @NotNull Panel panel, final Player viewer, final int pageToDisplay, final int maxOnPage) {
        panel.clear();
        // ...
        final var slotsIterator = UI_SLOTS.iterator();
        final var waypointsIterator = moveIterator(waypoints.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        // Rendering PREVIOUS PAGE button.
        if (waypointsIterator.hasPrevious() == true)
            panel.setItem(28, PluginItems.UI_NAVIGATION_PREVIOUS, (event) -> renderWaypoints(panel, viewer, pageToDisplay - 1, maxOnPage));
        // Rendering waypoints.
        while (waypointsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final Waypoint waypoint = waypointsIterator.next();
            final Location location = waypoint.getLocation().clone();
            // ...
            final int slot = slotsIterator.next();
            // ...
            final ItemBuilder icon = (waypoint.getSource() == Source.BLOCK)
                    ? new ItemBuilder(PluginItems.UI_ICON_WAYPOINT_BLOCK_SOURCE)
                    : new ItemBuilder(PluginItems.UI_ICON_WAYPOINT_COMMAND_SOURCE);
            // ...
            icon.edit(meta -> {
                final @Nullable Component name = meta.displayName();
                if (name != null)
                    meta.displayName(Message.of(name).replace("[WAYPOINY_NAME]", waypoint.getName()).replace("[NUMBER]", valueOf(waypointsIterator.nextIndex())).getMessage());
                // ...
                final @Nullable List<Component> lore = meta.lore();
                if (lore != null)
                    meta.lore(lore.stream().map(line -> Message.of(line)
                            .replace("[LOCATION]", location.blockX() + ", " + location.blockY() + ", " + location.blockZ())
                            .replace("[CREATED_ON]", DATE_FORMAT.format(new Date(waypoint.getCreatedOn())))
                            .getMessage()).toList());
            });
            // ...
            if (waypoint.getSource() == Source.COMMAND) {
                panel.setItem(slot, icon.build(), (event) -> {
                    location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> {
                        final BlockPosition position = toChunkPosition(location);
                        final NamespacedKey key = new NamespacedKey("claims", "waypoint_" + position.blockX() + "_" + position.blockY() + "_" + position.blockZ());
                        if (viewer.getUniqueId().toString().equals(chunk.getPersistentDataContainer().get(key, PersistentDataType.STRING)) == true) {
                            viewer.teleport(location.add(0.0, 0.5, 0.0), TeleportCause.PLUGIN);
                        }
                    });
                });
            }
        }
        // Rendering NEXT PAGE button.
        if (waypointsIterator.hasNext() == true)
            panel.setItem(34, PluginItems.UI_NAVIGATION_NEXT, (event) -> renderWaypoints(panel, viewer, pageToDisplay + 1, maxOnPage));
    }

    private void renderCommonButtons(final Panel panel) {
        panel.setItem(10, new ItemStack(PluginItems.UI_ICON_TELEPORT_TO_SPAWN), null);
        panel.setItem(12, new ItemStack(PluginItems.UI_ICON_BROWSE_WAYPOINTS), null);
        panel.setItem(14, new ItemStack(PluginItems.UI_ICON_BROWSE_OWNED_CLAIMS), null);
        panel.setItem(16, new ItemStack(PluginItems.UI_ICON_BROWSE_RELATIVE_CLAIMS), null);
        // RETURN
        panel.setItem(49, PluginItems.UI_NAVIGATION_RETURN, (event) -> {
            if (panel instanceof ClaimPanel cPanel) {
                cPanel.applyTemplate(BrowseCategories.INSTANCE, true);
                return;
            }
            panel.close();
        });
    }

    private static <T> ListIterator<T> moveIterator(final ListIterator<T> iterator, final int nextIndex) {
        while (iterator.nextIndex() != nextIndex) {
            if (iterator.nextIndex() < nextIndex)
                iterator.next();
            else if (iterator.nextIndex() > nextIndex)
                iterator.previous();
        }
        return iterator;
    }

}
