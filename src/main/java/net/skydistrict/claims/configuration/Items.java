package net.skydistrict.claims.configuration;

import me.grabsky.indigo.builders.ItemBuilder;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.ClaimLevel;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Items {

    public static ItemStack getClaimBlock(int level) {
        final ClaimLevel claimLevel = ClaimH.getClaimLevel(level);
        final Material material = claimLevel.getBlockMaterial();
        final ItemBuilder builder = new ItemBuilder(material)
                .setName(claimLevel.getColor() + "§lTeren")
                .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze " + claimLevel.getSize() + " §7bloków.");
        builder.getPersistentDataContainer().set(Claims.claimBlockLevel, PersistentDataType.INTEGER, level);
        return builder.build();
    }

    // Homes
    public static final ItemStack HOMES = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lTeren")
            .setLore("§7Kliknij, aby teleportować się na teren", "§7lub aby przeglądać listę regionów.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlmMTcxMjdmMTBhZDRmNzBkYjA5M2E2MDIzYzdiNjljZjkxYjQwOWI4MThhODNkZWIzZDU1NDU3YjMxNmY2ZSJ9fX0=")
            .build();

    // Settings
    public static final ItemStack SETTINGS = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lUstawienia")
            .setLore("§7Kliknij, aby zmienić ustawienia.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZiY2I1OWFkNGIyNjExM2IxYTIwMGE5MDNhNTMxYWRjMzI1MjJjMWJlMTc1N2E1NjZkYjhjOGIifX19")
            .build();

    public static final ItemStack FLAGS = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lFlagi")
            .setLore("§7Kliknij, aby zarządzać flagami.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNiYTcyNzdmYzg5NWJmM2I2NzM2OTQxNTk4NjRiODMzNTFhNGQxNDcxN2U0NzZlYmRhMWMzYmYzOGZjZjM3In19fQ==")
            .build();

    // Members
    public static final ItemStack ADD = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7§lDodaj")
            .setLore("§7Kliknij, aby przeglądać listę graczy.")
            .setCustomModelData(3)
            .build();

    // Navigation
    public static final ItemStack PREVIOUS = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7Poprzednia Strona")
            .setCustomModelData(1)
            .build();

    public static final ItemStack NEXT = new ItemBuilder(Material.STRUCTURE_VOID)
            .setName("§7Następna Strona")
            .setCustomModelData(2)
            .build();

    public static final ItemStack RETURN = new ItemBuilder(Material.BARRIER)
            .setName("§cPowrót")
            .setCustomModelData(1)
            .build();

    // Icons
    public static final ItemStack HOME = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§e§lTeren")
            .setLore("§7Kliknij, aby teleportować się", "§7na swój teren.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlmMTcxMjdmMTBhZDRmNzBkYjA5M2E2MDIzYzdiNjljZjkxYjQwOWI4MThhODNkZWIzZDU1NDU3YjMxNmY2ZSJ9fX0=")
            .build();

    public static final ItemStack HOME_DISABLED = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§7Nie posiadasz terenu.")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU3ZTkzOWRiZTJhN2NkNjMwNzAwMzI4YWY0YzE4ZGZiOGZiY2I2NDJjOTEwM2E4NWUzOTRmOTgxNmI1OWExMCJ9fX0=")
            .build();

    // Upgrade blocks
    public static final ItemBuilder COAL_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§8§lUlepsz")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkZmRlMDI5YWZhNDczYWM2NGIyZjE3ZGU3ZWQ5NDBlMzk5NjZlZDQ5MmJmM2Y0MTg1MjU5YjgwMjliNmIxMyJ9fX0=");

    public static final ItemBuilder IRON_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§f§lUlepsz")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjU2MjljMWM3N2FlYTJiMGNlYmNmMzMzNjU1ZTY4ZGIxMzRmNDg0MWMwOGQ5ZTg3NWMzMDc0YWMzMGUyYTZkZSJ9fX0=");

    public static final ItemBuilder GOLD_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§6§lUlepsz")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdmNTdlN2FhOGRlODY1OTFiYjBiYzUyY2JhMzBhNDlkOTMxYmZhYmJkNDdiYmM4MGJkZDY2MjI1MTM5MjE2MSJ9fX0=");

    public static final ItemBuilder DIAMOND_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§b§lUlepsz")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY3NGE5NjQ0ZWMzY2NiZTkzNmNhNjI5NDI5N2MwZWVjZTQ3MTZkMjUxMjdiYjFiMTI1MjFmM2Y1OGRmOTZkYSJ9fX0=");

    public static final ItemBuilder EMERALD_BLOCK = new ItemBuilder(Material.PLAYER_HEAD)
            .setName("§a§lUlepsz")
            .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTk2MGQ2ZmZhZjQ0ZThhZmNiZGY4YjI5YTc3ZDg0Y2UyMmM3MWQwMGM2NGJmZDk5YWYzNDBhNjk1MzViZmQ3In19fQ==");
}
