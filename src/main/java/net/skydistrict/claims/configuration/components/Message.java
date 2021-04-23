package net.skydistrict.claims.configuration.components;

import net.kyori.adventure.text.Component;

public class Message {
    private String string;
    private Component component;

    public Message(String string) {
        this.string = string;
    }

    public Message(Component component) {
        this.component = component;
    }

    public String getString() {
        return string;
    }

    public Component getComponent() {
        return component;
    }
}
