package net.skydistrict.claims.builders;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.grabsky.indigo.api.SkullCache;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material) {
        item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setName(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder setLore(String... lines) {
        meta.setLore(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder setLore(Component... lines) {
        meta.lore(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder setCustomModelData(int value) {
        meta.setCustomModelData(value);
        return this;
    }

    public ItemBuilder setItemFlags(ItemFlag... itemFlags) {
        meta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder setSkullValue(String value) {
        if (item.getType() == Material.PLAYER_HEAD) {
            final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));
            try {
                final Field field = meta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(meta, profile);
                field.setAccessible(false);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public ItemBuilder setSkullOwner(UUID uuid) {
        final String value = SkullCache.getHash(uuid);
        if (value != null) {
            this.setSkullValue(value);
        }
        return this;
    }

    public PersistentDataContainer getPersistentDataContainer() {
        return this.meta.getPersistentDataContainer();
    }

    public ItemMeta getMeta() {
        return meta;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
