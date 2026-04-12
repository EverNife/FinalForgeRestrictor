package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.vectors.CuboidSelection;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumteams.PermissionType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class IridiumSkyBlockHandler implements ProtectionHandler {

	@Override
	public boolean canBuild(Player player, Location location) {

		Island island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(location).orElse(null);

		if (island == null){
			return true;
		}

		User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);

		return IridiumSkyblock.getInstance().getTeamManager().getTeamPermission(island, user, PermissionType.BLOCK_BREAK);
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		return canBuild(player, location);
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return canBuild(player, location);
	}

	@Override
	public boolean canOpenContainer(Player player, Block block) {
		Island island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(block.getLocation()).orElse(null);

		if (island == null){
			return true;
		}

		User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);

		return IridiumSkyblock.getInstance().getTeamManager().getTeamPermission(island, user, PermissionType.OPEN_CONTAINERS);
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return canBuild(player, location);
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

		if (IridiumSkyblock.getInstance().getIslandManager().isInSkyblockWorld(location.getWorld())) {
			return true;
		}

		CuboidSelection cuboidSelection = CuboidSelection.of(BlockPos.from(location)).expand(range);

		List<Location> fourCorners = Arrays.asList(
				cuboidSelection.getMinium().getLocation(location.getWorld()),
				cuboidSelection.getMaximum().getLocation(location.getWorld()),
				new Location(location.getWorld(), cuboidSelection.getMinium().getX(), location.getY(), cuboidSelection.getMaximum().getZ()),
				new Location(location.getWorld(), cuboidSelection.getMaximum().getX(), location.getY(), cuboidSelection.getMinium().getZ())
		);

		Island island = IridiumSkyblock.getInstance().getDatabaseManager().getIslandTableManager().getEntries().stream().filter((isl) -> {
			for (Location fourCorner : fourCorners) {
				if (isl.isInIsland(fourCorner)) {
					return true;
				}
			}
			return false;
		}).findFirst().orElse(null);

		if (island == null){
			return true;
		}

		User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);

		return IridiumSkyblock.getInstance().getTeamManager().getTeamPermission(island, user, PermissionType.BLOCK_BREAK);
	}

	@Override
	public String getName() {
		return "IridiumSkyblock";
	}
}