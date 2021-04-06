package net.skydistrict.claimsgui.builders;

import com.sk89q.worldguard.protection.flags.Flag;
import net.skydistrict.claimsgui.interfaces.ToggleAction;
import net.skydistrict.claimsgui.utils.Flags;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

// TO-DO: NPE handling if flag is not set
// TO-DO: Clean-up and comments
public class FlagItemBuilder {
    private final ItemMeta meta;
    private final ItemStack item;
    private final List<Object> options;
    private final List<String> formattedOptions;
    private final int size;
    private int value;
    private String prefix;
    private String suffix;

    public FlagItemBuilder(Material material, Flag<?> flag, Object value) {
        // Item
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        // Flag properties
        this.options = Flags.getOptions(flag);
        this.formattedOptions = Flags.getFormattedOptions(flag);
        this.size = options.size();
        this.value = options.indexOf(value);

    }

    public FlagItemBuilder setName(String name) {
        this.meta.setDisplayName(name);
        return this;
    }

    public FlagItemBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public FlagItemBuilder setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public FlagItemBuilder updateLore() {
        List<String> lore = new ArrayList<String>();
        lore.add(prefix);
        lore.add("");
        for (int i = 0; i < size; i++) {
            String color = (i == value) ? "§e" : "§7";
            lore.add("§8›§r " + color + formattedOptions.get(i));
        }
        if (suffix != null) {
            lore.add("");
            lore.add(suffix);
        }
        this.meta.setLore(lore);
        return this;
    }

    public FlagItemBuilder toggle(ToggleAction action) {
        this.value = (value + 1 >= size) ? 0 : value + 1;
        this.updateLore();
        if (action != null) {
            action.run(this.options.get(this.value));
        }
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return item;
    }
}
