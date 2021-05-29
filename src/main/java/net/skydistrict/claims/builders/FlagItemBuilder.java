package net.skydistrict.claims.builders;

import com.sk89q.worldguard.protection.flags.Flag;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.interfaces.ToggleAction;
import net.skydistrict.claims.utils.ClaimsUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlagItemBuilder {
    private final ItemMeta meta;
    private final ItemStack item;
    private final Flag<?> flag;
    private final List<Object> options;
    private final List<String> formattedOptions;
    private final int size;
    private int value;
    private String[] prefix;
    private String[] suffix;

    /** Constructor; creates FlagItemBuilder object with initial values */
    public FlagItemBuilder(Material material, Flag<?> flag, Object value) {
        // Item
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        // Flag properties
        this.flag = flag;
        this.options = ClaimsUtils.getFlagOptions(flag);
        this.formattedOptions = ClaimsUtils.getFormattedFlagOptions(flag);
        this.size = options.size();
        this.value = options.indexOf(value);
    }

    /** Name of the flag displayed in GUI */
    public FlagItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    /** Sets prefix (description) above the flag options */
    public FlagItemBuilder setPrefix(String... prefix) {
        this.prefix = prefix;
        return this;
    }

    /** Sets suffix (disclaimer) under the flag options */
    public FlagItemBuilder setSuffix(String... suffix) {
        this.suffix = suffix;
        return this;
    }

    /** Updates lore of the item to highlight current flag value */
    public FlagItemBuilder updateLore() {
        final List<String> lore = new ArrayList<>(Arrays.asList(prefix));
        for (int i = 0; i < size; i++) {
            final ChatColor color = (i == value) ? ChatColor.YELLOW : ChatColor.GRAY;
            lore.add("§8› " + color + formattedOptions.get(i));
        }
        if (suffix != null) lore.addAll(Arrays.asList(suffix));
        meta.setLore(lore);
        return this;
    }

    /** Updates the flag value */
    public FlagItemBuilder toggle(ToggleAction action) {
        this.value = (value + 1 >= size) ? 0 : value + 1;
        this.updateLore();
        if (action != null) {
            action.run(options.get(value));
        }
        return this;
    }

    /** Builds the ItemStack */
    public ItemStack build() {
        if (value == -1) {
            final ConsoleLogger consoleLogger = Claims.getInstance().getConsoleLogger();
            consoleLogger.error("§c[ClaimsGUI/DEBUG] Error trying to get value for '" + flag.getName() + "' flag.");
            consoleLogger.error("§c[ClaimsGUI/DEBUG] Expected one of " + String.join(", ", options + " but found '" + value + "'."));
            return null;
        }
        item.setItemMeta(this.meta);
        return item;
    }
}
