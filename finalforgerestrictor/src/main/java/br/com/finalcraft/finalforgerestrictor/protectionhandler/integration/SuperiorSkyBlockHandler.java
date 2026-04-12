package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.vectors.CuboidSelection;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SuperiorSkyBlockHandler implements ProtectionHandler {

	private boolean hasNoIslandOrHasPermission(Player player, Location location, IslandPrivilege privilege) {
		Island island = SuperiorSkyblockAPI.getIslandAt(location);
		return island == null || island.hasPermission(player, privilege);
	}

	@Override
	public boolean canBuild(Player player, Location location) {
		return hasNoIslandOrHasPermission(player, location, IslandPrivileges.BUILD);
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		return canInteract(player, location);
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return canInteract(player, location);
	}

	@Override
	public boolean canOpenContainer(Player player, Block block) {
		return hasNoIslandOrHasPermission(player, block.getLocation(), IslandPrivileges.CHEST_ACCESS);
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return hasNoIslandOrHasPermission(player, location, IslandPrivileges.INTERACT);
	}

	@Override
	public boolean canAttack(Player damager, Entity damaged) {
		return true;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		return canBuild(player, location);
	}

	@Override
	public boolean canUseAoE(Player player, Location location, int range) {

		CuboidSelection cuboidSelection = CuboidSelection.of(BlockPos.from(location)).expand(range);

		List<Location> fourCornersAndCenter = Arrays.asList(
				location,
				cuboidSelection.getMinium().getLocation(location.getWorld()),
				cuboidSelection.getMaximum().getLocation(location.getWorld()),
				new Location(location.getWorld(), cuboidSelection.getMinium().getX(), location.getY(), cuboidSelection.getMaximum().getZ()),
				new Location(location.getWorld(), cuboidSelection.getMaximum().getX(), location.getY(), cuboidSelection.getMinium().getZ())
		);

		for (Location fourCorner : fourCornersAndCenter) {
			if (!canBuild(player, fourCorner)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String getName() {
		return "SuperiorSkyblock2";
	}
}