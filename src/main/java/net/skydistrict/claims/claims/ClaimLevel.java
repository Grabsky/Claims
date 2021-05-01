package net.skydistrict.claims.claims;

import me.grabsky.indigo.builders.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ClaimLevel {
    private final String aliasString;
    private final String sizeString;
    private final ChatColor color;
    private final Material blockMaterial;
    private final Material upgradeMaterial;
    private final ItemBuilder icon;

    public ClaimLevel(String alias, String size, ChatColor color, Material blockMaterial, Material upgradeMaterial, ItemBuilder icon) {
        this.aliasString = color + alias;
        this.sizeString = color + size;
        this.color = color;
        this.blockMaterial = blockMaterial;
        this.upgradeMaterial = upgradeMaterial;
        this.icon = icon;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getAlias() {
        return aliasString;
    }

    public String getSize() {
        return sizeString;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getUpgradeMaterial() {
        return upgradeMaterial;
    }

    public ItemBuilder getIcon() {
        return icon;
    }
}
