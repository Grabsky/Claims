package me.grabsky.claims.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.configuration.Config;
import me.grabsky.claims.configuration.Items;
import me.grabsky.claims.configuration.Lang;
import me.grabsky.claims.flags.ClaimFlags;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.panel.sections.SectionHomes;
import me.grabsky.claims.panel.sections.SectionMain;
import me.grabsky.claims.utils.ClaimsUtils;
import me.grabsky.indigo.framework.BaseCommand;
import me.grabsky.indigo.framework.ExecutorType;
import me.grabsky.indigo.framework.annotations.DefaultCommand;
import me.grabsky.indigo.framework.annotations.SubCommand;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class ClaimsCommand extends BaseCommand {
    private final Claims instance;
    private final ClaimManager manager;
    private final List<String> completions = Arrays.asList("get", "fix", "reload");

    public ClaimsCommand(Claims instance) {
        super("claims", Arrays.asList("claim", "teren"), "skydistrict.command.claims", ExecutorType.ALL);
        this.instance = instance;
        this.manager = instance.getClaimManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onClaims(sender);
        } else {
            switch (args[0]) {
                case "reload" -> this.onClaimsReload(sender);
                case "get" -> this.onClaimsGet(sender);
                case "fix" -> this.onClaimsFix(sender);
                default -> this.onClaimsPlayer(sender, args[0]);
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, int index) {
        if (index == 0) {
            final List<String> list = new ArrayList<>(UserCache.getNamesOfOnlineUsers());
            list.addAll(completions);
            return list;
        }
        return Collections.emptyList();
    }

    @DefaultCommand
    private void onClaims(final CommandSender sender) {
        if (sender instanceof Player executor) {
            this.openClaimMenu(executor, executor.getUniqueId());
            return;
        }
        Lang.send(sender, Lang.PLAYER_ONLY);
    }

    @SubCommand
    private void onClaimsReload(final CommandSender sender) {
        if (sender.hasPermission("skydistrict.command.claims.reload")) {
            if (instance.reload()) {
                Lang.send(sender, Lang.RELOAD_SUCCESS);
                return;
            }
            Lang.send(sender, Lang.RELOAD_FAIL);
            return;
        }
        Lang.send(sender, Lang.MISSING_PERMISSIONS);
    }

    @SubCommand
    private void onClaimsFix(final CommandSender sender) {
        if (sender instanceof Player executor) {
            if (executor.hasPermission("skydistrict.command.claims.fix")) {
                final Location loc = executor.getLocation();
                for (ProtectedRegion region : instance.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
                    if (region.getId().startsWith(Config.REGION_PREFIX)) {
                        // Both variables shouldn't be null unless claim was manually modified
                        final Location center = BukkitAdapter.adapt(region.getFlag(ClaimFlags.CLAIM_CENTER));
                        final Material type = ClaimsUtils.getClaimLevel(region.getFlag(ClaimFlags.CLAIM_LEVEL)).getBlockMaterial();
                        PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                            chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                            Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_SUCCESS);
                        });
                        return;
                    }
                }
                Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_FAIL);
                return;
            }
            Lang.send(sender, Lang.MISSING_PERMISSIONS);
            return;
        }
        Lang.send(sender, Lang.PLAYER_ONLY);
    }

    @SubCommand
    private void onClaimsGet(final CommandSender sender) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("skydistrict.command.claims.get")) {
                final Inventory inventory = executor.getInventory();
                inventory.addItem(Items.getClaimBlock(0)); // COAL
                inventory.addItem(Items.getClaimBlock(1)); // IRON
                inventory.addItem(Items.getClaimBlock(2)); // GOLD
                inventory.addItem(Items.getClaimBlock(3)); // DIAMOND
                inventory.addItem(Items.getClaimBlock(4)); // EMERALD
                Lang.send(sender, Lang.CLAIM_BLOCKS_ADDED);
                return;
            }
            Lang.send(sender, Lang.MISSING_PERMISSIONS);
            return;
        }
        Lang.send(sender, Lang.PLAYER_ONLY);
    }

    @SubCommand
    private void onClaimsPlayer(final CommandSender sender, final String name) {
        if (sender instanceof Player executor) {
            if (UserCache.contains(name)) {
                this.openClaimMenu(executor, UserCache.get(name).getUniqueId());
                return;
            }
            Lang.send(sender, Lang.PLAYER_NOT_FOUND);
            return;
        }
        Lang.send(sender, Lang.PLAYER_ONLY);
    }

    private void openClaimMenu(Player executor, UUID ownerUniqueId) {
        final ClaimPlayer owner = manager.getClaimPlayer(ownerUniqueId);
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
