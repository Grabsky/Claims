package net.skydistrict.claimsgui.builders;

import com.sk89q.worldguard.protection.flags.Flag;
import net.skydistrict.claimsgui.interfaces.NextAction;
import net.skydistrict.claimsgui.utils.Flags;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FlagItemBuilder {
    private final ItemMeta meta;
    private final ItemStack item;
    private final List<Object> options;
    private final List<String> formattedOptions;
    private final int size;
    private int value;
    private String desc;

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

    public FlagItemBuilder setDescription(String desc) {
        this.desc = desc;
        return this;
    }

    public FlagItemBuilder updateLore() {
        List<String> lore = new ArrayList<String>();
        lore.add(desc);
        lore.add("");
        for (int i = 0; i < size; i++) {
            String color = (i == value) ? "§e" : "§7";
            lore.add("§8›§r " + color + formattedOptions.get(i));
        }
        this.meta.setLore(lore);
        return this;
    }

    public FlagItemBuilder next(NextAction action) {
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
