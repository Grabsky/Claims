package net.skydistrict.claims.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.papermc.lib.PaperLib;
import me.grabsky.indigo.user.UserCache;
import net.skydistrict.claims.Claims;
import net.skydistrict.claims.claims.Claim;
import net.skydistrict.claims.claims.ClaimManager;
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
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimsCommand {
    private final Claims instance;
    private final ClaimManager manager;

    public ClaimsCommand(Claims instance) {
        this.instance = instance;
        this.manager = instance.getClaimManager();
    }

    public void register() {
        this.onClaimsCommand().register();
        this.onClaimsCommandFromName().register();
        this.onClaimsCommandFromPlayer().register();
    }

    public CommandAPICommand onClaimsCommand() {
        return new CommandAPICommand("claims")
                .withAliases("claim", "teren")
                .withPermission("skydistrict.command.claims")
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission("skydistrict.command.claims.reload")
                        .executes((sender, args) -> {
                            if (instance.reload()) {
                                Lang.send(sender, Lang.RELOAD_SUCCESS);
                                return;
                            }
                            Lang.send(sender, Lang.RELOAD_FAIL);
                        })
                ).withSubcommand(new CommandAPICommand("get")
                        .withPermission("skydistrict.command.claims.get")
                        .executesPlayer((sender, args) -> {
                            sender.getInventory().addItem(Items.getClaimBlock(0));
                            sender.getInventory().addItem(Items.getClaimBlock(1));
                            sender.getInventory().addItem(Items.getClaimBlock(2));
                            sender.getInventory().addItem(Items.getClaimBlock(3));
                            sender.getInventory().addItem(Items.getClaimBlock(4));
                            Lang.send(sender, Lang.CLAIM_BLOCKS_ADDED);
                        })
                ).withSubcommand(new CommandAPICommand("fix")
                        .withPermission("skydistrict.command.claims.fix")
                        .executesPlayer((sender, args) -> {
                            final Location loc = sender.getLocation();
                            for (ProtectedRegion region : instance.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
                                if (region.getId().startsWith(Config.REGION_PREFIX)) {
                                    // Both variables shouldn't be null unless claim was manually modified
                                    final Location center = BukkitAdapter.adapt(region.getFlag(ClaimFlags.CLAIM_CENTER));
                                    final Material type = ClaimH.getClaimLevel(region.getFlag(ClaimFlags.CLAIM_LEVEL)).getBlockMaterial();
                                    PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                                        chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                                        Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_SUCCESS);
                                    });
                                    return;
                                }
                            }
                            Lang.send(sender, Lang.RESTORE_CLAIM_BLOCK_FAIL);
                        })
                ).executesPlayer((sender, args) -> {
                    this.openClaimMenu(sender, sender.getUniqueId());
                });
    }

    public CommandAPICommand onClaimsCommandFromName() {
        return new CommandAPICommand("claims")
                .withAliases("claim", "teren")
                .withPermission("skydistrict.command.claims.others")
                .withArguments(new StringArgument("name"))
                .executesPlayer((sender, args) -> {
                    this.openClaimMenu(sender, UserCache.get(String.valueOf(args[0])).getUniqueId());
                });
    }

    public CommandAPICommand onClaimsCommandFromPlayer() {
        return new CommandAPICommand("claims")
                .withAliases("claim", "teren")
                .withPermission("skydistrict.command.claims.others")
                .withArguments(new EntitySelectorArgument("player", EntitySelectorArgument.EntitySelector.ONE_PLAYER))
                .executesPlayer((sender, args) -> {
                    this.openClaimMenu(sender, ((Player) args[0]).getUniqueId());
                });
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
