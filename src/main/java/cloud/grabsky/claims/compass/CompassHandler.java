package cloud.grabsky.claims.compass;

import cloud.grabsky.bedrock.components.ComponentBuilder;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.eclipse.sisu.Nullable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class CompassHandler implements Listener {

    private final Claims plugin;

    private final HashMap<UUID, BossBar> storage = new HashMap<>();

    private @Nullable BukkitTask task = null;

    private static final DecimalFormat COORD_FORMAT = new DecimalFormat("#,###");

    public void reload() {
        // Clearing current task in case it already exist.
        if (this.task != null) {
            // Cancelling existing task.
            task.cancel();
            // Removing viewers from curretly stored boss bars.
            storage.values().forEach(bar -> {
                bar.viewers().forEach(viewer -> {
                    bar.removeViewer((Audience) viewer);
                });
            });
            // Clearing the map, boss bars should (hopefully) be cleaned by GC.
            storage.clear();
        }
        // Returning in case enhanced compass is disabled.
        if (PluginConfig.COMPASS_SETTINGS_ENHANCED_COMPASS == false)
            return;
        // Scheduling the task.
        this.task = plugin.getBedrockScheduler().repeatAsync(PluginConfig.COMPASS_SETTINGS_REFRESH_RATE, PluginConfig.COMPASS_SETTINGS_REFRESH_RATE, Long.MAX_VALUE, (cycles) -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                // Getting or computing boss bar.
                final BossBar bar = storage.computeIfAbsent(player.getUniqueId(), (___) -> {
                    return BossBar.bossBar(Component.empty(), 0.0F, PluginConfig.COMPASS_SETTINGS_BOSSBAR.getColor(), PluginConfig.COMPASS_SETTINGS_BOSSBAR.getOverlay()).addViewer(player);
                });
                // Checking if player is currently holding a compass.
                final Component text = (player.getInventory().getItemInMainHand().getType() == Material.COMPASS || player.getInventory().getItemInOffHand().getType() == Material.COMPASS)
                        ? Component.text(PluginConfig.COMPASS_SETTINGS_BOSSBAR.getText()
                                .replace("<location_x>", COORD_FORMAT.format(player.getLocation().getBlockX()))
                                .replace("<location_y>", COORD_FORMAT.format(player.getLocation().getBlockY()))
                                .replace("<location_z>", COORD_FORMAT.format(player.getLocation().getBlockZ()))
                        ) : ComponentBuilder.EMPTY;
                // Updating the name of the boss bar.
                if (bar.name().equals(text) == false)
                    bar.name(text);
            }
            // ...
            return true;
        });
    }

}
