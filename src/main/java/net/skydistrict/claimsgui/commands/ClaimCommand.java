package net.skydistrict.claimsgui.commands;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.skydistrict.claimsgui.configuration.Lang;
import net.skydistrict.claimsgui.panel.Panel;
import net.skydistrict.claimsgui.panel.sections.SectionHomes;
import net.skydistrict.claimsgui.panel.sections.SectionMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ClaimCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player executor = (Player) sender;
            PSPlayer owner;
            if (args.length == 0) {
                owner = PSPlayer.fromPlayer(executor);
            } else {
                if (executor.hasPermission("skydistrict.claims.others")) {
                    UUID uuid = UUIDCache.getUUIDFromName(args[0]);
                    if (uuid != null) {
                        owner = PSPlayer.fromUUID(uuid);
                    } else {
                        executor.sendMessage(Lang.PLAYER_NOT_FOUND);
                        return true;
                    }
                } else {
                    executor.sendMessage(Lang.MISSING_PERMISSIONS);
                    return true;
                }
            }
            List<PSRegion> regions = owner.getPSRegions(Bukkit.getWorlds().get(0), false);
            if (regions.size() > 0) {
                PSRegion region = regions.get(0);
                Panel panel = new Panel(54, "§f\u7000\u7001");
                panel.applySection(new SectionMain(panel, executor, owner.getUuid(), region));
                panel.open(executor);
            } else if (executor.getUniqueId() == owner.getUuid()) {
                Panel panel = new Panel(54, "§f\u7000\u7006");
                panel.applySection(new SectionHomes(panel, executor, owner.getUuid()));
                panel.open(executor);
            } else {
                executor.sendMessage(Lang.NO_REGION);
            }
        }
        return true;
    }
}
