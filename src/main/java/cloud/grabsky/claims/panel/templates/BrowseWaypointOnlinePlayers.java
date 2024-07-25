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
import cloud.grabsky.azure.api.user.User;
import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.bedrock.helpers.ItemBuilder;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.claims.ClaimPlayer;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.claims.waypoints.Waypoint;
import cloud.grabsky.claims.waypoints.WaypointManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.claims.util.Utilities.moveIterator;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class BrowseWaypointOnlinePlayers implements Consumer<ClaimPanel> {

    private final Waypoint waypoint;

    private List<? extends Player> onlineClaimPlayers = new ArrayList<>();

    private static final Component INVENTORY_TITLE = translatable("ui.claims.browse_waypoint_online_players", NamedTextColor.WHITE);
    private static final List<Integer> UI_SLOTS = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    @Override
    public void accept(final ClaimPanel cPanel) {
        // ...
        this.onlineClaimPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.equals(cPanel.getViewer()) == false)
                .filter(player -> cPanel.getViewer().canSee(player) == true)
                .filter(player -> cPanel.getManager().getPlugin().getWaypointManager().getWaypoints(player).size() < PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS_LIMIT || player.hasPermission("claims.bypass.ignore_waypoints_limit") == true)
                .toList();
        // Changing (client-side) title of the inventory to render custom resource-pack texture on top of it.
        cPanel.updateTitle(INVENTORY_TITLE);
        // "Rendering" the inventory contents.
        this.render(cPanel, 1, UI_SLOTS.size());
    }

    private void render(final ClaimPanel cPanel, final int pageToDisplay, final int maxOnPage) {
        cPanel.clear();
        // ...
        final WaypointManager waypointManager = cPanel.getManager().getPlugin().getWaypointManager();
        // ...
        final var onlineClaimPlayersIterator = moveIterator(onlineClaimPlayers.listIterator(), (pageToDisplay * maxOnPage) - maxOnPage);
        final var uiSlotsIterator = UI_SLOTS.iterator();
        // ...
        // Rendering PREVIOUS PAGE button.
        if (onlineClaimPlayersIterator.hasPrevious() == true)
            cPanel.setItem(18, PluginItems.INTERFACE_NAVIGATION_PREVIOUS_PAGE, (event) -> this.render(cPanel, pageToDisplay - 1, maxOnPage));
        // ...
        final Player viewer = cPanel.getViewer();
        // ...
        while (onlineClaimPlayersIterator.hasNext() == true && uiSlotsIterator.hasNext() == true) {
            final Player player = onlineClaimPlayersIterator.next();
            final User user = AzureProvider.getAPI().getUserCache().getUser(player);
            // Creating player skull icon.
            final ItemStack head = new ItemBuilder(PluginItems.INTERFACE_FUNCTIONAL_ICON_TRANSFER_WAYPOINT)
                    .setName(text(user.getName(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                    .setSkullTexture(user.getTextures())
                    .build();
            // ...
            cPanel.setItem(uiSlotsIterator.next(), head, (event) -> {
                // Making sure not to exceed the members limit.
                if (cPanel.getManager().getPlugin().getWaypointManager().getWaypoints(player).size() < PluginConfig.WAYPOINT_SETTINGS_ENHANCED_LODESTONE_BLOCKS_LIMIT || player.hasPermission("claims.bypass.ignore_waypoints_limit") == true) {
                    // Should never be null.
                    final @Nullable Location location = waypoint.getLocation().complete();
                    // Returning when location happens to be null.
                    if (location == null)
                        return;
                    // Destroying waypoint at player's position and creating new one for the player.
                    waypoint.destroy(waypointManager).thenAccept(isSuccess -> {
                        if (isSuccess == true) {
                            final Waypoint newWaypoint = Waypoint.fromBlock(player.getUniqueId(), waypoint.getDisplayName(), location);
                            // Setting block...
                            location.getWorld().getChunkAtAsync(location).thenAccept(chunk -> {
                                location.getWorld().getBlockAt(location).setType(Material.LODESTONE);
                                // Creating...
                                newWaypoint.create(waypointManager);
                                // ...
                                viewer.closeInventory();
                                // Sending success message to the viewer.
                                Message.of(PluginLocale.UI_WAYPOINT_TRANSFER_SUCCESS)
                                        .placeholder("name", newWaypoint.getDisplayName())
                                        .placeholder("player", player)
                                        .send(viewer);
                                // Sending success message to the target.
                                Message.of(PluginLocale.UI_WAYPOINT_TRANSFER_SUCCESS_TARGET)
                                        .placeholder("player", viewer)
                                        .placeholder("name", newWaypoint.getDisplayName())
                                        .send(player);
                            });
                        }
                    });
                    // Returning as we're done with everything.
                    return;
                }
                // Closing the panel.
                viewer.closeInventory();
                // Sending error message that waypoints limit has been reached by this player.
                Message.of(PluginLocale.UI_WAYPOINT_TRANSFER_FAILURE_REACHED_LIMIT).send(viewer);
            });
        }
        // Rendering NEXT PAGE button.
        if (onlineClaimPlayersIterator.hasNext() == true)
            cPanel.setItem(26, PluginItems.INTERFACE_NAVIGATION_NEXT_PAGE, (event) -> this.render(cPanel, pageToDisplay + 1, maxOnPage));
        // ...
        cPanel.setItem(49, PluginItems.INTERFACE_NAVIGATION_RETURN, (event) -> cPanel.applyClaimTemplate(BrowseWaypoints.INSTANCE, true));
    }

}
