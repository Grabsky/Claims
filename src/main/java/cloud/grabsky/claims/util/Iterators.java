package cloud.grabsky.claims.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ListIterator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Iterators {

    public static <T> ListIterator<T> moveIterator(final ListIterator<T> iterator, final int nextIndex) {
        while (iterator.nextIndex() != nextIndex) {
            if (iterator.nextIndex() < nextIndex)
                iterator.next();
            else if (iterator.nextIndex() > nextIndex)
                iterator.previous();
        }
        return iterator;
    }

}
