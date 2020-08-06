package br.com.finalcraft.forgerestrictor.protectionhandler.integration;

import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionHandler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class WorldGuardHandler implements ProtectionHandler {
	WorldGuardPlugin worldGuard;
	
	public WorldGuardHandler() {
		this.worldGuard=WorldGuardPlugin.inst();
	}
	
	@Override
	public boolean canBuild(Player player, Location location) {
		return this.check(player, location, DefaultFlag.BUILD);
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		return this.check(player, location, DefaultFlag.USE);
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return this.check(player, location, DefaultFlag.BUILD);
	}

	@Override
	public boolean canOpenContainer(Player player, Block block) {
		return this.check(player, block.getLocation(), DefaultFlag.CHEST_ACCESS);
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return this.check(player, location, DefaultFlag.BUILD);
	}

	@Override
	public boolean canAttack(Player player, Entity entity) {
		if (entity instanceof Player) {
			return this.check(player, entity.getLocation(), DefaultFlag.PVP);
		}
		if (entity instanceof Animals || entity instanceof Villager) {
			return this.check(player, entity.getLocation(), DefaultFlag.DAMAGE_ANIMALS);
		}
		
		return true;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		return this.check(player, location, DefaultFlag.BUILD);
	}
	
	@Override
	public boolean canUseAoE(Player player, Location location, int range) {
		ApplicableRegionSet regions = this.getRegions(location, range);
		boolean perm = regions.queryState(worldGuard.wrapPlayer(player), DefaultFlag.BUILD) != State.DENY;
		if (!perm) {
			this.permissionDeniedMessage(player);
		}

		return perm;
	}
	
	protected boolean check(Player player, Location location, StateFlag flag) {
		if (flag == DefaultFlag.BUILD) {
			return worldGuard.canBuild(player, location);
		}
		
		ApplicableRegionSet regions = this.getRegions(location);
		boolean perm = regions.queryState(worldGuard.wrapPlayer(player), flag) != State.DENY;
		if (!perm) {
			this.permissionDeniedMessage(player);
		}

		return perm;
	}
	
	protected ApplicableRegionSet getRegions(Location location) {
		return this.worldGuard.getRegionManager(location.getWorld()).getApplicableRegions(location);
	}
	
	protected ApplicableRegionSet getRegions(Location location, int range) {
		ProtectedCuboidRegion pcr = new ProtectedCuboidRegion("ForgeRestrictorWGAoETest", new BlockVector(location.getBlockX()-range, 0, location.getBlockZ()-range), new BlockVector(location.getBlockX()+range, 255, location.getBlockZ()+range));
		return this.worldGuard.getRegionManager(location.getWorld()).getApplicableRegions(pcr);
	}
	
	protected void permissionDeniedMessage(Player player) {
		player.sendMessage("§4You don't have permission in this area.");
	}

	@Override
	public String getName() {
		return "WorldGuard";
	}
}
