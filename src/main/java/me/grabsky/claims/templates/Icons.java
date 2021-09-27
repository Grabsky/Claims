package me.grabsky.claims.templates;

import me.grabsky.indigo.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

// INFO: Even though it's inside 'configuration' package, items can't be modified by end-user yet.
// This is something I can consider changing in the future, but currently there is no good reason to do so.
public class Icons {

    // Homes
    public static final ItemStack CATEGORY_HOMES = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lTeren")
            .setLore("§7Kliknij §eLPM§7, aby teleportować się na teren.", "§7Kliknij §ePPM§7, aby przeglądać listę dostępnych regionów.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlmMTcxMjdmMTBhZDRmNzBkYjA5M2E2MDIzYzdiNjljZjkxYjQwOWI4MThhODNkZWIzZDU1NDU3YjMxNmY2ZSJ9fX0=")
            .build();

    // Members
    public static final ItemStack ICON_ADD_MEMBER = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7§lDodaj")
            .setLore("§7Kliknij, aby przeglądać listę graczy.")
            .setCustomModelData(3)
            .build();

    // Settings
    public static final ItemStack CATEGORY_SETTINGS = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lUstawienia")
            .setLore("§7Kliknij, aby zmienić ustawienia.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZiY2I1OWFkNGIyNjExM2IxYTIwMGE5MDNhNTMxYWRjMzI1MjJjMWJlMTc1N2E1NjZkYjhjOGIifX19")
            .build();

    public static final ItemStack CATEGORY_FLAGS = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lFlagi")
            .setLore("§7Kliknij, aby zarządzać flagami.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNiYTcyNzdmYzg5NWJmM2I2NzM2OTQxNTk4NjRiODMzNTFhNGQxNDcxN2U0NzZlYmRhMWMzYmYzOGZjZjM3In19fQ==")
            .build();

    public static final ItemStack ICON_SET_TELEPORT = new ItemBuilder(Material.RED_BED)
            .setName("§e§lTeleport")
            .setLore("§7Ustaw teleport na teren", "§7na miejsce, w którym stoisz.")
            .build();

    // Navigation
    public static final ItemStack NAVIGATION_PREVIOUS = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7Poprzednia Strona")
            .setCustomModelData(1)
            .build();

    public static final ItemStack NAVIGATION_NEXT = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7Następna Strona")
            .setCustomModelData(2)
            .build();

    public static final ItemStack NAVIGATION_RETURN = new ItemBuilder(Material.BARRIER)
            .setName("§cPowrót")
            .setCustomModelData(1)
            .build();

    // Upgrade Levels
    public static final ItemStack LEVEL_COAL = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§7§lUlepsz")
            .setLore("", "§7Obecny poziom: Węgiel", "§8› §7Rozmiar: 31x31", "", "§7Następny poziom: §fŻelazo", "§8› §7Rozmiar: §f41x41", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §f16x Sztabka Żelaza", "")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkZmRlMDI5YWZhNDczYWM2NGIyZjE3ZGU3ZWQ5NDBlMzk5NjZlZDQ5MmJmM2Y0MTg1MjU5YjgwMjliNmIxMyJ9fX0=")
            .build();

    public static final ItemStack LEVEL_IRON = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§f§lUlepsz")
            .setLore("", "§7Obecny poziom: §fŻelazo", "§8› §7Rozmiar: §f41x41", "", "§7Następny poziom: §6Złoto", "§8› §7Rozmiar: §651x51", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §e16x Sztabka Złota", "")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjU2MjljMWM3N2FlYTJiMGNlYmNmMzMzNjU1ZTY4ZGIxMzRmNDg0MWMwOGQ5ZTg3NWMzMDc0YWMzMGUyYTZkZSJ9fX0=")
            .build();

    public static final ItemStack LEVEL_GOLD = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§6§lUlepsz")
            .setLore("", "§7Obecny poziom: §6Diament", "§8› §7Rozmiar: §651x51", "", "§7Następny poziom: §bDiament", "§8› §7Rozmiar: §b61x61", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §b16x Diament", "")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdmNTdlN2FhOGRlODY1OTFiYjBiYzUyY2JhMzBhNDlkOTMxYmZhYmJkNDdiYmM4MGJkZDY2MjI1MTM5MjE2MSJ9fX0=")
            .build();

    public static final ItemStack LEVEL_DIAMOND = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§b§lUlepsz")
            .setLore("", "§7Obecny poziom: §bDiament", "§8› §7Rozmiar: §b61x61", "", "§7Następny poziom: §aSzmaragd", "§8› §7Rozmiar: §a71x71", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §a16x Szmaragd", "")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY3NGE5NjQ0ZWMzY2NiZTkzNmNhNjI5NDI5N2MwZWVjZTQ3MTZkMjUxMjdiYjFiMTI1MjFmM2Y1OGRmOTZkYSJ9fX0=")
            .build();

    public static final ItemStack LEVEL_EMERALD = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lUlepsz")
            .setLore("", "§7Obecny poziom: §aSzmaragd", "§8› §7Rozmiar: §a71x71", "", "§7Następny poziom: §8Netherite", "§8› §7Rozmiar: §881x81", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §88x Netheryt", "")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTk2MGQ2ZmZhZjQ0ZThhZmNiZGY4YjI5YTc3ZDg0Y2UyMmM3MWQwMGM2NGJmZDk5YWYzNDBhNjk1MzViZmQ3In19fQ==")
            .build();

    public static final ItemStack LEVEL_NETHERITE = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§8§lUlepsz")
            .setLore("", "§7Obecny poziom: §8Netherite", "§8› §7Rozmiar: §881x81", "", "§7Osiągnąłeś maksymalny poziom terenu.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmVjODAzMTM0MDRhYWE2MGNiYWMxMDk4ZTNlMDVmMTA3YjlmNjY1MzQxMDNlOWI1OTRlOTIzMGVhMjI2YjVjZSJ9fX0=")
            .build();
}
