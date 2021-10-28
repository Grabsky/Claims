package me.grabsky.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.grabsky.claims.api.ClaimsAPI;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.commands.ClaimsCommand;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.configuration.ClaimsLang;
import me.grabsky.claims.flags.ExtraFlags;
import me.grabsky.claims.listeners.RegionListener;
import me.grabsky.indigo.framework.commands.CommandManager;
import me.grabsky.indigo.logger.ConsoleLogger;
import me.grabsky.indigo.logger.FileLogger;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;

public final class Claims extends JavaPlugin {
    // Instances
    private static Claims instance;
    private ConsoleLogger consoleLogger;
    private FileLogger fileLogger;
    private ClaimsConfig config;
    private ClaimsLang lang;
    private RegionManager region;
    private ClaimManager claim;
    // Getters
    public static Claims getInstance() { return instance; }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public FileLogger getFileLogger() { return fileLogger; }
    public RegionManager getRegionManager() { return region; }
    public ClaimManager getClaimManager() { return claim; }
    public ClaimsAPI getAPI() { return claim; }

    public static NamespacedKey claimBlockLevel;

    @Override
    public void onEnable() {
        instance = this;
        this.consoleLogger = new ConsoleLogger(this);
        this.fileLogger = new FileLogger(this, consoleLogger, new SimpleDateFormat("MM-yyyy").format(System.currentTimeMillis()), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
        // Initializing configuration
        this.lang = new ClaimsLang(this);
        this.config = new ClaimsConfig(this);
        // Reloading configuration files
        this.reload();
        // Registering flag handlers
        ExtraFlags.registerHandlers();
        // Creating NamespacedKey
        claimBlockLevel = new NamespacedKey(this, "claimBlockLevel");
        // Creating instance of RegionManager
        final World world = BukkitAdapter.adapt(ClaimsConfig.DEFAULT_WORLD);
        this.region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Creating other instances
        this.claim = new ClaimManager(this);
        // Initializing Indigo's InventoryManager
        // Registering events
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        // Registering command(s)
        final CommandManager commands = new CommandManager(this);
        commands.register(new ClaimsCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onLoad() {
        ExtraFlags.registerFlags();
    }

    public boolean reload() {
        config.reload();
        lang.reload();
        return true;
    }
}
