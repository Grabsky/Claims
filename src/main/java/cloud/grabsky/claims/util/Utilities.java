package cloud.grabsky.claims.util;

import cloud.grabsky.commands.exception.NumberParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ListIterator;
import java.util.function.Supplier;

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

}
