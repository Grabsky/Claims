package me.grabsky.claims.flags;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.ExitFlag;

public class ExtraFlags {
    public static final Flag<Integer> CLAIM_LEVEL = new IntegerFlag("claim-level");
    public static final Flag<Location> CLAIM_CENTER = new LocationFlag("claim-home");
    public static final Flag<String> GREETING_ACTIONBAR = new StringFlag("greeting-actionbar");
    public static final Flag<String> FAREWELL_ACTIONBAR = new StringFlag("farewell-actionbar");

    public static void registerHandlers() {
        final SessionManager session = WorldGuard.getInstance().getPlatform().getSessionManager();
        session.registerHandler(GreetingActionBarFlag.FACTORY, ExitFlag.FACTORY);
        session.registerHandler(FarewellActionBarFlag.FACTORY, ExitFlag.FACTORY);
    }

    public static void registerFlags() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(CLAIM_LEVEL);
            registry.register(CLAIM_CENTER);
            registry.register(GREETING_ACTIONBAR);
            registry.register(FAREWELL_ACTIONBAR);
        } catch (FlagConflictException e) {
            e.printStackTrace();
        }
    }
}
