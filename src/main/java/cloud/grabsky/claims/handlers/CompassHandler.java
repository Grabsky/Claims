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
package cloud.grabsky.claims.handlers;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CompassHandler implements Listener {

    private final Claims plugin;
    private final HashMap<UUID, BossBar> storage;
    private @Nullable BukkitTask task = null;

    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("#,###");

    public CompassHandler(final @NotNull Claims plugin) {
        this.plugin = plugin;
        this.storage = new HashMap<>();
    }

    /**
     * Cleans-up and reloads this handler and related components.
     */
    public void reload() {
        // Clearing handlers list.
        HandlerList.unregisterAll(this);
        // Clearing current task in case it already exist.
        if (this.task != null) {
            // Cancelling existing task.
            task.cancel();
            // Removing viewers from currently stored boss bars.
            storage.values().forEach(bar -> bar.viewers().forEach(viewer -> {
                // Trying to cast to an Audience, which is very unlikely to fail.
                if (viewer instanceof Audience audience)
                    // Removing this audience from viewers.
                    bar.removeViewer(audience);
            }));
            // Clearing the map, boss bars should (hopefully) be cleaned by GC.
            storage.clear();
        }
        // Returning in case enhanced compass is disabled.
        if (PluginConfig.COMPASS_SETTINGS_ENHANCED_COMPASS == false)
            return;
        // Registering event handlers.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Scheduling the task.
        this.task = plugin.getBedrockScheduler().repeatAsync(PluginConfig.COMPASS_SETTINGS_REFRESH_RATE, PluginConfig.COMPASS_SETTINGS_REFRESH_RATE, Long.MAX_VALUE, (cycles) -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                // Getting or computing boss bar.
                final BossBar bar = storage.computeIfAbsent(player.getUniqueId(), (___) -> {
                    return BossBar.bossBar(Component.empty(), 0.0F, PluginConfig.COMPASS_SETTINGS_BOSSBAR.getColor(), PluginConfig.COMPASS_SETTINGS_BOSSBAR.getOverlay());
                });
                if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS || player.getInventory().getItemInOffHand().getType() == Material.COMPASS) {
                    final Component text = Component.text(getFormattedCoords(player));
                    // Updating the name in case different.
                    if (bar.name().equals(text) == false)
                        bar.name(text);
                    // Showing in case hidden.
                    if (bar.viewers().iterator().hasNext() == false)
                        bar.addViewer(player);
                } else if (bar.viewers().iterator().hasNext() == true)
                    bar.removeViewer(player);
            }
            // ...
            return true;
        });
    }

    @EventHandler // Clean-ups boss bar related stuff when player leaves. Apparently boss bars are untracked by the server and can be subject to memory leaks.
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final UUID uniqueId = event.getPlayer().getUniqueId();
        // Removing boss bar from the map.
        final @Nullable BossBar bar = storage.remove(uniqueId);
        // Removing player from viewers of this boss bar.
        if (bar != null)
            event.getPlayer().hideBossBar(bar);
    }

    /**
     * Formats and returns human-readable coordinates ready to be converted to a {@link Component}.
     */
    private static @NotNull String getFormattedCoords(final @NotNull Player player) {
        return PluginConfig.COMPASS_SETTINGS_BOSSBAR.getText()
                .replace("<location_x>", COORD_FORMAT.format(player.getLocation().getBlockX()))
                .replace("<location_y>", COORD_FORMAT.format(player.getLocation().getBlockY()))
                .replace("<location_z>", COORD_FORMAT.format(player.getLocation().getBlockZ()));
    }

}
