package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.flags.object.FixedTime;
import cloud.grabsky.claims.flags.object.FixedWeather;
import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static cloud.grabsky.bedrock.helpers.Conditions.requirePresent;
import static java.util.Arrays.asList;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public abstract class ClaimFlag<T> {

    @Getter(AccessLevel.PUBLIC)
    private final T defaultValue;

    @Getter(AccessLevel.PUBLIC)
    private final ItemStack display;

    @Getter(AccessLevel.PUBLIC)
    private final List<Option<T>> options;

    public static <T> List<Option<T>> createOptions(final List<T> options, final List<Component> displayOptions) throws IllegalArgumentException {
        if (options.size() != displayOptions.size())
            throw new IllegalArgumentException("Both options arrays must be of the same length. Expected legnth: " + options.size());
        // ...
        final List<Option<T>> result = new ArrayList<>();
        // ...
        final Iterator<T> optionsIterator = options.iterator();
        final Iterator<Component> displayOptionsIterator = displayOptions.iterator();
        // ...
        while (optionsIterator.hasNext() == true && displayOptionsIterator.hasNext() == true) {
            result.add(new Option<T>(optionsIterator.next(), displayOptionsIterator.next()));
        }
        return result;
    }

    public T next(final @Nullable T current) {
        final Iterator<Option<T>> iterator = options.iterator();
        // ...
        while (iterator.hasNext() == true) {
            if (iterator.next().getValue() == current)
                return (iterator.hasNext() == true) ? iterator.next().getValue() : options.get(0).getValue();
        }
        throw new IllegalArgumentException("Unexpected flag value: " + current);
    }

    public ItemStack getDisplay(final T value) {
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
        private final V value;

        @Getter(AccessLevel.PUBLIC)
        private final Component display;

    }

    public static final class State extends ClaimFlag<StateFlag.State> {

        public State(final StateFlag.State defaultValue, final ItemStack display, final List<Component> displayOptions) {
            super(
                    defaultValue,
                    display,
                    createOptions(asList(StateFlag.State.ALLOW, StateFlag.State.DENY), displayOptions)
            );
        }

    }

    public static final class Time extends ClaimFlag<FixedTime> {

        public Time(final FixedTime defaultValue, final ItemStack display, final List<Component> displayOptions) {
            super(
                    defaultValue,
                    display,
                    createOptions(asList(null, FixedTime.MORNING, FixedTime.NOON, FixedTime.EVENING, FixedTime.MIDNIGHT), displayOptions)
            );
        }

    }

    public static final class Weather extends ClaimFlag<FixedWeather> {

        public Weather(final FixedWeather defaultValue, final ItemStack display, final List<Component> displayOptions) {
            super(
                    defaultValue,
                    display,
                    createOptions(asList(null, FixedWeather.CLEAR, FixedWeather.RAINY), displayOptions)
            );
        }

    }

}
