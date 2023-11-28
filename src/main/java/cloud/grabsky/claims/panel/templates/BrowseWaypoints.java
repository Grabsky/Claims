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
import cloud.grabsky.claims.waypoints.WaypointManager;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static cloud.grabsky.claims.util.Utilities.moveIterator;
import static cloud.grabsky.claims.util.Utilities.toChunkPosition;
import static cloud.grabsky.claims.waypoints.WaypointManager.toChunkDataKey;
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
            if (location == null) {
                System.out.println("BrowseWaypoints#render: LOCATION NOT FOUND");
                cPanel.setItem(slot, new ItemStack(Material.POISONOUS_POTATO), null);
                continue;
            }
            // ...
            final ItemBuilder icon = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_WAYPOINT).edit(meta -> {
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
                                    // Closing the panel.
                                    cPanel.close();
                                    // Teleporting...
                                    Utilities.teleport(viewer, location.add(0.0, 0.5, 0.0), PluginConfig.WAYPOINT_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                                        if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                                            // Displaying particles. NOTE: This can expose vanished players.
                                            if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS != null) {
                                                PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS.forEach(it -> {
                                                    current.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffestX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                                                });
                                            }
                                            // Playing sounds. NOTE: This can expose vanished players.
                                            if (PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_OUT != null)
                                                old.getWorld().playSound(PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_OUT, old.x(), old.y(), old.z());
                                            if (PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_IN != null)
                                                current.getWorld().playSound(PluginConfig.CLAIM_SETTINGS_TELEPORT_SOUNDS_IN, current.x(), current.y(), current.z());
                                        }
                                    });
                                    return;
                                }
                                cPanel.close();
                                Message.of(PluginLocale.UI_WAYPOINT_TELEPORT_FAILURE_NOT_EXISTENT).sendActionBar(viewer);
                            });
                            return;
                        }
                        // Just teleport otherwise... (non BLOCK source waypoints)
                        cPanel.close();
                        // Teleporting...
                        Utilities.teleport(viewer, location.add(0.0, 0.5, 0.0), PluginConfig.CLAIM_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", null);
                    }
                    // Changing name...
                    case RIGHT, SHIFT_RIGHT -> {
                        // Craeting new (or overriding previous) session.
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
                    if (location != null) Utilities.teleport(viewer, location, PluginConfig.WAYPOINT_SETTINGS_TELEPORT_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                        if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                            // Displaying particles. NOTE: This can expose vanished players.
                            if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS != null) {
                                PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS.forEach(it -> {
                                    current.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffestX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                                });
                            }
                            // Playing sounds. NOTE: This can expose vanished players.
                            if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_OUT != null)
                                old.getWorld().playSound(PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_OUT, old.x(), old.y(), old.z());
                            if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_IN != null)
                                current.getWorld().playSound(PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_IN, current.x(), current.y(), current.z());
                        }
                    });
                    // Othwerwise, sending error message to the sender.
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
            Utilities.teleport(viewer, location, PluginConfig.SPAWN_TELEPORT_DELAY, "claims.bypass.teleport_delay", (old, current) -> {
                if (AzureProvider.getAPI().getUserCache().getUser(viewer).isVanished() == false) {
                    // Displaying particles. NOTE: This can expose vanished players.
                    if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS != null) {
                        PluginConfig.WAYPOINT_SETTINGS_TELEPORT_EFFECTS.forEach(it -> {
                            current.getWorld().spawnParticle(it.getParticle(), viewer.getLocation().add(0, (viewer.getHeight() / 2), 0), it.getAmount(), it.getOffestX(), it.getOffsetY(), it.getOffsetZ(), it.getSpeed());
                        });
                    }
                    // Playing sounds. NOTE: This can expose vanished players.
                    if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_OUT != null)
                        old.getWorld().playSound(PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_OUT, old.x(), old.y(), old.z());
                    if (PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_IN != null)
                        current.getWorld().playSound(PluginConfig.WAYPOINT_SETTINGS_TELEPORT_SOUNDS_IN, current.x(), current.y(), current.z());
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

        private static final Component INVENTORY_TITLE = text("\u7000\u7108", NamedTextColor.WHITE);

        @Override
        public void accept(final @NotNull ClaimPanel cPanel) {
            cPanel.updateTitle(INVENTORY_TITLE);
            // ...
            final Player viewer = (Player) cPanel.getInventory().getViewers().get(0);
            // ...
            final ItemBuilder icon = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_DELETE_WAYPOINT).edit(meta -> {
                meta.displayName(text(waypoint.getDisplayName()).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                // ...
                final @Nullable List<Component> lore = meta.lore();
                if (lore != null)
                    meta.lore(lore.stream().map(line -> {
                        return Message.of(line)
                                .replace("[LOCATION]", (int) waypoint.getLocation().x() + ", " + (int) waypoint.getLocation().y() + ", " + (int) waypoint.getLocation().z())
                                .replace("[CREATED_ON]", DATE_FORMAT.format(new Date(waypoint.getCreatedOn())))
                                .getMessage();
                    }).toList());
            });
            // ...
            cPanel.setItem(13, icon.build(), (event) -> {
                this.destroy(event.getWhoClicked().getUniqueId(), waypoint).thenAccept(isSuccess -> {
                    cPanel.getManager().getPlugin().getBedrockScheduler().run(1L, (task) -> {
                        // Re-opening panel if access block still exists.
                        if (waypoint.getLocation().complete() == null || waypoint.getLocation().complete().equals(cPanel.getAccessBlockLocation()) == false) {
                            cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true);
                            return;
                        }
                        // Closing the panel otherwise.
                        cPanel.close();
                    });
                });
            });
            // RETURN
            cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
        }

        private CompletableFuture<Boolean> destroy(final UUID owner, final @NotNull Waypoint waypoint) {
            // Invalidating sessions and closing inventories.
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
                    if (waypoint.getLocation().equals(cPanel.getAccessBlockLocation()) == true)
                        player.closeInventory();
            });
            final Claims plugin = Claims.getInstance();
            final @Nullable Location location = waypoint.getLocation().complete();
            // ...
            if (location == null) {
                System.out.println("BrowseWaypoints#destroy: LOCATION NOT FOUND");
                return CompletableFuture.completedFuture(false);
            }
            // ...

            final NamespacedKey key = toChunkDataKey(toChunkPosition(location));
            // Trying to remove the waypoint...
            return plugin.getWaypointManager().removeWaypoints(owner, waypoint).thenCombine(location.getWorld().getChunkAtAsync(location), (isSuccess, chunk) -> {
                // Returning 'false' as soon as removal failed.
                if (isSuccess == false) return false;
                // ...
                final BlockPosition pos = toChunkPosition(location);
                // This stuff have to be scheduled onto the main thread.
                plugin.getBedrockScheduler().run(1L, (task) -> {
                    // ...
                    final Block block = chunk.getBlock(pos.blockX(), pos.blockY(), pos.blockZ());
                    // ...
                    if (block.getType() == Material.LODESTONE)
                        block.setType(Material.AIR);
                    // ...
                    location.getChunk().getPersistentDataContainer().remove(key);
                    // Displaying visual effects.
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 80, 0.25, 0.25, 0.25, 0.03);
                    location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.0F);
                    // Removing TextDisplay above the block.
                    location.getNearbyEntities(2, 2, 2).stream().filter(TextDisplay.class::isInstance).forEach(entity -> {
                        if (entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE) == true)
                            entity.remove();
                    });
                });
                return true;
            });
        }

    }

}
