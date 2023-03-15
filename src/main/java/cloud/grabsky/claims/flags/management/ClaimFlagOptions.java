package cloud.grabsky.claims.flags.management;

import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.protection.flags.StateFlag;
import me.grabsky.indigo.utils.Components;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class ClaimFlagOptions {
    private final Object[] raw;
    private final Component[] formatted;

    public ClaimFlagOptions(final Object[] raw, final String[] formatted) {
        this.raw = raw;
        this.formatted = new Component[formatted.length];
        for (int i = 0; i < formatted.length; i++) {
            this.formatted[i] = Components.parseSection(formatted[i]).decoration(TextDecoration.ITALIC, false);
        }
    }

    public Object[] getRaw() {
        return raw;
    }

    public Component[] getFormatted() {
        return formatted;
    }

    /* Supported Flag Options */

    public static ClaimFlagOptions STATE = new ClaimFlagOptions(new StateFlag.State[] { StateFlag.State.ALLOW, StateFlag.State.DENY }, new String[] { "Włączone", "Wyłączone" });
    public static ClaimFlagOptions WEATHER = new ClaimFlagOptions(new WeatherType[] { null, WeatherTypes.CLEAR, WeatherTypes.RAIN, WeatherTypes.THUNDER_STORM }, new String[] { "Wyłączone", "Słonecznie", "Deszcz", "Burza" });
    public static ClaimFlagOptions TIME = new ClaimFlagOptions(new String[] { null, "0", "6000", "12000", "18000" }, new String[] { "Wyłączone", "Rano (6:00)", "Południe (12:00)", "Wieczór (18:00)", "Północ (00:00)" });
}
