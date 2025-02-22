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
            case "light_purple" -> NamedTextColor.LIGHT_PURPLE;
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
