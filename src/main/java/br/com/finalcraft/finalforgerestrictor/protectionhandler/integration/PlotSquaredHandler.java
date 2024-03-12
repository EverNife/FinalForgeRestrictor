package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.vectors.CuboidSelection;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RegionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class PlotSquaredHandler  implements ProtectionHandler {

	@Override
	public boolean canBuild(Player player, Location location) {

		com.intellectualcrafters.plot.object.Location pLocation = fromBukkit(location);

		PlotArea plotArea = PS.get().getApplicablePlotArea(pLocation);
		if (plotArea != null){
			Plot plot = plotArea.getPlot(pLocation);
			if(plot != null){
				return plot.isOwner(player.getUniqueId()) || isTrusted(plot, player) || isAddAvailable(plot, player);
			}
		}

		return true;
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
		return canBuild(player, block.getLocation());
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

		CuboidSelection cuboidSelection = CuboidSelection.of(BlockPos.from(location)).expand(range);

		RegionWrapper regionWrapper = new RegionWrapper(
				cuboidSelection.getMinium().getX(),
				cuboidSelection.getMinium().getZ(),
				cuboidSelection.getMaximum().getX(),
				cuboidSelection.getMaximum().getZ()
		);

		Set<PlotArea> plotAreas = PS.get().getPlotAreas(location.getWorld().getName(), regionWrapper);

		//Not inside a plot, not on plot-world probably?
		if (plotAreas.size() == 0){
			return true;
		}

		//More than one plot present on the range, deny the action
		if (plotAreas.size() != 1){
			return false;
		}

		PlotArea plotArea = plotAreas.stream().findFirst().get();

		//now make sure all 4 corners are inside the same plot
		if (!plotArea.contains(cuboidSelection.getMinium().getX(), cuboidSelection.getMinium().getZ())
				|| !plotArea.contains(cuboidSelection.getMinium().getX(), cuboidSelection.getMaximum().getZ())
				|| !plotArea.contains(cuboidSelection.getMaximum().getX(), cuboidSelection.getMinium().getZ())
				|| !plotArea.contains(cuboidSelection.getMaximum().getX(), cuboidSelection.getMaximum().getZ())) {
			return false;
		}

		return true;
	}

	public com.intellectualcrafters.plot.object.Location fromBukkit(Location location){
		return new com.intellectualcrafters.plot.object.Location(location.getWorld().getName(),location.getBlockX(),location.getBlockY(),location.getBlockZ(),location.getYaw(),location.getPitch());
	}

	public boolean isTrusted(Plot plot, Player player){
		return plot.getTrusted().contains(player.getUniqueId());
	}

	public boolean isAddAvailable(Plot plot, Player player){

		boolean isAnyOwnerOnline = false;

		for(UUID uuid : plot.getOwners()){
			Player p;
			if((p = Bukkit.getPlayer(uuid)) != null &&  p.isOnline()){
				isAnyOwnerOnline = true;
				break;
			}
		}

		return plot.isAdded(player.getUniqueId()) && isAnyOwnerOnline;
	}


	@Override
	public String getName() {
		return "PlotSquared";
	}
}