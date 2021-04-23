package net.skydistrict.claims.panel.sections;

import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.skydistrict.claims.builders.FlagItemBuilder;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SectionFlags extends Section {
    private final ProtectedRegion wgRegion;

    private FlagItemBuilder use;
    private FlagItemBuilder entry;
    private FlagItemBuilder tnt;
    private FlagItemBuilder mob_spawning;
    private FlagItemBuilder creeper_explosion;
    private FlagItemBuilder snow_melt;
    private FlagItemBuilder ice_melt;
    private FlagItemBuilder time_lock;
    private FlagItemBuilder weather_lock;

    public SectionFlags(Panel panel, Player executor, UUID owner, Claim claim) {
        super(panel, executor, owner, claim);
        this.wgRegion = claim.getWGRegion();
    }

    @Override
    public void prepare() {
        this.use = new FlagItemBuilder(Material.ENCHANTING_TABLE, Flags.USE, wgRegion.getFlag(Flags.USE))
                .setName("§e§lInterakcja")
                .setPrefix("§7Interakcja z blokami użytkowymi.", "", "§7Zakres: §eGoście", "")
                .setSuffix("", "§cNie dotyczy kontenerów na przedmioty.")
                .updateLore();
        this.entry = new FlagItemBuilder(Material.DARK_OAK_DOOR, Flags.ENTRY, wgRegion.getFlag(Flags.ENTRY))
                .setName("§e§lWejście")
                .setPrefix("§7Możliwość wejścia na teren.", "", "§7Zakres: §eGoście", "")
                .updateLore();
        this.tnt = new FlagItemBuilder(Material.TNT, Flags.TNT, wgRegion.getFlag(Flags.TNT))
                .setName("§e§lWybuch TNT")
                .setPrefix("§7Niszczenie terenu przez wybuch TNT.", "", "§7Zakres: §eŚrodowisko", "")
                .updateLore();
        this.creeper_explosion = new FlagItemBuilder(Material.CREEPER_HEAD, Flags.CREEPER_EXPLOSION, wgRegion.getFlag(Flags.CREEPER_EXPLOSION))
                .setName("§e§lWybuch Creeperów")
                .setPrefix("§7Niszczenie terenu przez creepery.", "", "§7Zakres: §eŚrodowisko", "")
                .updateLore();
        this.snow_melt = new FlagItemBuilder(Material.SNOWBALL, Flags.SNOW_MELT, wgRegion.getFlag(Flags.SNOW_MELT))
                .setName("§e§lTopnienie Śniegu")
                .setPrefix("§7Topnienie śniegu na terenie.", "", "§7Zakres: §eŚrodowisko", "")
                .updateLore();
        this.ice_melt = new FlagItemBuilder(Material.ICE, Flags.ICE_MELT, wgRegion.getFlag(Flags.ICE_MELT))
                .setName("§e§lTopnienie Lodu")
                .setPrefix("§7Topnienie lodu na terenie.", "", "§7Zakres: §eŚrodowisko", "")
                .updateLore();
        this.mob_spawning = new FlagItemBuilder(Material.SPAWNER, Flags.MOB_SPAWNING, wgRegion.getFlag(Flags.MOB_SPAWNING))
                .setName("§e§lSpawn Mobów")
                .setPrefix("§7Spawn mobów na terenie.", "", "§7Zakres: §eŚrodowisko", "")
                .updateLore();
        this.time_lock = new FlagItemBuilder(Material.CLOCK, Flags.TIME_LOCK, wgRegion.getFlag(Flags.TIME_LOCK))
                .setName("§e§lGodzina")
                .setPrefix("§7Godzina widoczna na terenie.", "", "§7Zakres: §eŚrodowisko", "")
                .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
                .updateLore();
        this.weather_lock = new FlagItemBuilder(Material.NAUTILUS_SHELL, Flags.WEATHER_LOCK, wgRegion.getFlag(Flags.WEATHER_LOCK))
                .setName("§e§lPogoda")
                .setPrefix("§7Pogoda widoczna na terenie.", "", "§7Zakres: §eŚrodowisko", "")
                .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
                .updateLore();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7105", editMode);
        // Setting up flags
        panel.setItem(11, use.build(), (event) -> event.setCurrentItem(use.toggle(value -> wgRegion.setFlag(Flags.USE, (StateFlag.State) value)).build()));
        panel.setItem(12, entry.build(), (event) -> event.setCurrentItem(entry.toggle(value -> {
            wgRegion.setFlag(Flags.ENTRY, (StateFlag.State) value);
            wgRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        }).build()));
        panel.setItem(13, tnt.build(), (event) -> event.setCurrentItem(tnt.toggle(value -> wgRegion.setFlag(Flags.TNT, (StateFlag.State) value)).build()));
        panel.setItem(14, creeper_explosion.build(), (event) -> event.setCurrentItem(creeper_explosion.toggle(value -> wgRegion.setFlag(Flags.CREEPER_EXPLOSION, (StateFlag.State) value)).build()));
        panel.setItem(15, snow_melt.build(), (event) -> event.setCurrentItem(snow_melt.toggle(value -> wgRegion.setFlag(Flags.SNOW_MELT, (StateFlag.State) value)).build()));
        panel.setItem(20, ice_melt.build(), (event) -> event.setCurrentItem(ice_melt.toggle(value -> wgRegion.setFlag(Flags.ICE_MELT, (StateFlag.State) value)).build()));
        panel.setItem(21, mob_spawning.build(), (event) -> event.setCurrentItem(mob_spawning.toggle(value -> wgRegion.setFlag(Flags.MOB_SPAWNING, (StateFlag.State) value)).build()));
        panel.setItem(22, time_lock.build(), (event) -> event.setCurrentItem(time_lock.toggle(value -> wgRegion.setFlag(Flags.TIME_LOCK, (String) value)).build()));
        panel.setItem(23, weather_lock.build(), (event) -> event.setCurrentItem(weather_lock.toggle(value -> wgRegion.setFlag(Flags.WEATHER_LOCK, (WeatherType) value)).build()));
        // Return button
        panel.setItem(49, Items.RETURN, (event) -> panel.applySection(new SectionSettings(panel, executor, owner, claim)));
    }

}
