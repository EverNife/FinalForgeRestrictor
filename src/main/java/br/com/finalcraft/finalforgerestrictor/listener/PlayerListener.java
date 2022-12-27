package br.com.finalcraft.finalforgerestrictor.listener;

import br.com.finalcraft.evernifecore.listeners.base.ECListener;
import br.com.finalcraft.evernifecore.material.FCMaterialUtil;
import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import br.com.finalcraft.finalforgerestrictor.PermissionNodes;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictedItem;
import br.com.finalcraft.finalforgerestrictor.config.restricteditem.RestrictionType;
import br.com.finalcraft.finalforgerestrictor.config.settings.FFResSettings;
import br.com.finalcraft.finalforgerestrictor.confiscation.ConfiscationManager;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionHandler;
import br.com.finalcraft.finalforgerestrictor.protectionhandler.ProtectionPlugins;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kaikk.mc.gpp.GriefPreventionPlus;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements ECListener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {

		// ignore stepping into a block
		if (event.getAction() == Action.PHYSICAL) {
			return;
		}

		final Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (!FFResSettings.isWorldEnabled(player.getWorld())){
			return;
		}

		final ItemStack heldItem = event.getItem() != null
				? event.getItem()
				: FCBukkitUtil.getPlayersHeldItem(player);

		// if not holding anything, just ignore
		if (heldItem == null || heldItem.getType() == Material.AIR){
			return;
		}

		//Ignore if the player has correct permission
		if (player.hasPermission(PermissionNodes.BYPASS_RESTRICTIONS)){
			return;
		}

		// ignore heads
		if (FCMaterialUtil.isHead(heldItem.getType())){
			return;
		}

		// ignore all vanilla items and edible items actions into vanilla blocks or air
		if ((heldItem.getType().isEdible() || FCMaterialUtil.isVanilla(heldItem.getType()))
				&& (block == null || FCMaterialUtil.isVanilla(block.getType()))) {
			return;
		}

		// ignore investigation tool on GPP
		if (ProtectionPlugins.GriefPreventionPlus != null){
			if (heldItem.getType() == GriefPreventionPlus.getInstance().config.claims_investigationTool) {
				return;
			}
		}

		// ignore investigation tool on GP
		if (ProtectionPlugins.GriefPrevention != null){
			if (heldItem.getType() == GriefPrevention.instance.config_claims_investigationTool) {
				return;
			}
		}

		RestrictedItem restrictedItem = FFResSettings.getRestrictedItem(heldItem);

		// Check for Whitelisted Items
		if (restrictedItem != null && restrictedItem.getType() == RestrictionType.WHITELIST){
			return;
		}

		// Special aoe items list (needs to check a wide area...)
		if (restrictedItem != null && restrictedItem.getType() == RestrictionType.AOE) {
			// check players location
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canUseAoE(player, player.getLocation(), restrictedItem.getRange())) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
					event.setCancelled(true);
					ConfiscationManager.confiscateInventory(player);
					return;
				}
			}
			return;
		}

		//When hitting the AIR, check if item is RANGED RESTRICTED
		if (block == null && restrictedItem != null && restrictedItem.getType() == RestrictionType.RANGED) {
			// This is really not precise, if the player is targeting the AIR, this will just not work :/
			// But waterver, it was this way before, it doesn't matter at all :/
			block = FCBukkitUtil.getTargetBlock(player, restrictedItem.getRange());
		}

		Location targetLocation = block == null
				? player.getLocation()
				: block.getLocation();

		// check permissions on that location
		for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
			if (!protection.canInteract(player, targetLocation)) {
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				event.setCancelled(true);
				ConfiscationManager.confiscateInventory(player);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() == EntityType.PLAYER) {
			final Player damager = (Player) event.getDamager();

			if (!FFResSettings.isWorldEnabled(damager.getWorld())){
				return;
			}

			if (FCBukkitUtil.isFakePlayer(damager)) {
				return;
			}

			final Entity damaged = event.getEntity();

			//Ignore if the player has correct permission
			if (damager.hasPermission(PermissionNodes.BYPASS_RESTRICTIONS)){
				return;
			}

			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canAttack(damager, damaged)) {
					event.setCancelled(true);
					ConfiscationManager.confiscateInventory(damager);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		final Player player = event.getPlayer();

		if (!FFResSettings.isWorldEnabled(player.getWorld())){
			return;
		}

		if (FCBukkitUtil.isFakePlayer(player)) {
			return;
		}

		//Ignore if the player has correct permission
		if (player.hasPermission(PermissionNodes.BYPASS_RESTRICTIONS)){
			return;
		}

		ItemStack heldItem = event.getItemInHand();

		// special aoe items list (needs to check a wide area...)
		RestrictedItem restrictedItem = FFResSettings.getRestrictedItem(heldItem);

		if (restrictedItem != null && restrictedItem.getType() == RestrictionType.AOE) {
			Location blockLocation = event.getBlock().getLocation();
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canUseAoE(player, blockLocation, restrictedItem.getRange())) {
					event.setBuild(false);
					event.setCancelled(true);
					ConfiscationManager.confiscateInventory(player);
					return;
				}
			}
		}
	}

	// blocks projectiles explosions
	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		final Entity entity = event.getEntity();

		if (entity instanceof Projectile) {
			final Projectile projectile = (Projectile) entity;
			if (projectile.getShooter() instanceof Player) {

				if (!FFResSettings.isWorldEnabled(projectile.getLocation().getWorld())){
					return;
				}

				//Ignore if the player has correct permission
				if (((Player)projectile.getShooter()).hasPermission(PermissionNodes.BYPASS_RESTRICTIONS)){
					return;
				}

				if (checkAndRemoveProjectileIfCannotHitLocation(projectile, projectile.getLocation())) {
					event.setCancelled(true);
					event.setRadius(0);
				}
			}
		}
	}


