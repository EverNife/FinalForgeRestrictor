package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.logger.ECLogger;
import br.com.finalcraft.evernifecore.logger.debug.IDebugModule;
import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.util.FCPosUtil;
import br.com.finalcraft.evernifecore.util.commons.MinMax;
import br.com.finalcraft.evernifecore.vectors.CuboidSelection;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlotSquaredHandler extends ProtectionHandler {

	public PlotSquaredHandler(ECLogger logger, IDebugModule debugModule) {
		super(logger, debugModule);
	}

    @FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §cVocê precisa estar dentro do centro do seu Plot para fazer isso!")
    @FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §cYou must be inside the center of your plot to do that.")
    private static LocaleMessage YOU_MUST_BE_INSIDE_THE_CENTER_OF_YOUR_PLOT;

	@Override
	public boolean canBuild(Player player, Location location) {

		this.getLog().debugModule(this.getDebugModule(), () -> {
			return String.format("Checking Plot for player %s at location [%s, %d, %d, %d]", player.getName(), location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		});

		com.plotsquared.core.location.Location pLocation = fromBukkit(location);

		PlotArea plotArea = PlotSquared.get().getPlotAreaManager().getPlotArea(pLocation);
		if (plotArea != null){
			Plot plot = plotArea.getPlot(pLocation);
			if(plot != null){
				boolean isOwner = plot.isOwner(player.getUniqueId());
				boolean isTrusted = isTrusted(plot, player);
				boolean isAdded = isAddAvailable(plot, player);
				boolean result = isOwner || isTrusted || isAdded;
				this.getLog().debugModule(this.getDebugModule(), () -> {
					return String.format("Plot %s found for player %s | isOwner: %s, isTrusted: %s, isAdded: %s -> result: %s", plot.getId(), player.getName(), isOwner, isTrusted, isAdded, result);
				});
				return result;
			}
			this.getLog().debugModule(this.getDebugModule(), () -> {
				return String.format("No plot found at location for player %s in plotArea %s", player.getName(), plotArea.getWorldName());
			});
		} else {
			this.getLog().debugModule(this.getDebugModule(), () -> {
				return String.format("No PlotArea found at location for player %s, allowing action", player.getName());
			});
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

		this.getLog().debugModule(this.getDebugModule(), () -> {
			return String.format("Checking AoE for player %s at location [%s, %d, %d, %d] with range %d", player.getName(), location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), range);
		});

		CuboidSelection cuboidSelection = CuboidSelection.of(BlockPos.from(location)).expand(range);

		Location minLoc = cuboidSelection.getMinium().getLocation(location.getWorld());
		Location maxLoc = cuboidSelection.getMaximum().getLocation(location.getWorld());

		com.plotsquared.core.location.Location plotMinLoc = BukkitUtil.adapt(minLoc);
		com.plotsquared.core.location.Location plotMaxLoc = BukkitUtil.adapt(maxLoc);

		CuboidRegion regionWrapper = new CuboidRegion(
                new BukkitWorld(minLoc.getWorld()),
				plotMinLoc.getBlockVector3(),
				plotMaxLoc.getBlockVector3()
		);

		Set<@NonNull PlotArea> plotAreas = Arrays.stream(PlotSquared.get().getPlotAreaManager().getPlotAreas(location.getWorld().getName(), regionWrapper))
				.collect(Collectors.toSet());

		//Not inside a plot, not on plot-world probably?
		if (plotAreas.size() == 0){
			this.getLog().debugModule(this.getDebugModule(), () -> {
				return String.format("No PlotAreas (probably not a PlotWorld) found for AoE check of player %s, allowing action", player.getName());
			});
			return true;
		}

		//More than one plot present on the range, deny the action
		if (plotAreas.size() != 1){
			this.getLog().debugModule(this.getDebugModule(), () -> {
				return String.format("Multiple PlotAreas (%d) found for AoE check of player %s, denying action", plotAreas.size(), player.getName());
			});
			return false;
		}

		PlotArea plotArea = plotAreas.stream().findFirst().get();

        Plot plot = plotArea.getPlot(BukkitUtil.adapt(minLoc));

        if (plot == null){
            this.getLog().debugModule(this.getDebugModule(), () -> {
                return String.format("No Plot found for AoE check of player %s, denying action", plotAreas.size(), player.getName());
            });
            return false;
        }

        com.plotsquared.core.location.Location[] corners = plot.getCorners();

        MinMax<BlockPos> minimumAndMaximum = FCPosUtil.getMinimumAndMaximum(
                Arrays.asList(
                        BlockPos.at(corners[0].getX(), corners[0].getY(), corners[0].getZ()),
                        BlockPos.at(corners[1].getX(), corners[1].getY(), corners[1].getZ())
                )
        );

		BlockPos corner1 = cuboidSelection.getMinium();
		BlockPos corner2 = cuboidSelection.getMinium().setZ(cuboidSelection.getMaximum().getZ());
		BlockPos corner3 = cuboidSelection.getMaximum();
		BlockPos corner4 = cuboidSelection.getMaximum().setZ(cuboidSelection.getMinium().getZ());

        CuboidSelection plotRealArea = CuboidSelection.of(
                minimumAndMaximum.getMin(),
                minimumAndMaximum.getMax()
        );

		//now make sure all 4 corners are inside the same plot
		boolean containsCorner1 = plotRealArea.contains(corner1);
		boolean containsCorner2 = plotRealArea.contains(corner2);
		boolean containsCorner3 = plotRealArea.contains(corner3);
		boolean containsCorner4 = plotRealArea.contains(corner4);

		boolean shouldAllow;

		if (!containsCorner1 || !containsCorner2 || !containsCorner3 || !containsCorner4){
			shouldAllow = false;
		} else {
            shouldAllow = true;
        }

        this.getLog().debugModule(this.getDebugModule(), () -> {
			return String.format("[%s] [%s] AoE CornerCheck [%s: %s, %s: %s, %s: %s, %s: %s] == %s",
                    plotArea.getId(),
					player.getName(),
                    corner1, containsCorner1,
                    corner2, containsCorner2,
                    corner3, containsCorner3,
                    corner4, containsCorner4,
                    shouldAllow
			);
		});

		return shouldAllow;
	}

	public com.plotsquared.core.location.Location fromBukkit(Location location){
		return BukkitUtil.adapt(location);
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