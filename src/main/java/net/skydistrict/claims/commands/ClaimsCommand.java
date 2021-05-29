package net.skydistrict.claims.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.grabsky.indigo.acf.BaseCommand;
import me.grabsky.indigo.acf.annotation.*;
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
import net.skydistrict.claims.utils.ClaimsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

@CommandAlias("claims|claim|teren")
@CommandPermission("skydistrict.command.claims")
public class ClaimsCommand extends BaseCommand {
    private final Claims instance;
    private final ClaimManager manager;

    public ClaimsCommand(Claims instance) {
        this.instance = instance;
        this.manager = instance.getClaimManager();
    }

    @Default
    @CatchUnknown
    public void onClaimsDefault(Player executor) {
        this.openClaimMenu(executor, executor.getUniqueId());
    }

    @Default
    @CommandPermission("skydistrict.command.claims.others")
    @CommandCompletion("@players")
    public void onClaimsOthers(Player executor, String owner) {
        this.openClaimMenu(executor, UserCache.get(owner).getUniqueId());
    }

    @Default
    @Subcommand("fix")
    @CommandPermission("skydistrict.command.claims.fix")
    public void onClaimsFix(Player player) {
        final Location loc = player.getLocation();
        for (ProtectedRegion region : instance.getRegionManager().getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())).getRegions()) {
            if (region.getId().startsWith(Config.REGION_PREFIX)) {
                // Both variables shouldn't be null unless claim was manually modified
                final Location center = BukkitAdapter.adapt(region.getFlag(ClaimFlags.CLAIM_CENTER));
                final Material type = ClaimsUtils.getClaimLevel(region.getFlag(ClaimFlags.CLAIM_LEVEL)).getBlockMaterial();
                PaperLib.getChunkAtAsync(center).thenAccept(chunk -> {
                    chunk.getBlock((center.getBlockX() & 0xF), center.getBlockY(), (center.getBlockZ() & 0xF)).setType(type);
                    Lang.send(player, Lang.RESTORE_CLAIM_BLOCK_SUCCESS);
                });
                return;
            }
        }
        Lang.send(player, Lang.RESTORE_CLAIM_BLOCK_FAIL);
    }

    @Default
    @Subcommand("get")
    @CommandPermission("skydistrict.command.claims.get")
    public void onClaimsGet(Player player) {
        final Inventory inventory = player.getInventory();
        inventory.addItem(Items.getClaimBlock(0)); // COAL
        inventory.addItem(Items.getClaimBlock(1)); // IRON
        inventory.addItem(Items.getClaimBlock(2)); // GOLD
        inventory.addItem(Items.getClaimBlock(3)); // DIAMOND
        inventory.addItem(Items.getClaimBlock(4)); // EMERALD
        Lang.send(player, Lang.CLAIM_BLOCKS_ADDED);
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
