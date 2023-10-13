package cloud.grabsky.claims.util;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.commands.exception.NumberParseException;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utilities {

    public static <T> ListIterator<T> moveIterator(final ListIterator<T> iterator, final int nextIndex) {
        while (iterator.nextIndex() != nextIndex) {
            if (iterator.nextIndex() < nextIndex)
                iterator.next();
            else if (iterator.nextIndex() > nextIndex)
                iterator.previous();
        }
        return iterator;
    }

    public static <T extends Number> @UnknownNullability T getNumberOrDefault(final @NotNull Supplier<T> supplier, final @Nullable T def) {
        try {
            return supplier.get();
        } catch (final NullPointerException | NumberFormatException | NumberParseException e) {
            return def;
        }
    }

    @Experimental
    @SuppressWarnings("UnstableApiUsage") // Status inherited from Paper's Position API.
    public static @NotNull Stream<BlockPosition> getAroundPosition(final @NotNull BlockPosition position, final int radius) {
        return new ArrayList<BlockPosition>((int) Math.pow((radius + 1) * 2, 3)) {{
            final int centerX = position.blockX();
            final int centerY = position.blockY();
            final int centerZ = position.blockZ();
            // ...
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                        add(Position.block(x, y, z));
                    }
                }
            }
        }}.stream();
    }

    // NOTE: Safe teleports may (and probably should) be introduced in the future. Teleport invulnerability is a temporary solution.
    public static void teleport(final @NotNull HumanEntity source, final @NotNull Location destination, final int delay, final @Nullable String bypassPermission, final @Nullable BiConsumer<Location, Location> then) {
        // Handling teleports with no (or bypassed) delay.
        if (bypassPermission != null && source.hasPermission(bypassPermission) == true) {
            final Location old = source.getLocation();
            // ...
            source.teleportAsync(destination, TeleportCause.PLUGIN).thenAccept(isSuccess -> {
                if (isSuccess == false) {
                    Message.of(PluginLocale.TELEPORT_FAILURE_UNKNOWN).sendActionBar(source);
                    return;
                }
                // Sending success message through action bar.
                Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(source);
                // Returning if no effects were provided or player is vanished. (vanish checks compatible only with Azure)
                if (then == null)
                    return;
                // Scheduling task that spawns provided effects.
                Claims.getInstance().getBedrockScheduler().run(1L, (task) -> {
                    // Making player invulnerable for next 5 seconds.
                    source.setNoDamageTicks(100);
                    // Executing provided task.
                    then.accept(old, destination);
                });
            });
            return;
        }
        // Handling delayed teleports.
        final Location sourceInitialLocation = source.getLocation();
        // Sending action bar message with delay information.
        Message.of(PluginLocale.TELEPORT_IN_PROGRESS).placeholder("delay", delay).sendActionBar(source);
        // Scheduling a repeating task every 1 second, until specified numbers of iterations is reached.
        Claims.getInstance().getBedrockScheduler().repeat(20L, 20L, (delay - 1), (cycle) -> {
            // Sending action bar message with delay information.
            Message.of(PluginLocale.TELEPORT_IN_PROGRESS).placeholder("delay", delay - cycle).sendActionBar(source);
            // Handling teleport interrupt. (moving)
            if (source.getLocation().distanceSquared(sourceInitialLocation) > 1.0) {
                Message.of(PluginLocale.TELEPORT_FAILURE_MOVED).sendActionBar(source);
                return false;
            }
            // Handling last iteration.
            if (cycle == delay) {
                final Location old = source.getLocation();
                // ...
                source.teleportAsync(destination, TeleportCause.PLUGIN).thenAccept(isSuccess -> {
                    if (isSuccess == false) {
                        Message.of(PluginLocale.TELEPORT_FAILURE_UNKNOWN).sendActionBar(source);
                        return;
                    }
                    // Sending success message through action bar.
                    Message.of(PluginLocale.TELEPORT_SUCCESS).sendActionBar(source);
                    // Returning if no effects were provided.
                    if (then == null)
                        return;
                    // Scheduling task that spawns provided effects.
                    Claims.getInstance().getBedrockScheduler().run(1L, (task) -> {
                        // Making player invulnerable for next 5 seconds.
                        source.setNoDamageTicks(100);
                        // Executing provided task.
                        then.accept(old, destination);
                    });
                });
            }
            return true;
        });

    }

    /**
     * Returns {@link BlockPosition} containing chunk position of provided {@link Position}.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull BlockPosition toChunkPosition(final Position position) {
        return Position.block((position.blockX() & 0xF), position.blockY(), (position.blockZ() & 0xF));
    }

}
