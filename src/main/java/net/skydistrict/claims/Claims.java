package net.skydistrict.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.commands.ClaimCommand;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.panel.PanelManager;
import net.skydistrict.claims.utils.UpgradeH;
import org.bukkit.plugin.java.JavaPlugin;

public final class Claims extends JavaPlugin {
    // Instances
    private static Claims instance;
    private RegionManager region;
    private ClaimManager claim;
    private PanelManager panel;
    // Getters
    public static Claims getInstance() { return instance; }
    public RegionManager getRegionManager() { return region; }
    public ClaimManager getProvinceManager() { return claim; }
    public PanelManager getPanelManager() { return panel; }
    // Custom flags
    public static IntegerFlag PROVINCE_LEVEL;

    @Override
    public void onEnable() {
        instance = this;
        // Creating instance of RegionManager
        World world = BukkitAdapter.adapt(Config.DEFAULT_WORLD);
        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Creating other instances
        this.claim = new ClaimManager(this);
        this.panel = new PanelManager(this);
        // Registering a command
        this.getCommand("province").setExecutor(new ClaimCommand(this));
        // Initializing available upgrades
        UpgradeH.initialize();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onLoad() {
        ClaimFlags.register();
    }
}
