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

    private FlagItemBuilder TNT;
    private FlagItemBuilder ENTRY;
    private FlagItemBuilder MOB_SPAWNING;
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
        this.TNT = new FlagItemBuilder(Material.TNT, Flags.TNT, wgRegion.getFlag(Flags.TNT))
                .setName("§f§lWybuch TNT")
                .setPrefix("§7Przełącz wybuch TNT na terenie.")
                .updateLore();
        this.ENTRY = new FlagItemBuilder(Material.DARK_OAK_DOOR, Flags.ENTRY, wgRegion.getFlag(Flags.ENTRY))
                .setName("§e§lWejście")
                .setPrefix("§7Przełącz możliwość wejścia na teren.")
                .updateLore();
        this.MOB_SPAWNING = new FlagItemBuilder(Material.SPAWNER, Flags.MOB_SPAWNING, wgRegion.getFlag(Flags.MOB_SPAWNING))
                .setName("§e§lSpawn Mobów")
                .setPrefix("§7Przełącz spawn mobów na terenie.")
                .updateLore();
        this.TIME_LOCK = new FlagItemBuilder(Material.CLOCK, Flags.TIME_LOCK, wgRegion.getFlag(Flags.TIME_LOCK))
                .setName("§e§lGodzina")
                .setPrefix("§7Zmień godzinę na terenie.")
                .setSuffix("§cZmiany widoczne po przelogowaniu.")
                .updateLore();
        this.WEATHER_LOCK = new FlagItemBuilder(Material.NAUTILUS_SHELL, Flags.WEATHER_LOCK, wgRegion.getFlag(Flags.WEATHER_LOCK))
                .setName("§e§lPogoda")
                .setPrefix("§7Zmień pogodę na terenie.")
                .setSuffix("§cZmiany widoczne po przelogowaniu.")
                .updateLore();
    }


    @Override
    public void apply() {
        // Changing panel texture
        NMS.updateTitle(player, "§f\u7000\u7004", Containers.GENERIC_9X6);
        // Setting up flags
        this.panel.setItem(11, TNT.build(), event -> event.setCurrentItem(TNT.toggle(value -> wgRegion.setFlag(Flags.TNT, (StateFlag.State) value)).build()));
        this.panel.setItem(12, ENTRY.build(), event -> event.setCurrentItem(ENTRY.toggle(value -> {
            wgRegion.setFlag(Flags.ENTRY, (StateFlag.State) value);
            wgRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        }).build()));
        this.panel.setItem(11, MOB_SPAWNING.build(), event -> event.setCurrentItem(MOB_SPAWNING.toggle(value -> wgRegion.setFlag(Flags.MOB_SPAWNING, (StateFlag.State) value)).build()));
        this.panel.setItem(13, TIME_LOCK.build(), event -> event.setCurrentItem(TIME_LOCK.toggle(value -> wgRegion.setFlag(Flags.TIME_LOCK, (String) value)).build()));
        this.panel.setItem(14, WEATHER_LOCK.build(), event -> event.setCurrentItem(WEATHER_LOCK.toggle(value -> wgRegion.setFlag(Flags.WEATHER_LOCK, (WeatherType) value)).build()));
        // Return
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new SettingsSection(panel, player, region)));
    }
}
