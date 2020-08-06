package br.com.finalcraft.forgerestrictor.protectionhandler.integration;

import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionHandler;
import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.DataStore;
import net.kaikk.mc.gpp.GriefPreventionPlus;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;


public class GriefPreventionPlusHandler implements ProtectionHandler {
	DataStore dataStore;
	net.kaikk.mc.gpp.Config gConfig;

	public GriefPreventionPlusHandler() {
		this.dataStore = GriefPreventionPlus.getInstance().getDataStore();
		this.gConfig = GriefPreventionPlus.getInstance().config;
	}

	@Override
	public boolean canBuild(Player player, Location location) {
		Claim claim = this.dataStore.getClaimAt(location, false);
		if (claim==null) {
			return true;
		}
		
		String reason=claim.canBuild(player);
		
		if (reason==null) {
			return true;
		}
		
		player.sendMessage(reason);
		
		return false;
	}

	@Override
	public boolean canAccess(Player player, Location location) {
		Claim claim = this.dataStore.getClaimAt(location, false);
		if (claim==null) {
			return true;
		}
		
		String reason=claim.canAccess(player);
		
		if (reason==null) {
			return true;
		}
		
		player.sendMessage(reason);
		
		return false;
	}

	@Override
	public boolean canUse(Player player, Location location) {
		Claim claim = this.dataStore.getClaimAt(location, false);
		if (claim==null) {
			return true;
		}
		
		String reason=claim.canBuild(player);
		
		if (reason==null) {
			return true;
		}
		
		player.sendMessage(reason);
		
		return false;
	}
	
	@Override
	public boolean canOpenContainer(Player player, Block block) {
		Claim claim = this.dataStore.getClaimAt(block.getLocation(), false);
		if (claim==null) {
			return true;
		}
		
		String reason=claim.canOpenContainers(player);
		
		if (reason==null) {
			return true;
		}
		
		player.sendMessage(reason);
		
		return false;
	}

	@Override
	public boolean canInteract(Player player, Location location) {
		Claim claim = this.dataStore.getClaimAt(location, false);
		if (claim==null) {
			return true;
		}
		
		String reason=claim.canBuild(player);
		
		if (reason==null) {
			return true;
		}
		
		player.sendMessage(reason);
		
		return false;
	}


	@Override
	public boolean canAttack(Player damager, Entity damaged) {
		if (damaged instanceof Player) {
			if (!GriefPreventionPlus.getInstance().config.pvp_enabledWorlds.contains(damaged.getWorld().getUID())) {
				return false;
			}
			
			Claim claim = this.dataStore.getClaimAt(damaged.getLocation(), false);
			if (claim==null) {
				return true;
			}
			
			if (claim.isAdminClaim()) {
				if (claim.getParent()==null) {
					if (GriefPreventionPlus.getInstance().config.pvp_noCombatInAdminLandClaims) {
						return false;
					}
				} else {
					if (GriefPreventionPlus.getInstance().config.pvp_noCombatInAdminSubdivisions) {
						return false;
					}
				}
			} else {
				if (GriefPreventionPlus.getInstance().config.pvp_noCombatInPlayerLandClaims) {
					return false;
				}
			}
			
			String reason=claim.canBuild(damager);
			if (reason==null) {
				return true;
			}
			
			damager.sendMessage(reason);
		} else if (damaged instanceof Animals || damaged instanceof Villager) {
			Claim claim = this.dataStore.getClaimAt(damaged.getLocation(), false);
			if (claim==null) {
				return true;
			}
			
			String reason=claim.canOpenContainers(damager); // allow farming with /containertrust
			
			if (reason==null) {
				return true;
			}
			
			damager.sendMessage(reason);
		} else {
			return true;
		}
		
		return false;
	}

	@Override
	public boolean canProjectileHit(Player player, Location location) {
		Claim claim = this.dataStore.getClaimAt(location, false);
		if (claim==null) {
			return true;
		}
		
		String reason=claim.canBuild(player);
		
		if (reason==null) {
			return true;
		}
		
		player.sendMessage(reason);
		
		return false;
	}
	
	@Override
	public boolean canUseAoE(Player player, Location location, int range) {
		Claim claim = this.dataStore.getClaimAt(location, false);
		if (claim!=null) {
			if (claim.canBuild(player)!=null) {
				// you have no perms on this claim, disallow.
				return false;
			}
			
			if (claimContains(claim, location, range)) {
				// the item's range is in this claim's boundaries. You're allowed to use this item.
				return true;
			}
			
			if (claim.getParent()!=null) {
				// you're on a subdivision
				if (claim.getParent().canBuild(player)!=null) {
					// you have no build permission on the top claim... disallow.
					return false;
				}
			
				if (claimContains(claim, location, range)) {
				    // the restricted item's range is in the top claim's boundaries. you're allowed to use this item.
					return true;
				}
			}
		}
		
		// the range is not entirely on a claim you're trusted in... we need to search for nearby claims too.
		for (Claim nClaim : this.dataStore.posClaimsGet(location, range).values()) {
			if (nClaim.canBuild(player)!=null) {
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
		return "GriefPreventionPlus";
	}
}
