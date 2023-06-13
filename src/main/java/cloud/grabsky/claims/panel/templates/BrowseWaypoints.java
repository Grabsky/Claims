package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.util.Utilities;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.Waypoint.Source;
import cloud.grabsky.claims.waypoints.WaypointManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static cloud.grabsky.claims.util.Utilities.moveIterator;
import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkPosition;
import static java.util.Comparator.comparing;
import static net.kyori.adventure.text.Component.text;
import static org.bukkit.persistence.PersistentDataType.STRING;

// TO-DO: Clean up the mess.
@SuppressWarnings("UnstableApiUsage")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrowseWaypoints implements Consumer<ClaimPanel> {
    /* SINGLETON */ public static final BrowseWaypoints INSTANCE = new BrowseWaypoints();

    private List<Waypoint> waypoints;

    private static final Component INVENTORY_TITLE = text("\u7000\u7300", NamedTextColor.WHITE);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private static final List<Integer> UI_SLOTS = List.of(29, 30, 31, 32, 33);

    @Override
    public void accept(final @NotNull ClaimPanel cPanel) {
        final Player viewer = (Player) cPanel.getInventory().getViewers().get(0);
        // ...
        this.waypoints = cPanel.getManager().getPlugin().getWaypointManager().getWaypoints(viewer.getUniqueId()).stream().sorted(comparing(Waypoint::getDisplayName)).toList();
        // ...
        cPanel.updateTitle(INVENTORY_TITLE);
        // ...
        this.render(cPanel, viewer, 1, UI_SLOTS.size());
    }

    public void render(final @NotNull ClaimPanel cPanel, final Player viewer, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final var slotsIterator = UI_SLOTS.iterator();
        final var waypointsIterator = moveIterator(waypoints.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        // ...
        this.renderCommonButtons(cPanel);
        // Rendering PREVIOUS PAGE button.
        if (waypointsIterator.hasPrevious() == true)
            cPanel.setItem(28, PluginItems.INTERFACE_NAVIGATION_PREVIOUS_PAGE, (event) -> render(cPanel, viewer, pageToDisplay - 1, maxOnPage));
        // Getting variables.
        final ClaimManager manager = cPanel.getManager();
        final Claims plugin = manager.getPlugin();
        final @Nullable Claim claim = cPanel.getClaim();
        // Rendering waypoints.
        while (waypointsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final Waypoint waypoint = waypointsIterator.next();
            final Location location = waypoint.getLocation().clone();
            // ...
            final int slot = slotsIterator.next();
            // ...
            final ItemBuilder icon = (waypoint.getSource() == Source.BLOCK)
                    ? new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_WAYPOINT_BLOCK)
                    : new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_WAYPOINT_COMMAND);
            // ...
            icon.edit(meta -> {
                meta.displayName(text(waypoint.getDisplayName()).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                // ...
                final @Nullable List<Component> lore = meta.lore();
                if (lore != null)
                    meta.lore(lore.stream().map(line -> {
                        return Message.of(line)
                                .replace("[LOCATION]", location.blockX() + ", " + location.blockY() + ", " + location.blockZ())
                                .replace("[CREATED_ON]", DATE_FORMAT.format(new Date(waypoint.getCreatedOn())))
                                .getMessage();
                    }).toList());
            });
            // ...
            cPanel.setItem(slot, icon.build(), (event) -> {
                switch (event.getClick()) {
                    // Teleporting...
                    case LEFT, SHIFT_LEFT -> {
                        if (waypoint.getSource() == Source.BLOCK) {
                            location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> {
                                final NamespacedKey key = WaypointManager.toChunkDataKey(toChunkPosition(location));
                                if (chunk.getPersistentDataContainer().getOrDefault(key, STRING, "").equals(viewer.getUniqueId().toString()) == true) {
                                    cPanel.close();
                                    Utilities.teleport(viewer, location.add(0.0, 0.5, 0.0), PluginConfig.WAYPOINT_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS);
                                    return;
                                }
                                Message.of("Waypoint does not exist anymore.").send(viewer); // TO-DO: Replace with a configuration entry.
                            });
                            return;
                        }
                        // Just teleport otherwise...
                        cPanel.close();
                        Utilities.teleport(viewer, location.add(0.0, 0.5, 0.0), PluginConfig.WAYPOINT_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS);
                    }
                    // Changing name...
                    case RIGHT, SHIFT_RIGHT -> {
                        // Overriding previous session(s).
                        Session.Listener.CURRENT_EDIT_SESSIONS.put(event.getWhoClicked().getUniqueId(), new Session.WaypointRenameSession(waypoint, cPanel));
                        // ...
                        waypoint.setPendingRename(true);
                        // Creating a title.
                        final Title title = Title.title(
                                PluginConfig.WAYPOINT_SETTINGS_RENAME_PROMPT_TITLE,
                                PluginConfig.WAYPOINT_SETTINGS_RENAME_PROMPT_SUBTITLE,
                                Title.Times.times(
                                        Duration.ofMillis(200),
                                        Duration.ofMillis((PluginConfig.WAYPOINT_SETTINGS_RENAME_PROMPT_DURATION * 1000) - 400),
                                        Duration.ofMillis(200)
                                )
                        );
                        // Closing panel for viewer(s).
                        cPanel.close();
                        // Showing title to the player.
                        viewer.showTitle(title);
                    }
                }
            });
        }
        // Rendering NEXT PAGE button.
        if (waypointsIterator.hasNext() == true)
            cPanel.setItem(34, PluginItems.INTERFACE_NAVIGATION_NEXT_PAGE, (event) -> render(cPanel, viewer, pageToDisplay + 1, maxOnPage));
    }

    private void renderCommonButtons(final ClaimPanel cPanel) {
        cPanel.setItem(10, new ItemStack(PluginItems.INTERFACE_FUNCTIONAL_ICON_SPAWN), null);
        cPanel.setItem(12, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_WAYPOINTS), null);
        cPanel.setItem(14, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_OWNED_CLAIMS), (event) -> cPanel.applyClaimTemplate(BrowseOwnedClaims.INSTANCE, true));
        cPanel.setItem(16, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_RELATIVE_CLAIMS), (event) -> cPanel.applyClaimTemplate(BrowseRelativeClaims.INSTANCE, true));
        // RETURN
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> {
            if (cPanel.getClaim() != null) {
                cPanel.applyTemplate(BrowseCategories.INSTANCE, true);
                return;
            }
            cPanel.close();
        });
    }

}
