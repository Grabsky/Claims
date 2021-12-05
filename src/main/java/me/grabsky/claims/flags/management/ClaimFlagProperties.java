package me.grabsky.claims.flags.management;

import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClaimFlagProperties {
    private final Material material;
    private final Component displayName;
    private final ClaimFlagOptions claimFlagOptions;
    private final List<Component> prefix;
    private final List<Component> suffix;

    public static class Builder {
        private Material material;
        private Component displayName;
        private ClaimFlagOptions claimFlagOptions;
        private List<Component> prefix;
        private List<Component> suffix;

        public Builder setMaterial(final Material material) {
            this.material = material;
            return this;
        }

        public Builder setFlagOptions(final ClaimFlagOptions claimFlagOptions) {
            this.claimFlagOptions = claimFlagOptions;
            return this;
        }

        public Builder setDisplayName(final String displayName) {
            this.displayName = Components.parseSection(displayName).decoration(TextDecoration.ITALIC, false);
            return this;
        }

        public Builder setPrefix(final String... prefix) {
            this.prefix = new ArrayList<>(prefix.length);
            for (final String line : prefix) {
                this.prefix.add(Components.parseSection(line).decoration(TextDecoration.ITALIC, false));
            }
            return this;
        }

        public Builder setSuffix(final String... suffix) {
            this.suffix = new ArrayList<>(suffix.length);
            for (final String line : suffix) {
                this.suffix.add(Components.parseSection(line).decoration(TextDecoration.ITALIC, false));
            }
            return this;
        }

        public ClaimFlagProperties build() {
            return new ClaimFlagProperties(material, displayName, claimFlagOptions, prefix, suffix);
        }
    }

    /** Constructor; creates FlagItemBuilder object with initial values */
    protected ClaimFlagProperties(final Material material, final Component displayName, final ClaimFlagOptions claimFlagOptions, @Nullable final List<Component> prefix, @Nullable final List<Component> suffix) {
        // Item
        this.material = material;
        this.displayName = displayName;
        // Flag properties
        this.claimFlagOptions = claimFlagOptions;
        // Item-related
        this.prefix = (prefix != null) ? prefix : new ArrayList<>();
        this.suffix = (suffix != null) ? suffix : new ArrayList<>();
    }

    public Material getMaterial() {
        return material;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public List<Component> getPrefix() {
        return prefix;
    }

    public List<Component> getSuffix() {
        return suffix;
    }

    public ClaimFlagOptions getFlagOptions() {
        return claimFlagOptions;
    }

    /* Supported Flags */

    public static final ClaimFlagProperties USE = new ClaimFlagProperties.Builder()
            .setMaterial(Material.CRAFTING_TABLE)
            .setDisplayName("§e§lInterakcja")
            .setPrefix("§7Interakcja z blokami użytkowymi.", "", "§7Zakres: §eGoście", "")
            .setSuffix("", "§cNie dotyczy kontenerów na przedmioty.")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties ENTRY = new ClaimFlagProperties.Builder()
            .setMaterial(Material.DARK_OAK_DOOR)
            .setDisplayName("§e§lWejście")
            .setPrefix("§7Możliwość wejścia na teren.", "",  "§7Zakres: §eGoście", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties TNT = new Builder()
            .setMaterial(Material.TNT)
            .setDisplayName("§e§lWybuch TNT")
            .setPrefix("§7Niszczenie terenu przez creepery.", "", "§7Zakres: §eŚrodowisko", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties CREEPER_EXPLOSION = new ClaimFlagProperties.Builder()
            .setMaterial(Material.CREEPER_HEAD)
            .setDisplayName("§e§lWybuch Creeperów")
            .setPrefix("§7Niszczenie terenu przez creepery.", "", "§7Zakres: §eŚrodowisko", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties SNOW_MELT = new ClaimFlagProperties.Builder()
            .setMaterial(Material.SNOWBALL)
            .setDisplayName("§e§lTopnienie Śniegu")
            .setPrefix("§7Topnienie śniegu na terenie.", "", "§7Zakres: §eŚrodowisko", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties ICE_MELT = new ClaimFlagProperties.Builder()
            .setMaterial(Material.BLUE_ICE)
            .setDisplayName("§e§lTopnienie Lodu")
            .setPrefix("§7Topnienie lodu na terenie.", "", "§7Zakres: §eŚrodowisko", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties FIRE_SPREAD = new ClaimFlagProperties.Builder()
            .setMaterial(Material.FLINT_AND_STEEL)
            .setDisplayName("§e§lRozprzestrzenianie Ognia")
            .setPrefix("§7Rozprzestrzenianie się ognia na terenie.", "", "§7Zakres: §eŚrodowisko", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties MOB_SPAWNING = new ClaimFlagProperties.Builder()
            .setMaterial(Material.SPAWNER)
            .setDisplayName("§e§lSpawn Mobów")
            .setPrefix("§7Spawn mobów na terenie.", "", "§7Zakres: §eŚrodowisko", "")
            .setFlagOptions(ClaimFlagOptions.STATE)
            .build();

    public static final ClaimFlagProperties TIME_LOCK = new ClaimFlagProperties.Builder()
            .setMaterial(Material.CLOCK)
            .setDisplayName("§e§lGodzina")
            .setPrefix("§7Godzina widoczna na terenie.", "", "§7Zakres: §eŚrodowisko", "")
            .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
            .setFlagOptions(ClaimFlagOptions.TIME)
            .build();

    public static final ClaimFlagProperties WEATHER_LOCK = new ClaimFlagProperties.Builder()
            .setMaterial(Material.NAUTILUS_SHELL)
            .setDisplayName("§e§lPogoda")
            .setPrefix("§7Pogoda widoczna na terenie.", "", "§7Zakres: §eŚrodowisko", "")
            .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
            .setFlagOptions(ClaimFlagOptions.WEATHER)
            .build();
}
