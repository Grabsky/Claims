package net.skydistrict.claims.utils;

import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;

import java.util.Arrays;
import java.util.List;

public class FlagsH {

    /** Returns list of options (flag values) */
    public static List<Object> getOptions(Flag<?> flagType) {
        if (flagType instanceof StateFlag) {
            return Arrays.asList(StateFlag.State.ALLOW, StateFlag.State.DENY);
        }
        // Comparing instances is useless in some cases - let's check the name instead
        switch (flagType.getName()) {
            case "weather-lock":
                return Arrays.asList(null, WeatherTypes.CLEAR, WeatherTypes.RAIN, WeatherTypes.THUNDER_STORM);
            case "time-lock":
                return Arrays.asList(null, "0", "6000", "12000", "18000");
        }
        return null;
    }

    /** Returns list of options (formatted flag values) displayed in GUI */
    public static List<String> getFormattedOptions(Flag<?> flagType) {
        if (flagType instanceof StateFlag) {
            return Arrays.asList("Włączone", "Wyłączone");
        }
        // Comparing instances is useless in some cases - let's check the name instead
        switch (flagType.getName()) {
            case "weather-lock":
                return Arrays.asList("Wyłączone", "Słonecznie", "Deszcz", "Burza");
            case "time-lock":
                return Arrays.asList("Wyłączone", "Rano (6:00)", "Południe (12:00)", "Wieczór (18:00)", "Północ (00:00)");
        }
        return null;
    }
}
