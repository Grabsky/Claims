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
package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.flags.object.FixedTime;
import cloud.grabsky.claims.flags.object.FixedWeather;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static java.util.Arrays.asList;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract sealed class ClaimFlag<T> permits ClaimFlag.State, ClaimFlag.Time, ClaimFlag.Weather {

    @Getter(AccessLevel.PUBLIC)
    private final @Nullable T defaultValue; // May be null because it is technically a valid region flag value.

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull ItemStack display;

    @Getter(AccessLevel.PUBLIC)
    private final @NotNull List<Option<T>> options;

    public static <T> @NotNull List<Option<T>> createOptions(final @NotNull List<T> options, final @NotNull List<Component> displayOptions) throws IllegalArgumentException {
        if (options.size() != displayOptions.size())
            throw new IllegalArgumentException("Both options arrays must be of the same length. Expected length: " + options.size());
        // ...
        final List<Option<T>> result = new ArrayList<>();
        // ...
        final Iterator<T> optionsIterator = options.iterator();
        final Iterator<Component> displayOptionsIterator = displayOptions.iterator();
        // ...
        while (optionsIterator.hasNext() == true && displayOptionsIterator.hasNext() == true) {
            result.add(new Option<>(optionsIterator.next(), displayOptionsIterator.next()));
        }
        return result;
    }

    // May accept or return null because it's technically a valid region flag value.
    public @Nullable T next(final @Nullable T current) throws IllegalArgumentException {
        final Iterator<Option<T>> iterator = options.iterator();
        // ...
        while (iterator.hasNext() == true) {
            if (iterator.next().getValue() == current)
                return (iterator.hasNext() == true) ? iterator.next().getValue() : options.getFirst().getValue();
        }
        throw new IllegalArgumentException("Unexpected flag value: " + current);
    }

    // May accept null because it's technically a valid region flag value.
    public @NotNull ItemStack getDisplay(final @Nullable T value) {
        final ItemStack result = new ItemStack(display);
        result.editMeta(meta -> {
            final List<Component> lore = new ArrayList<>();
            // Applying options
            requirePresent(meta.lore(), new ArrayList<Component>()).forEach(line -> {
                if (plainText().serialize(line).equals("[OPTIONS]") == true)
                    options.forEach(option -> {
                        final Component comp = (option.getValue() != value)
                                ? PluginFlags.OPTION_PREFIX
                                        .color(NamedTextColor.DARK_GRAY)
                                        .decoration(TextDecoration.ITALIC, false)
                                        .append(option.getDisplay().color(PluginFlags.OPTION_COLOR))
                                : PluginFlags.OPTION_PREFIX
                                        .color(NamedTextColor.DARK_GRAY)
                                        .decoration(TextDecoration.ITALIC, false)
                                        .append(option.getDisplay().color(PluginFlags.OPTION_COLOR_SELECTED));
                        lore.add(comp);
                    });
                else lore.add(line);
            });
            meta.lore(lore);
        });
        return result;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Option<V> {

        @Getter(AccessLevel.PUBLIC)
        private final @Nullable V value; // May be null because it is technically a valid region flag value.

        @Getter(AccessLevel.PUBLIC)
        private final @NotNull Component display;

    }

    public static final class State extends ClaimFlag<StateFlag.State> {

        // May accept null because it's technically a valid region flag value.
        public State(final @Nullable StateFlag.State defaultValue, final @NotNull ItemStack display, final @NotNull List<Component> displayOptions) {
            super(
                    defaultValue,
                    display,
                    createOptions(asList(StateFlag.State.ALLOW, StateFlag.State.DENY), displayOptions)
            );
        }

    }

    public static final class Time extends ClaimFlag<FixedTime> {

        // May accept null because it's technically a valid region flag value.
        public Time(final @Nullable FixedTime defaultValue, final @NotNull ItemStack display, final @NotNull List<Component> displayOptions) {
            super(
                    defaultValue,
                    display,
                    createOptions(asList(null, FixedTime.MORNING, FixedTime.NOON, FixedTime.EVENING, FixedTime.MIDNIGHT), displayOptions)
            );
        }

    }

    public static final class Weather extends ClaimFlag<FixedWeather> {

        // May accept null because it's technically a valid region flag value.
        public Weather(final @Nullable FixedWeather defaultValue, final @NotNull ItemStack display, final @NotNull List<Component> displayOptions) {
            super(
                    defaultValue,
                    display,
                    createOptions(asList(null, FixedWeather.CLEAR, FixedWeather.RAINY), displayOptions)
            );
        }

    }

}
