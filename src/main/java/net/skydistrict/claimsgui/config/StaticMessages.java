package net.skydistrict.claimsgui.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class StaticMessages {
    // Instances
    private MiniMessage mini;
    // Messages
    public static Component MESSAGE;

    // Constructor
    public StaticMessages() {
        mini = MiniMessage.get();
        this.parse();
    }

    public void parse() {
        MESSAGE = mini.parse("<rainbow>I got clicked!");
    }
}
