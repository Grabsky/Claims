package cloud.grabsky.claims.panel.views;

import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.waypoints.Waypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public enum TeleportView implements Consumer<Panel> {
    INSTANCE;

    private boolean isLoading = false;

    private static final Component INVENTORY_TITLE = text("\u7000\u7302", NamedTextColor.WHITE);
    private static List<Integer> UI_SLOTS = List.of(29, 30, 31, 32, 33);

    @Override
    public void accept(final @NotNull Panel panel) {
        final ClaimPanel cPanel = (ClaimPanel) panel;
        // ...
        cPanel.updateClientTitle(INVENTORY_TITLE);
        // ...
        this.renderWaypoints(cPanel, 1, UI_SLOTS.size());
    }

    public void renderWaypoints(final @NotNull ClaimPanel cPanel, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final Player viewer = cPanel.getViewer();
        // For each added member slot
        final var slotsIterator = UI_SLOTS.iterator();
        final var waypointsIterator = moveIterator(cPanel.getManager().getWaypointManager().getWaypoints(viewer.getUniqueId()).listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        // ...
        this.isLoading = true;
        cPanel.setItem(53, new ItemBuilder(Material.STRUCTURE_VOID, 1).setCustomModelData(5).build(), null);
        // ...
        while (waypointsIterator.hasNext() == true && slotsIterator.hasNext() == true) {
            final Waypoint waypoint = waypointsIterator.next();
            final Location location = waypoint.getLocation();
            // ...
            final int slot = slotsIterator.next();
            // ...

            // ...
            if (waypoint.getSource() == Waypoint.Source.COMMAND) {
                cPanel.setItem(slot, new ItemBuilder(Material.ENDER_EYE, 1)
                        .setName("<!i><yellow><b>" + waypoint.getName())
                        .setLore("<!i><gray>Kliknij, aby się teleportować.")
                        .build(), (event) -> viewer.teleportAsync(location).thenAccept(success -> spawnParticles(location))
                );
                continue;
            }
            // ...
            cPanel.setItem(slot, new ItemBuilder(Material.ENDER_EYE, 1)
                    .setName("<!i><yellow><b>" + waypoint.getName())
                    .setLore("<!i><gray>Ładowanie...")
                    .build(), null
            );
            // ...
            location.getWorld().getChunkAtAsync(location, (Consumer<Chunk>) (chunk) -> {
                System.out.println(1);
                final Block block = chunk.getBlock((location.getBlockX() & 0xF), location.getBlockY() - 1, (location.getBlockZ() & 0xF));
                System.out.println(block);
                System.out.println(block.getType());
                if (block.getType() == PluginConfig.WAYPOINT_BLOCK.getType()) {
                    if (isLoading == true && viewer.getOpenInventory().getTopInventory().getHolder() instanceof ClaimPanel) {
                        cPanel.setItem(slot, new ItemBuilder(Material.ENDER_EYE, 1)
                                .setName("<!i><yellow><b>" + waypoint.getName())
                                .setLore("<!i><gray>Kliknij, aby się teleportować.")
                                .build(), (event) -> viewer.teleportAsync(location).thenAccept(success -> spawnParticles(location))
                        );
                    }
                }
            });
        }
        this.isLoading = false;
        // ...
        cPanel.removeItem(53);
        // ...
        if (pageToDisplay > 1)
            cPanel.setItem(28, PluginItems.UI_NAVIGATION_PREVIOUS, (event) -> renderWaypoints(cPanel, pageToDisplay - 1, maxOnPage));
        // ...
        if (waypointsIterator.hasNext() == true)
            cPanel.setItem(34, PluginItems.UI_NAVIGATION_NEXT, (event) -> renderWaypoints(cPanel, pageToDisplay + 1, maxOnPage));
        // ...
        cPanel.setItem(49, PluginItems.UI_NAVIGATION_RETURN, (event) -> cPanel.applyTemplate(MainView.INSTANCE, true));
    }

    private static void spawnParticles(final Location location) {
        location.getWorld().spawnParticle(Particle.END_ROD, location.add(0.0, 1.0, 0.0), 150, 0.25, 0.5, 0.25, 0.05);
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
