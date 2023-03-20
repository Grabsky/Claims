package cloud.grabsky.claims.flags;

import cloud.grabsky.claims.Claims;
import cloud.grabsky.claims.flags.object.FixedTime;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientTimeFlag extends FlagValueChangeHandler<FixedTime> {

    public ClientTimeFlag(final Session session) {
        super(session, Claims.CustomFlag.CLIENT_TIME);
    }

    @Override
    protected void onInitialValue(
            final LocalPlayer localPlayer,
            final ApplicableRegionSet regionSet,
            final @Nullable FixedTime value
    ) {
        if (value != null) {
            localPlayer.setPlayerTime(value.getTicks(), false);
        } else {
            localPlayer.resetPlayerTime();
        }
    }

    @Override
    protected boolean onSetValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet regionSet,
            final @NotNull FixedTime currentValue,
            final @Nullable FixedTime lastValue,
            final MoveType moveType
    ) {
        // Updating...
        localPlayer.setPlayerTime(currentValue.getTicks(), false);
        // Returning true as to allow movement
        return true;
    }

    @Override
    protected boolean onAbsentValue(
            final LocalPlayer localPlayer,
            final Location from,
            final Location to,
            final ApplicableRegionSet regionSet,
            final @Nullable FixedTime lastValue,
            final MoveType moveType
    ) {
        // Removing...
        localPlayer.resetPlayerTime();
        // Returning true as to allow movement
        return true;
    }

    public static final Factory<ClientTimeFlag> FACTORY = new Handler.Factory<>() {

        @Override
        public ClientTimeFlag create(final Session session) {
            return new ClientTimeFlag(session);
        }

    };

}
