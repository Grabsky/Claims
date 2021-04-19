package net.skydistrict.claims.commands;

import me.grabsky.indigo.api.UUIDCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.api.ClaimsAPI;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimPlayer;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.configuration.StaticItems;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.panel.sections.SectionHomes;
import net.skydistrict.claims.panel.sections.SectionMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// TO-DO: Fix /claim <name> not working properly (for some reason)
public class ClaimCommand implements CommandExecutor {
    private final Claims instance;

    public ClaimCommand(Claims instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player executor = (Player) sender;
            UUID ownerUniqueId = executor.getUniqueId();
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("get") && executor.hasPermission("skydistrict.claims.get")) {
                    executor.getInventory().addItem(StaticItems.getClaimBlock(0));
                    executor.getInventory().addItem(StaticItems.getClaimBlock(1));
                    executor.getInventory().addItem(StaticItems.getClaimBlock(2));
                    executor.getInventory().addItem(StaticItems.getClaimBlock(3));
                    executor.getInventory().addItem(StaticItems.getClaimBlock(4));
                    return true;
                } else if (executor.hasPermission("skydistrict.claims.panel.others")) {
                    ownerUniqueId = UUIDCache.get(args[0]);
                    if (ownerUniqueId == null) {
                        executor.sendMessage(Lang.PLAYER_NOT_FOUND);
                        return true;
                    }
                } else {
                    executor.sendMessage(Lang.MISSING_PERMISSIONS);
                    return true;
                }
            }
            // Opening Claim management panel for of specific player
            ClaimPlayer owner = ClaimsAPI.getClaimPlayer(ownerUniqueId);
            if (owner.hasClaim()) {
                Claim claim = owner.getClaim();
                Panel panel = new Panel(54, "§f\u7000\u7100");
                Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionMain(panel, executor, owner.getUniqueId(), claim)), 1L);
                panel.open(executor);
            } else {
                Panel panel = new Panel(54, "§f\u7000\u7100");
                Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionHomes(panel, executor, owner.getUniqueId())), 1L);
                panel.open(executor);
            }
        }
        return true;
    }
}
