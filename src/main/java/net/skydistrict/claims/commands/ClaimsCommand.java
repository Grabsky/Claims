package net.skydistrict.claims.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.grabsky.indigo.user.UserCache;
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

public class ClaimsCommand implements CommandExecutor {
    private final Claims instance;

    public ClaimsCommand(Claims instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                final Player executor = (Player) sender;
                // Opening Claim menu of executor, to executor
                this.openClaimMenu(executor, executor.getUniqueId());
                return true;
            }
            Lang.send(sender, Lang.PLAYER_ONLY);
            return true;
        } else {
            // Command: /claim reload
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("skydistrict.claims.reload")) {
                    if (instance.reload()) {
                        Lang.send(sender, Lang.RELOAD_SUCCESS);
                        return true;
                    }
                    Lang.send(sender, Lang.RELOAD_FAIL);
                    return true;
                }
                Lang.send(sender, Lang.MISSING_PERMISSIONS);
                return true;
            // Command: /claim get
            } else if (args[0].equalsIgnoreCase("get")) {
                if (sender.hasPermission("skydistrict.claims.get")) {
                    if (sender instanceof Player) {
                        final Player executor = (Player) sender;
                        executor.getInventory().addItem(Items.getClaimBlock(0));
                        executor.getInventory().addItem(Items.getClaimBlock(1));
                        executor.getInventory().addItem(Items.getClaimBlock(2));
                        executor.getInventory().addItem(Items.getClaimBlock(3));
                        executor.getInventory().addItem(Items.getClaimBlock(4));
                        Lang.send(sender, Lang.CLAIM_BLOCKS_ADDED);
                        return true;
                    }
                    Lang.send(sender, Lang.PLAYER_ONLY);
                    return true;
                }
                Lang.send(sender, Lang.MISSING_PERMISSIONS);
                return true;
            // Command: /claim fix
            } else if (args[0].equalsIgnoreCase("fix")) {
                if (sender.hasPermission("skydistrict.claims.fix")) {
                    if (sender instanceof Player) {
                        final Player executor = (Player) sender;
                        final Location loc = executor.getLocation();
                        for (ProtectedRegion region : instance.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
                            if (region.getId().startsWith(Config.REGION_PREFIX)) {
                                // Both variables shouldn't be null unless claim was manually modified
                                final Location center = BukkitAdapter.adapt(region.getFlag(ClaimFlags.CLAIM_CENTER));
                                final Material type = ClaimH.getClaimLevel(region.getFlag(ClaimFlags.CLAIM_LEVEL)).getBlockMaterial();
                                PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                                    chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                                    Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_SUCCESS);
                                });
                                return true;
                            }
                        }
                        Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_FAIL);
                        return true;
                    }
                    Lang.send(sender, Lang.PLAYER_ONLY);
                    return true;
                }
                Lang.send(sender, Lang.MISSING_PERMISSIONS);
                return true;
            // Command: /claim <name>
            } else if (sender.hasPermission("skydistrict.claims.other")) {
                final Player executor = (Player) sender;
                final UUID ownerUniqueId = UserCache.get(args[0]).getUniqueId();
                if (ownerUniqueId != null) {
                    this.openClaimMenu(executor, ownerUniqueId);
                    return true;
                }
                Lang.send(sender, Lang.PLAYER_NOT_FOUND);
                return true;
            }
        }
        Lang.send(sender, Lang.MISSING_PERMISSIONS);
        return true;
    }

    private void openClaimMenu(Player executor, UUID ownerUniqueId) {
        final ClaimPlayer owner = ClaimsAPI.getClaimPlayer(ownerUniqueId);
        final Panel panel = new Panel(54, "§f\u7000\u7100");
        if (owner.hasClaim()) {
            final Claim claim = owner.getClaim();
            Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionMain(panel, executor, owner.getUniqueId(), claim)), 1L);
        } else {
            Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionHomes(panel, executor, owner.getUniqueId())), 1L);
        }
        panel.open(executor);
    }
}
