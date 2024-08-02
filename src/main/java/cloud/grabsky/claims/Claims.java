/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.claims;

import cloud.grabsky.bedrock.BedrockPlugin;
import cloud.grabsky.bedrock.inventory.Panel;
import cloud.grabsky.claims.claims.ClaimManager;
import cloud.grabsky.claims.commands.ClaimsCommand;
import cloud.grabsky.claims.commands.WaypointCommand;
import cloud.grabsky.claims.commands.templates.CommandArgumentTemplate;
import cloud.grabsky.claims.commands.templates.CommandExceptionTemplate;
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
import cloud.grabsky.claims.listeners.WaypointListener;
import cloud.grabsky.claims.session.Session;
import cloud.grabsky.claims.waypoints.WaypointManager;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.configuration.ConfigurationHolder;
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
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cloud.grabsky.configuration.paper.util.Resources.ensureResourceExistence;

public final class Claims extends BedrockPlugin {

    @Getter(AccessLevel.PUBLIC)
    private static Claims instance;

    @Getter(AccessLevel.PUBLIC)
    private ClaimManager claimManager;

    @Getter(AccessLevel.PUBLIC)
    private WaypointManager waypointManager;

    private ConfigurationMapper mapper;

    // Can be passed to some CompletableFuture methods to make sure code is executed on the main thread.
    public static Executor MAIN_THREAD_EXECUTOR;

    @Override
    public void onEnable() {
        super.onEnable();
        // Setting the plugin instance.
        instance = this;
        // Setting the main-thread executor.
        MAIN_THREAD_EXECUTOR = this.getServer().getScheduler().getMainThreadExecutor(this);
        // Creating ConfigurationMapper instance.
        this.mapper = PaperConfigurationMapper.create(moshi -> {
            moshi.add(NamedTextColor.class, NamedTextColorAdapter.INSTANCE);
            moshi.add(ClaimFlagAdapterFactory.INSTANCE);
        });
        // Reloading configuration and shutting the server down in case it fails.
        if (this.onReload() == false)
            this.getServer().shutdown();
        // Creating instance of RegionManager
        final World world = BukkitAdapter.adapt(PluginConfig.DEFAULT_WORLD);
        final RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        // Registering additional WorldGuard flags' handlers.
        Claims.CustomFlag.registerHandlers();
        // Initializing ClaimManager
        this.claimManager = new ClaimManager(this, regionManager);
        this.waypointManager = new WaypointManager(this);
        // Registering Panel/GUI listeners.
        Panel.registerDefaultListeners(this);
        // Registering plugin listeners.
        this.getServer().getPluginManager().registerEvents(new RegionListener(this, claimManager), this);
        this.getServer().getPluginManager().registerEvents(new WaypointListener(this), this);
        this.getServer().getPluginManager().registerEvents(Session.Listener.INSTANCE, this);
        // Setting-up RootCommandManager... (applying templates, registering commands)
        new RootCommandManager(this)
                // Registering templates...
                .apply(new CommandArgumentTemplate(this))
                .apply(CommandExceptionTemplate.INSTANCE)
                // Registering dependencies...
                .registerDependency(Claims.class, instance)
                .registerDependency(ClaimManager.class, claimManager)
                .registerDependency(WaypointManager.class, waypointManager)
                // Registering commands...
                .registerCommand(ClaimsCommand.class)
                .registerCommand(WaypointCommand.class);
        // Registering PAPI placeholders...
        Placeholders.INSTANCE.register();
    }

    @Override
    public void onLoad() {
        // Registering additional WorldGuard flags.
        Claims.CustomFlag.registerFlags();
    }

    @Override
    public boolean onReload() throws ConfigurationMappingException, IllegalStateException {
        try {
            final File config = ensureResourceExistence(this, new File(this.getDataFolder(), "config.json"));
            final File locale = ensureResourceExistence(this, new File(this.getDataFolder(), "locale.json"));
            final File localeCommands = ensureResourceExistence(this, new File(this.getDataFolder(), "locale_commands.json"));
            final File items = ensureResourceExistence(this, new File(this.getDataFolder(), "items.json"));
            final File flags = ensureResourceExistence(this, new File(this.getDataFolder(), "flags.json"));
            // Mapping the files.
            mapper.map(
                    ConfigurationHolder.of(PluginConfig.class, config),
                    ConfigurationHolder.of(PluginLocale.class, locale),
                    ConfigurationHolder.of(PluginLocale.Commands.class, localeCommands),
                    ConfigurationHolder.of(PluginItems.class, items),
                    ConfigurationHolder.of(PluginFlags.class, flags)
            );
            // Returning true, as everything seemed to reload properly.
            return true;
        } catch (final IOException e) {
            this.getLogger().severe("An error occurred while trying to reload the plugin.");
            this.getLogger().severe("  " + e.getMessage());
            // Returning false, as plugin has failed to reload.
            return false;
        }
    }


    /**
     * Instances of {@link NamespacedKey} keys used all-over the place in the plugin logic.
     */
    public static final class Key {

        /**
         * Represents the {@link NamespacedKey} for the claim type.
         */
        public static final NamespacedKey CLAIM_TYPE = new NamespacedKey("claims", "claim_level");

        /**
         * Represents the {@link NamespacedKey} for the waypoint decoration. Used to identify the decoration associated with waypoints.
         */
        public static final NamespacedKey WAYPOINT_DECORATION = new NamespacedKey("claims", "waypoint_decoration");

    }


    /**
     * Represents custom WorldGuard flags registered by the plugin.
     */
    public static final class CustomFlag {

        public static final Flag<String> CLAIM_CREATED = new StringFlag("claim-created");
        public static final Flag<String> CLAIM_NAME = new StringFlag("claim-name");
        public static final Flag<String> CLAIM_TYPE = new StringFlag("claim-type");
        public static final Flag<Location> CLAIM_CENTER = new LocationFlag("claim-home");
        public static final Flag<String> ENTER_ACTIONBAR = new StringFlag("enter-actionbar");
        public static final Flag<String> LEAVE_ACTIONBAR = new StringFlag("leave-actionbar");
        public static final Flag<FixedTime> CLIENT_TIME = new EnumFlag<>("client-time", FixedTime.class);
        public static final Flag<FixedWeather> CLIENT_WEATHER = new EnumFlag<>("client-weather", FixedWeather.class);

        public static void registerFlags() {
            final FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
            // ...
            flagRegistry.register(CLAIM_NAME);
            flagRegistry.register(CLAIM_CREATED);
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

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Placeholders extends PlaceholderExpansion {
        public static final Placeholders INSTANCE = new Placeholders(); // SINGLETON

        @Override
        public @NotNull String getAuthor() {
            return "Grabsky";
        }

        @Override
        public @NotNull String getIdentifier() {
            return "claims";
        }

        @Override
        public @NotNull String getVersion() {
            return Claims.getInstance().getPluginMeta().getVersion();
        }

        @Override
        public String onRequest(final @NotNull OfflinePlayer player, final @NotNull String params) {
            if (params.equalsIgnoreCase("claims_count") == true && player instanceof Player onlinePlayer && onlinePlayer.isOnline() == true) {
                final int count = Claims.getInstance().getClaimManager().getClaimPlayer(onlinePlayer).getClaims().size();
                return String.valueOf(count);
            } else if (params.equalsIgnoreCase("waypoints_count") == true && player instanceof Player onlinePlayer && onlinePlayer.isOnline() == true) {
                final int count = Claims.getInstance().getWaypointManager().getWaypoints(onlinePlayer).size();
                return String.valueOf(count);
            }
            return null;
        }

    }

}
