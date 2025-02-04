/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
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
