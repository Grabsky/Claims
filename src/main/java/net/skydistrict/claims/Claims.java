package net.skydistrict.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.commands.ClaimCommand;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.flags.ClaimFlags;
import net.skydistrict.claims.listeners.RegionListener;
import net.skydistrict.claims.logger.FileLogger;
import net.skydistrict.claims.panel.PanelManager;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Claims extends JavaPlugin {
    // Instances
    private static Claims instance;
    private static Logger logger;
    private RegionManager region;
    private ClaimManager claim;
    private PanelManager panel;
    // Getters
    public static Claims getInstance() { return instance; }
    public RegionManager getRegionManager() { return region; }
    public ClaimManager getClaimManager() { return claim; }
    public PanelManager getPanelManager() { return panel; }

    public static NamespacedKey claimBlockLevel;

    @Override
    public void onEnable() {
        instance = this;
        logger = this.getLogger();
        // Setting-up FileLogger
        FileLogger.setup();
        // Reloading configs
        reload();
        // Registering flag handlers
        ClaimFlags.registerHandlers();
        // Creating NamespacedKey
        claimBlockLevel = new NamespacedKey(this, "claimBlockLevel");
        // Creating instance of RegionManager
        World world = BukkitAdapter.adapt(Config.DEFAULT_WORLD);
        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Creating other instances
        this.claim = new ClaimManager(this);
        this.panel = new PanelManager(this);
        // Registering a command
        this.getCommand("claim").setExecutor(new ClaimCommand(this));
        // Registering events
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        // Initializing available upgrades
        ClaimH.initialize();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onLoad() {
        ClaimFlags.registerFlags();
    }

    public static boolean reload() {
        Config.reload();
        logger.info("Configuration file (config.yml) has been reloaded.");
        Lang.reload();
        logger.info("Language file (lang.yml) has been reloaded.");
        return true;
    }
}
