package cloud.grabsky.claims.util;

import cloud.grabsky.commands.exception.NumberParseException;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

}
