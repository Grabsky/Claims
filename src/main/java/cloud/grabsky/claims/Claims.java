package cloud.grabsky.claims;

import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.configuration.ClaimsConfig;
import cloud.grabsky.claims.configuration.ClaimsLocale;
import cloud.grabsky.claims.flags.ExtraFlags;
import cloud.grabsky.claims.listeners.RegionListener;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

// TO-DO: Console & File loggers
// TO-DO: Claims API (if needed)
public final class Claims extends BedrockPlugin {

    @Getter(AccessLevel.PUBLIC)
    private static Claims instance;

    private ConfigurationMapper mapper;

    @Getter(AccessLevel.PUBLIC)
    private RegionManager regionManager;

    @Getter(AccessLevel.PUBLIC)
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        instance = this;
        // ...
        this.mapper = PaperConfigurationMapper.create();
        // ...
        if (this.onReload() == false) {
            return; // Plugin should be disabled automatically whenever exception is thrown.
        }
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
    public void onLoad() {
        ExtraFlags.registerFlags();
    }

    @Override
    public boolean onReload() throws ConfigurationMappingException {
        try {
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            // ...
            mapper.map(ClaimsConfig.class, locale);
            mapper.map(ClaimsLocale.class, config);
            return true;
        } catch (final IOException exc) {
            throw new IllegalStateException(exc); // Re-throwing as runtime exception
        }
    }

    public boolean reloadConfiguration() {
        try {
            return onReload();
        } catch (final ConfigurationMappingException exc) {
            return false;
        }
    }

    public static final class Key {

        public static final NamespacedKey CLAIM_LEVEL = new NamespacedKey("claims", "claim_level");

    }

}
