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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClockHandler implements Listener {

    private final Claims plugin;
    private final HashMap<UUID, BossBar> storage;

    private @Nullable BukkitTask task = null;

    public ClockHandler(final @NotNull Claims plugin) {
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
            // Removing viewers from curretly stored boss bars.
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
        if (PluginConfig.CLOCK_SETTINGS_ENHANCED_CLOCK == false)
            return;
        // Registering event handlers.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        // Scheduling the task.
        this.task = plugin.getBedrockScheduler().repeatAsync(PluginConfig.CLOCK_SETTINGS_REFRESH_RATE, PluginConfig.CLOCK_SETTINGS_REFRESH_RATE, Long.MAX_VALUE, (cycles) -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                // Getting or computing boss bar.
                final BossBar bar = storage.computeIfAbsent(player.getUniqueId(), (___) -> {
                    return BossBar.bossBar(Component.empty(), 0.0F, PluginConfig.CLOCK_SETTINGS_BOSSBAR.getColor(), PluginConfig.CLOCK_SETTINGS_BOSSBAR.getOverlay());
                });
                if (player.getInventory().getItemInMainHand().getType() == Material.CLOCK || player.getInventory().getItemInOffHand().getType() == Material.CLOCK) {
                    final Component text = Component.text(PluginConfig.CLOCK_SETTINGS_BOSSBAR.getText().replace("<time>", WorldTimeFormatter.getFormattedTime(player.getWorld().getTime())));
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



    /*
     *
     * Player-Expansion (https://github.com/PlaceholderAPI/Player-Expansion) (modified contents of com.extendedclip.papi.expansion.player.PlayerUtil)
     * Copyright (C) 2018 Ryan McCarthy
     *
     * This program is free software: you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation, either version 3 of the License, or
     * (at your option) any later version.
     *
     * This program is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     * GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License
     * along with this program. If not, see <http://www.gnu.org/licenses/>.
     */
    public static final class WorldTimeFormatter {

        private static final int TICKS_AT_MIDNIGHT = 18_000;
        private static final int TICKS_PER_DAY = 24_000;
        private static final int TICKS_PER_HOUR = 1_000;
        private static final double TICKS_PER_MINUTE = 1_000D / 60D;

        private static @NotNull String getFormattedTime(long ticks) {
            ticks = ticks - TICKS_AT_MIDNIGHT + TICKS_PER_DAY;
            long hours = ticks / TICKS_PER_HOUR;
            ticks -= hours * TICKS_PER_HOUR;
            long mins = (long) Math.floor(ticks / TICKS_PER_MINUTE);
            if (hours >= 24)
                hours = hours - 24;
            return (hours < 10 ? "0" + hours : hours) + ":" + (mins < 10 ? "0" + mins : mins);
        }

    }

}
