package cloud.grabsky.claims.flags.object;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.WeatherType;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FixedWeather {
    CLEAR(WeatherType.CLEAR),
    RAINY(WeatherType.DOWNFALL);

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull WeatherType bukkit;

}
