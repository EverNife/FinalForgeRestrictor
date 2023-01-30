package br.com.finalcraft.finalforgerestrictor.protectionhandler.integration;

import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.reflection.MethodInvoker;
import br.com.finalcraft.evernifecore.util.FCReflectionUtil;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.EntityEventHandler;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class GriefPreventionHandler implements ProtectionHandler {

	@FCLocale(lang = LocaleType.EN_US, text = "§e§l ▶ §cVocê está muito perto de um Claim para fazer isso!")
	@FCLocale(lang = LocaleType.PT_BR, text = "§e§l ▶ §cYou are to close to a claim to do that!!")
	private static LocaleMessage YOU_ARE_TOO_CLOSE_TO_A_CLAIM;

	@Override
	public boolean canBuild(Player player, Location location) {
		String reason = GriefPrevention.instance.allowBuild(player, location, Material.STONE);
		
		if (reason == null) {
			return true;
		}
		
		player.sendMessage(reason);
		return false;
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, playerData.lastClaim);

		if (claim == null) {
			return true;
		}

		String reason = claim.allowAccess(player);
		
		if (reason == null) {
			return true;
		}
		
		player.sendMessage(reason);
		return false;
	}

	@Override
	public boolean canUse(Player player, Location location) {
		return this.canBuild(player, location);
	}
	
	@Override
	public boolean canOpenContainer(Player player, Block block) {
		PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), false, playerData.lastClaim);

		if (claim == null) {
			return true;
		}

		String reason = claim.allowContainers(player);

		if (reason == null) {
			return true;
		}

		player.sendMessage(reason);
		return false;
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		return this.canBuild(player, location);
	}


	private final EntityEventHandler dummyEntityEventHandler = new EntityEventHandler(null, GriefPrevention.instance);
	private final MethodInvoker handleEntityDamageEvent = FCReflectionUtil.getMethod(EntityEventHandler.class, "handleEntityDamageEvent");
	@Override
	public boolean canAttack(Player damager, Entity damaged) {

		if (GriefPrevention.instance.pvpRulesApply(damaged.getWorld()) == false){
			return false;
		}

		EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, damaged, EntityDamageByEntityEvent.DamageCause.ENTITY_ATTACK, 1);

		//Do all PVP Checks
		handleEntityDamageEvent.invoke(dummyEntityEventHandler, event, true);

		return event.isCancelled() == false;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		return this.canBuild(player, location);
	}
	
	@Override
	public boolean canUseAoE(Player player, Location location, int range) {
		PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, playerData.lastClaim);

		if (claim != null) {
			String message = claim.allowBuild(player, Material.STONE);
			if (message != null) {
				// you have no perms on this claim, disallow.
				player.sendMessage(message);
				return false;
			}
			
			if (claimContains(claim, location, range)) {
				// the item's range is in this claim's boundaries. You're allowed to use this item.
				return true;
			}
			
			if (claim.parent != null) {
				// you're on a subdivision
				message = claim.parent.allowBuild(player, Material.STONE);
				if (message != null) {
					// you have no build permission on the top claim... disallow.
					player.sendMessage(message);
					return false;
				}
			
				if (claimContains(claim, location, range)) {
				    // the restricted item's range is in the top claim's boundaries. you're allowed to use this item.
					return true;
				}
			}
		}
		
		// the range is not entirely on a claim you're trusted in... we need to search for nearby claims too.
		for (Claim nClaim : getChunksAroundLocation(location, range).values()) {
			if (nClaim.allowBuild(player, Material.STONE) != null) {
				YOU_ARE_TOO_CLOSE_TO_A_CLAIM.send(player);
				// if not allowed on claims in range, disallow.
				return false;
			}
		}
		return true;
	}
	
	static boolean claimContains(Claim claim, Location location, int range) {
		return (claim.contains(new Location(location.getWorld(), location.getBlockX()+range, 0, location.getBlockZ()+range), true, false) &&
				claim.contains(new Location(location.getWorld(), location.getBlockX()-range, 0, location.getBlockZ()-range), true, false));
	}

	@Override
	public String getName() {
		return "GriefPrevention";
	}


	// This is a translation of the original method from GriefPreventionPlus
	public Map<Long, Claim> getChunksAroundLocation(Location loc, int blocksRange) {

		Location lesserBoundaryCorner = new BlockPos(loc.getBlockX() - blocksRange, 0, loc.getBlockZ() - blocksRange).getLocation(loc.getWorld());
		Location greaterBoundaryCorner = new BlockPos(loc.getBlockX() + blocksRange, 255, loc.getBlockZ() + blocksRange).getLocation(loc.getWorld());

		BoundingBox boundingBox = new BoundingBox(lesserBoundaryCorner, greaterBoundaryCorner);

		int lx = loc.getBlockX() - blocksRange;
		int lz = loc.getBlockZ() - blocksRange;

		int gx = loc.getBlockX() + blocksRange;
		int gz = loc.getBlockZ() + blocksRange;

		lx = lx >> 4;
		lz = lz >> 4;
		gx = gx >> 4;
		gz = gz >> 4;

		final Map<Long, Claim> claims = new HashMap<>();
		for (int i = lx; i <= gx; i++) {
			for (int j = lz; j <= gz; j++) {
				final Collection<Claim> claimList = GriefPrevention.instance.dataStore.getClaims(i,j);

				if (claimList.isEmpty()){
					continue;
				}

				for (final Claim claim : claimList) {
					if (overlaps(claim, loc.getWorld(), boundingBox)) {
						claims.put(claim.getID(), claim);
					}
				}
			}
		}
		return claims;
	}

	private boolean overlaps(Claim reference, World world, BoundingBox boundingBox) {
		return !Objects.equals(reference.getLesserBoundaryCorner().getWorld(), world) ? false : (new BoundingBox(reference)).intersects(boundingBox);
	}
}
