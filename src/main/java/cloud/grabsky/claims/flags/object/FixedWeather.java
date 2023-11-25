package cloud.grabsky.claims.flags.object;

import org.bukkit.WeatherType;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FixedWeather {
    CLEAR(WeatherType.CLEAR),
    RAINY(WeatherType.DOWNFALL);

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull WeatherType bukkit;

}
