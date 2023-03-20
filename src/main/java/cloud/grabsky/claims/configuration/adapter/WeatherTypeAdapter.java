package cloud.grabsky.claims.configuration.adapter;

import com.sk89q.worldedit.world.weather.WeatherType;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WeatherTypeAdapter extends JsonAdapter<WeatherType> {
    public static final WeatherTypeAdapter INSTANCE = new WeatherTypeAdapter();

    @Override
    public @Nullable WeatherType fromJson(final @NotNull JsonReader in) throws IOException {
        final String value = in.nextString().toLowerCase();
        // ...
        final WeatherType weather = WeatherType.REGISTRY.get(value);
        // ...
        if (weather != null)
            return weather;
        // ...
        throw new JsonDataException("Expected one of " + WeatherType.REGISTRY.values() + " at " + in.getPath() + " but found: " + value);
    }

    @Override
    public void toJson(final @NotNull JsonWriter out, final @Nullable WeatherType value) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED");
    }

}
