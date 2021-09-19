package me.grabsky.claims.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.grabsky.claims.Claims;
import me.grabsky.claims.claims.Claim;
import me.grabsky.claims.claims.ClaimManager;
import me.grabsky.claims.claims.ClaimPlayer;
import me.grabsky.claims.configuration.ClaimsConfig;
import me.grabsky.claims.configuration.ClaimsLang;
import me.grabsky.claims.configuration.Items;
import me.grabsky.claims.flags.ClaimFlags;
import me.grabsky.claims.panel.Panel;
import me.grabsky.claims.panel.sections.SectionHomes;
import me.grabsky.claims.panel.sections.SectionMain;
import me.grabsky.claims.utils.ClaimsUtils;
import me.grabsky.indigo.configuration.Global;
import me.grabsky.indigo.framework.commands.BaseCommand;
import me.grabsky.indigo.framework.commands.Context;
import me.grabsky.indigo.framework.commands.ExecutorType;
import me.grabsky.indigo.framework.commands.annotations.DefaultCommand;
import me.grabsky.indigo.framework.commands.annotations.SubCommand;
import me.grabsky.indigo.user.UserCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ClaimsCommand extends BaseCommand {
    private final Claims instance;
    private final ClaimManager manager;

    public ClaimsCommand(Claims instance) {
        super("claims", Arrays.asList("claim", "teren"), "claims.command.claims", ExecutorType.ALL);
        this.instance = instance;
        this.manager = instance.getClaimManager();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Context context, int index) {
        if (index == 0) return List.of("edit", "fix", "get", "reload");
        else return switch (context.get(0)) {
            case "edit" -> null;
            default -> Collections.emptyList();
        };
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.onClaims(sender);
        } else switch (args[0]) {
            case "edit" -> {
                if (args.length == 2) {
                    this.onClaimsEdit(sender, args[1]);
                    return;
                }
                ClaimsLang.send(sender, Global.CORRECT_USAGE + "/claims edit <player>");
            }
            case "fix" -> this.onClaimsFix(sender);
            case "get" -> this.onClaimsGet(sender);
            case "reload" -> this.onClaimsReload(sender);

        }
    }

    @DefaultCommand
    private void onClaims(final CommandSender sender) {
        if (sender instanceof Player executor) {
            this.openClaimMenu(executor, executor.getUniqueId());
            return;
        }
        ClaimsLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    public void onClaimsEdit(final CommandSender sender, final String ownerName) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("claims.command.claims.edit")) {
                if (UserCache.contains(ownerName)) {
                    this.openClaimMenu(executor, UserCache.get(ownerName).getUniqueId());
                    return;
                }
                ClaimsLang.send(sender, Global.PLAYER_NOT_FOUND);
                return;
            }
            ClaimsLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        ClaimsLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    private void onClaimsFix(final CommandSender sender) {
        if (sender instanceof Player executor) {
            if (executor.hasPermission("claims.command.claims.fix")) {
                final Location loc = executor.getLocation();
                for (ProtectedRegion region : instance.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
                    if (region.getId().startsWith(ClaimsConfig.REGION_PREFIX)) {
                        // Both variables shouldn't be null unless claim was manually modified
                        final Location center = BukkitAdapter.adapt(region.getFlag(ClaimFlags.CLAIM_CENTER));
                        final Material type = ClaimsUtils.getClaimLevel(region.getFlag(ClaimFlags.CLAIM_LEVEL)).getBlockMaterial();
                        PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                            chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                            ClaimsLang.send(sender, ClaimsLang.RESTORE_CLAIM_BLOCK_SUCCESS);
                        });
                        return;
                    }
                }
                ClaimsLang.send(sender, ClaimsLang.RESTORE_CLAIM_BLOCK_FAIL);
                return;
            }
            ClaimsLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        ClaimsLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    private void onClaimsGet(final CommandSender sender) {
        if (sender instanceof Player executor) {
            if (sender.hasPermission("claims.command.claims.get")) {
                final Inventory inventory = executor.getInventory();
                inventory.addItem(Items.getClaimBlock(0)); // COAL
                inventory.addItem(Items.getClaimBlock(1)); // IRON
                inventory.addItem(Items.getClaimBlock(2)); // GOLD
                inventory.addItem(Items.getClaimBlock(3)); // DIAMOND
                inventory.addItem(Items.getClaimBlock(4)); // EMERALD
                inventory.addItem(Items.UPGRADE_CRYSTAL); // UPGRADE_CRYSTAL
                ClaimsLang.send(sender, ClaimsLang.CLAIM_BLOCKS_ADDED);
                return;
            }
            ClaimsLang.send(sender, Global.MISSING_PERMISSIONS);
            return;
        }
        ClaimsLang.send(sender, Global.PLAYER_ONLY_COMMAND);
    }

    @SubCommand
    private void onClaimsReload(final CommandSender sender) {
        if (sender.hasPermission("claims.command.claims.reload")) {
            if (instance.reload()) {
                ClaimsLang.send(sender, Global.RELOAD_SUCCESS);
                return;
            }
            ClaimsLang.send(sender, Global.RELOAD_FAIL);
            return;
        }
        ClaimsLang.send(sender, Global.MISSING_PERMISSIONS);
    }

    private void openClaimMenu(Player executor, UUID ownerUniqueId) {
        final ClaimPlayer owner = manager.getClaimPlayer(ownerUniqueId);
        final Panel panel = new Panel(54, "§f\u7000\u7100");
        if (owner.hasClaim()) {
            final Claim claim = owner.getClaim();
            Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionMain(panel, executor, owner.getUniqueId(), claim)), 1L);
        } else if (owner.hasRelatives()) {
            Bukkit.getScheduler().runTaskLater(instance, () -> panel.applySection(new SectionHomes(panel, executor, owner.getUniqueId(), null)), 1L);
        } else {
            ClaimsLang.send(executor, (executor.getUniqueId().equals(ownerUniqueId)) ? ClaimsLang.YOU_DONT_HAVE_A_CLAIM : ClaimsLang.PLAYER_HAS_NO_CLAIM);
            return;
        }
        panel.open(executor);
    }
}
