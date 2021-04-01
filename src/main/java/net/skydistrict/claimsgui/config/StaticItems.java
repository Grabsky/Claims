package net.skydistrict.claimsgui.config;

import me.grabsky.indigo.api.SkullCache;
import net.kyori.adventure.text.Component;
import net.skydistrict.claimsgui.utils.ItemFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StaticItems {
    public static ItemStack MAIN_HOME = SkullCache.applyFromValue(new ItemFactory(Material.PLAYER_HEAD)
            .setName("§a§lTeren")
            .setLore("", "§8§l› §a§lLPM§8 - §7Teleportacja na teren", "§8§l› §a§lPPM§8 - §7Lista dostępnych terenów")
            .build(), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg1NDA2MGFhNTc3NmI3MzY2OGM4OTg2NTkwOWQxMmQwNjIyNDgzZTYwMGI2NDZmOTBjMTg2YzY1Yjc1ZmY0NSJ9fX0=");
}
