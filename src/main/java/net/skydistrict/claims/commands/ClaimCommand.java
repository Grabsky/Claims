package net.skydistrict.claims.commands;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.UUIDCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.panel.sections.SectionHomes;
import net.skydistrict.claims.panel.sections.SectionMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ClaimCommand implements CommandExecutor {
    private final Claims instance;

    public ClaimCommand(Claims instance) {
        this.instance = instance;
    }

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
                Panel panel = new Panel(54, "§f\u7000\u7100");
                Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionMain(panel, executor, owner.getUuid(), region)), 1L);
                panel.open(executor);
            } else if (executor.getUniqueId() == owner.getUuid()) {
                Panel panel = new Panel(54, "§f\u7000\u7100");
                Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionHomes(panel, executor, owner.getUuid())), 1L);
                panel.open(executor);
            } else {
                executor.sendMessage(Lang.NO_REGION);
            }
        }
        return true;
    }
}
