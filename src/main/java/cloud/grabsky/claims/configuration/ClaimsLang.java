package cloud.grabsky.claims.configuration;

import cloud.grabsky.claims.Claims;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.lang.AbstractLang;
import me.grabsky.indigo.logger.ConsoleLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ClaimsLang extends AbstractLang {
    private final Claims instance;
    private final ConsoleLogger consoleLogger;
    private final File file;

    public static Component PLAYER_HAS_NO_CLAIM;
    public static Component YOU_DONT_HAVE_A_CLAIM;
    public static Component TOO_CLOSE_TO_SPAWN;
    public static Component OVERLAPS_OTHER_CLAIM;
    public static Component NOT_MEMBER;
    public static Component NOT_OWNER;
    public static Component REACHED_CLAIMS_LIMIT;
    public static Component PLACE_SUCCESS;
    public static Component DESTROY_SUCCESS;
    public static Component NOT_SNEAKING;
    public static Component RESTORE_CLAIM_BLOCK_SUCCESS;
    public static Component RESTORE_CLAIM_BLOCK_FAIL;
    public static Component CLAIM_BLOCKS_ADDED;
    public static Component BLACKLISTED_WORLD;
    public static Component SET_HOME_SUCCESS;
    public static Component SET_HOME_FAIL;

    public static String REACHED_MEMBERS_LIMIT;
    public static String UPGRADE_SUCCESS;

    public static String DEFAULT_GREETING;
    public static String DEFAULT_FAREWELL;

    public ClaimsLang(Claims instance) {
        super(instance);
        this.instance = instance;
        this.consoleLogger = instance.getConsoleLogger();
        this.file = new File(instance.getDataFolder() + File.separator + "lang.yml");
    }

    @Override
    public void reload() {
        // Saving default plugin translation file
        if(!file.exists()) {
            instance.saveResource("lang.yml", false);
        }
        // Overriding...
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (fileConfiguration.getInt("version") != 3) {
            consoleLogger.error(Global.OUTDATED_LANG);
        }
        // Claims
        PLAYER_HAS_NO_CLAIM = component("claims.player-has-no-claim");
        YOU_DONT_HAVE_A_CLAIM = component ("claims.you-dont-have-a-claim");
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
        // Flags
        DEFAULT_GREETING = fileConfiguration.getString("flags.default-greeting", "");
        DEFAULT_FAREWELL = fileConfiguration.getString("flags.default-farewell", "");
    }
}

