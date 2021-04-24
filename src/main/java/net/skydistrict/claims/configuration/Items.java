package net.skydistrict.claims.configuration;

import net.skydistrict.claims.Claims;
import net.skydistrict.claims.builders.ItemBuilder;
import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Items {

    public static ItemStack getClaimBlock(int level) {
        ClaimLevel claimLevel = ClaimH.getClaimLevel(level);
        Material material = claimLevel.getBlockMaterial();
        ItemBuilder builder = new ItemBuilder(material)
                .setName(claimLevel.getColor() + "§lTeren")
                .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze " + claimLevel.getSize() + " §7bloków.");
        builder.getPersistentDataContainer().set(Claims.claimBlockLevel, PersistentDataType.INTEGER, level);
        return builder.build();
    }

    // Homes
    public static ItemStack HOMES = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lTeren")
            .setLore("§7Kliknij, aby teleportować się na teren", "§7lub aby przeglądać listę regionów.")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg1NDA2MGFhNTc3NmI3MzY2OGM4OTg2NTkwOWQxMmQwNjIyNDgzZTYwMGI2NDZmOTBjMTg2YzY1Yjc1ZmY0NSJ9fX0=")
            .build();

    // Settings
    public static ItemStack SETTINGS = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lUstawienia")
            .setLore("§7Kliknij, aby zmienić ustawienia.")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZiY2I1OWFkNGIyNjExM2IxYTIwMGE5MDNhNTMxYWRjMzI1MjJjMWJlMTc1N2E1NjZkYjhjOGIifX19")
            .build();
    public static ItemStack FLAGS = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lFlagi")
            .setLore("§7Kliknij, aby zarządzać flagami.")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNiYTcyNzdmYzg5NWJmM2I2NzM2OTQxNTk4NjRiODMzNTFhNGQxNDcxN2U0NzZlYmRhMWMzYmYzOGZjZjM3In19fQ==")
            .build();

    // Members
    public static ItemStack ADD = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7&lDodaj")
            .setLore("§7Kliknij, aby przeglądać listę graczy.")
            .setCustomModelData(3)
            .build();

    // Navigation
    public static ItemStack PREVIOUS = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7Poprzednia Strona")
            .setCustomModelData(1)
            .build();
    public static ItemStack NEXT = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7Następna Strona")
            .setCustomModelData(2)
            .build();
    public static ItemStack RETURN = new ItemBuilder(Material.BARRIER)
            .setName("§cPowrót")
            .setCustomModelData(1)
            .build();

    // Upgrade blocks
    public static ItemBuilder COAL_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§8§lUlepsz")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkZmRlMDI5YWZhNDczYWM2NGIyZjE3ZGU3ZWQ5NDBlMzk5NjZlZDQ5MmJmM2Y0MTg1MjU5YjgwMjliNmIxMyJ9fX0=");
    public static ItemBuilder IRON_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§f§lUlepsz")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjU2MjljMWM3N2FlYTJiMGNlYmNmMzMzNjU1ZTY4ZGIxMzRmNDg0MWMwOGQ5ZTg3NWMzMDc0YWMzMGUyYTZkZSJ9fX0=");
    public static ItemBuilder GOLD_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§6§lUlepsz")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdmNTdlN2FhOGRlODY1OTFiYjBiYzUyY2JhMzBhNDlkOTMxYmZhYmJkNDdiYmM4MGJkZDY2MjI1MTM5MjE2MSJ9fX0=");
    public static ItemBuilder DIAMOND_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§b§lUlepsz")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY3NGE5NjQ0ZWMzY2NiZTkzNmNhNjI5NDI5N2MwZWVjZTQ3MTZkMjUxMjdiYjFiMTI1MjFmM2Y1OGRmOTZkYSJ9fX0=");
    public static ItemBuilder EMERALD_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lUlepsz")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTk2MGQ2ZmZhZjQ0ZThhZmNiZGY4YjI5YTc3ZDg0Y2UyMmM3MWQwMGM2NGJmZDk5YWYzNDBhNjk1MzViZmQ3In19fQ==");

}
