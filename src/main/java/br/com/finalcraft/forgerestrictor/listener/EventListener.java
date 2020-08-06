package br.com.finalcraft.forgerestrictor.listener;

import br.com.finalcraft.forgerestrictor.ForgeRestrictor;
import br.com.finalcraft.forgerestrictor.Utils;
import br.com.finalcraft.forgerestrictor.config.ConfiscatedInventory;
import br.com.finalcraft.forgerestrictor.config.ListedRangedItem;
import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionHandler;
import br.com.finalcraft.forgerestrictor.protectionhandler.ProtectionPlugins;
import net.kaikk.mc.gpp.GriefPreventionPlus;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.UUID;

public class EventListener implements Listener {
	private ForgeRestrictor instance;
	static ArrayList<ConfiscatedInventory> confiscatedInventories;
	private UUID lastConfiscationUUID;
	private long lastConfiscationTime;
	private Material lastConfiscationMaterial;

	public EventListener(ForgeRestrictor instance) {
		this.instance = instance;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		// ignore stepping onto or into a block 
		if (event.getAction()==Action.PHYSICAL) {
			return;
		}

		final Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		ItemStack item = event.getItem();
		if (item==null) {
			item = player.getItemInHand();
		}
		
		// ignore skulls
		if  (item.getType() == Material.SKULL_ITEM) {
			return;
		}
		
		// ignore all vanilla items and edible items in vanilla blocks actions
		if (block!=null && (item.getData().getItemType().isEdible() || isVanilla(item.getType())) && isVanilla(block.getType())) {
			return;
		}
		
		// ignore investigation tool
		if (item.getType() == GriefPreventionPlus.getInstance().config.claims_investigationTool) {
			return;
		}

		// whitelisted item check
		if (this.instance.config.matchWhitelistItem(item.getType(), item.getDurability(), player.getWorld().getName()) !=null) {
			return;
		}

		// special aoe items list (needs to check a wide area...)
		ListedRangedItem rangeItem = this.instance.config.matchAoEItem(item.getType(), item.getDurability(), player.getWorld().getName());
		if (rangeItem!=null) {
			// check players location
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canUseAoE(player, player.getLocation(), rangeItem.range)) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
					event.setCancelled(true);
					this.confiscateInventory(player);
					return;
				}
			}
			return;
		}

		if (block==null) {
			// check if the item in hand is a ranged item
			rangeItem = this.instance.config.matchRangedItem(item.getType(), item.getDurability(), player.getWorld().getName());
			if (rangeItem!=null) {
				block=getTargetBlock(player, rangeItem.range);
			}

		}

		Location targetLocation;
		if (block==null) {
			targetLocation=player.getLocation();
		} else {
			targetLocation=block.getLocation();
		}

		// check permissions on that location
		for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
			if (!protection.canInteract(player, targetLocation)) {
				if (item.getType() != Material.AIR || block == null || !protection.canOpenContainer(player, block)) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
					event.setCancelled(true);
					this.confiscateInventory(player);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType()==EntityType.PLAYER) {
			final Player damager = (Player) event.getDamager();

			if (damager.getName().startsWith("[")) {
				return;
			}

			final Entity damaged = event.getEntity();

			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canAttack(damager, damaged)) {
					event.setCancelled(true);
					this.confiscateInventory(damager);
					return;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		if (player.getName().startsWith("[")) {
			return;
		}
		
		ItemStack itemInHand=event.getItemInHand();

		// special aoe items list (needs to check a wide area...)
		ListedRangedItem item = this.instance.config.getAoEItem(itemInHand.getType(), itemInHand.getDurability(), player.getWorld().getName());
		if (item!=null) {
			Location blockLocation = event.getBlock().getLocation();
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canUseAoE(player, blockLocation, item.range)) {
					event.setBuild(false);
					event.setCancelled(true);
					this.confiscateInventory(player);
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
				if (this.projectileCheck(projectile, projectile.getLocation())) {
					event.setCancelled(true);
					event.setRadius(0);
				}
			}
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		final Projectile projectile = event.getEntity();
		if (projectile.getShooter() instanceof Player) {
			final Player player = (Player) projectile.getShooter();
			Block targetBlock = getTargetBlock(player, 100); // TODO max distance to config

			if (targetBlock==null) { 
				event.setCancelled(true);
				projectile.remove(); // In order to prevent targeting any far away protected area, remove the projectile. (TODO use a items list for this feature?)
			} else {
				if (this.projectileCheck(projectile, targetBlock.getLocation())) { // Check if the target block can be hit by this player
					event.setCancelled(true);
					this.confiscateInventory(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		final Projectile projectile = event.getEntity();

		if (projectile.getShooter() instanceof Player) {
			this.projectileCheck(projectile, projectile.getLocation());
		}
	}

	private boolean projectileCheck(Projectile projectile, Location location) {
		final Player player = (Player) projectile.getShooter();
		for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
			if (!protection.canProjectileHit(player, projectile.getLocation())) {
				projectile.remove();
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginEnable(PluginEnableEvent event) {
		this.pluginEnable(event.getPlugin().getName());
	}

	public void pluginEnable(String pluginName) {
		ProtectionPlugins protectionPlugin;
		try {
			protectionPlugin = ProtectionPlugins.valueOf(pluginName);
		} catch (Exception e1) {
			return;
		}

		if (protectionPlugin.isEnabled()) {
			try {
				this.instance.getLogger().info("Loading protection plugin: "+pluginName);
				protectionPlugin.createHandler();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginDisable(PluginDisableEvent event) {
		ProtectionPlugins protectionPlugin;
		try {
			protectionPlugin = ProtectionPlugins.valueOf(event.getPlugin().getName());
		} catch (Exception e) {
			return;
		}

		this.instance.getLogger().info("Unloading protection plugin: "+event.getPlugin().getName());
		protectionPlugin.removeHandler();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (confiscatedInventories!=null) {
			for (ConfiscatedInventory cis : confiscatedInventories) {
				if (cis.getPlayer()==event.getPlayer()) {
					cis.release();
					break;
				}
			}
		}
	}
	
	public void confiscateInventory(Player player) {
		confiscateInventory(player, this.instance.config.confiscateTicks);
	}

	public void confiscateInventory(Player player, int ticks) {
		if (ticks<1) {
			return;
		}
		
		if (player.getName().startsWith("[") || !player.isOnline() || isInventoryEmpty(player)) {
			return;
		}

		if (confiscatedInventories==null) {
			confiscatedInventories=new ArrayList<ConfiscatedInventory>();

			new BukkitRunnable() {
				@Override
				public void run() {
					for (ConfiscatedInventory cis : EventListener.confiscatedInventories) {
						cis.release();
					}

					EventListener.confiscatedInventories=null;
				}
			}.runTaskLater(this.instance, ticks);
		} else {
			// check if this player has his inventory already confiscated
			for (ConfiscatedInventory ci : confiscatedInventories) {
				if (player==ci.getPlayer()){
					return;
				}
			}
		}
		
		if (this.instance.config.confiscateLog && (!player.getUniqueId().equals(lastConfiscationUUID) || player.getItemInHand().getType() != lastConfiscationMaterial || (System.currentTimeMillis()-lastConfiscationTime)>5000)) {
			this.instance.getLogger().info(player.getName()+"'s inventory has been confiscated for "+ticks+" ticks. Location: "+ Utils.locationToString(player.getLocation())+" - ItemInHand: "+player.getItemInHand());
			lastConfiscationUUID = player.getUniqueId();
			lastConfiscationTime = System.currentTimeMillis();
			lastConfiscationMaterial = player.getItemInHand().getType();
		}
		confiscatedInventories.add(new ConfiscatedInventory(player));
	}

	private static boolean isInventoryEmpty(Player player) {
		for(ItemStack is : player.getInventory()) {
			if (is!=null && is.getType()!=Material.AIR) {
				return false;
			}
		}
		return true;
	}

	public static Block getTargetBlock(Player player, int maxDistance) {
		Block result = player.getLocation().getBlock().getRelative(BlockFace.UP);
		try {
			BlockIterator iterator = new BlockIterator(player.getLocation(), player.getEyeHeight(), maxDistance);

			while (iterator.hasNext()) {
				result = iterator.next();
				if (result.getType() != Material.AIR && result.getType() != Material.STATIONARY_WATER) {
					return result;
				}
			}
		} catch (Exception e) {  }
		return result;
	}

	public static boolean isVanilla(Material material) {
		switch(material) {
		case ACACIA_STAIRS:
		case ACTIVATOR_RAIL:
		case AIR:
		case ANVIL:
		case APPLE:
		case ARROW:
		case BAKED_POTATO:
		case BEACON:
		case BED:
		case BEDROCK:
		case BED_BLOCK:
		case BIRCH_WOOD_STAIRS:
		case BLAZE_POWDER:
		case BLAZE_ROD:
		case BOAT:
		case BONE:
		case BOOK:
		case BOOKSHELF:
		case BOOK_AND_QUILL:
		case BOW:
		case BOWL:
		case BREAD:
		case BREWING_STAND:
		case BREWING_STAND_ITEM:
		case BRICK:
		case BRICK_STAIRS:
		case BROWN_MUSHROOM:
		case BUCKET:
		case BURNING_FURNACE:
		case CACTUS:
		case CAKE:
		case CAKE_BLOCK:
		case CARPET:
		case CARROT:
		case CARROT_ITEM:
		case CARROT_STICK:
		case CAULDRON:
		case CAULDRON_ITEM:
		case CHAINMAIL_BOOTS:
		case CHAINMAIL_CHESTPLATE:
		case CHAINMAIL_HELMET:
		case CHAINMAIL_LEGGINGS:
		case CHEST:
		case CLAY:
		case CLAY_BALL:
		case CLAY_BRICK:
		case COAL:
		case COAL_BLOCK:
		case COAL_ORE:
		case COBBLESTONE:
		case COBBLESTONE_STAIRS:
		case COBBLE_WALL:
		case COCOA:
		case COMMAND:
		case COMMAND_MINECART:
		case COMPASS:
		case COOKED_BEEF:
		case COOKED_CHICKEN:
		case COOKED_FISH:
		case COOKIE:
		case CROPS:
		case DARK_OAK_STAIRS:
		case DAYLIGHT_DETECTOR:
		case DEAD_BUSH:
		case DETECTOR_RAIL:
		case DIAMOND:
		case DIAMOND_AXE:
		case DIAMOND_BARDING:
		case DIAMOND_BLOCK:
		case DIAMOND_BOOTS:
		case DIAMOND_CHESTPLATE:
		case DIAMOND_HELMET:
		case DIAMOND_HOE:
		case DIAMOND_LEGGINGS:
		case DIAMOND_ORE:
		case DIAMOND_PICKAXE:
		case DIAMOND_SPADE:
		case DIAMOND_SWORD:
		case DIODE:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case DIRT:
		case DISPENSER:
		case DOUBLE_PLANT:
		case DOUBLE_STEP:
		case DRAGON_EGG:
		case DROPPER:
		case EGG:
		case EMERALD:
		case EMERALD_BLOCK:
		case EMERALD_ORE:
		case EMPTY_MAP:
		case ENCHANTED_BOOK:
		case ENCHANTMENT_TABLE:
		case ENDER_CHEST:
		case ENDER_PEARL:
		case ENDER_PORTAL:
		case ENDER_PORTAL_FRAME:
		case ENDER_STONE:
		case EXPLOSIVE_MINECART:
		case EXP_BOTTLE:
		case EYE_OF_ENDER:
		case FEATHER:
		case FENCE:
		case FENCE_GATE:
		case FERMENTED_SPIDER_EYE:
		case FIRE:
		case FIREBALL:
		case FIREWORK:
		case FIREWORK_CHARGE:
		case FISHING_ROD:
		case FLINT:
		case FLINT_AND_STEEL:
		case FLOWER_POT:
		case FLOWER_POT_ITEM:
		case FURNACE:
		case GHAST_TEAR:
		case GLASS:
		case GLASS_BOTTLE:
		case GLOWING_REDSTONE_ORE:
		case GLOWSTONE:
		case GLOWSTONE_DUST:
		case GOLDEN_APPLE:
		case GOLDEN_CARROT:
		case GOLD_AXE:
		case GOLD_BARDING:
		case GOLD_BLOCK:
		case GOLD_BOOTS:
		case GOLD_CHESTPLATE:
		case GOLD_HELMET:
		case GOLD_HOE:
		case GOLD_INGOT:
		case GOLD_LEGGINGS:
		case GOLD_NUGGET:
		case GOLD_ORE:
		case GOLD_PICKAXE:
		case GOLD_PLATE:
		case GOLD_RECORD:
		case GOLD_SPADE:
		case GOLD_SWORD:
		case GRASS:
		case GRAVEL:
		case GREEN_RECORD:
		case GRILLED_PORK:
		case HARD_CLAY:
		case HAY_BLOCK:
		case HOPPER:
		case HOPPER_MINECART:
		case HUGE_MUSHROOM_1:
		case HUGE_MUSHROOM_2:
		case ICE:
		case INK_SACK:
		case IRON_AXE:
		case IRON_BARDING:
		case IRON_BLOCK:
		case IRON_BOOTS:
		case IRON_CHESTPLATE:
		case IRON_DOOR:
		case IRON_DOOR_BLOCK:
		case IRON_FENCE:
		case IRON_HELMET:
		case IRON_HOE:
		case IRON_INGOT:
		case IRON_LEGGINGS:
		case IRON_ORE:
		case IRON_PICKAXE:
		case IRON_PLATE:
		case IRON_SPADE:
		case IRON_SWORD:
		case ITEM_FRAME:
		case JACK_O_LANTERN:
		case JUKEBOX:
		case JUNGLE_WOOD_STAIRS:
		case LADDER:
		case LAPIS_BLOCK:
		case LAPIS_ORE:
		case LAVA:
		case LAVA_BUCKET:
		case LEASH:
		case LEATHER:
		case LEATHER_BOOTS:
		case LEATHER_CHESTPLATE:
		case LEATHER_HELMET:
		case LEATHER_LEGGINGS:
		case LEAVES:
		case LEAVES_2:
		case LEVER:
		case LOG:
		case LOG_2:
		case LONG_GRASS:
		case MAGMA_CREAM:
		case MAP:
		case MELON:
		case MELON_BLOCK:
		case MELON_SEEDS:
		case MELON_STEM:
		case MILK_BUCKET:
		case MINECART:
		case MOB_SPAWNER:
		case MONSTER_EGG:
		case MONSTER_EGGS:
		case MOSSY_COBBLESTONE:
		case MUSHROOM_SOUP:
		case MYCEL:
		case NAME_TAG:
		case NETHERRACK:
		case NETHER_BRICK:
		case NETHER_BRICK_ITEM:
		case NETHER_BRICK_STAIRS:
		case NETHER_FENCE:
		case NETHER_STALK:
		case NETHER_STAR:
		case NETHER_WARTS:
		case NOTE_BLOCK:
		case OBSIDIAN:
		case PACKED_ICE:
		case PAINTING:
		case PAPER:
		case PISTON_BASE:
		case PISTON_EXTENSION:
		case PISTON_MOVING_PIECE:
		case PISTON_STICKY_BASE:
		case POISONOUS_POTATO:
		case PORK:
		case PORTAL:
		case POTATO:
		case POTATO_ITEM:
		case POTION:
		case POWERED_MINECART:
		case POWERED_RAIL:
		case PUMPKIN:
		case PUMPKIN_PIE:
		case PUMPKIN_SEEDS:
		case PUMPKIN_STEM:
		case QUARTZ:
		case QUARTZ_BLOCK:
		case QUARTZ_ORE:
		case QUARTZ_STAIRS:
		case RAILS:
		case RAW_BEEF:
		case RAW_CHICKEN:
		case RAW_FISH:
		case RECORD_10:
		case RECORD_11:
		case RECORD_12:
		case RECORD_3:
		case RECORD_4:
		case RECORD_5:
		case RECORD_6:
		case RECORD_7:
		case RECORD_8:
		case RECORD_9:
		case REDSTONE:
		case REDSTONE_BLOCK:
		case REDSTONE_COMPARATOR:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case REDSTONE_LAMP_OFF:
		case REDSTONE_LAMP_ON:
		case REDSTONE_ORE:
		case REDSTONE_TORCH_OFF:
		case REDSTONE_TORCH_ON:
		case REDSTONE_WIRE:
		case RED_MUSHROOM:
		case RED_ROSE:
		case ROTTEN_FLESH:
		case SADDLE:
		case SAND:
		case SANDSTONE:
		case SANDSTONE_STAIRS:
		case SAPLING:
		case SEEDS:
		case SHEARS:
		case SIGN:
		case SIGN_POST:
		case SKULL:
		case SKULL_ITEM:
		case SLIME_BALL:
		case SMOOTH_BRICK:
		case SMOOTH_STAIRS:
		case SNOW:
		case SNOW_BALL:
		case SNOW_BLOCK:
		case SOIL:
		case SOUL_SAND:
		case SPECKLED_MELON:
		case SPIDER_EYE:
		case SPONGE:
		case SPRUCE_WOOD_STAIRS:
		case STAINED_CLAY:
		case STAINED_GLASS:
		case STAINED_GLASS_PANE:
		case STATIONARY_LAVA:
		case STATIONARY_WATER:
		case STEP:
		case STICK:
		case STONE:
		case STONE_AXE:
		case STONE_BUTTON:
		case STONE_HOE:
		case STONE_PICKAXE:
		case STONE_PLATE:
		case STONE_SPADE:
		case STONE_SWORD:
		case STORAGE_MINECART:
		case STRING:
		case SUGAR:
		case SUGAR_CANE:
		case SUGAR_CANE_BLOCK:
		case SULPHUR:
		case THIN_GLASS:
		case TNT:
		case TORCH:
		case TRAPPED_CHEST:
		case TRAP_DOOR:
		case TRIPWIRE:
		case TRIPWIRE_HOOK:
		case VINE:
		case WALL_SIGN:
		case WATCH:
		case WATER:
		case WATER_BUCKET:
		case WATER_LILY:
		case WEB:
		case WHEAT:
		case WOOD:
		case WOODEN_DOOR:
		case WOOD_AXE:
		case WOOD_BUTTON:
		case WOOD_DOOR:
		case WOOD_DOUBLE_STEP:
		case WOOD_HOE:
		case WOOD_PICKAXE:
		case WOOD_PLATE:
		case WOOD_SPADE:
		case WOOD_STAIRS:
		case WOOD_STEP:
		case WOOD_SWORD:
		case WOOL:
		case WORKBENCH:
		case WRITTEN_BOOK:
		case YELLOW_FLOWER:
			return true;
		default:
			return false;
		}
	}
}
