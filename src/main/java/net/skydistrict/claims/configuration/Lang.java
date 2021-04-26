package net.skydistrict.claims.configuration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.configuration.components.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

public class Lang {
    private static final Claims instance = Claims.getInstance();
    private static final Component EMPTY_COMPONENT = Component.empty();
    private static final int LANG_VERSION = 1;

    public static Message
            PLAYER_NOT_FOUND,
            MISSING_PERMISSIONS,
            PLAYER_ONLY,
            RELOAD_SUCCESS,
            RELOAD_FAIL,
            NO_CLAIM,
            TOO_CLOSE_TO_SPAWN,
            OVERLAPS_OTHER_CLAIM,
            NOT_MEMBER,
            NOT_OWNER,
            REACHED_MEMBERS_LIMIT,
            REACHED_CLAIMS_LIMIT,
            PLACE_SUCCESS,
            DESTROY_SUCCESS,
            NOT_SNEAKING,
            UPGRADE_SUCCESS,
            TELEPORTING,
            TELEPORT_SUCCESS,
            TELEPORT_FAIL,
            TELEPORT_FAIL_UNKNOWN,
            RESTORE_CLAIM_BLOCK_SUCCESS,
            RESTORE_CLAIM_BLOCK_FAIL,
            CLAIM_BLOCKS_ADDED,
            BLACKLISTED_WORLD;

    public static String
            DEFAULT_GREETING,
            DEFAULT_FAREWELL;

    public static void reload() {
        // Saving default plugin translation file
        final File file = new File(instance.getDataFolder() + "/lang.yml");
        if(!file.exists()) {
            instance.saveResource("lang.yml", false);
        }
        // Overriding...
        final FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (fc.getInt("version") != LANG_VERSION) {
            instance.getLogger().warning("Your lang.yml file is outdated. Some messages may not display properly.");
        }
        // General
        PLAYER_NOT_FOUND = message(fc, "general.player-not-found", true);
        MISSING_PERMISSIONS = message(fc, "general.missing-permissions", true);
        PLAYER_ONLY = message(fc, "general.player-only", true);
        RELOAD_SUCCESS = message(fc, "general.reload-success", true);
        RELOAD_FAIL = message(fc, "general.reload-fail", true);
        // Claims
        NO_CLAIM = message(fc, "claims.no-claim", true);
        TOO_CLOSE_TO_SPAWN = message(fc, "claims.too-close-to-spawn", true);
        OVERLAPS_OTHER_CLAIM = message(fc, "claims.overlaps-other-claim", true);
        NOT_MEMBER = message(fc, "claims.not-member", true);
        NOT_OWNER = message(fc, "claims.not-owner", true);
        REACHED_CLAIMS_LIMIT = message(fc, "claims.reached-claims-limit", true);
        REACHED_MEMBERS_LIMIT = message(fc, "claims.reached-members-limit", false);
        PLACE_SUCCESS = message(fc, "claims.place-success", true);
        DESTROY_SUCCESS = message(fc, "claims.destroy-success", true);
        NOT_SNEAKING = message(fc, "claims.not-sneaking", true);
        UPGRADE_SUCCESS = message(fc, "claims.upgrade-success", false);
        RESTORE_CLAIM_BLOCK_SUCCESS = message(fc, "claims.restore-claim-block-success", true);
        RESTORE_CLAIM_BLOCK_FAIL = message(fc, "claims.restore-claim-block-fail", true);
        CLAIM_BLOCKS_ADDED = message(fc, "claims.claim-blocks-added", true);
        BLACKLISTED_WORLD = message(fc, "claims.blacklisted-world", true);
        // Teleport
        TELEPORTING = message(fc, "teleport.teleporting", false);
        TELEPORT_SUCCESS = message(fc, "teleport.teleport-success", true);
        TELEPORT_FAIL = message(fc, "teleport.teleport-fail", true);
        TELEPORT_FAIL_UNKNOWN = message(fc, "teleport.teleport-fail-unknown", true);
        // Flags
        DEFAULT_GREETING = string(fc, "flags.default-greeting");
        DEFAULT_FAREWELL = string(fc, "flags.default-farewell");
    }

    /**
     * Returns Message value from given path
     */
    public static Message message(FileConfiguration fc, String path, boolean compile) {
        final StringBuilder builder = new StringBuilder();
        if (fc.isList(path)) {
            final List<String> list = fc.getStringList(path);
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i));
                if (i + 1 != list.size()) {
                    builder.append("\n");
                }
            }
        } else {
            builder.append(fc.getString(path));
        }
        if (compile) return new Message(MiniMessage.get().parse(builder.toString()));
        return new Message(builder.toString());
    }

    /**
     * Returns message as a String value from given path
     */
    public static String string(FileConfiguration fc, String path) {
        // If value is single String...
        if (fc.isString(path)) return fc.getString(path);
        // If value is something else... (assuming it's a StringList)
        final List<String> list = fc.getStringList(path);
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i + 1 != list.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * Sends message with placeholders (compiled just before sending)
     */
    public static void send(CommandSender sender, @NotNull Message message, Object... replacements) {
        final String string = message.getString();
        if (string != null && !string.equals("")) {
            sender.sendMessage(MiniMessage.get().parse(MessageFormat.format(string, replacements)));
        }
    }

    /**
     * Sends compiled (static) message
     */
    public static void send(CommandSender sender, Message message) {
        final Component component = message.getComponent();
        if (component != null && component != EMPTY_COMPONENT) {
            sender.sendMessage(component);
        }
    }
}

