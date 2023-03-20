package cloud.grabsky.claims.configuration.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamedTextColorAdapter extends JsonAdapter<NamedTextColor> {
    public static final NamedTextColorAdapter INSTANCE = new NamedTextColorAdapter();

    @Override
    public @Nullable NamedTextColor fromJson(final @NotNull JsonReader in) throws IOException {
        final String color = in.nextString();
        // ...
        return switch (color.toLowerCase()) {
            case "aqua" -> NamedTextColor.AQUA;
            case "black" -> NamedTextColor.BLACK;
            case "blue" -> NamedTextColor.BLUE;
            case "dark_aqua" -> NamedTextColor.DARK_AQUA;
            case "dark_blue" -> NamedTextColor.DARK_BLUE;
            case "dark_gray" -> NamedTextColor.DARK_GRAY;
            case "dark_green" -> NamedTextColor.DARK_GREEN;
            case "dark_purple" -> NamedTextColor.DARK_PURPLE;
            case "dark_red" -> NamedTextColor.DARK_RED;
            case "gold" -> NamedTextColor.GOLD;
            case "gray" -> NamedTextColor.GRAY;
            case "green" -> NamedTextColor.GREEN;
            case "light_pruple" -> NamedTextColor.LIGHT_PURPLE;
            case "red" -> NamedTextColor.RED;
            case "white" -> NamedTextColor.WHITE;
            case "yellow" -> NamedTextColor.YELLOW;
            default -> throw new JsonDataException("...");
        };
    }

    @Override
    public void toJson(final @NotNull JsonWriter out, final @Nullable NamedTextColor value) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED");
    }

}
