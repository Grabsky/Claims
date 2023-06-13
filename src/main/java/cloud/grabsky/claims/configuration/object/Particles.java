package cloud.grabsky.claims.configuration.object;

import com.squareup.moshi.Json;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Particle;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class Particles {

    @Getter(AccessLevel.PUBLIC)
    private final Particle particle;

    @Getter(AccessLevel.PUBLIC)
    private final int amount;

    @Getter(AccessLevel.PUBLIC)
    private final float speed;

    @Json(name = "offset_x")
    @Getter(AccessLevel.PUBLIC)
    private final double offestX;

    @Json(name = "offset_y")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetY;

    @Json(name = "offset_z")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetZ;

}
