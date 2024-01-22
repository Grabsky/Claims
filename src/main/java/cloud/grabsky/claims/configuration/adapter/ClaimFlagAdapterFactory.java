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

import cloud.grabsky.claims.claims.ClaimFlag;
import cloud.grabsky.claims.flags.object.FixedTime;
import cloud.grabsky.claims.flags.object.FixedWeather;
import cloud.grabsky.configuration.util.LazyInit;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonReader.Token;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import static com.squareup.moshi.Types.getRawType;
import static com.squareup.moshi.Types.newParameterizedType;
import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClaimFlagAdapterFactory implements JsonAdapter.Factory {
    public static final ClaimFlagAdapterFactory INSTANCE = new ClaimFlagAdapterFactory();

    @Override
    public @Nullable JsonAdapter<ClaimFlag<?>> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
        if (ClaimFlag.class.isAssignableFrom(getRawType(type)) == false)
            return null;
        // ...
        final Type generic0 = ((ParameterizedType) type).getActualTypeArguments()[0];
        // ...
        final var adapter0 = moshi.adapter(generic0);
        final var adapter1 = moshi.adapter(newParameterizedType(List.class, Component.class));
        final var adapter2 = moshi.adapter(ItemStack.class);
        // ...
        return new JsonAdapter<>() {

            @Override @SuppressWarnings("unchecked")
            public ClaimFlag<?> fromJson(final @NotNull JsonReader in) throws IOException {
                in.beginObject();
                // ...
                final ClaimFlagInit init = new ClaimFlagInit(getRawType(generic0));
                // ...
                while (in.hasNext() == true) {
                    final String nextName = in.nextName();
                    // ...
                    switch (nextName.toLowerCase()) {
                        case "default_value" -> {
                            if (in.peek() == Token.STRING && in.peekJson().nextString().equals("NONE") == true) {
                                init.defaultValue = null;
                                in.skipValue();
                            } else {
                                init.defaultValue = adapter0.fromJson(in);
                            }
                        }
                        case "ui_display_options" -> init.displayOptions = (List<Component>) adapter1.fromJson(in);
                        case "ui_display" -> init.display = adapter2.fromJson(in);
                        default -> throw new JsonDataException("Unexpected field: " + nextName);
                    }
                }
                in.endObject();
                // ...
                return init.init();
            }

            @Override
            public void toJson(final @NotNull JsonWriter out, final @Nullable ClaimFlag<?> value) {
                throw new UnsupportedOperationException("NOT IMPLEMENTED");
            }

        };
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ClaimFlagInit implements LazyInit<ClaimFlag<?>> {

        private final Class<?> type;

        private @Nullable Object defaultValue;
        private @Nullable ItemStack display;
        private @Nullable List<Component> displayOptions;

        @Override
        public ClaimFlag<?> init() throws IllegalStateException {
            // StateFlag.State
            if (StateFlag.State.class.isAssignableFrom(type) == true)
                return new ClaimFlag.State(
                        (StateFlag.State) defaultValue,
                        requireNonNull(display),
                        requireNonNull(displayOptions)
                );
            // FixedTime
            else if (FixedTime.class.isAssignableFrom(type) == true)
                return new ClaimFlag.Time(
                        (FixedTime) defaultValue,
                        requireNonNull(display),
                        requireNonNull(displayOptions)
                );
            // FixedWeather
            else if (FixedWeather.class.isAssignableFrom(type) == true)
                return new ClaimFlag.Weather(
                        (FixedWeather) defaultValue,
                        requireNonNull(display),
                        requireNonNull(displayOptions)
                );
            throw new IllegalArgumentException("Flag of type " + type.getName() + " is not supported.");
        }

    }

}
