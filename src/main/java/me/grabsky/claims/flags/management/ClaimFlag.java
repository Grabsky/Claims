package me.grabsky.claims.flags.management;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ClaimFlag {
    private final ClaimFlagProperties properties;
    private final ItemStack inventoryItem;
    private int valueIndex;

    /* Styles */

    private static final Component OPTION_PREFIX = Component.text("› ").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);
    private static final Style OPTION_DISABLED_STYLE = Style.style(NamedTextColor.GRAY);
    private static final Style OPTION_ENABLED_STYLE = Style.style(NamedTextColor.YELLOW);

    public ClaimFlag(final ClaimFlagProperties properties, final Object initialValue) {
        this.properties = properties;
        this.valueIndex = Arrays.asList(properties.getFlagOptions().getRaw()).indexOf(initialValue);
        this.inventoryItem = new ItemStack(properties.getMaterial());
        inventoryItem.editMeta((meta) -> meta.displayName(properties.getDisplayName().decoration(TextDecoration.ITALIC, false)));
    }

    public ItemStack updateItem() {
        inventoryItem.editMeta((meta) -> {
            final List<Component> lore = new ArrayList<>(properties.getPrefix());
            // Applying options
            int index = 0;
            for (final Component option : properties.getFlagOptions().getFormatted()) {
                lore.add(OPTION_PREFIX.append((index == valueIndex) ? option.style(OPTION_ENABLED_STYLE) : option.style(OPTION_DISABLED_STYLE)));
                index++;
            }
            // Applying suffix
            lore.addAll(properties.getSuffix());
            meta.lore(lore);
        });
        return inventoryItem;
    }

    public ItemStack nextOption(Consumer<Object> onNextValue) {
        this.valueIndex = (valueIndex + 1 >= properties.getFlagOptions().getRaw().length) ? 0 : valueIndex + 1;
        onNextValue.accept(properties.getFlagOptions().getRaw()[valueIndex]);
        return updateItem();
    }

}
