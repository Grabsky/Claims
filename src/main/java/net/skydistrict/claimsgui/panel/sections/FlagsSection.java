package net.skydistrict.claimsgui.panel.sections;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import net.minecraft.server.v1_16_R3.Containers;
import net.skydistrict.claimsgui.config.StaticItems;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.builders.FlagItemBuilder;
import net.skydistrict.claimsgui.builders.ItemBuilder;
import net.skydistrict.claimsgui.utils.NMS;
import org.bukkit.Material;
import org.bukkit.entity.Player;

// I still have to decide whether I should wrap flags to their own object or use a bunch of utils.
// Both methods would work fine, but I feel like OOP is the best way to go.
public class FlagsSection extends Section {
    private final Panel panel;
    private final Player player;
    private final PSRegion region;
    private final ProtectedRegion wgRegion;

    private FlagItemBuilder TNT;
    private FlagItemBuilder ENTRY;
    private ItemBuilder WEATHER_LOCK;

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
                .setDescription("§7Przełącz wybuch TNT na terenie.")
                .updateLore();
        this.ENTRY = new FlagItemBuilder(Material.DARK_OAK_DOOR, Flags.ENTRY.getRegionGroupFlag(), wgRegion.getFlag(Flags.ENTRY.getRegionGroupFlag()))
                .setName("§f§lWybuch TNT")
                .setDescription("§7Przełącz wybuch TNT na terenie.")
                .updateLore();
    }


    @Override
    public void apply() {
        NMS.updateTitle(player, "§f\u7000\u7004", Containers.GENERIC_9X6);
        this.panel.setItem(11, TNT.build(), event -> event.setCurrentItem(TNT.next(value -> wgRegion.setFlag(Flags.TNT, (StateFlag.State) value)).build()));
        this.panel.setItem(12, ENTRY.build(), event -> event.setCurrentItem(TNT.next(value -> {
            wgRegion.setFlag(Flags.ENTRY, (StateFlag.State) value);
            wgRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
        }).build()));
        panel.setItem(40, StaticItems.RETURN, (event) -> panel.applySection(new SettingsSection(panel, player, region)));
    }
}
