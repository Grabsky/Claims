package cloud.grabsky.claims.claims;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.templates.Items;
import me.grabsky.indigo.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;

public class ClaimLevel {
    private final int numericLevel;
    private final Material claimBlockMaterial;
    private final ItemStack claimBlockItem;
    private final Set<ItemStack> upgradeCost;
    private final ItemStack inventoryIcon;

    public static class Builder {
        private final int numericLevel;
        private Material claimBlockMaterial;
        private ItemStack claimBlockItem;
        private ItemStack inventoryIcon;
        private Set<ItemStack> upgradeCost;

        public Builder(final int numericLevel) {
            this.numericLevel = numericLevel;
        }

        public Builder setClaimBlockMaterial(final Material claimBlockMaterial) {
            this.claimBlockMaterial = claimBlockMaterial;
            return this;
        }

        public Builder setClaimBlockItem(final ItemStack claimBlockItem) {
            this.claimBlockItem = claimBlockItem;
            return this;
        }

        public Builder setInventoryIcon(final ItemStack inventoryIcon) {
            this.inventoryIcon = inventoryIcon;
            return this;
        }

        public Builder setUpgradeCost(final ItemStack... items) {
            this.upgradeCost = Set.of(items);
            return this;
        }

        public ClaimLevel build() {
            return new ClaimLevel(numericLevel, claimBlockMaterial, claimBlockItem, inventoryIcon, upgradeCost);
        }
    }

    private ClaimLevel(final int numericLevel, final Material claimBlockMaterial, final ItemStack claimBlockItem, final ItemStack inventoryIcon, final Set<ItemStack> upgradeCost) {
        this.numericLevel = numericLevel;
        this.claimBlockMaterial = claimBlockMaterial;
        this.claimBlockItem = claimBlockItem;
        this.inventoryIcon = inventoryIcon;
        this.upgradeCost = upgradeCost;
    }

    public int getNumericLevel() {
        return numericLevel;
    }

    public Material getClaimBlockMaterial() {
        return claimBlockMaterial;
    }

    public ItemStack getClaimBlockItem() {
        return claimBlockItem;
    }

    public ItemStack getInventoryIcon() {
        return inventoryIcon;
    }

    public Set<ItemStack> getUpgradeCost() {
        return upgradeCost;
    }

    /* Supported Levels */

    // Returns ClaimLevel for given level (numeric) (defaults to COAL if provided level is invalid)
    public static ClaimLevel getClaimLevel(int level) {
        return switch(level) {
            case 2 -> ClaimLevel.IRON;
            case 3 -> ClaimLevel.GOLD;
            case 4 -> ClaimLevel.DIAMOND;
            case 5 -> ClaimLevel.EMERALD;
            case 6 -> ClaimLevel.NETHERITE;
            default -> ClaimLevel.COAL;
        };
    }

