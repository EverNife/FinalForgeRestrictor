package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.protection.ProtectionWorldGuard;
import br.com.finalcraft.evernifecore.protection.worldguard.FCWorldGuardRegion;
import br.com.finalcraft.evernifecore.protection.worldguard.WGFlags;
import br.com.finalcraft.evernifecore.protection.worldguard.WGPlatform;
import br.com.finalcraft.evernifecore.protection.worldguard.adapter.FCRegionResultSet;
import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class WorldGuardHandler implements ProtectionHandler {

	@FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §cVocê não tem permissão nessa Região!")
	@FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §cYou do not have permission on this Area!")
	private static LocaleMessage YOU_DO_NOT_HAVE_PERMISSION_ON_THIS_AREA;

	@Override
	public boolean canBuild(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		return this.check(player, location, WGFlags.USE);
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}

	@Override
	public boolean canOpenContainer(Player player, Block block) {
		return this.check(player, block.getLocation(), WGFlags.CHEST_ACCESS);
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}

	@Override
	public boolean canAttack(Player player, Entity entity) {
		if (entity instanceof Player) {
			return this.check(player, entity.getLocation(), WGFlags.PVP);
		}
		if (entity instanceof Animals || entity instanceof Villager) {
			return this.check(player, entity.getLocation(), WGFlags.DAMAGE_ANIMALS);
		}
		
		return true;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		return this.check(player, location, WGFlags.BUILD);
	}
	
	@Override
	public boolean canUseAoE(Player player, Location location, int range) {
		FCRegionResultSet regions = this.getRegions(location, range);
		boolean perm = regions.queryState(WGPlatform.getInstance().wrapPlayer(player), WGFlags.BUILD) != State.DENY;
		if (!perm) {
			this.permissionDeniedMessage(player);
		}

		return perm;
	}
	
	protected boolean check(Player player, Location location, StateFlag flag) {
		if (flag == WGFlags.BUILD) {
			return ProtectionWorldGuard.canBuild(player, location.getBlock());
		}

		FCRegionResultSet regions = this.getRegions(location);
		boolean perm = regions.queryState(WGPlatform.getInstance().wrapPlayer(player), flag) != State.DENY;
		if (!perm) {
			this.permissionDeniedMessage(player);
		}

		return perm;
	}
	
	protected FCRegionResultSet getRegions(Location location) {
		return WGPlatform.getInstance().getRegionManager(location.getWorld()).getApplicableRegions(location);
	}
	
	protected FCRegionResultSet getRegions(Location location, int range) {
		FCWorldGuardRegion region = FCWorldGuardRegion.of("ForgeRestrictorWGAoETest",
				BlockPos.from(location).add(-range, location.getBlockY(), -range),
				BlockPos.from(location).add(range, 255 - location.getBlockY(), range)
		);
		return WGPlatform.getInstance().getRegionManager(location.getWorld()).getApplicableRegions(region);
	}
	
	protected void permissionDeniedMessage(Player player) {
		YOU_DO_NOT_HAVE_PERMISSION_ON_THIS_AREA.send(player);
	}

	@Override
	public String getName() {
		return "WorldGuard";
	}
}
