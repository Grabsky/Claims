package net.skydistrict.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import me.grabsky.indigo.logger.FileLogger;
import net.skydistrict.claims.claims.ClaimManager;
import net.skydistrict.claims.commands.ClaimsCommand;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.flags.ClaimFlags;
import net.skydistrict.claims.listeners.RegionListener;
import net.skydistrict.claims.panel.PanelManager;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;

public final class Claims extends JavaPlugin {
    // Instances
    private static Claims instance;
    private ConsoleLogger consoleLogger;
    private FileLogger fileLogger;
    private Config config;
    private Lang lang;
    private RegionManager region;
    private ClaimManager claim;
    private PanelManager panel;
    // Getters
    public static Claims getInstance() { return instance; }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public FileLogger getFileLogger() { return fileLogger; }
    public RegionManager getRegionManager() { return region; }
    public ClaimManager getClaimManager() { return claim; }
    public PanelManager getPanelManager() { return panel; }

    public static NamespacedKey claimBlockLevel;

    @Override
    public void onEnable() {
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        this.fileLogger = new FileLogger(this, consoleLogger, new SimpleDateFormat("MM-yyyy").format(System.currentTimeMillis()), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
        // Initializing configuration
        this.lang = new Lang(this);
        this.config = new Config(this);
        // Reloading configuration files
        this.reload();
        // Registering flag handlers
        ClaimFlags.registerHandlers();
        // Creating NamespacedKey
        claimBlockLevel = new NamespacedKey(this, "claimBlockLevel");
        // Creating instance of RegionManager
        final World world = BukkitAdapter.adapt(Config.DEFAULT_WORLD);
        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Creating other instances
        this.claim = new ClaimManager(this);
        this.panel = new PanelManager(this);
        // Registering a command
        this.getCommand("claims").setExecutor(new ClaimsCommand(this));
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

    public boolean reload() {
        config.reload();
        lang.reload();
        return true;
    }
}
