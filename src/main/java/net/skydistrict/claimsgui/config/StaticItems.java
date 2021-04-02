package net.skydistrict.claimsgui.config;

import net.skydistrict.claimsgui.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StaticItems {

    // Homes
    public static ItemStack HOMES = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lTeren")
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
            .setName("§9§lFlagi")
            .setLore("§7Kliknij, aby zarządzać flagami.")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNiYTcyNzdmYzg5NWJmM2I2NzM2OTQxNTk4NjRiODMzNTFhNGQxNDcxN2U0NzZlYmRhMWMzYmYzOGZjZjM3In19fQ==")
            .build();
    public static ItemBuilder UPGRADE = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lUlepsz")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjE0YWVlMzljOTYxNDIzYzFiMzgxN2E3MTAzZjI0YzkxN2UyZGI4ZmE5NjJhMmYwMGMzMThkZmNiMGM1YzhmYyJ9fX0=");


    // Info
    public static ItemBuilder INFO = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lInformacje")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgzNTBjNzU2ZTM3MThjMDQ2NTRlMmVjNDZkNzA0NGMxZWU2N2ZlYjNmZDBlMjA2MGMyYjhjNTY3YjBlZmU3MiJ9fX0=");


    // Members
    public static ItemStack ADD = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lDodaj")
            .setLore("§7Kliknij, aby przeglądać listę graczy.")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTU3YmJlMmVmY2U3NThmMDA5ZjhhNzQzNGQyNDY2NGNkZGZlN2EyZDAzOTQxOTAxYzA1YjMzMWM0NjEyZTljNCJ9fX0")
            .build();
    public static ItemBuilder ADD_DISABLED = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§7§lDodaj")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRmN2E2ZDUxM2M3ZjIzMmE0OTFmMTVlYWRjOGM0ZWI3YTE5NTk5Y2VmMjQxYzVmNjIwYTM4NTNmZWYxMzkxNCJ9fX0=");
    public static ItemStack REMOVE = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§c§lWyrzuć")
            .setLore("§7Kliknij, aby przeglądać listę graczy.")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDVhMWI4MzBkNTI0NDljODAyMDk5OWM2MTcwYjI3YTZjOTgzZTczMmIyNmRhNzMzN2I2NmFkYzEyZjM4MzIyYiJ9fX0=")
            .build();
    public static ItemBuilder REMOVE_DISABLED = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§7§lWyrzuć")
            .setSkullValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjBjNDQ4NGE1MGU1NzllZDE1OTgyN2JiZGE3N2E3NDBlYTE3MDk3YzMwN2YzY2I2Y2FkOWU2YWQ1NWE5NzFkYSJ9fX0=");


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

}
