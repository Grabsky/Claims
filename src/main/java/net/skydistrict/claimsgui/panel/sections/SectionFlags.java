package net.skydistrict.claimsgui.panel.sections;

import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import net.skydistrict.claimsgui.builders.FlagItemBuilder;
import net.skydistrict.claimsgui.configuration.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.InventoryH;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SectionFlags extends Section {
    private final ProtectedRegion wgRegion;

    private FlagItemBuilder use;
    private FlagItemBuilder tnt;
    private FlagItemBuilder entry;
    private FlagItemBuilder mob_spawning;
    private FlagItemBuilder creeper_explosion;
    private FlagItemBuilder snow_melt;
    private FlagItemBuilder ice_melt;
    private FlagItemBuilder time_lock;
    private FlagItemBuilder weather_lock;

    public SectionFlags(Panel panel, Player executor, UUID owner, PSRegion region) {
        super(panel, executor, owner, region);
        this.wgRegion = region.getWGRegion();
    }


    @Override
    public void prepare() {
        this.use = new FlagItemBuilder(Material.ENCHANTING_TABLE, Flags.USE, wgRegion.getFlag(Flags.USE))
                .setName("§e§lInterakcja")
                .setPrefix("§7Przełącz interakcję z blokami użytkowymi.", "", "§7Zakres: §eGoście", "")
                .setSuffix("", "§cNie dotyczy kontenerów na przedmioty.")
                .updateLore();
        this.tnt = new FlagItemBuilder(Material.TNT, Flags.TNT, wgRegion.getFlag(Flags.TNT))
                .setName("§e§lWybuch TNT")
                .setPrefix("§7Przełącz niszczenie terenu przez wybuch TNT.", "")
                .updateLore();
        this.creeper_explosion = new FlagItemBuilder(Material.CREEPER_HEAD, Flags.CREEPER_EXPLOSION, wgRegion.getFlag(Flags.CREEPER_EXPLOSION))
                .setName("§e§lWybuch Creeperów")
                .setPrefix("§7Przełącz niszczenie terenu przez creepery.", "")
                .updateLore();
        this.snow_melt = new FlagItemBuilder(Material.SNOWBALL, Flags.SNOW_MELT, wgRegion.getFlag(Flags.SNOW_MELT))
                .setName("§e§lTopnienie Śniegu")
                .setPrefix("§7Przełącz topnienie śniegu na terenie.", "")
                .updateLore();
        this.ice_melt = new FlagItemBuilder(Material.ICE, Flags.ICE_MELT, wgRegion.getFlag(Flags.ICE_MELT))
                .setName("§e§lTopnienie Lodu")
                .setPrefix("§7Przełącz topnienie lodu na terenie.", "")
                .updateLore();
        this.entry = new FlagItemBuilder(Material.DARK_OAK_DOOR, Flags.ENTRY, wgRegion.getFlag(Flags.ENTRY))
                .setName("§e§lWejście")
                .setPrefix("§7Przełącz możliwość wejścia na teren.", "", "§7Zakres: §eGoście", "")
                .updateLore();
        this.mob_spawning = new FlagItemBuilder(Material.SPAWNER, Flags.MOB_SPAWNING, wgRegion.getFlag(Flags.MOB_SPAWNING))
                .setName("§e§lSpawn Mobów")
                .setPrefix("§7Przełącz spawn mobów na terenie.", "")
                .updateLore();
        this.time_lock = new FlagItemBuilder(Material.CLOCK, Flags.TIME_LOCK, wgRegion.getFlag(Flags.TIME_LOCK))
                .setName("§e§lGodzina")
                .setPrefix("§7Zmień godzinę na terenie.", "", "§7Zakres: §eWszyscy", "")
                .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
                .updateLore();
        this.weather_lock = new FlagItemBuilder(Material.NAUTILUS_SHELL, Flags.WEATHER_LOCK, wgRegion.getFlag(Flags.WEATHER_LOCK))
                .setName("§e§lPogoda")
                .setPrefix("§7Zmień pogodę na terenie.", "", "§7Zakres: §eWszyscy", "")
                .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
                .updateLore();
    }

    @Override
    public void apply() {
        // Changing panel texture
        InventoryH.updateTitle(executor, "§f\u7000\u7105", editMode);
        // Setting up flags
        this.panel.setItem(11, use.build(), (event) -> event.setCurrentItem(use.toggle(value -> wgRegion.setFlag(Flags.USE, (StateFlag.State) value)).build()));
        this.panel.setItem(12, tnt.build(), (event) -> event.setCurrentItem(tnt.toggle(value -> wgRegion.setFlag(Flags.TNT, (StateFlag.State) value)).build()));
        this.panel.setItem(13, creeper_explosion.build(), (event) -> event.setCurrentItem(creeper_explosion.toggle(value -> wgRegion.setFlag(Flags.CREEPER_EXPLOSION, (StateFlag.State) value)).build()));
        this.panel.setItem(14, snow_melt.build(), (event) -> event.setCurrentItem(snow_melt.toggle(value -> wgRegion.setFlag(Flags.SNOW_MELT, (StateFlag.State) value)).build()));
        this.panel.setItem(15, ice_melt.build(), (event) -> event.setCurrentItem(ice_melt.toggle(value -> wgRegion.setFlag(Flags.ICE_MELT, (StateFlag.State) value)).build()));
        this.panel.setItem(20, entry.build(), (event) -> event.setCurrentItem(entry.toggle(value -> {
            wgRegion.setFlag(Flags.ENTRY, (StateFlag.State) value);
            wgRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        }).build()));
        this.panel.setItem(21, mob_spawning.build(), (event) -> event.setCurrentItem(mob_spawning.toggle(value -> wgRegion.setFlag(Flags.MOB_SPAWNING, (StateFlag.State) value)).build()));
        this.panel.setItem(22, time_lock.build(), (event) -> event.setCurrentItem(time_lock.toggle(value -> wgRegion.setFlag(Flags.TIME_LOCK, (String) value)).build()));
        this.panel.setItem(23, weather_lock.build(), (event) -> event.setCurrentItem(weather_lock.toggle(value -> wgRegion.setFlag(Flags.WEATHER_LOCK, (WeatherType) value)).build()));
        // Return button
        panel.setItem(49, StaticItems.RETURN, (event) -> panel.applySection(new SectionSettings(panel, executor, owner, region)));
    }
}
