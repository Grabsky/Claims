/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.claims.configuration.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamedTextColorAdapter extends JsonAdapter<NamedTextColor> {
    public static final NamedTextColorAdapter INSTANCE = new NamedTextColorAdapter();

    @Override
    public NamedTextColor fromJson(final @NotNull JsonReader in) throws IOException {
        final String value = in.nextString();
        // ...
        return switch (value.toLowerCase()) {
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
            default -> throw new JsonDataException("Expected " + NamedTextColor.class.getName() + " but found: " + value);
        };
    }

    @Override
    public void toJson(final @NotNull JsonWriter out, final @Nullable NamedTextColor value) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED");
    }

}
