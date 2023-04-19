package cloud.grabsky.claims.panel.views;

import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.waypoints.Waypoint;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.Comparator.comparingLong;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class TeleportView implements Consumer<Panel> {

    private final Claims plugin;

    private List<Waypoint> waypoints;

    public static final Component INVENTORY_TITLE = text("\u7000\u7300", NamedTextColor.WHITE);
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
        // ...
        final CompletableFuture<Chunk> queue = CompletableFuture.completedFuture(null); // Initial is not processed and can be null.
        // ...
        while (waypointsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final Waypoint waypoint = waypointsIterator.next();
            final Location location = waypoint.getLocation().clone();
            // ...
            final int slot = slotsIterator.next();
            // ...
            final ItemStack loading = new ItemBuilder(PluginItems.UI_ICON_WAYPOINT_LOADING)
                    .setName(text(waypoint.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                    .build();
            // ...
            final ItemStack ready = new ItemBuilder(PluginItems.UI_ICON_WAYPOINT_READY)
                    .setName(text(waypoint.getName(), NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                    .build();
            // ...
            panel.setItem(slot, loading, null);
            // ...
            if (waypoint.getSource() == Waypoint.Source.COMMAND) {
                panel.setItem(slot, ready, (event) -> {
                    viewer.teleportAsync(location, TeleportCause.PLUGIN)
                            .thenAccept(success -> {
                                if (success == true) {
                                    // [SEND MESSAGE]
                                    // Spawning particles...
                                    spawnParticles(location.add(0.0F, 1.0F, 0.0F));
                                }
                            });
                });
                continue;
            }
            // ...
            queue.thenComposeAsync(___ -> {
                // System.out.println("Chaining " + waypoint.getName() + "...");
                return location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> {
                    // System.out.println("  LOADED (" + location.x() + ", " + location.y() + ", " + location.z() + ")");
                    if (viewer.getOpenInventory().getTopInventory().getHolder() == panel) {
                        // ...
                        final Block block = chunk.getBlock((location.getBlockX() & 0xF), location.getBlockY(), (location.getBlockZ() & 0xF));
                        // ...
                        if (block.getType() == PluginConfig.WAYPOINT_BLOCK.getType()) {
                            chunk.unload();
                            panel.setItem(slot, ready, (event) -> {
                                viewer.teleportAsync(location.add(0.0F, 0.5F, 0.0F), TeleportCause.PLUGIN)
                                        .thenAccept(success -> {
                                            if (success == true) {
                                                // [SEND MESSAGE]
                                                // Spawning particles...
                                                spawnParticles(location.add(0.0F, 1.0F, 0.0F));
                                            }
                                        });
                            });
                        }
                    }
                });
            });
        }
        // ...
        if (pageToDisplay > 1)
            panel.setItem(28, PluginItems.UI_NAVIGATION_PREVIOUS, (event) -> renderWaypoints(panel, viewer, pageToDisplay - 1, maxOnPage));
        // ...
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
                cPanel.applyTemplate(MainView.INSTANCE, true);
                return;
            }
            panel.close();
        });
    }

    private static void spawnParticles(final Location location) {
        location.getWorld().spawnParticle(Particle.END_ROD, location, 150, 0.25, 0.5, 0.25, 0.05);
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
