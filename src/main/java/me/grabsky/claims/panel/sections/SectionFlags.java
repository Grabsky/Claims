package me.grabsky.claims.panel.sections;

import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.grabsky.claims.flags.management.ClaimFlag;
import me.grabsky.claims.flags.management.ClaimFlagProperties;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.templates.Icons;

public class SectionFlags extends Section {
    private final ProtectedRegion wgRegion;

    private ClaimFlag useFlag;
    private ClaimFlag entryFlag;
    private ClaimFlag tntFlag;
    private ClaimFlag creeperExplosionFlag;
    private ClaimFlag snowMelt;
    private ClaimFlag iceMelt;
    private ClaimFlag mobSpawning;
    private ClaimFlag timeLock;
    private ClaimFlag weatherLock;

    public SectionFlags(Panel panel) {
        super(panel);
        this.wgRegion = panel.getClaimOwner().getClaim().getWGRegion();

    }

    @Override
    public void prepare() {
        this.useFlag = new ClaimFlag(ClaimFlagProperties.USE, wgRegion.getFlag(Flags.USE));
        this.entryFlag = new ClaimFlag(ClaimFlagProperties.ENTRY, wgRegion.getFlag(Flags.ENTRY));
        this.tntFlag = new ClaimFlag(ClaimFlagProperties.TNT, wgRegion.getFlag(Flags.TNT));
        this.creeperExplosionFlag = new ClaimFlag(ClaimFlagProperties.CREEPER_EXPLOSION, wgRegion.getFlag(Flags.CREEPER_EXPLOSION));
        this.snowMelt = new ClaimFlag(ClaimFlagProperties.SNOW_MELT, wgRegion.getFlag(Flags.SNOW_MELT));
        this.iceMelt = new ClaimFlag(ClaimFlagProperties.ICE_MELT, wgRegion.getFlag(Flags.ICE_MELT));
        this.mobSpawning = new ClaimFlag(ClaimFlagProperties.MOB_SPAWNING, wgRegion.getFlag(Flags.MOB_SPAWNING));
        this.timeLock = new ClaimFlag(ClaimFlagProperties.TIME_LOCK, wgRegion.getFlag(Flags.TIME_LOCK));
        this.weatherLock = new ClaimFlag(ClaimFlagProperties.WEATHER_LOCK, wgRegion.getFlag(Flags.WEATHER_LOCK));
    }

    @Override
    public void apply() {
        // Changing panel texture
        panel.updateClientTitle("§f\u7000\u7105");
        // Adding flag items
        panel.setItem(11, useFlag.updateItem(), (event) -> {
            event.setCurrentItem(useFlag.nextOption((newVal) -> {
                wgRegion.setFlag(Flags.USE, (StateFlag.State) newVal);
                wgRegion.setFlag(Flags.USE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            }));
        });
        panel.setItem(12, entryFlag.updateItem(), (event) -> {
            event.setCurrentItem(entryFlag.nextOption((newVal) -> {
                wgRegion.setFlag(Flags.ENTRY, (StateFlag.State) newVal);
                wgRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            }));
        });
        panel.setItem(13, tntFlag.updateItem(), (event) -> event.setCurrentItem(tntFlag.nextOption((newVal) -> wgRegion.setFlag(Flags.TNT, (StateFlag.State) newVal))));
        panel.setItem(14, creeperExplosionFlag.updateItem(), (event) -> event.setCurrentItem(creeperExplosionFlag.nextOption((newVal) -> wgRegion.setFlag(Flags.CREEPER_EXPLOSION, (StateFlag.State) newVal))));
        panel.setItem(15, snowMelt.updateItem(), (event) -> event.setCurrentItem(snowMelt.nextOption((newVal) -> wgRegion.setFlag(Flags.SNOW_MELT, (StateFlag.State) newVal))));
        panel.setItem(20, iceMelt.updateItem(), (event) -> event.setCurrentItem(iceMelt.nextOption((newVal) -> wgRegion.setFlag(Flags.ICE_MELT, (StateFlag.State) newVal))));
        panel.setItem(21, mobSpawning.updateItem(), (event) -> event.setCurrentItem(mobSpawning.nextOption((newVal) -> wgRegion.setFlag(Flags.MOB_SPAWNING, (StateFlag.State) newVal))));
        panel.setItem(22, timeLock.updateItem(), (event) -> event.setCurrentItem(timeLock.nextOption((newVal) -> wgRegion.setFlag(Flags.TIME_LOCK, (String) newVal))));
        panel.setItem(23, weatherLock.updateItem(), (event) -> event.setCurrentItem(weatherLock.nextOption((newVal) -> wgRegion.setFlag(Flags.WEATHER_LOCK, (WeatherType) newVal))));
        panel.setItem(49, Icons.NAVIGATION_RETURN, (event) -> panel.applySection(new SectionSettings(panel)));
    }

}
