package net.skydistrict.claimsgui.commands;

import net.kyori.adventure.text.Component;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.panel.sections.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Panel panel = new Panel(54, Component.text("§f\u7000\u7001"));
            panel.applySection(new MainSection(panel, player));
            panel.open(player);
        }
        return true;
    }
}
