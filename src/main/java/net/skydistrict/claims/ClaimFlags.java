package net.skydistrict.claims;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class ClaimFlags {
    public static final Flag<Integer> CLAIM_LEVEL = new IntegerFlag("claim-level");

    public static void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(CLAIM_LEVEL);
        } catch (FlagConflictException e) {
            e.printStackTrace();
        }
    }
}