//	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
//	public void onProjectileLaunch(ProjectileLaunchEvent event) {
//		final Projectile projectile = event.getEntity();
//
//		if (projectile.getShooter() instanceof Player) {
//			final Player player = (Player) projectile.getShooter();
//
//			if (!FFResSettings.isWorldEnabled(player.getWorld())){
//				return;
//			}
//
//			Block targetBlock = FCBukkitUtil.getTargetBlock(player, 100); // TODO max distance to config
//
//			if (targetBlock==null) {
//				event.setCancelled(true);
//				projectile.remove(); // In order to prevent targeting any far away protected area, remove the projectile. (TODO use a items list for this feature?)
//			} else {
//				if (this.canProjectileHitLocation(projectile, targetBlock.getLocation())) { // Check if the target block can be hit by this player
//					event.setCancelled(true);
//					this.confiscateInventory(player);
//				}
//			}
//		}
//	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		final Projectile projectile = event.getEntity();

		if (projectile.getShooter() instanceof Player) {

			if (!FFResSettings.isWorldEnabled(projectile.getLocation().getWorld())){
				return;
			}

			//Ignore if the player has correct permission
			if (((Player)projectile.getShooter()).hasPermission(PermissionNodes.BYPASS_RESTRICTIONS)){
				return;
			}

			this.checkAndRemoveProjectileIfCannotHitLocation(projectile, projectile.getLocation());
		}
	}

	private boolean checkAndRemoveProjectileIfCannotHitLocation(Projectile projectile, Location location) {
		final Player player = (Player) projectile.getShooter();

		for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
			if (!protection.canProjectileHit(player, location)) {
				projectile.remove();
				return true;
			}
		}

		return false;
	}

    @EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		ConfiscationManager.restoreInventoryIfNeeded(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		ConfiscationManager.restoreInventoryIfNeeded(event.getEntity());
	}

}
