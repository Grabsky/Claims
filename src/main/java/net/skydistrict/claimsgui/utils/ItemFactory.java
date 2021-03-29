package net.skydistrict.claimsgui.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ItemFactory {
    public static ItemStack create(@NotNull ItemStack item, @NotNull Boolean isGlowing, @NotNull Component name, @NotNull Component... lore) {
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.displayName(name);
            meta.lore(Arrays.asList(lore));
            if (isGlowing) {
                item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 7);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack create(@NotNull ItemStack item, @NotNull Component name, @NotNull Component... lore) {
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.displayName(name);
            meta.lore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack create(@NotNull ItemStack item, @NotNull Component name) {
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
