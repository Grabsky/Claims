package net.skydistrict.claimsgui.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemFactory {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemFactory(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemFactory(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemFactory setName(String name) {
        this.meta.setDisplayName(name);
        return this;
    }

    public ItemFactory setName(Component name) {
        this.meta.displayName(name);
        return this;
    }

    public ItemFactory setLore(String... lines) {
        this.meta.setLore(Arrays.asList(lines));
        return this;
    }

    public ItemFactory setCustomModelData(int value) {
        this.meta.setCustomModelData(value);
        return this;
    }

    public ItemFactory setItemFlags(ItemFlag... itemFlags) {
        this.meta.addItemFlags(itemFlags);
        return this;
    }

    public ItemFactory addEnchantment(Enchantment enchantment, int level) {
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}
