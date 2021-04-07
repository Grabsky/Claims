package net.skydistrict.claimsgui.panel.sections;

import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.builders.FlagItemBuilder;
import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FlagsSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;
    private final ProtectedRegion wgRegion;

    private FlagItemBuilder USE;
    private FlagItemBuilder TNT;
    private FlagItemBuilder ENTRY;
    private FlagItemBuilder MOB_SPAWNING;
    private FlagItemBuilder CREEPER_EXPLOSION;
    private FlagItemBuilder SNOW_MELT;
    private FlagItemBuilder ICE_MELT;
    private FlagItemBuilder TIME_LOCK;
    private FlagItemBuilder WEATHER_LOCK;


    public FlagsSection(Panel panel, Player player, PSRegion region) {
        super(panel, player, region);
        this.panel = panel;
        this.player = player;
        this.region = region;
        this.wgRegion = region.getWGRegion();
        this.prepare();
    }

    @Override
    public void prepare() {
        this.USE = new FlagItemBuilder(Material.ENCHANTING_TABLE, Flags.USE, wgRegion.getFlag(Flags.USE))
                .setName("§e§lInterakcja")
                .setPrefix("§7Przełącz interakcję z blokami użytkowymi.", "", "§7Zakres: §eGoście", "")
                .setSuffix("", "§cNie dotyczy kontenerów na przedmioty.")
                .updateLore();
        this.TNT = new FlagItemBuilder(Material.TNT, Flags.TNT, wgRegion.getFlag(Flags.TNT))
                .setName("§e§lWybuch TNT")
                .setPrefix("§7Przełącz niszczenie terenu przez wybuch TNT.", "")
                .updateLore();
        this.CREEPER_EXPLOSION = new FlagItemBuilder(Material.CREEPER_HEAD, Flags.CREEPER_EXPLOSION, wgRegion.getFlag(Flags.CREEPER_EXPLOSION))
                .setName("§e§lWybuch Creeperów")
                .setPrefix("§7Przełącz niszczenie terenu przez creepery.", "")
                .updateLore();
        this.SNOW_MELT = new FlagItemBuilder(Material.SNOWBALL, Flags.SNOW_MELT, wgRegion.getFlag(Flags.SNOW_MELT))
                .setName("§e§lTopnienie Śniegu")
                .setPrefix("§7Przełącz topnienie śniegu na terenie.", "")
                .updateLore();
        this.ICE_MELT = new FlagItemBuilder(Material.ICE, Flags.ICE_MELT, wgRegion.getFlag(Flags.ICE_MELT))
                .setName("§e§lTopnienie Lodu")
                .setPrefix("§7Przełącz topnienie lodu na terenie.", "")
                .updateLore();
        this.ENTRY = new FlagItemBuilder(Material.DARK_OAK_DOOR, Flags.ENTRY, wgRegion.getFlag(Flags.ENTRY))
                .setName("§e§lWejście")
                .setPrefix("§7Przełącz możliwość wejścia na teren.", "", "§7Zakres: §eGoście", "")
                .updateLore();
        this.MOB_SPAWNING = new FlagItemBuilder(Material.SPAWNER, Flags.MOB_SPAWNING, wgRegion.getFlag(Flags.MOB_SPAWNING))
                .setName("§e§lSpawn Mobów")
                .setPrefix("§7Przełącz spawn mobów na terenie.", "")
                .updateLore();
        this.TIME_LOCK = new FlagItemBuilder(Material.CLOCK, Flags.TIME_LOCK, wgRegion.getFlag(Flags.TIME_LOCK))
                .setName("§e§lGodzina")
                .setPrefix("§7Zmień godzinę na terenie.", "", "§7Zakres: §eWszyscy", "")
                .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
                .updateLore();
        this.WEATHER_LOCK = new FlagItemBuilder(Material.NAUTILUS_SHELL, Flags.WEATHER_LOCK, wgRegion.getFlag(Flags.WEATHER_LOCK))
                .setName("§e§lPogoda")
                .setPrefix("§7Zmień pogodę na terenie.", "", "§7Zakres: §eWszyscy", "")
                .setSuffix("", "§cZmiany widoczne po przelogowaniu.")
                .updateLore();
    }


    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(player, "§f\u7000\u7004", Containers.GENERIC_9X6);
        // Setting up flags
        this.panel.setItem(11, USE.build(), event -> event.setCurrentItem(USE.toggle(value -> wgRegion.setFlag(Flags.USE, (StateFlag.State) value)).build()));
        this.panel.setItem(12, TNT.build(), event -> event.setCurrentItem(TNT.toggle(value -> wgRegion.setFlag(Flags.TNT, (StateFlag.State) value)).build()));
        this.panel.setItem(13, CREEPER_EXPLOSION.build(), event -> event.setCurrentItem(CREEPER_EXPLOSION.toggle(value -> wgRegion.setFlag(Flags.CREEPER_EXPLOSION, (StateFlag.State) value)).build()));
        this.panel.setItem(14, SNOW_MELT.build(), event -> event.setCurrentItem(SNOW_MELT.toggle(value -> wgRegion.setFlag(Flags.SNOW_MELT, (StateFlag.State) value)).build()));
        this.panel.setItem(15, ICE_MELT.build(), event -> event.setCurrentItem(ICE_MELT.toggle(value -> wgRegion.setFlag(Flags.ICE_MELT, (StateFlag.State) value)).build()));
        this.panel.setItem(20, ENTRY.build(), event -> event.setCurrentItem(ENTRY.toggle(value -> {
            wgRegion.setFlag(Flags.ENTRY, (StateFlag.State) value);
            wgRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        }).build()));
        this.panel.setItem(21, MOB_SPAWNING.build(), event -> event.setCurrentItem(MOB_SPAWNING.toggle(value -> wgRegion.setFlag(Flags.MOB_SPAWNING, (StateFlag.State) value)).build()));
        this.panel.setItem(22, TIME_LOCK.build(), event -> event.setCurrentItem(TIME_LOCK.toggle(value -> wgRegion.setFlag(Flags.TIME_LOCK, (String) value)).build()));
        this.panel.setItem(23, WEATHER_LOCK.build(), event -> event.setCurrentItem(WEATHER_LOCK.toggle(value -> wgRegion.setFlag(Flags.WEATHER_LOCK, (WeatherType) value)).build()));
        // Return
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new SettingsSection(panel, player, region)));
    }
}
