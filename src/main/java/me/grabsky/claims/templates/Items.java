package me.grabsky.claims.templates;

import me.grabsky.indigo.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class Items {
    public static final ItemStack UPGRADE_CRYSTAL = new ItemBuilder(Material.AMETHYST_SHARD)
            .setName("§d§lKryształ Ulepszenia")
            .setLore("§7Potrzebny do ulepszenia terenu.")
            .addEnchantment(Enchantment.ARROW_INFINITE, 1)
            .setItemFlags(ItemFlag.HIDE_ENCHANTS)
            .build();
}
