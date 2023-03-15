package cloud.grabsky.claims;

import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.configuration.ClaimsLang;
import cloud.grabsky.claims.flags.ExtraFlags;
import cloud.grabsky.claims.listeners.RegionListener;
import cloud.grabsky.commands.RootCommandManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

// TO-DO: Console & File loggers
// TO-DO: Claims API (if needed)
public final class Claims extends JavaPlugin {

    @Getter(AccessLevel.PUBLIC)
    private static Claims instance;

    private ClaimsConfig config;
    private ClaimsLang lang;

    @Getter(AccessLevel.PUBLIC)
    private RegionManager regionManager;

    @Getter(AccessLevel.PUBLIC)
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        instance = this;
        // ...
        // Initializing configuration
        this.lang = new ClaimsLang(this);
        this.config = new ClaimsConfig(this);
        // Reloading configuration files
        this.reload();
        // Registering flag handlers
        ExtraFlags.registerHandlers();
        // Creating instance of RegionManager
        final World world = BukkitAdapter.adapt(ClaimsConfig.DEFAULT_WORLD);
        this.regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Initializing ClaimManager and caching claims
        this.claimManager = new ClaimManager(this);
        this.claimManager.cacheClaims();
        // Registering events
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        // Registering command(s)
        final RootCommandManager commands = new RootCommandManager(this);
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

    public static final class Key {

        public static final NamespacedKey CLAIM_LEVEL = new NamespacedKey("claims", "claim_level");

    }

}
