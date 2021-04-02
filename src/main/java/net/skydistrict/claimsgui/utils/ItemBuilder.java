package net.skydistrict.claimsgui.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.grabsky.indigo.api.SkullCache;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        this.meta.setDisplayName(TextUtils.color(name));
        return this;
    }

    public ItemBuilder setName(Component name) {
        this.meta.displayName(name);
        return this;
    }

    public ItemBuilder setLore(String... lines) {
        ArrayList<String> lore = new ArrayList<>(lines.length);
        for (String line : lines) {
            lore.add(TextUtils.color(line));
        }
        this.meta.setLore(lore);
        return this;
    }

    public ItemBuilder setCustomModelData(int value) {
        this.meta.setCustomModelData(value);
        return this;
    }

    public ItemBuilder setItemFlags(ItemFlag... itemFlags) {
        this.meta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder setSkullValue(String value) {
        if (this.item.getType() == Material.PLAYER_HEAD) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            PropertyMap map = profile.getProperties();
            map.put("textures", new Property("textures", value));
            try {
                Field field = meta.getClass().getDeclaredField("profile");
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
        String value = SkullCache.getHash(uuid);
        if (value != null) {
            this.setSkullValue(value);
        }
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}
