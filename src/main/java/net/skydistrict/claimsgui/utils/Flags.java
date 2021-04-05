package net.skydistrict.claimsgui.utils;

import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.WeatherTypeFlag;

import java.util.Arrays;
import java.util.List;

public class Flags {

    public static List<Object> getOptions(Flag<?> flagType) {
        if (flagType instanceof StateFlag) {
            return Arrays.asList(StateFlag.State.ALLOW, StateFlag.State.DENY);
        }
        if (flagType instanceof WeatherTypeFlag) {
            return Arrays.asList(WeatherTypes.CLEAR, WeatherTypes.RAIN, WeatherTypes.THUNDER_STORM);
        }
        return null;
    }

    public static List<String> getFormattedOptions(Flag<?> flagType) {
        if (flagType instanceof StateFlag) {
            return Arrays.asList("Włączone", "Wyłączone");
        }
        if (flagType instanceof WeatherTypeFlag) {
            return Arrays.asList("Słonecznie", "Deszcz", "Burza");
        }
        return null;
    }
}
