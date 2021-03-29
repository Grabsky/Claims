package net.skydistrict.claimsgui.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
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
            Panel panel = new Panel(45, (MiniMessage.get().parse("<rainbow>Claim")));
            panel.open(player);
            new MainSection(panel, player);
        }
        return true;
    }
}
