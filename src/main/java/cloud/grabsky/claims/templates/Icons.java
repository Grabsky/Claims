package cloud.grabsky.claims.templates;

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
}
