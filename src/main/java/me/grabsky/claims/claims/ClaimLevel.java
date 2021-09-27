package me.grabsky.claims.claims;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ClaimLevel {
    private final String name;
    private final String size;
    private final String colorCode;
    private final Material blockMaterial;
    private final Set<ItemStack> upgradeCost;
    private final ItemStack icon;

    public ClaimLevel(final String name, final String size, final String colorCode, final Material blockMaterial, final ItemStack icon) {
        this.name = name;
        this.size = size;
        this.colorCode = colorCode;
        this.blockMaterial = blockMaterial;
        this.icon = icon;
        this.upgradeCost = new HashSet<>();
    }

    public ClaimLevel addUpgradeItems(final ItemStack... items) {
        this.upgradeCost.addAll(Set.of(items));
        return this;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Set<ItemStack> getUpgradeCost() {
        return upgradeCost;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
