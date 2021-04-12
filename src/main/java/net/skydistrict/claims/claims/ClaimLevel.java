package net.skydistrict.claims.claims;

import net.skydistrict.claims.builders.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ClaimLevel {
    private final String alias;
    private final String size;
    private final ChatColor color;
    private final Material material;
    private final ItemBuilder inventoryItem;

    public ClaimLevel(String alias, ChatColor color, String size, Material material, ItemBuilder inventoryItem) {
        this.alias = alias;
        this.color = color;
        this.size = size;
        this.material = material;
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

    public Material getMaterial() {
        return material;
    }

    public ItemBuilder getInventoryItem() {
        return inventoryItem;
    }
}
