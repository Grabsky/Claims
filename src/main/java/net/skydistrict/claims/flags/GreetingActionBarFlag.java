package net.skydistrict.claims.flags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;

public class GreetingActionBarFlag extends FlagValueChangeHandler<String> {
    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<GreetingActionBarFlag> {
        @Override
        public GreetingActionBarFlag create(Session session) {
            return new GreetingActionBarFlag(session);
        }
    }

    public GreetingActionBarFlag(Session session) {
        super(session, ClaimFlags.GREETING_ACTIONBAR);
    }

    @Override
    protected void onInitialValue(LocalPlayer lp, ApplicableRegionSet regionSet, String value) {
        // Nothing
    }

    @Override
    protected boolean onSetValue(LocalPlayer lp, Location from, Location to, ApplicableRegionSet regionSet, String currentValue, String lastValue, MoveType moveType) {
        if (currentValue != null && !currentValue.equals(lastValue)) {
            Player player = BukkitAdapter.adapt(lp);
            if(player != null && player.isOnline()) {
                player.sendActionBar(currentValue);
            }
        }
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer lp, Location from, Location to, ApplicableRegionSet toSet, String lastValue, MoveType moveType) {
        return true;
    }
}
