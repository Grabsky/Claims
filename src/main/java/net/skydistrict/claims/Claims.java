package net.skydistrict.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.skydistrict.claims.claims.ClaimCache;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.commands.ClaimCommand;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.listeners.PlayerListener;
import net.skydistrict.claims.listeners.RegionListener;
import net.skydistrict.claims.utils.UpgradeH;
import org.bukkit.plugin.java.JavaPlugin;

public final class Claims extends JavaPlugin {
    // Instances
    private static Claims instance;
    private RegionManager region;
    private ClaimManager claim;
//    private PanelManager panel;
    // Getters
    public static Claims getInstance() { return instance; }
    public RegionManager getRegionManager() { return region; }
    public ClaimManager getClaimManager() { return claim; }
//    public PanelManager getPanelManager() { return panel; }

    @Override
    public void onEnable() {
        instance = this;
        // Creating instance of RegionManager
        World world = BukkitAdapter.adapt(Config.DEFAULT_WORLD);
        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Creating other instances
        this.claim = new ClaimManager(this);
        new ClaimCache(this);
//        this.panel = new PanelManager(this);
        // Registering a command
        this.getCommand("claim").setExecutor(new ClaimCommand());
        // Registering events
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
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
