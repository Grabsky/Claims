/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.claims.panel.templates;

import cloud.grabsky.azure.api.AzureProvider;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.util.Utilities;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.Waypoint.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static cloud.grabsky.claims.util.Utilities.moveIterator;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

// TO-DO: Clean up the mess.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrowseWaypoints implements Consumer<ClaimPanel> {
    /* SINGLETON */ public static final BrowseWaypoints INSTANCE = new BrowseWaypoints();

    private List<Waypoint> waypoints;

    private static final Component INVENTORY_TITLE = translatable("ui.claims.browse_waypoints", NamedTextColor.WHITE);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static final List<Integer> UI_SLOTS = List.of(29, 30, 31, 32, 33);

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
    }

    @Override
    public void accept(final @NotNull ClaimPanel cPanel) {
        final Player viewer = (Player) cPanel.getInventory().getViewers().getFirst();
        // ...
        this.waypoints = cPanel.getManager().getPlugin().getWaypointManager().getWaypoints(viewer.getUniqueId()).stream()
                .sorted((s1, s2) -> s1.getDisplayName().compareToIgnoreCase(s2.getDisplayName()))
                .toList();
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
        renderCommonButtons(cPanel);
        // Rendering PREVIOUS PAGE button.
        if (waypointsIterator.hasPrevious() == true)
            cPanel.setItem(28, PluginItems.INTERFACE_NAVIGATION_PREVIOUS_PAGE, (event) -> render(cPanel, viewer, pageToDisplay - 1, maxOnPage));
        // Rendering waypoints.
        while (waypointsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final int slot = slotsIterator.next();
            // ...
            final Waypoint waypoint = waypointsIterator.next();
            final @Nullable Location location = waypoint.getLocation().complete();
            // ...
            final ItemBuilder icon = new ItemBuilder(location != null ? PluginItems.INTERFACE_FUNCTIONAL_ICON_WAYPOINT : PluginItems.INTERFACE_FUNCTIONAL_ICON_WAYPOINT_INVALID).edit(meta -> {
                meta.displayName(text(waypoint.getDisplayName()).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                // ...
                final @Nullable List<Component> lore = meta.lore();
                if (lore != null)
                    meta.lore(lore.stream().map(line -> {
                        return Message.of(line)
                                .replace("[LOCATION]", (int) Math.floor(waypoint.getLocation().x()) + ", " + (int) Math.floor(waypoint.getLocation().y()) + ", " + (int) Math.floor(waypoint.getLocation().z()))
                                .replace("[DIMENSION]", PluginLocale.DIMENSIONS.getOrDefault(waypoint.getLocation().world().asString(), "N/A"))
                                .replace("[CREATED_ON]", DATE_FORMAT.format(new Date(waypoint.getCreatedOn())))
                                .getMessage();
                    }).toList());
            });
            // ...
            cPanel.setItem(slot, icon.build(), (event) -> {
                switch (event.getClick()) {
                    // Teleporting...
                    case LEFT, SHIFT_LEFT -> {
                        if (location == null)
                            return;
                        // Teleport with effects... (BLOCK source waypoints)
                        if (waypoint.getSource() == Source.BLOCK) {
                            location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> {
                                // Closing the panel.
                                cPanel.close();
                                // Teleporting...
                                Utilities.teleport(viewer, location.add(0.0, 0.5, 0.0), PluginConfig.TELEPORTATION_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                                    if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                                        // Displaying particles. NOTE: This can expose vanished players.
                                        if (PluginConfig.TELEPORTATION_PARTICLES != null) {
                                            PluginConfig.TELEPORTATION_PARTICLES.forEach(it -> {
                                                current.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffsetX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                                            });
                                        }
                                        // Playing sounds. NOTE: This can expose vanished players.
                                        if (PluginConfig.TELEPORTATION_SOUNDS_OUT != null)
                                            old.getWorld().playSound(PluginConfig.TELEPORTATION_SOUNDS_OUT, old.x(), old.y(), old.z());
                                        if (PluginConfig.TELEPORTATION_SOUNDS_IN != null)
                                            current.getWorld().playSound(PluginConfig.TELEPORTATION_SOUNDS_IN, current.x(), current.y(), current.z());
                                    }
                                });
                            });
                            return;
                        }
                        // Just teleport otherwise... (non-BLOCK source waypoints)
                        cPanel.close();
                        // Teleporting...
                        Utilities.teleport(viewer, location.add(0.0, 0.5, 0.0), PluginConfig.TELEPORTATION_DELAY, "claims.bypass.teleport_delay", null);
                    }
                    // Changing name...
                    case RIGHT, SHIFT_RIGHT -> {
                        // Creating new (or overriding previous) session.
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
                    case DROP -> cPanel.applyClaimTemplate(new Confirmation(waypoint), true);
                    case SWAP_OFFHAND -> cPanel.applyClaimTemplate(new BrowseWaypointOnlinePlayers(waypoint), true);
                }
            });
        }
        // Rendering NEXT PAGE button.
        if (waypointsIterator.hasNext() == true)
            cPanel.setItem(34, PluginItems.INTERFACE_NAVIGATION_NEXT_PAGE, (event) -> render(cPanel, viewer, pageToDisplay + 1, maxOnPage));
    }

    private static void renderCommonButtons(final ClaimPanel cPanel) {
        // In case access location is a public waypoint, rendering RANDOM TELEPORT button.
        if (cPanel.getAccessBlockLocation() != null && Utilities.findFirstBlockUnder(cPanel.getAccessBlockLocation(), 5, Material.COMMAND_BLOCK) != null)
            cPanel.setItem(10, new ItemStack(PluginItems.INTERFACE_FUNCTIONAL_ICON_RANDOM_TELEPORT), (event) -> {
                final Player viewer = cPanel.getViewer();
                // Sending message to the player.
                Message.of(PluginLocale.RANDOM_TELEPORT_SEARCHING).sendActionBar(viewer);
                // Searching for safe location...
                Utilities.getSafeLocation(PluginConfig.RANDOM_TELEPORT_MIN_DISTANCE, PluginConfig.RANDOM_TELEPORT_MAX_DISTANCE).thenAccept(location -> {
                    // In case location was found, teleporting player to it.
                    if (location != null) Utilities.teleport(viewer, location, PluginConfig.TELEPORTATION_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                        if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                            // Displaying particles. NOTE: This can expose vanished players.
                            if (PluginConfig.TELEPORTATION_PARTICLES != null) {
                                PluginConfig.TELEPORTATION_PARTICLES.forEach(it -> {
                                    current.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffsetX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                                });
                            }
                            // Playing sounds. NOTE: This can expose vanished players.
                            if (PluginConfig.TELEPORTATION_SOUNDS_OUT != null)
                                old.getWorld().playSound(PluginConfig.TELEPORTATION_SOUNDS_OUT, old.x(), old.y(), old.z());
                            if (PluginConfig.TELEPORTATION_SOUNDS_IN != null)
                                current.getWorld().playSound(PluginConfig.TELEPORTATION_SOUNDS_IN, current.x(), current.y(), current.z());
                        }
                    });
                    // Otherwise, sending error message to the sender.
                    else Message.of(PluginLocale.RANDOM_TELEPORT_FAILURE_NOT_FOUND).sendActionBar(viewer);

                });
                // ...
                cPanel.close();
            });
        // Otherwise, rendering SPAWN TELEPORT button.
        else cPanel.setItem(10, new ItemStack(PluginItems.INTERFACE_FUNCTIONAL_ICON_SPAWN), (event) -> {
            final Player viewer = cPanel.getViewer();
            final Location location = AzureProvider.getAPI().getWorldManager().getSpawnPoint(PluginConfig.DEFAULT_WORLD);
            // Closing the panel.
            cPanel.close();
            // Teleporting...
            Utilities.teleport(viewer, location, PluginConfig.TELEPORTATION_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                    // Displaying particles. NOTE: This can expose vanished players.
                    if (PluginConfig.TELEPORTATION_PARTICLES != null) {
                        PluginConfig.TELEPORTATION_PARTICLES.forEach(it -> {
                            current.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffsetX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                        });
                    }
                    // Playing sounds. NOTE: This can expose vanished players.
                    if (PluginConfig.TELEPORTATION_SOUNDS_OUT != null)
                        old.getWorld().playSound(PluginConfig.TELEPORTATION_SOUNDS_OUT, old.x(), old.y(), old.z());
                    if (PluginConfig.TELEPORTATION_SOUNDS_IN != null)
                        current.getWorld().playSound(PluginConfig.TELEPORTATION_SOUNDS_IN, current.x(), current.y(), current.z());
                }
            });
        });
        // Rendering other buttons.
        cPanel.setItem(12, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_WAYPOINTS), null);
        cPanel.setItem(14, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_OWNED_CLAIMS), (event) -> cPanel.applyClaimTemplate(BrowseOwnedClaims.INSTANCE, true));
        cPanel.setItem(16, new ItemStack(PluginItems.INTERFACE_CATEGORIES_BROWSE_RELATIVE_CLAIMS), (event) -> cPanel.applyClaimTemplate(BrowseRelativeClaims.INSTANCE, true));
        // Rendering return button.
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> {
            // Returning to previous view if applicable.
            if (cPanel.getClaim() != null)
                cPanel.applyClaimTemplate(BrowseCategories.INSTANCE, true);
            // Otherwise, closing the panel.
            else cPanel.close();
        });
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Confirmation implements Consumer<ClaimPanel> {

        private final Waypoint waypoint;

        private static final Component INVENTORY_TITLE = translatable("ui.claims.confirmation", NamedTextColor.WHITE);

        @Override
        public void accept(final @NotNull ClaimPanel cPanel) {
            cPanel.updateTitle(INVENTORY_TITLE);
            // ...
            final Player viewer = (Player) cPanel.getInventory().getViewers().getFirst();
            // ...
            final ItemBuilder icon = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_DELETE_WAYPOINT).edit(meta -> {
                meta.displayName(text(waypoint.getDisplayName()).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                // ...
                final @Nullable List<Component> lore = meta.lore();
                if (lore != null)
                    meta.lore(lore.stream().map(line -> Message.of(line)
                            .replace("[LOCATION]", (int) waypoint.getLocation().x() + ", " + (int) waypoint.getLocation().y() + ", " + (int) waypoint.getLocation().z())
                            .replace("[CREATED_ON]", DATE_FORMAT.format(new Date(waypoint.getCreatedOn())))
                            .getMessage()
                    ).toList());
            });
            // ...
            cPanel.setItem(13, icon.build(), (event) -> waypoint.destroy(Claims.getInstance().getWaypointManager()).thenAcceptAsync((___) -> {
                cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true);
            }));
            // RETURN
            cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
        }

    }

}
