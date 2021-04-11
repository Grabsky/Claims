package net.skydistrict.claimsgui;

import net.skydistrict.claimsgui.commands.ClaimCommand;
import net.skydistrict.claimsgui.panel.PanelManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClaimsGUI extends JavaPlugin {
    // Instances
    private static ClaimsGUI instance;
    private PanelManager panelManager;
    // Getters
    public static ClaimsGUI getInstance() { return instance; }
    public PanelManager getPanelManager() { return panelManager; }

    @Override
    public void onEnable() {
        instance = this;
        this.panelManager = new PanelManager(this);
        this.getCommand("claim").setExecutor(new ClaimCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