    public static final ClaimLevel COAL = new ClaimLevel.Builder(1)
            .setClaimBlockMaterial(Material.COAL_BLOCK)
            .setClaimBlockItem(new ItemBuilder(Material.COAL_BLOCK)
                    .setName("§7§lTeren")
                    .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze 31x31 bloków.")
                    .setPersistentData(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER, 1)
                    .build())
            .setInventoryIcon(new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§7§lUlepsz")
                    .setLore("", "§7Obecny poziom: Węgiel", "§8› §7Rozmiar: 31x31", "", "§7Następny poziom: §fŻelazo", "§8› §7Rozmiar: §f41x41", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §f16x Sztabka Żelaza", "")
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkZmRlMDI5YWZhNDczYWM2NGIyZjE3ZGU3ZWQ5NDBlMzk5NjZlZDQ5MmJmM2Y0MTg1MjU5YjgwMjliNmIxMyJ9fX0=")
                    .build())
            .build();

    public static final ClaimLevel IRON = new ClaimLevel.Builder(2)
            .setClaimBlockMaterial(Material.IRON_BLOCK)
            .setClaimBlockItem(new ItemBuilder(Material.IRON_BLOCK)
                    .setName("§f§lTeren")
                    .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze §f41x41§7 bloków.")
                    .setPersistentData(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER, 2)
                    .build())
            .setInventoryIcon(new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§f§lUlepsz")
                    .setLore("", "§7Obecny poziom: §fŻelazo", "§8› §7Rozmiar: §f41x41", "", "§7Następny poziom: §6Złoto", "§8› §7Rozmiar: §651x51", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §e16x Sztabka Złota", "")
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjU2MjljMWM3N2FlYTJiMGNlYmNmMzMzNjU1ZTY4ZGIxMzRmNDg0MWMwOGQ5ZTg3NWMzMDc0YWMzMGUyYTZkZSJ9fX0=")
                    .build())
            .setUpgradeCost(Items.UPGRADE_CRYSTAL, new ItemStack(Material.IRON_INGOT, 16))
            .build();

    public static final ClaimLevel GOLD = new ClaimLevel.Builder(3)
            .setClaimBlockMaterial(Material.GOLD_BLOCK)
            .setClaimBlockItem(new ItemBuilder(Material.GOLD_BLOCK)
                    .setName("§6§lTeren")
                    .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze §651x51§7 bloków.")
                    .setPersistentData(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER, 3)
                    .build())
            .setInventoryIcon(new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§6§lUlepsz")
                    .setLore("", "§7Obecny poziom: §6Złoto", "§8› §7Rozmiar: §651x51", "", "§7Następny poziom: §bDiament", "§8› §7Rozmiar: §b61x61", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §b16x Diament", "")
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdmNTdlN2FhOGRlODY1OTFiYjBiYzUyY2JhMzBhNDlkOTMxYmZhYmJkNDdiYmM4MGJkZDY2MjI1MTM5MjE2MSJ9fX0=")
                    .build())
            .setUpgradeCost(Items.UPGRADE_CRYSTAL, new ItemStack(Material.GOLD_INGOT, 16))
            .build();

    public static final ClaimLevel DIAMOND = new ClaimLevel.Builder(4)
            .setClaimBlockMaterial(Material.DIAMOND_BLOCK)
            .setClaimBlockItem(new ItemBuilder(Material.DIAMOND_BLOCK)
                    .setName("§b§lTeren")
                    .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze §b61x61§7 bloków.")
                    .setPersistentData(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER, 4)
                    .build())
            .setInventoryIcon(new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§b§lUlepsz")
                    .setLore("", "§7Obecny poziom: §bDiament", "§8› §7Rozmiar: §b61x61", "", "§7Następny poziom: §aSzmaragd", "§8› §7Rozmiar: §a71x71", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §a16x Szmaragd", "")
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY3NGE5NjQ0ZWMzY2NiZTkzNmNhNjI5NDI5N2MwZWVjZTQ3MTZkMjUxMjdiYjFiMTI1MjFmM2Y1OGRmOTZkYSJ9fX0=")
                    .build())
            .setUpgradeCost(Items.UPGRADE_CRYSTAL, new ItemStack(Material.DIAMOND, 16))
            .build();

    public static final ClaimLevel EMERALD = new ClaimLevel.Builder(5)
            .setClaimBlockMaterial(Material.EMERALD_BLOCK)
            .setClaimBlockItem(new ItemBuilder(Material.EMERALD_BLOCK)
                    .setName("§a§lTeren")
                    .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze §a71x71§7 bloków.")
                    .setPersistentData(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER, 5)
                    .build())
            .setInventoryIcon(new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§a§lUlepsz")
                    .setLore("", "§7Obecny poziom: §aSzmaragd", "§8› §7Rozmiar: §a71x71", "", "§7Następny poziom: §8Netheryt", "§8› §7Rozmiar: §881x81", "", "§7Koszt ulepszenia:", "§8› §d1x Kryształ Ulepszenia", "§8› §84x Netheryt", "")
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTk2MGQ2ZmZhZjQ0ZThhZmNiZGY4YjI5YTc3ZDg0Y2UyMmM3MWQwMGM2NGJmZDk5YWYzNDBhNjk1MzViZmQ3In19fQ==")
                    .build())
            .setUpgradeCost(Items.UPGRADE_CRYSTAL, new ItemStack(Material.EMERALD, 16))
            .build();

    public static final ClaimLevel NETHERITE = new ClaimLevel.Builder(6)
            .setClaimBlockMaterial(Material.NETHERITE_BLOCK)
            .setClaimBlockItem(new ItemBuilder(Material.NETHERITE_BLOCK)
                    .setName("§8§lTeren")
                    .setLore("§7Postaw, aby ochronić obszar ", "§7o rozmiarze §881x81§7 bloków.")
                    .setPersistentData(Claims.Key.CLAIM_LEVEL, PersistentDataType.INTEGER, 6)
                    .build())
            .setInventoryIcon(new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§8§lUlepsz")
                    .setLore("", "§7Obecny poziom: §8Netheryt", "§8› §7Rozmiar: §881x81", "", "§7Osiągnąłeś maksymalny poziom terenu.")
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmVjODAzMTM0MDRhYWE2MGNiYWMxMDk4ZTNlMDVmMTA3YjlmNjY1MzQxMDNlOWI1OTRlOTIzMGVhMjI2YjVjZSJ9fX0=")
                    .build())
            .setUpgradeCost(Items.UPGRADE_CRYSTAL, new ItemStack(Material.NETHER_BRICK, 4))
            .build();
}
