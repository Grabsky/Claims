package me.grabsky.claims.configuration;

import me.grabsky.claims.Claims;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Lang {
    private final Claims instance;
    private final ConsoleLogger consoleLogger;
    private final File file;
    private FileConfiguration fileConfiguration;

    public static Component PLAYER_NOT_FOUND;
    public static Component MISSING_PERMISSIONS;
    public static Component PLAYER_ONLY;
    public static Component RELOAD_SUCCESS;
    public static Component RELOAD_FAIL;
    public static Component NO_CLAIM;
    public static Component TOO_CLOSE_TO_SPAWN;
    public static Component OVERLAPS_OTHER_CLAIM;
    public static Component NOT_MEMBER;
    public static Component NOT_OWNER;
    public static Component REACHED_CLAIMS_LIMIT;
    public static Component PLACE_SUCCESS;
    public static Component DESTROY_SUCCESS;
    public static Component NOT_SNEAKING;
    public static Component TELEPORT_SUCCEED;
    public static Component TELEPORT_CANCELLED;
    public static Component TELEPORT_FAILED;
    public static Component RESTORE_CLAIM_BLOCK_SUCCESS;
    public static Component RESTORE_CLAIM_BLOCK_FAIL;
    public static Component CLAIM_BLOCKS_ADDED;
    public static Component BLACKLISTED_WORLD;
    public static Component SET_HOME_SUCCESS;
    public static Component SET_HOME_FAIL;

    public static String REACHED_MEMBERS_LIMIT;
    public static String UPGRADE_SUCCESS;
    public static String TELEPORTING;

    public static String DEFAULT_GREETING;
    public static String DEFAULT_FAREWELL;

    public Lang(Claims instance) {
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "lang.yml");
    }

    public void reload() {
        // Saving default plugin translation file
        if(!file.exists()) {
            instance.saveResource("lang.yml", false);
        }
        // Overriding...
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (fileConfiguration.getInt("version") != 2) {
            consoleLogger.error("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        // General
        PLAYER_NOT_FOUND = component("general.player-not-found");
        MISSING_PERMISSIONS = component("general.missing-permissions");
        PLAYER_ONLY = component("general.player-only");
        RELOAD_SUCCESS = component("general.reload-success");
        RELOAD_FAIL = component("general.reload-fail");
        // Claims
        NO_CLAIM = component("claims.no-claim");
        TOO_CLOSE_TO_SPAWN = component("claims.too-close-to-spawn");
        OVERLAPS_OTHER_CLAIM = component("claims.overlaps-other-claim");
        NOT_MEMBER = component("claims.not-member");
        NOT_OWNER = component("claims.not-owner");
        REACHED_CLAIMS_LIMIT = component("claims.reached-claims-limit");
        REACHED_MEMBERS_LIMIT = string("claims.reached-members-limit");
        PLACE_SUCCESS = component("claims.place-success");
        DESTROY_SUCCESS = component("claims.destroy-success");
        NOT_SNEAKING = component("claims.not-sneaking");
        UPGRADE_SUCCESS = string("claims.upgrade-success");
        RESTORE_CLAIM_BLOCK_SUCCESS = component("claims.restore-claim-block-success");
        RESTORE_CLAIM_BLOCK_FAIL = component("claims.restore-claim-block-fail");
        CLAIM_BLOCKS_ADDED = component("claims.claim-blocks-added");
        BLACKLISTED_WORLD = component("claims.blacklisted-world");
        SET_HOME_SUCCESS = component("claims.set-home-success");
        SET_HOME_FAIL = component("claims.set-home-fail");
        // Teleport
        TELEPORTING = string("teleport.teleporting");
        TELEPORT_SUCCEED = component("teleport.teleport-succeed");
        TELEPORT_CANCELLED = component("teleport.teleport-cancelled");
        TELEPORT_FAILED = component("teleport.teleport-failed");
        // Flags
        DEFAULT_GREETING = fileConfiguration.getString("flags.default-greeting", "");
        DEFAULT_FAREWELL = fileConfiguration.getString("flags.default-farewell", "");
    }

    private String string(String path) {
        final StringBuilder sb = new StringBuilder();
        if (fileConfiguration.isList(path)) {
            final List<String> list = fileConfiguration.getStringList(path);
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i + 1 != list.size()) {
                    sb.append("\n");
                }
            }
        } else {
            sb.append(fileConfiguration.getString(path));
        }
        return sb.toString();
    }

    private Component component(String path) {
        return LegacyComponentSerializer.legacySection().deserialize(this.string(path));
    }

    /** Sends parsed component */
    public static void send(@NotNull CommandSender sender, @NotNull Component component) {
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }

    /** Parses and sends component */
    public static void send(@NotNull CommandSender sender, @NotNull String text) {
        final Component component = LegacyComponentSerializer.legacySection().deserialize(text);
        if (component != Component.empty()) {
            sender.sendMessage(component);
        }
    }
}

