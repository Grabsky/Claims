package net.skydistrict.claims.flags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import net.skydistrict.claims.utils.TextH;
import org.bukkit.entity.Player;

public class FarewellActionBarFlag extends FlagValueChangeHandler<String> {
    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<FarewellActionBarFlag> {
        @Override
        public FarewellActionBarFlag create(Session session) {
            return new FarewellActionBarFlag(session);
        }
    }

    public FarewellActionBarFlag(Session session) {
        super(session, ClaimFlags.FAREWELL_ACTIONBAR);
    }

    @Override
    protected void onInitialValue(LocalPlayer lp, ApplicableRegionSet regionSet, String value) {
        // Nothing
    }

    @Override
    protected boolean onSetValue(LocalPlayer lp, Location from, Location to, ApplicableRegionSet regionSet, String currentValue, String lastValue, MoveType moveType) {
        if (regionSet.size() > 0) return true;
        if (lastValue != null && !lastValue.equals(currentValue)) {
            Player player = BukkitAdapter.adapt(lp);
            if(player != null && player.isOnline()) {
                player.sendActionBar(TextH.color(lastValue));
            }
        }
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer lp, Location from, Location to, ApplicableRegionSet regionSet, String lastValue, MoveType moveType) {
        Player player = BukkitAdapter.adapt(lp);
        if (player != null && player.isOnline() && lastValue != null) {
            player.sendActionBar(TextH.color(lastValue));
        }
        return true;
    }
}
