package me.grabsky.claims.templates;

import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.ClaimLevel;
import me.grabsky.claims.utils.ClaimsUtils;
import me.grabsky.indigo.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Items {
    public static ItemStack getClaimBlock(int level) {
        final ClaimLevel claimLevel = ClaimsUtils.getClaimLevel(level);
        final Material material = claimLevel.getBlockMaterial();
        final ItemBuilder builder = new ItemBuilder(material)
                .setName(claimLevel.getColorCode() + "§lTeren")
                .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze " + claimLevel.getSize() + " §7bloków.");
        builder.getPersistentDataContainer().set(Claims.claimBlockLevel, PersistentDataType.INTEGER, level);
        return builder.build();
    }

    public static final ItemStack UPGRADE_CRYSTAL = new ItemBuilder(Material.AMETHYST_SHARD)
            .setName("§d§lKryształ Ulepszenia")
            .setLore("§7Potrzebny do ulepszenia terenu.")
            .addEnchantment(Enchantment.ARROW_INFINITE, 1)
            .setItemFlags(ItemFlag.HIDE_ENCHANTS)
            .build();
}
