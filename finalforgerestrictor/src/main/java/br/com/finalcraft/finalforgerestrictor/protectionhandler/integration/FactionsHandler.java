package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.cache.CacheableSupplier;
import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.minecraft.vector.ChunkPos;
import br.com.finalcraft.evernifecore.vectors.CuboidSelection;
import br.com.finalcraft.finalforgerestrictor.FinalForgeRestrictor;
import br.com.finalcraft.finalforgerestrictor.logging.FFRDebugModule;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import com.massivecraft.factions.*;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.perms.PermissibleActions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;


public class FactionsHandler implements ProtectionHandler {

	@FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §cVocê está muito perto de uma Faction para fazer isso!")
	@FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §cYou are to close to a faction to do that!!")
	private static LocaleMessage YOU_ARE_TOO_CLOSE_TO_A_FACTION;

	private boolean checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(Player player, Location location) {

		if (!FactionsPlugin.getInstance().worldUtil().isEnabled(location.getWorld())){
			return true; // World is disabled so it's not a faction world, allow the action
		}

		boolean canBuildDestroyBlock = FactionsBlockListener.playerCanBuildDestroyBlock(player, location, PermissibleActions.BUILD, true);

		FinalForgeRestrictor.getLog().debugModule(FFRDebugModule.FACTIONS, () -> {
			Faction factionAt = Board.getInstance().getFactionAt(new FLocation(location));

			return String.format("Checking canBreakBuild for player %s at location %s, inside faction %s, with result [%s]",
					player.getName(),
					location,
					factionAt.isWilderness() ? "Wilderness" : factionAt.getTag(),
					canBuildDestroyBlock
			);
		});

		return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, PermissibleActions.BUILD, true);
	}

	@Override
	public boolean canBuild(Player player, Location location) {
		return checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(player, location);
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		return checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(player, location);
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(player, location);
	}
	
	@Override
	public boolean canOpenContainer(Player player, Block block) {
		return checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(player, block.getLocation());
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return this.checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(player, location);
	}

	@Override
	public boolean canAttack(Player damager, Entity damaged) {
		return true;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		return this.checkIfWorldHasFactionsDisabledOrIfThePlayerCanBuildThere(player, location);
	}
	
	@Override
	public boolean canUseAoE(Player player, Location location, int range) {

		if (!FactionsPlugin.getInstance().worldUtil().isEnabled(location.getWorld())){
			return true; // World is disabled so it's not a faction world, allow the action
		}

		List<FactionNearChunk> factionsNearChunk = getFactionsNearChunk(location, range);

		FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

		for (FactionNearChunk factionNearChunk : factionsNearChunk) {
			Faction faction = factionNearChunk.getFaction();

			if (!faction.hasAccess(fPlayer, PermissibleActions.BUILD, null)) {
				YOU_ARE_TOO_CLOSE_TO_A_FACTION.send(player);
				return false;
			}
		}

		return true;
	}
	
	@Override
	public String getName() {
		return "Factions";
	}

	private WeakHashMap<AoeCheckingRangePos, CacheableSupplier<List<FactionNearChunk>>> FACTIONS_WORLD_AOE_CACHE = new WeakHashMap<>();

	@Getter
	@AllArgsConstructor
	private static class AoeCheckingRangePos {
		private final String worldName;
		private final ChunkPos chunkPos;
		private final int range;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AoeCheckingRangePos that = (AoeCheckingRangePos) o;
			return range == that.range && Objects.equals(worldName, that.worldName) && Objects.equals(chunkPos, that.chunkPos);
		}

		@Override
		public int hashCode() {
			return Objects.hash(worldName, chunkPos, range);
		}
	}

	@Data
	public static class FactionNearChunk {
		private final Faction faction;
		private final FLocation location;
		private final ChunkPos chunkPos;
	}

	public List<FactionNearChunk> getFactionsNearChunk(Location location, int range) {

		final String worldName = location.getWorld().getName();

		AoeCheckingRangePos aoeCheckingRangePos = new AoeCheckingRangePos(worldName, ChunkPos.from(location), range);

		CacheableSupplier<List<FactionNearChunk>> CACHED_SUPPLIER = FACTIONS_WORLD_AOE_CACHE.computeIfAbsent(aoeCheckingRangePos, w -> {

			final Set<ChunkPos> chunksOfSelection = CuboidSelection.of(BlockPos.from(location))
					.expand(range)
					.getChunks()
					.stream()
					.collect(Collectors.toSet());

			return CacheableSupplier.of(() -> {

				List<FactionNearChunk> factionNearChunk = new ArrayList<>();

				for (Faction faction : Factions.getInstance().getAllFactions()) {

					for (FLocation chunkClaim : faction.getAllClaims()) {

						if (!chunkClaim.getWorldName().equalsIgnoreCase(worldName)) {
							continue;
						}

						ChunkPos chunkPos = new ChunkPos((int) chunkClaim.getX(), (int) chunkClaim.getZ());

						if (chunksOfSelection.contains(chunkPos)) {
							factionNearChunk.add(
									new FactionNearChunk(
											faction,
											chunkClaim,
											chunkPos
									)
							);
							break;
						}
					}
				}

				return factionNearChunk;
			}).withInterval(2000);
		});

		return CACHED_SUPPLIER.getValue();
	}


}
