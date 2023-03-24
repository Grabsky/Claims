package cloud.grabsky.claims;

import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.claims.claims.Claim;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.commands.ClaimsCommand;
import cloud.grabsky.claims.commands.argument.ClaimArgument;
import cloud.grabsky.claims.configuration.PluginConfig;
import cloud.grabsky.claims.configuration.PluginFlags;
import cloud.grabsky.claims.configuration.PluginItems;
import cloud.grabsky.claims.configuration.PluginLocale;
import cloud.grabsky.claims.configuration.adapter.ClaimFlagAdapterFactory;
import cloud.grabsky.claims.configuration.adapter.NamedTextColorAdapter;
import cloud.grabsky.claims.flags.ClientTimeFlag;
import cloud.grabsky.claims.flags.ClientWeatherFlag;
import cloud.grabsky.claims.flags.EnterActionBarFlag;
import cloud.grabsky.claims.flags.LeaveActionBarFlag;
import cloud.grabsky.claims.flags.object.FixedTime;
import cloud.grabsky.claims.flags.object.FixedWeather;
import cloud.grabsky.claims.listeners.RegionListener;
import cloud.grabsky.claims.panel.ClaimPanel;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationMapper;
import cloud.grabsky.configuration.exception.ConfigurationMappingException;
import cloud.grabsky.configuration.paper.PaperConfigurationMapper;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.ExitFlag;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;

import java.io.File;
import java.io.IOException;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

// TO-DO: Console & File loggers
// TO-DO: Claims API (if needed)
public final class Claims extends BedrockPlugin {

    @Getter(AccessLevel.PUBLIC)
    private static Claims instance;

    @Getter(AccessLevel.PUBLIC)
    private RegionManager regionManager;

    @Getter(AccessLevel.PUBLIC)
    private ClaimManager claimManager;

    private ConfigurationMapper mapper;

    @Override
    public void onEnable() {
        super.onEnable();
        // ...
        instance = this;
        // ...
        this.mapper = PaperConfigurationMapper.create(moshi -> {
            moshi.add(NamedTextColor.class, NamedTextColorAdapter.INSTANCE);
            moshi.add(ClaimFlagAdapterFactory.INSTANCE);
        });
        // ...
        if (this.onReload() == false) {
            return; // Plugin should be disabled automatically whenever exception is thrown.
        }
        ClaimPanel.registerListener(this);
        // Creating instance of RegionManager
        final World world = BukkitAdapter.adapt(PluginConfig.DEFAULT_WORLD);
        // ...
        this.regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // ...
        Claims.CustomFlag.registerHandlers();
        // Initializing ClaimManager and caching claims
        this.claimManager = new ClaimManager(this);
        this.claimManager.cacheClaimTypes();
        this.claimManager.cacheClaims();
        // Registering events
        this.getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        // Registering command(s)
        final RootCommandManager commands = new RootCommandManager(this);
        final ClaimArgument argument = new ClaimArgument(claimManager);
        commands.setArgumentParser(Claim.class, argument);
        commands.setCompletionsProvider(Claim.class, argument);
        commands.registerCommand(new ClaimsCommand(this));
        // ...
    }

    @Override
    public void onLoad() {
        Claims.CustomFlag.registerFlags();
    }

    @Override
    public boolean onReload() throws ConfigurationMappingException {
        try {
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            final File items = ensureResourceExistence(this, new File(this.getDataFolder(), "items.json"));
            final File flags = ensureResourceExistence(this, new File(this.getDataFolder(), "flags.json"));
            // ...
            mapper.map(PluginConfig.class, config);
            mapper.map(PluginLocale.class, locale);
            mapper.map(PluginItems.class, items);
            mapper.map(PluginFlags.class, flags);
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

        public static final NamespacedKey CLAIM_TYPE = new NamespacedKey("claims", "claim_level");

    }

    /**
     * Instances {@link Flag}
     */
    public static final class CustomFlag {

        public static final Flag<String> CLAIM_TYPE = new StringFlag("claim-type");
        public static final Flag<Location> CLAIM_CENTER = new LocationFlag("claim-home");
        public static final Flag<String> ENTER_ACTIONBAR = new StringFlag("enter-actionbar");
        public static final Flag<String> LEAVE_ACTIONBAR = new StringFlag("leave-actionbar");
        public static final Flag<FixedTime> CLIENT_TIME = new EnumFlag<>("client-time", FixedTime.class);
        public static final Flag<FixedWeather> CLIENT_WEATHER = new EnumFlag<>("client-weather", FixedWeather.class);

        public static void registerFlags() {
            final FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
            // ...
            flagRegistry.register(CLAIM_TYPE);
            flagRegistry.register(CLAIM_CENTER);
            flagRegistry.register(ENTER_ACTIONBAR);
            flagRegistry.register(LEAVE_ACTIONBAR);
            flagRegistry.register(CLIENT_TIME);
            flagRegistry.register(CLIENT_WEATHER);
        }

        public static void registerHandlers() {
            final SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
            // ...
            sessionManager.registerHandler(EnterActionBarFlag.FACTORY, ExitFlag.FACTORY);
            sessionManager.registerHandler(LeaveActionBarFlag.FACTORY, ExitFlag.FACTORY);
            sessionManager.registerHandler(ClientTimeFlag.FACTORY, ExitFlag.FACTORY);
            sessionManager.registerHandler(ClientWeatherFlag.FACTORY, ExitFlag.FACTORY);
        }

    }

}
