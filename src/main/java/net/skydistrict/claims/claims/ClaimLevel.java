package net.skydistrict.claims.claims;

import net.skydistrict.claims.builders.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ClaimLevel {
    private final String alias;
    private final String size;
    private final ChatColor color;
    private final Material blockMaterial;
    private final Material upgradeMaterial;
    private final ItemBuilder inventoryItem;

    public ClaimLevel(String alias, ChatColor color, String size, Material blockMaterial, Material upgradeMaterial, ItemBuilder inventoryItem) {
        this.alias = alias;
        this.color = color;
        this.size = size;
        this.blockMaterial = blockMaterial;
        this.upgradeMaterial = upgradeMaterial;
        this.inventoryItem = inventoryItem;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getSize() {
        return size;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getUpgradeMaterial() {
        return upgradeMaterial;
    }

    public ItemBuilder getInventoryItem() {
        return inventoryItem;
    }
}
