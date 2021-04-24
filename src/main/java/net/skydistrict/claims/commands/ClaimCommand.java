package net.skydistrict.claims.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.grabsky.indigo.api.UUIDCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.api.ClaimsAPI;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimPlayer;
import net.skydistrict.claims.configuration.Config;
import net.skydistrict.claims.configuration.Items;
import net.skydistrict.claims.configuration.Lang;
import net.skydistrict.claims.flags.ClaimFlags;
import net.skydistrict.claims.panel.Panel;
import net.skydistrict.claims.panel.sections.SectionHomes;
import net.skydistrict.claims.panel.sections.SectionMain;
import net.skydistrict.claims.utils.ClaimH;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClaimCommand implements CommandExecutor {
    private final Claims instance;

    public ClaimCommand(Claims instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            final Player executor = (Player) sender;
            UUID ownerUniqueId = executor.getUniqueId();
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("get") && executor.hasPermission("skydistrict.claims.get")) {
                    executor.getInventory().addItem(Items.getClaimBlock(0));
                    executor.getInventory().addItem(Items.getClaimBlock(1));
                    executor.getInventory().addItem(Items.getClaimBlock(2));
                    executor.getInventory().addItem(Items.getClaimBlock(3));
                    executor.getInventory().addItem(Items.getClaimBlock(4));
                    return true;
                } else if (args[0].equalsIgnoreCase("reload") && executor.hasPermission("skydistrict.claims.reload")) {
                    if (Claims.reload()) {
                        Lang.send(sender, Lang.RELOAD_SUCCESS);
                        return true;
                    }
                    Lang.send(sender, Lang.RELOAD_FAIL);
                    return true;
                } else if (args[0].equalsIgnoreCase("fix") && executor.hasPermission("skydistrict.claims.fix")) {
                    Location loc = executor.getLocation();
                    for (ProtectedRegion region : instance.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
                        if (region.getId().startsWith(Config.REGION_PREFIX)) {
                            // Both variables shouldn't be null unless claim was manually modified
                            Location center = BukkitAdapter.adapt(region.getFlag(ClaimFlags.CLAIM_CENTER));
                            Material type = ClaimH.getClaimLevel(region.getFlag(ClaimFlags.CLAIM_LEVEL)).getBlockMaterial();
                            PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                                chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                                Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_SUCCESS);
                            });
                            return true;
                        }
                    }
                    Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_FAIL);
                    return true;
                } else if (executor.hasPermission("skydistrict.claims.panel.others")) {
                    ownerUniqueId = UUIDCache.get(args[0]);
                    if (ownerUniqueId == null) {
                        Lang.send(sender, Lang.PLAYER_NOT_FOUND);
                        return true;
                    }
                } else {
                    Lang.send(sender, Lang.MISSING_PERMISSIONS);
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
            return true;
        }
        Lang.send(sender, Lang.PLAYER_ONLY);
        return true;
    }
}
