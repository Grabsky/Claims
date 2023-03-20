package cloud.grabsky.claims.flags.object;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FixedTime {
    MORNING(0L),
    NOON(6000L),
    EVENING(12000L),
    MIDNIGHT(18000L);

    @Getter(AccessLevel.PUBLIC)
    private final long ticks;

}
